package me.ddggdd135.slimeae.core;

import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IDisposable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NetworkInfo implements IDisposable {
    private final Location controller;
    private Set<Location> children = new ConcurrentHashSet<>();
    private final Set<Location> craftingHolders = new ConcurrentHashSet<>();
    private final Map<Location, Set<CraftingRecipe>> recipeMap = new ConcurrentHashMap<>();
    private final Map<CraftType, Integer> virtualCraftingDeviceSpeeds = new ConcurrentHashMap<>();
    private final Map<CraftType, Integer> virtualCraftingDeviceUsed = new ConcurrentHashMap<>();
    private volatile StorageCollection storage = new StorageCollection();
    private volatile IStorage storageNoNetworks = new StorageCollection();
    private final ConcurrentHashSet<AutoCraftingTask> autoCraftingTasks = new ConcurrentHashSet<>();
    private final AEMenu autoCraftingMenu = new AEMenu("&e自动合成任务");
    private final ItemStorage tempStorage = new ItemStorage();

    // === F9: 配方缓存 ===
    private volatile Set<CraftingRecipe> cachedRecipes = null;

    private volatile Map<ItemKey, CraftingRecipe> outputIndex = new ConcurrentHashMap<>();

    private volatile Map<CraftingRecipe, List<Location>> recipeToHolders = new ConcurrentHashMap<>();

    private volatile Map<Location, Block[]> cachedCraftingDevices = Collections.emptyMap();

    private volatile Set<Location> tickableChildren = Collections.emptySet();

    private volatile boolean disposed = false;

    private volatile boolean needsStorageUpdate = false;
    private volatile boolean needsRecipeUpdate = false;

    private volatile int parallelProcessorCount = 0;

    private static int maxCraftingSessions;
    private static int maxCraftingAmount;
    private static int maxDevicesPerTick;
    private static boolean parallelEnabled;
    private static int maxParallelism;

    public static int getMaxCraftingSessions() {
        return maxCraftingSessions;
    }

    public static int getMaxCraftingAmount() {
        return maxCraftingAmount;
    }

    public static int getMaxDevicesPerTick() {
        return maxDevicesPerTick;
    }

    public static boolean isParallelEnabled() {
        return parallelEnabled;
    }

    public static int getMaxParallelism() {
        return maxParallelism;
    }

    // 重载配置
    public static void reloadConfig() {
        maxCraftingSessions = SlimeAEPlugin.getInstance().getConfig().getInt("auto-crafting.max-tasks", 32);
        maxCraftingAmount = SlimeAEPlugin.getInstance().getConfig().getInt("auto-crafting.max-amount", 100000);
        maxDevicesPerTick = SlimeAEPlugin.getInstance().getConfig().getInt("auto-crafting.max-devices-per-tick", 16384);
        parallelEnabled = SlimeAEPlugin.getInstance().getConfig().getBoolean("auto-crafting.parallel.enabled", true);
        maxParallelism = SlimeAEPlugin.getInstance().getConfig().getInt("auto-crafting.parallel.max-parallelism", 16);
    }

    // 静态初始化块,在类加载时加载配置
    static {
        reloadConfig();
    }

    @Nonnull
    public Location getController() {
        return controller;
    }

    @Nonnull
    public Set<Location> getChildren() {
        return children;
    }

    public void clearChildren() {
        NetworkData networkData = SlimeAEPlugin.getNetworkData();
        for (Location loc : children) {
            networkData.locationToNetwork.remove(loc, this);
        }
        children.clear();
    }

    public void replaceChildren(@Nonnull Set<Location> newChildren) {
        NetworkData networkData = SlimeAEPlugin.getNetworkData();
        ConcurrentHashSet<Location> newSet = new ConcurrentHashSet<>();
        newSet.addAll(newChildren);
        for (Location loc : newChildren) {
            networkData.locationToNetwork.put(loc, this);
        }
        networkData.locationToNetwork.put(controller, this);
        Set<Location> oldChildren = this.children;
        this.children = newSet;
        for (Location loc : oldChildren) {
            if (!newChildren.contains(loc)) {
                networkData.locationToNetwork.remove(loc, this);
            }
        }
    }

    public NetworkInfo(@Nonnull Location controller) {
        this.controller = controller;
        autoCraftingMenu.setSize(54);
        tempStorage.setReadonly(true);
    }

    public NetworkInfo(@Nonnull Location controller, @Nonnull Set<Location> children) {
        this.controller = controller;
        this.children = children;
        autoCraftingMenu.setSize(54);
        tempStorage.setReadonly(true);
    }

    @Nonnull
    public StorageCollection getStorage() {
        return storage;
    }

    public void setStorage(@Nonnull StorageCollection storage) {
        this.storage = storage;
    }

    @Nonnull
    public IStorage getStorageNoNetworks() {
        return storageNoNetworks;
    }

    public void setStorageNoNetworks(@Nonnull IStorage storage) {
        this.storageNoNetworks = storage;
    }

    @Override
    public synchronized void dispose() {
        if (disposed) return;

        for (AutoCraftingTask task : autoCraftingTasks) {
            task.suspend();
        }

        for (AutoCraftingTask task : autoCraftingTasks) {
            task.dispose();
        }

        updateTempStorage();

        disposed = true;

        NetworkData networkData = SlimeAEPlugin.getNetworkData();
        networkData.AllNetworkData.remove(this);

        for (Location loc : children) {
            networkData.locationToNetwork.remove(loc, this);
        }
        networkData.locationToNetwork.remove(controller, this);
    }

    public boolean isDisposed() {
        return disposed;
    }

    public boolean needsStorageUpdate() {
        return needsStorageUpdate;
    }

    public void setNeedsStorageUpdate(boolean value) {
        this.needsStorageUpdate = value;
    }

    public boolean needsRecipeUpdate() {
        return needsRecipeUpdate;
    }

    public void setNeedsRecipeUpdate(boolean value) {
        this.needsRecipeUpdate = value;
    }

    public int getParallelProcessorCount() {
        return parallelProcessorCount;
    }

    public void setParallelProcessorCount(int count) {
        this.parallelProcessorCount = count;
    }

    public void clearDirtyFlags() {
        this.needsStorageUpdate = false;
        this.needsRecipeUpdate = false;
    }

    @Override
    public int hashCode() {
        return controller.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkInfo that = (NetworkInfo) o;
        return controller.equals(that.controller);
    }

    @Nonnull
    public Set<Location> getCraftingHolders() {
        return craftingHolders;
    }

    @Nonnull
    public Set<CraftingRecipe> getRecipes(@Nonnull Block holder) {
        return recipeMap.get(holder.getLocation());
    }

    @Nonnull
    public Map<Location, Set<CraftingRecipe>> getRecipeMap() {
        return recipeMap;
    }

    @Nonnull
    public Map<CraftType, Integer> getVirtualCraftingDeviceSpeeds() {
        return virtualCraftingDeviceSpeeds;
    }

    @Nonnull
    public Map<CraftType, Integer> getVirtualCraftingDeviceUsed() {
        return virtualCraftingDeviceUsed;
    }

    @Nonnull
    public Set<CraftingRecipe> getRecipes() {
        // F9: 使用缓存的配方集合
        Set<CraftingRecipe> cached = cachedRecipes;
        if (cached != null) return cached;

        Set<CraftingRecipe> recipes = new HashSet<>();
        for (Location location : craftingHolders) {
            // fix NullPointException in here
            Set<CraftingRecipe> recipes1 = recipeMap.get(location);
            if (null != recipes1) {
                recipes.addAll(recipes1);
            }
        }
        Set<CraftingRecipe> result = Collections.unmodifiableSet(recipes);
        cachedRecipes = result;
        return result;
    }

    /**
     * 使配方缓存失效，当 recipeMap 或 craftingHolders 发生变化时调用
     */
    public void invalidateRecipeCache() {
        cachedRecipes = null;
    }

    /**
     * 直接设置配方缓存快照。
     * 用于在 updateAutoCraft() 中原子替换底层数据时，先设置新缓存，
     * 确保 getRecipes() 在 clear()+putAll() 之间不会返回空集合。
     */
    public void setRecipeCache(@Nonnull Set<CraftingRecipe> cache) {
        cachedRecipes = cache;
    }

    @Nullable public CraftingRecipe getRecipeFor(@Nonnull ItemStack output) {
        ItemKey key = new ItemKey(output.asOne());
        CraftingRecipe indexed = outputIndex.get(key);
        if (indexed != null) return indexed;

        for (CraftingRecipe recipe : getRecipes()) {
            for (ItemStack itemStack : recipe.getOutput()) {
                if (itemStack.asOne().equals(output.asOne())) return recipe;
            }
        }

        return null;
    }

    @Nonnull
    public Map<CraftingRecipe, List<Location>> getRecipeToHolders() {
        return recipeToHolders;
    }

    public void setOutputIndex(@Nonnull Map<ItemKey, CraftingRecipe> index) {
        this.outputIndex = index;
    }

    public void setRecipeToHolders(@Nonnull Map<CraftingRecipe, List<Location>> map) {
        this.recipeToHolders = map;
    }

    @Nonnull
    public Map<Location, Block[]> getCachedCraftingDevices() {
        return cachedCraftingDevices;
    }

    public void setCachedCraftingDevices(@Nonnull Map<Location, Block[]> map) {
        this.cachedCraftingDevices = map;
    }

    @Nonnull
    public Set<Location> getTickableChildren() {
        return tickableChildren;
    }

    public void setTickableChildren(@Nonnull Set<Location> set) {
        this.tickableChildren = set;
    }

    @Nonnull
    public ConcurrentHashSet<AutoCraftingTask> getAutoCraftingSessions() {
        return autoCraftingTasks;
    }

    public void openAutoCraftingSessionsMenu(@Nonnull Player player) {
        updateAutoCraftingMenu();
        autoCraftingMenu.open(player);
    }

    public void updateAutoCraftingMenu() {
        List<AutoCraftingTask> tasks = getAutoCraftingSessions().toList();
        if (tasks.size() > 53) tasks = tasks.subList(tasks.size() - 53, tasks.size());

        int i = 0;
        for (AutoCraftingTask task : tasks) {
            ItemStack[] itemStacks = task.getRecipe().getOutput();
            ItemStack itemStack;
            if (itemStacks.length == 1) {
                itemStack = itemStacks[0].clone();
                itemStack.setAmount((int) Math.min(64, task.getCount()));
                if (itemStack.isEmpty()) continue;
            } else {
                itemStack = new AdvancedCustomItemStack(
                        Material.BARREL,
                        "&e&l多物品",
                        Arrays.stream(itemStacks)
                                .map(x -> "  &e- &f" + ItemUtils.getItemName(x) + "&f x " + x.getAmount())
                                .toArray(String[]::new));
                itemStack.setAmount((int) Math.min(64, task.getCount()));
            }

            if (itemStack.isEmpty()) continue;
            List<String> lore = itemStack.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            lore.add("&e点击查看");
            itemStack.setLore(CMIChatColor.translate(lore));

            autoCraftingMenu.replaceExistingItem(i, itemStack);
            autoCraftingMenu.addMenuClickHandler(i, (player, i1, itemStack1, clickAction) -> {
                task.showGUI(player);
                return false;
            });
            i++;
        }

        for (int j = i; j < 54; j++) {
            autoCraftingMenu.replaceExistingItem(j, null);
            autoCraftingMenu.addMenuClickHandler(j, ChestMenuUtils.getEmptyClickHandler());
        }
        autoCraftingMenu.getContents();
    }

    @Nonnull
    public ItemStorage getTempStorage() {
        return tempStorage;
    }

    public synchronized void updateTempStorage() {
        Set<ItemKey> toPush = new HashSet<>(tempStorage.getStorageUnsafe().sourceKeySet());
        for (ItemKey key : toPush) {
            ItemHashMap<Long> items = tempStorage
                    .takeItem(new ItemRequest(key, Integer.MAX_VALUE))
                    .getStorageUnsafe();
            storage.pushItem(items);
            ItemUtils.trim(items);
            tempStorage.addItem(items, true);
        }
    }
}
