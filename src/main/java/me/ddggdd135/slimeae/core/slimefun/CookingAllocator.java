package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IMERealCraftDevice;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class CookingAllocator extends MEBus implements IMERealCraftDevice {

    private static final String DATA_KEY_RUNNING = "cooking_running";
    private static final String DATA_KEY_RECIPE_TYPE = "cooking_recipe_type";
    private static final String DATA_KEY_RECIPE_INPUT = "cooking_recipe_input";
    private static final String DATA_KEY_RECIPE_OUTPUT = "cooking_recipe_output";

    private static final Map<Location, CraftingRecipe> recipeCache = new ConcurrentHashMap<>();

    private static final Set<Location> runningCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public CookingAllocator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemHandler(onBlockBreak());
    }

    private void saveState(@Nonnull Location location, @Nonnull CraftingRecipe recipe) {
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) return;
        StorageCacheUtils.setData(location, DATA_KEY_RUNNING, "true");
        StorageCacheUtils.setData(
                location, DATA_KEY_RECIPE_TYPE, recipe.getCraftType().name());

        ItemStack[] inputs = ItemUtils.trimItems(recipe.getInput());
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 0; i < inputs.length; i++) {
            if (i > 0) inputBuilder.append("|");
            inputBuilder.append(serializeItemStack(inputs[i]));
        }
        StorageCacheUtils.setData(location, DATA_KEY_RECIPE_INPUT, inputBuilder.toString());

        ItemStack[] outputs = ItemUtils.trimItems(recipe.getOutput());
        StringBuilder outputBuilder = new StringBuilder();
        for (int i = 0; i < outputs.length; i++) {
            if (i > 0) outputBuilder.append("|");
            outputBuilder.append(serializeItemStack(outputs[i]));
        }
        StorageCacheUtils.setData(location, DATA_KEY_RECIPE_OUTPUT, outputBuilder.toString());
    }

    private void clearState(@Nonnull Location location) {
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) return;
        StorageCacheUtils.removeData(location, DATA_KEY_RUNNING);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_TYPE);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_INPUT);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_OUTPUT);
    }

    private void restoreState(@Nonnull Location location) {
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) return;
        String running = StorageCacheUtils.getData(location, DATA_KEY_RUNNING);
        if (!"true".equals(running)) return;

        String typeName = StorageCacheUtils.getData(location, DATA_KEY_RECIPE_TYPE);
        String inputData = StorageCacheUtils.getData(location, DATA_KEY_RECIPE_INPUT);
        String outputData = StorageCacheUtils.getData(location, DATA_KEY_RECIPE_OUTPUT);

        if (typeName == null || inputData == null || outputData == null) {
            clearState(location);
            return;
        }

        CraftType craftType = CraftType.fromName(typeName);
        if (craftType == null) {
            clearState(location);
            return;
        }

        try {
            ItemStack[] inputs = deserializeItemStacks(inputData);
            ItemStack[] outputs = deserializeItemStacks(outputData);
            if (inputs.length == 0 || outputs.length == 0) {
                clearState(location);
                return;
            }

            CraftingRecipe recipe = new CraftingRecipe(craftType, inputs, outputs);
            recipeCache.put(location, recipe);
            runningCache.add(location);
        } catch (Exception e) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.WARNING, "Failed to restore CookingAllocator state at " + location, e);
            clearState(location);
        }
    }

    @Nonnull
    private String serializeItemStack(@Nonnull ItemStack itemStack) {
        return SerializeUtils.object2String(itemStack);
    }

    @Nonnull
    private ItemStack[] deserializeItemStacks(@Nonnull String data) {
        if (data.isEmpty()) return new ItemStack[0];
        String[] parts = data.split("\\|");
        List<ItemStack> items = new ArrayList<>();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            Object obj = SerializeUtils.string2Object(part);
            if (obj instanceof ItemStack item && !item.getType().isAir()) {
                items.add(item);
            }
        }
        return items.toArray(new ItemStack[0]);
    }

    @Nullable private Block getTargetBlock(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return null;
        BlockFace direction = getDirection(blockMenu);
        if (direction == BlockFace.SELF) return null;
        return block.getRelative(direction);
    }

    private boolean checkAndCleanup(@Nonnull Location location) {
        if (!runningCache.contains(location)) return false;
        Block block = location.getBlock();
        Block target = getTargetBlock(block);
        if (target == null || target.getType().isAir()) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.WARNING, "CookingAllocator at {0} lost target block, clearing state", location);
            recipeCache.remove(location);
            runningCache.remove(location);
            clearState(location);
            return false;
        }

        return true;
    }

    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block block) {
                Location loc = block.getLocation();
                recipeCache.remove(loc);
                runningCache.remove(loc);
                clearState(loc);

                BlockMenu blockMenu = StorageCacheUtils.getMenu(loc);
                if (blockMenu == null) return;

                for (int slot : getCardSlots()) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.CARD, true, false))) {
                        block.getWorld().dropItemNaturally(loc, itemStack);
                    }
                }
            }
        };
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);
        restoreState(block.getLocation());
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    public boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return recipe.getCraftType().isProcess();
    }

    @Override
    public boolean canStartCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        if (!isSupport(block, recipe)) return false;

        Location loc = block.getLocation();

        if (checkAndCleanup(loc)) return false;
        if (runningCache.contains(loc)) return false;

        Block target = getTargetBlock(block);
        if (target == null) return false;
        if (target.getType().isAir()) return false;

        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.getLocation());
        if (targetMenu == null) return false;

        ItemStack[] inputs = ItemUtils.trimItems(recipe.getInput());
        if (inputs.length == 0) return false;
        ItemStack[] outputs = ItemUtils.trimItems(recipe.getOutput());
        if (outputs.length == 0) return false;

        int[] inputSlots;
        int[] outputSlots;
        try {
            inputSlots = targetMenu
                    .getPreset()
                    .getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.INSERT, inputs[0]);
            outputSlots = targetMenu
                    .getPreset()
                    .getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.WITHDRAW, outputs[0]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (inputSlots == null || outputSlots == null) return false;

        return InvUtils.fitAll(targetMenu.getInventory(), inputs, inputSlots)
                && InvUtils.fitAll(targetMenu.getInventory(), outputs, outputSlots);
    }

    @Override
    public void startCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        Location loc = block.getLocation();
        Block target = getTargetBlock(block);
        if (target == null) return;

        IStorage targetStorage = ItemUtils.getStorage(target, false, false);
        if (targetStorage == null) return;

        targetStorage.pushItem(Arrays.stream(ItemUtils.trimItems(recipe.getInput()))
                .map(ItemStack::clone)
                .toArray(ItemStack[]::new));
        recipeCache.put(loc, recipe);
        runningCache.add(loc);
        saveState(loc, recipe);
    }

    @Override
    public boolean isFinished(@Nonnull Block block) {
        Location loc = block.getLocation();

        if (!runningCache.contains(loc)) return false;
        CraftingRecipe recipe = recipeCache.get(loc);
        if (recipe == null) return false;
        if (!isSupport(block, recipe)) return false;
        Block target = getTargetBlock(block);
        if (target == null) {
            recipeCache.remove(loc);
            runningCache.remove(loc);
            clearState(loc);
            return false;
        }

        IStorage storage = ItemUtils.getStorage(target, false, false);
        if (storage == null) return false;

        return storage.contains(ItemUtils.createRequests(ItemUtils.getAmounts(recipe.getOutput())));
    }

    @Override
    @Nullable public CraftingRecipe getFinishedCraftingRecipe(@Nonnull Block block) {
        return recipeCache.getOrDefault(block.getLocation(), null);
    }

    @Override
    public void finishCrafting(@Nonnull Block block) {
        Location loc = block.getLocation();
        CraftingRecipe recipe = recipeCache.get(loc);
        if (recipe == null) return;

        Block target = getTargetBlock(block);
        if (target == null) {
            recipeCache.remove(loc);
            runningCache.remove(loc);
            clearState(loc);
            return;
        }

        IStorage targetStorage = ItemUtils.getStorage(target, false, false);
        if (targetStorage != null) {
            targetStorage.takeItem(ItemUtils.createRequests(ItemUtils.getAmounts(recipe.getOutput())));
        }
        recipeCache.remove(loc);
        runningCache.remove(loc);
        clearState(loc);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onMEBusTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        checkAndCleanup(block.getLocation());
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}
}
