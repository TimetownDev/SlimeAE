package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.tools.WirelessTerminal;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MESecurityTerminal extends METerminal {
    public MESecurityTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int[] getBorderSlots() {
        return new int[] {
            0, 1, 3, 4, 5, 6, 7, 8, 14, 15, 17, 23, 24, 26, 32, 33, 34, 35, 41, 42, 44, 45, 47, 49, 50, 51, 52, 53
        };
    }

    @Override
    public int[] getDisplaySlots() {
        return new int[] {9, 10, 11, 12, 13, 18, 19, 20, 21, 22, 27, 28, 29, 30, 31, 36, 37, 38, 39, 40};
    }

    @Override
    public int getInputSlot() {
        return 2;
    }

    @Override
    public int getChangeSort() {
        return 47;
    }

    @Override
    public int getFilter() {
        return 45;
    }

    @Override
    public int getPagePrevious() {
        return 46;
    }

    @Override
    public int getPageNext() {
        return 48;
    }

    public int getTerminalInputSlot() {
        return 16;
    }

    public int getTerminalOutputSlot() {
        return 43;
    }

    public int getDescriptionSlot() {
        return 25;
    }

    @Override
    public void init(@NotNull BlockMenuPreset preset) {
        super.init(preset);
        preset.addItem(
                getDescriptionSlot(),
                MenuItems.ME_SECURITY_TERMINAL_DESCRIPTION,
                ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        super.tick(block, item, data);
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        ItemStack out = blockMenu.getItemInSlot(getTerminalOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = blockMenu.getItemInSlot(getTerminalInputSlot());
        SlimefunItem slimefunItem = SlimefunItem.getByItem(in);
        if (!(slimefunItem instanceof WirelessTerminal)) return;
        WirelessTerminal.bindTo(block, in);
        blockMenu.replaceExistingItem(getTerminalOutputSlot(), in);
        blockMenu.replaceExistingItem(getTerminalInputSlot(), null);
    }

    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), getInputSlot());
                    blockMenu.dropItems(b.getLocation(), getTerminalInputSlot(), getTerminalOutputSlot());
                }
            }
        };
    }

    @Override
    public boolean fastInsert() {
        return super.fastInsert();
    }
}
