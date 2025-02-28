package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.MEChainedBus;
import me.ddggdd135.slimeae.api.blockdata.MEChainedExportBusData;
import me.ddggdd135.slimeae.api.blockdata.MEChainedExportBusDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEChainedExportBus extends MEChainedBus {
    private final MEChainedExportBusDataAdapter adapter = new MEChainedExportBusDataAdapter();

    public MEChainedExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;
                blockMenu.dropItems(b.getLocation(), getSettingSlots());

                for (int slot : getCardSlots()) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.CARD, true, false))) {
                        b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                    }
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    public void onExport(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        BlockFace direction = getDirection(blockMenu);
        if (direction == BlockFace.SELF) return;

        int distance = getDistance(block.getLocation());
        Block target = block.getRelative(direction);
        for (int i = 0; i < distance; i++) {
            BlockMenu targetInv = StorageCacheUtils.getMenu(target.getLocation());
            if (targetInv == null) return;

            IStorage networkStorage = info.getStorage();

            for (int slot : getSettingSlots()) {
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), slot);
                if (setting == null || setting.getType().isAir()) {
                    target = target.getRelative(direction);
                    continue;
                }

                int[] inputSlots = targetInv
                        .getPreset()
                        .getSlotsAccessedByItemTransport(targetInv, ItemTransportFlow.INSERT, setting);
                if (inputSlots == null || inputSlots.length == 0) {
                    target = target.getRelative(direction);
                    continue;
                }

                if (targetInv.fits(setting, inputSlots)) {
                    ItemStack[] taken = networkStorage.tryTakeItem(new ItemRequest(setting, setting.getAmount()));
                    if (taken.length != 0) {
                        targetInv.pushItem(taken[0], inputSlots);
                    }
                }

                target = target.getRelative(direction);
            }
        }
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onMEBusTick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu inv = StorageCacheUtils.getMenu(data.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(data.getLocation());
        if (info == null) return;

        onExport(data.getLocation().getBlock());
    }

    @Override
    public int getNorthSlot() {
        return 1;
    }

    @Override
    public int getSouthSlot() {
        return 19;
    }

    @Override
    public int getEastSlot() {
        return 11;
    }

    @Override
    public int getWestSlot() {
        return 9;
    }

    @Override
    public int getUpSlot() {
        return 2;
    }

    @Override
    public int getDownSlot() {
        return 20;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);
    }

    @Override
    public int[] getBorderSlots() {
        return new int[] {
            0,
            1,
            2,
            6,
            7,
            8,
            9,
            10,
            11,
            15,
            16,
            17,
            18,
            19,
            25,
            26,
            27,
            28,
            29,
            30,
            31,
            32,
            33,
            34,
            35,
            36,
            37,
            38,
            39,
            40,
            41,
            42,
            43,
            44,
            48,
            49,
            50,
            51,
            52,
            53 // 移除45,46,47用于卡槽
        };
    }

    public int[] getSettingSlots() {
        return new int[] {3, 4, 5, 12, 13, 14, 21, 22, 23};
    }

    @Nullable public MEChainedExportBusData getData(@Nonnull Location location) {
        MEChainedExportBusData data = new MEChainedExportBusData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        data.setDirection(getDirection(blockMenu));
        data.setDistance(getDistance(location));

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
        MEChainedExportBusData meChainedExportBusData = (MEChainedExportBusData) data;

        setDirection(blockMenu, meChainedExportBusData.getDirection());
        setDistance(location, meChainedExportBusData.getDistance());

        ItemStack[] itemStacks = meChainedExportBusData.getItemStacks();

        blockMenu.dropItems(location, getSettingSlots());

        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            blockMenu.replaceExistingItem(slot, itemStacks[i]);
        }
    }

    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MEChainedExportBusData;
    }

    @Nonnull
    public IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }
}
