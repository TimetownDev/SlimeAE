package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MEPatternTerminal extends METerminal {
    public MEPatternTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int[] getBackgroundSlots() {
        return new int[] {0, 1, 3, 5, 12, 13, 14, 21, 23, 30, 31, 32, 33, 34, 35, 39, 41, 48, 49, 50, 51, 52, 53};
    }

    @Override
    public int[] getDisplaySlots() {
        return new int[] {9, 10, 11, 18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46, 47};
    }

    @Override
    public int getInputSlot() {
        return 2;
    }

    @Override
    public int getChangeSort() {
        return 21;
    }

    @Override
    public int getFilter() {
        return 30;
    }

    @Override
    public int getPagePrevious() {
        return 39;
    }

    @Override
    public int getPageNext() {
        return 48;
    }

    public int[] getCraftSlots() {
        return new int[] {6, 7, 8, 15, 16, 17, 24, 25, 26};
    }

    public int[] getCraftOutputSlots() {
        return new int[] {42, 43, 44};
    }

    public int getReturnItemSlot() {
        return 34;
    }

    public int getCraftTypeSlot() {
        return 33;
    }

    public int getCraftButtonSlot() {
        return 22;
    }

    public int getPatternSlot() {
        return 4;
    }

    public int getPatternOutputSlot() {
        return 40;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@NotNull BlockMenuPreset preset) {
        super.init(preset);
        preset.addItem(getReturnItemSlot(), MenuItems.PUSH_BACK);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        super.newInstance(blockMenu, block);
        blockMenu.addMenuClickHandler(getReturnItemSlot(), (player, i, itemStack, clickAction) -> {
            BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
            if (inv == null) return false;
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) return false;
            IStorage networkStorage = info.getStorage();
            for (int slot : getCraftSlots()) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) networkStorage.pushItem(item);
            }
            for (int slot : getCraftOutputSlots()) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) networkStorage.pushItem(item);
            }
            return false;
        });
        blockMenu.addItem(getCraftTypeSlot(), MenuItems.CRAFTING_TABLE);
        blockMenu.addMenuClickHandler(getCraftTypeSlot(), (player, i, cursor, clickAction) -> {
            ItemStack craftingTypeItem = blockMenu.getItemInSlot(i);
            if (craftingTypeItem == null || SlimefunUtils.isItemSimilar(craftingTypeItem, MenuItems.COOKING, true))
                blockMenu.replaceExistingItem(i, MenuItems.CRAFTING_TABLE);
            else blockMenu.replaceExistingItem(i, MenuItems.COOKING);
            return false;
        });
        blockMenu.addItem(getCraftButtonSlot(), MenuItems.CRAFT_ITEM);
        blockMenu.addMenuClickHandler(getCraftButtonSlot(), (player, i, itemStack, clickAction) -> {
            makePattern(block);
            return false;
        });
    }

    private void makePattern(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        ItemStack out = inv.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = inv.getItemInSlot(getPatternSlot());
        if (in == null || in.getType().isAir() || !(SlimefunItem.getByItem(in) instanceof Pattern)) return;
        ItemStack craftingTypeItem = inv.getItemInSlot(getCraftTypeSlot());
        ItemStack toOut = SlimefunAEItems.ENCODED_PATTERN.clone();

        if (craftingTypeItem == null || SlimefunUtils.isItemSimilar(craftingTypeItem, MenuItems.COOKING, true)) {
            toOut.setAmount(1);
            in.subtract();
            CraftingRecipe recipe = new CraftingRecipe(
                    CraftType.COOKING,
                    Arrays.stream(getCraftSlots()).mapToObj(inv::getItemInSlot).toArray(ItemStack[]::new),
                    Arrays.stream(getCraftOutputSlots())
                            .mapToObj(inv::getItemInSlot)
                            .toArray(ItemStack[]::new));
            Pattern.setRecipe(toOut, recipe);
        } else {
            ItemStack output = null;
            for (int slot : getCraftOutputSlots()) {
                ItemStack itemStack = inv.getItemInSlot(slot);
                if (output != null
                        && !output.getType().isAir()
                        && itemStack != null
                        && !itemStack.getType().isAir()) return;
                if (itemStack != null && !itemStack.getType().isAir()) output = itemStack;
            }
            if (output == null) return;
            CraftingRecipe recipe = RecipeUtils.getRecipe(output);
            if (recipe == null) return;
            for (int i = 0; i < getCraftSlots().length; i++) {
                int slot = getCraftSlots()[i];
                ItemStack itemStack = inv.getItemInSlot(slot);
                ItemStack target = recipe.getInput()[i];
                if (!SlimefunUtils.isItemSimilar(target, itemStack, true, false)) return;
            }
            toOut.setAmount(1);
            in.subtract();
            Pattern.setRecipe(toOut, recipe);
        }
        inv.replaceExistingItem(getPatternOutputSlot(), toOut);
    }
}
