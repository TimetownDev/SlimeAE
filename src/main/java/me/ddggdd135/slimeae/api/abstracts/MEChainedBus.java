package me.ddggdd135.slimeae.api.abstracts;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.blockdata.MEChainedBusData;
import me.ddggdd135.slimeae.api.blockdata.MEChainedBusDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class MEChainedBus extends MEBus {
    private static final MEChainedBusDataAdapter adapter = new MEChainedBusDataAdapter();

    public MEChainedBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public int getDistanceSlot() {
        return 24;
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);

        if (menu.getItemInSlot(getDistanceSlot()) == null
                || menu.getItemInSlot(getDistanceSlot()).getType().isAir()) {
            menu.replaceExistingItem(getDistanceSlot(), MenuItems.DISTANCE);
        }
        menu.addMenuClickHandler(getDistanceSlot(), ItemUtils.getDistanceSlotClickHandler());
    }

    public int getDistance(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return 0;
        BlockMenu menu = slimefunBlockData.getBlockMenu();
        if (menu == null) return 0;
        return menu.getItemInSlot(getDistanceSlot()).getAmount();
    }

    public void setDistance(@Nonnull Location location, int distance) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return;
        BlockMenu menu = slimefunBlockData.getBlockMenu();
        if (menu == null) return;
        if (distance <= 0) distance = 1;
        menu.replaceExistingItem(getDistanceSlot(), MenuItems.DISTANCE.asQuantity(distance));
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        if (data.getBlockMenu().hasViewer()) updateGui(data);
    }

    public void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (slimefunBlockData == null) return;
        tickCards(block, this, slimefunBlockData);
        onMEBusTick(block, this, slimefunBlockData);
    }

    @Override
    public int[] getBorderSlots() {
        return new int[] {
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            13,
            14,
            16,
            17,
            18,
            19,
            21,
            23,
            25,
            26,
            27,
            28,
            29,
            31,
            32,
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

    @Nullable public MEChainedBusData getData(@Nonnull Location location) {
        MEChainedBusData data = new MEChainedBusData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedBus)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        data.setDirection(getDirection(blockMenu));
        data.setDistance(getDistance(location));

        return data;
    }

    public void applyData(@Nonnull Location location, @Nullable IBlockData data) {
        if (!canApplyData(location, data)) return;
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        MEChainedBusData meChainedBusData = (MEChainedBusData) data;

        setDirection(blockMenu, meChainedBusData.getDirection());
        setDistance(location, meChainedBusData.getDistance());
    }

    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MEChainedBusData;
    }

    @Nonnull
    public IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }
}
