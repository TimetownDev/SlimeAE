package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.blockdata.MEExportBusData;
import me.ddggdd135.slimeae.api.blockdata.MEExportBusDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.ISettingSlotHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEExportBus extends MEBus implements ISettingSlotHolder {
    private static final MEExportBusDataAdapter adapter = new MEExportBusDataAdapter();

    public MEExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

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

        Block target = block.getRelative(direction);
        BlockMenu targetInv = StorageCacheUtils.getMenu(target.getLocation());
        if (targetInv == null) return;

        IStorage networkStorage = info.getStorage();

        if (!ISettingSlotHolder.cache.containsKey(block.getLocation()))
            ISettingSlotHolder.updateCache(block, this, StorageCacheUtils.getBlock(block.getLocation()));
        List<Pair<ItemKey, Integer>> settings = ISettingSlotHolder.getCache(block.getLocation());
        for (int i = 0; i < getSettingSlots().length; i++) {
            Pair<ItemKey, Integer> setting = settings.get(i);

            if (setting == null) {
                continue;
            }

            ItemStack itemStack = setting.getFirstValue().getItemStack();

            int[] inputSlots = targetInv
                    .getPreset()
                    .getSlotsAccessedByItemTransport(targetInv, ItemTransportFlow.INSERT, itemStack);
            if (inputSlots == null || inputSlots.length == 0) continue;

            if (targetInv.fits(itemStack.asQuantity(setting.getSecondValue()), inputSlots)) {
                ItemStack[] taken = networkStorage
                        .tryTakeItem(new ItemRequest(setting.getFirstValue(), setting.getSecondValue()))
                        .toItemStacks();
                if (taken.length != 0) {
                    targetInv.pushItem(taken[0], inputSlots);
                }
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

        initSettingSlots(menu);
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
            24,
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

    @Nullable public MEExportBusData getData(@Nonnull Location location) {
        MEExportBusData data = new MEExportBusData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEExportBus)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        data.setDirection(getDirection(blockMenu));

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
        MEExportBusData meExportBusData = (MEExportBusData) data;

        setDirection(blockMenu, meExportBusData.getDirection());

        ItemStack[] itemStacks = meExportBusData.getItemStacks();

        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            blockMenu.replaceExistingItem(slot, itemStacks[i]);
        }
        ISettingSlotHolder.cache.remove(location);
    }

    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MEExportBusData;
    }

    @Nonnull
    public IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }
}
