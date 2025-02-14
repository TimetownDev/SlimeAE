package me.ddggdd135.slimeae.api.abstracts;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class ChainedMEBus extends MEBus {
    public ChainedMEBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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
}
