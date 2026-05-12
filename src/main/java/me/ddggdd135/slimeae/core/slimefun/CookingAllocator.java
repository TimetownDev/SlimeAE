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
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IMERealCraftDevice;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
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
    private static final String DATA_KEY_OUTPUT_BASELINE = "cooking_output_baseline";

    private static final Map<Location, CraftingRecipe> recipeCache = new ConcurrentHashMap<>();

    private static final Map<Location, ItemHashMap<Long>> baselineCache = new ConcurrentHashMap<>();

    private static final Set<Location> runningCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public CookingAllocator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemHandler(onBlockBreak());
    }

    private void saveState(
            @Nonnull Location location, @Nonnull CraftingRecipe recipe, @Nonnull ItemHashMap<Long> baseline) {
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
        StorageCacheUtils.setData(location, DATA_KEY_OUTPUT_BASELINE, serializeBaseline(recipe.getOutput(), baseline));
    }

    private void clearState(@Nonnull Location location) {
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) return;
        StorageCacheUtils.removeData(location, DATA_KEY_RUNNING);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_TYPE);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_INPUT);
        StorageCacheUtils.removeData(location, DATA_KEY_RECIPE_OUTPUT);
        StorageCacheUtils.removeData(location, DATA_KEY_OUTPUT_BASELINE);
    }

    private void restoreState(@Nonnull Location location) {
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) {
            clearState(location);
            return;
        }
        String running = blockData.getData(DATA_KEY_RUNNING);
        if (!"true".equals(running)) {
            clearState(location);
            return;
        }
        String recipeTypeName = blockData.getData(DATA_KEY_RECIPE_TYPE);
        String inputStr = blockData.getData(DATA_KEY_RECIPE_INPUT);
        String outputStr = blockData.getData(DATA_KEY_RECIPE_OUTPUT);
        String baselineStr = blockData.getData(DATA_KEY_OUTPUT_BASELINE);
        if (recipeTypeName == null || inputStr == null || outputStr == null) {
            clearState(location);
            return;
        }
        try {
            me.ddggdd135.slimeae.api.autocraft.CraftType craftType =
                    me.ddggdd135.slimeae.api.autocraft.CraftType.fromName(recipeTypeName);
            if (craftType == null) {
                clearState(location);
                return;
            }
            List<ItemStack> inputList = new ArrayList<>();
            for (String s : inputStr.split("\\|")) {
                ItemStack item = (ItemStack) SerializeUtils.string2Object(s);
                if (item != null) inputList.add(item);
            }
            List<ItemStack> outputList = new ArrayList<>();
            for (String s : outputStr.split("\\|")) {
                ItemStack item = (ItemStack) SerializeUtils.string2Object(s);
                if (item != null) outputList.add(item);
            }
            if (inputList.isEmpty() || outputList.isEmpty()) {
                clearState(location);
                return;
            }
            CraftingRecipe recipe = new CraftingRecipe(
                    craftType, inputList.toArray(new ItemStack[0]), outputList.toArray(new ItemStack[0]));
            recipeCache.put(location, recipe);
            baselineCache.put(location, deserializeBaseline(recipe.getOutput(), baselineStr));
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
    private String serializeBaseline(@Nonnull ItemStack[] outputs, @Nonnull ItemHashMap<Long> baseline) {
        ItemStack[] items = ItemUtils.trimItems(outputs);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) builder.append("|");
            builder.append(baseline.getOrDefault(new ItemKey(items[i]), 0L));
        }
        return builder.toString();
    }

    @Nonnull
    private ItemHashMap<Long> deserializeBaseline(@Nonnull ItemStack[] outputs, @Nullable String data) {
        ItemHashMap<Long> baseline = new ItemHashMap<>();
        if (data == null || data.isBlank()) return baseline;
        String[] values = data.split("\\|");
        ItemStack[] items = ItemUtils.trimItems(outputs);
        for (int i = 0; i < items.length && i < values.length; i++) {
            try {
                baseline.putKey(new ItemKey(items[i]), Long.parseLong(values[i]));
            } catch (NumberFormatException ignored) {
                baseline.putKey(new ItemKey(items[i]), 0L);
            }
        }
        return baseline;
    }

    @Nullable private int[] getTransportSlots(
            @Nonnull BlockMenu blockMenu, @Nonnull ItemTransportFlow flow, @Nullable ItemStack itemStack) {
        try {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, flow, itemStack);
            if (slots != null && slots.length > 0) return slots;
        } catch (IllegalArgumentException ignored) {
        }
        try {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(flow);
            if (slots != null && slots.length > 0) return slots;
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private long getItemAmount(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack itemStack) {
        int[] slots = getTransportSlots(blockMenu, ItemTransportFlow.WITHDRAW, itemStack);
        if (slots == null) return 0;
        ItemKey expected = new ItemKey(itemStack);
        long amount = 0;
        for (int slot : slots) {
            ItemStack item = blockMenu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) continue;
            if (new ItemKey(item).equals(expected)) amount += item.getAmount();
        }
        return amount;
    }

    @Nonnull
    private ItemHashMap<Long> getOutputAmounts(@Nonnull BlockMenu blockMenu, @Nonnull CraftingRecipe recipe) {
        ItemHashMap<Long> amounts = new ItemHashMap<>();
        for (ItemStack output : ItemUtils.trimItems(recipe.getOutput())) {
            amounts.putKey(new ItemKey(output), getItemAmount(blockMenu, output));
        }
        return amounts;
    }

    @Nonnull
    private Map<Integer, ItemStack> snapshotSlots(
            @Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, @Nonnull ItemTransportFlow flow) {
        Map<Integer, ItemStack> snapshots = new HashMap<>();
        for (ItemStack item : items) {
            int[] slots = getTransportSlots(blockMenu, flow, item);
            if (slots == null) continue;
            for (int slot : slots) {
                if (!snapshots.containsKey(slot)) {
                    ItemStack current = blockMenu.getItemInSlot(slot);
                    snapshots.put(slot, current == null ? null : current.clone());
                }
            }
        }
        return snapshots;
    }

    private void restoreSlots(@Nonnull BlockMenu blockMenu, @Nonnull Map<Integer, ItemStack> snapshots) {
        for (Map.Entry<Integer, ItemStack> entry : snapshots.entrySet()) {
            blockMenu.replaceExistingItem(
                    entry.getKey(),
                    entry.getValue() == null ? null : entry.getValue().clone());
        }
        blockMenu.markDirty();
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
            baselineCache.remove(location);
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
                baselineCache.remove(loc);
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

        for (ItemStack input : inputs) {
            int[] inputSlots = getTransportSlots(targetMenu, ItemTransportFlow.INSERT, input);
            if (inputSlots == null
                    || !InvUtils.fitAll(targetMenu.getInventory(), new ItemStack[] {input}, inputSlots)) {
                return false;
            }
        }
        for (ItemStack output : outputs) {
            int[] outputSlots = getTransportSlots(targetMenu, ItemTransportFlow.WITHDRAW, output);
            if (outputSlots == null
                    || !InvUtils.fitAll(targetMenu.getInventory(), new ItemStack[] {output}, outputSlots)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean startCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        Location loc = block.getLocation();
        Block target = getTargetBlock(block);
        if (target == null) return false;

        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.getLocation());
        if (targetMenu == null) return false;

        IStorage targetStorage = ItemUtils.getStorage(target, false, false);
        if (targetStorage == null) return false;

        ItemStack[] inputs = Arrays.stream(ItemUtils.trimItems(recipe.getInput()))
                .map(ItemStack::clone)
                .toArray(ItemStack[]::new);
        Map<Integer, ItemStack> snapshots = snapshotSlots(targetMenu, inputs, ItemTransportFlow.INSERT);
        ItemHashMap<Long> baseline = getOutputAmounts(targetMenu, recipe);
        targetStorage.pushItem(inputs);

        for (ItemStack input : inputs) {
            if (input != null && !input.getType().isAir() && input.getAmount() > 0) {
                restoreSlots(targetMenu, snapshots);
                recipeCache.remove(loc);
                baselineCache.remove(loc);
                runningCache.remove(loc);
                return false;
            }
        }

        recipeCache.put(loc, recipe);
        baselineCache.put(loc, baseline);
        runningCache.add(loc);
        saveState(loc, recipe, baseline);
        return true;
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
            baselineCache.remove(loc);
            runningCache.remove(loc);
            clearState(loc);
            return false;
        }

        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.getLocation());
        if (targetMenu == null) return false;

        ItemHashMap<Long> baseline = baselineCache.getOrDefault(loc, new ItemHashMap<>());
        for (Map.Entry<ItemKey, Long> entry : recipe.getOutputAmounts().keyEntrySet()) {
            long current = getItemAmount(targetMenu, entry.getKey().getItemStack());
            long start = baseline.getOrDefault(entry.getKey(), 0L);
            if (current - start < entry.getValue()) return false;
        }
        return true;
    }

    @Override
    @Nullable public CraftingRecipe getFinishedCraftingRecipe(@Nonnull Block block) {
        return recipeCache.getOrDefault(block.getLocation(), null);
    }

    @Override
    @Nonnull
    public ItemStorage finishCrafting(@Nonnull Block block) {
        Location loc = block.getLocation();
        CraftingRecipe recipe = recipeCache.get(loc);
        if (recipe == null) return new ItemStorage();

        Block target = getTargetBlock(block);
        if (target == null) {
            recipeCache.remove(loc);
            baselineCache.remove(loc);
            runningCache.remove(loc);
            clearState(loc);
            return new ItemStorage();
        }

        IStorage targetStorage = ItemUtils.getStorage(target, false, false);
        if (targetStorage == null) return new ItemStorage();
        ItemRequest[] requests = ItemUtils.createRequests(recipe.getOutputAmounts());
        ItemStorage taken = targetStorage.takeItem(requests);
        if (!taken.contains(requests)) {
            ItemHashMap<Long> toReturn = new ItemHashMap<>(taken.getStorageUnsafe());
            targetStorage.pushItem(toReturn);
            return new ItemStorage();
        }
        recipeCache.remove(loc);
        baselineCache.remove(loc);
        runningCache.remove(loc);
        clearState(loc);
        return taken;
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onMEBusTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        checkAndCleanup(block.getLocation());
    }

    @Override
    public void onMEBusTick(
            Block block,
            SlimefunItem item,
            SlimefunBlockData data,
            me.ddggdd135.slimeae.api.abstracts.BusTickContext context) {
        checkAndCleanup(block.getLocation());
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}
}
