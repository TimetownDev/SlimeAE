package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.blockdata.MEExportBusData;
import me.ddggdd135.slimeae.api.blockdata.MEInterfaceData;
import me.ddggdd135.slimeae.api.blockdata.MEInterfaceDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEInterface extends TickingBlock
        implements IMECraftHolder, InventoryBlock, ICardHolder, IDataBlock, ISettingSlotHolder {
    private static final MEInterfaceDataAdapter adapter = new MEInterfaceDataAdapter();
    public static final BlockFace[] VaildBlockFace = new BlockFace[] {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN
    };

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        IStorage networkStorage = info.getStorage();
        if (!ISettingSlotHolder.cache.containsKey(block.getLocation()))
            ISettingSlotHolder.updateCache(block, this, StorageCacheUtils.getBlock(block.getLocation()));
        List<Pair<ItemKey, Integer>> settings = ISettingSlotHolder.getCache(block.getLocation());
        for (int i = 0; i < getItemSlots().length; i++) {
            int slot = getItemSlots()[i];

            Pair<ItemKey, Integer> setting = settings.get(i);
            ItemStack itemStack = blockMenu.getItemInSlot(slot);

            if (setting == null) {
                if (itemStack != null && !itemStack.getType().isAir()) networkStorage.pushItem(itemStack);
                continue;
            }

            ItemStack settingItemStack = setting.getFirstValue().getItemStack();

            if (!SlimefunUtils.isItemSimilar(settingItemStack, itemStack, true, false)) {
                if (itemStack != null && !itemStack.getType().isAir()) networkStorage.pushItem(itemStack);
                continue;
            }

            int amount = 0;
            if (itemStack != null && !itemStack.getType().isAir()) amount = itemStack.getAmount();
            if (amount == setting.getSecondValue()) continue;
            if (amount > setting.getSecondValue()) {
                ItemStack toPush = itemStack.clone();
                toPush.setAmount(amount - setting.getSecondValue());
                networkStorage.pushItem(toPush);
                itemStack.setAmount(setting.getSecondValue() + toPush.getAmount());
                continue;
            }

            ItemStack newItemStack = null;
            if (itemStack != null && !itemStack.getType().isAir()) {
                newItemStack = itemStack.clone();
                itemStack.setAmount(0);
            }
            ItemStack[] received = networkStorage
                    .takeItem(new ItemRequest(setting.getFirstValue(), setting.getSecondValue() - amount))
                    .toItemStacks();
            if (received.length != 0) {
                if (newItemStack != null && !newItemStack.getType().isAir()) {
                    newItemStack.setAmount(amount + received[0].getAmount());
                    blockMenu.replaceExistingItem(slot, newItemStack);
                } else blockMenu.replaceExistingItem(slot, received[0]);
            } else if (newItemStack != null) {
                itemStack.setAmount(newItemStack.getAmount());
            }
        }

        tickCards(block, item, data);
    }

    public MEInterface(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), getItemSlots());
                    dropCards(blockMenu);
                }

                for (int slot : getPatternSlots()) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.PATTERN, true, false))) {
                        b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                    }
                }
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return getItemSlots();
    }

    @Override
    public int[] getOutputSlots() {
        return getItemSlots();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        preset.drawBackground(getBorderSlots());
        for (int slot : getPatternSlots()) {
            preset.addMenuClickHandler(slot, ItemUtils.getPatternSlotClickHandler());
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        initSettingSlots(menu);
        initCardSlots(menu);
        for (int slot : getPatternSlots()) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir()) menu.replaceExistingItem(slot, MenuItems.PATTERN);
        }
    }

    @Nonnull
    @Override
    public Block[] getCraftingDevices(@Nonnull Block block) {
        List<Block> result = new ArrayList<>();
        for (BlockFace blockFace : VaildBlockFace) {
            Block relative = block.getRelative(blockFace);
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(relative.getLocation());
            if (blockData == null) continue;
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftDevice) result.add(relative);
        }
        return result.toArray(Block[]::new);
    }

    @Nonnull
    @Override
    public CraftingRecipe[] getSupportedRecipes(@Nonnull Block block) {
        Set<CraftingRecipe> result = new HashSet<>();
        Block[] devices = getCraftingDevices(block);
        for (Block device : devices) {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(device.getLocation());
            if (blockData == null) continue;
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftDevice IMECraftDevice) {
                for (CraftingRecipe recipe : getRecipes(block)) {
                    if (result.contains(recipe)) continue;
                    if (IMECraftDevice.isSupport(device, recipe)) result.add(recipe);
                }
            }
        }
        return result.toArray(CraftingRecipe[]::new);
    }

    @Nonnull
    public CraftingRecipe[] getRecipes(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return new CraftingRecipe[0];
        Set<CraftingRecipe> result = new HashSet<>();
        for (int slot : getPatternSlots()) {
            ItemStack patternItem = blockMenu.getItemInSlot(slot);
            if (patternItem == null || patternItem.getType().isAir()) continue;
            SlimefunItem slimefunItem = SlimefunItem.getByItem(patternItem);
            if (!(slimefunItem instanceof Pattern)) continue;
            CraftingRecipe recipe = Pattern.getRecipe(patternItem);
            if (recipe == null) continue;
            result.add(recipe);
        }
        return result.toArray(CraftingRecipe[]::new);
    }

    @Override
    public int[] getCardSlots() {
        return new int[] {45, 46, 47};
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    public int[] getSettingSlots() {
        return new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};
    }

    public int[] getPatternSlots() {
        return new int[] {
            36, 37, 38, 39, 40, 41, 42, 43, 44,
        };
    }

    public int[] getItemSlots() {
        return new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26};
    }

    public int[] getBorderSlots() {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 27, 28, 29, 30, 31, 32, 33, 34, 35, 48, 49, 50, 51, 52, 53};
    }

    @Nullable public MEExportBusData getData(@Nonnull Location location) {
        MEExportBusData data = new MEExportBusData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEInterface)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        ItemStack[] itemStacks = new ItemStack[getSettingSlots().length];
        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            itemStacks[i] = blockMenu.getItemInSlot(slot);
        }
        data.setItemStacks(itemStacks);

        return data;
    }

    public void applyData(@Nonnull Location location, @Nullable IBlockData data) {
        if (!canApplyData(location, data)) return;
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        MEInterfaceData meInterfaceData = (MEInterfaceData) data;

        ItemStack[] itemStacks = meInterfaceData.getItemStacks();

        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            blockMenu.replaceExistingItem(slot, itemStacks[i]);
        }
    }

    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEInterface)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEInterface)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MEInterfaceData;
    }

    @Nonnull
    public IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }
}
