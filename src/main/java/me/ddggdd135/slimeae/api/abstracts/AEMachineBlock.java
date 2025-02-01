package me.ddggdd135.slimeae.api.abstracts;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.abstracts.AbstractMachineBlock;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class AEMachineBlock extends AbstractMachineBlock implements ICardHolder {
    private static final int[] CARD_SLOTS = {36, 37, 38}; // 左下角3个槽位

    @Override
    public int[] getCardSlots() {
        return CARD_SLOTS;
    }

    protected AEMachineBlock(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onAEBlockBreak());
    }

    protected AEMachineBlock(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
        addItemHandler(onAEBlockBreak());
    }

    public int[] getBorder() {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 31, 39, 40, 41, 42, 43, 44};
    }

    @Nonnull
    protected BlockBreakHandler onAEBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                for (int slot : CARD_SLOTS) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.Card, true, false))) {
                        b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                    }
                }
            }
        };
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);

        for (int slot : getCardSlots()) {
            preset.addMenuClickHandler(slot, ItemUtils.getCardSlotClickHandler());
        }
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);

        for (int slot : getCardSlots()) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir()) {
                menu.replaceExistingItem(slot, MenuItems.Card);
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        tickCards(block, item, data);
        super.tick(block, item, data);
    }
}
