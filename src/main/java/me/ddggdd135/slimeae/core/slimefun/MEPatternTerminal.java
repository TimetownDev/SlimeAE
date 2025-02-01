package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Arrays;
import java.util.Objects;
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

public class MEPatternTerminal extends METerminal {
    public MEPatternTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int[] getBackgroundSlots() {
        return new int[] {0, 1, 3, 5, 12, 13, 14, 21, 23, 30, 31, 32, 34, 35, 39, 41, 48, 49, 50, 51, 52, 53};
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
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);
        preset.addItem(getReturnItemSlot(), MenuItems.PUSH_BACK);
        preset.addItem(getCraftButtonSlot(), MenuItems.CRAFT_ITEM);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        super.newInstance(blockMenu, block);
        blockMenu.addMenuClickHandler(getReturnItemSlot(), (player, i, itemStack, clickAction) -> {
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) return false;
            IStorage networkStorage = info.getStorage();
            for (int slot : getCraftSlots()) {
                ItemStack item = blockMenu.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) networkStorage.pushItem(item);
            }
            for (int slot : getCraftOutputSlots()) {
                ItemStack item = blockMenu.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) networkStorage.pushItem(item);
            }
            return false;
        });
        blockMenu.replaceExistingItem(getCraftTypeSlot(), MenuItems.CRAFTING_TABLE);
        blockMenu.addMenuClickHandler(getCraftTypeSlot(), (player, i, cursor, clickAction) -> {
            ItemStack craftingTypeItem = blockMenu.getItemInSlot(i);
            if (craftingTypeItem == null || SlimefunUtils.isItemSimilar(craftingTypeItem, MenuItems.COOKING, true))
                blockMenu.replaceExistingItem(i, MenuItems.CRAFTING_TABLE);
            else blockMenu.replaceExistingItem(i, MenuItems.COOKING);
            return false;
        });
        blockMenu.addMenuClickHandler(getCraftButtonSlot(), (player, i, itemStack, clickAction) -> {
            makePattern(block);
            return false;
        });
    }

    private void makePattern(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        ItemStack out = blockMenu.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());
        if (in == null || in.getType().isAir() || !(SlimefunItem.getByItem(in) instanceof Pattern)) return;
        ItemStack craftingTypeItem = blockMenu.getItemInSlot(getCraftTypeSlot());
        ItemStack toOut = SlimefunAEItems.ENCODED_PATTERN.clone();

        if (craftingTypeItem == null || SlimefunUtils.isItemSimilar(craftingTypeItem, MenuItems.COOKING, true)) {
            toOut.setAmount(1);
            in.subtract();
            CraftingRecipe recipe = new CraftingRecipe(
                    CraftType.COOKING,
                    Arrays.stream(getCraftSlots())
                            .mapToObj(blockMenu::getItemInSlot)
                            .filter(Objects::nonNull)
                            .filter(x -> !x.getType().isAir())
                            .toArray(ItemStack[]::new),
                    Arrays.stream(getCraftOutputSlots())
                            .mapToObj(blockMenu::getItemInSlot)
                            .filter(Objects::nonNull)
                            .filter(x -> !x.getType().isAir())
                            .toArray(ItemStack[]::new));
            toOut = Pattern.setRecipe(toOut, recipe);
        } else {
            ItemStack output = null;
            for (int slot : getCraftOutputSlots()) {
                ItemStack itemStack = blockMenu.getItemInSlot(slot);
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
                ItemStack itemStack = blockMenu.getItemInSlot(slot);
                ItemStack target = recipe.getInput().length - 1 >= i ? recipe.getInput()[i] : null;
                if (target == null && itemStack != null) return;
                if (target != null && itemStack == null) return;
                if (target == null) continue;
                if (!target.equals(itemStack)) return;
            }
            toOut.setAmount(1);
            in.subtract();
            toOut = Pattern.setRecipe(toOut, recipe);
        }
        blockMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
    }

    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), getInputSlot());
                    blockMenu.dropItems(b.getLocation(), getCraftSlots());
                    blockMenu.dropItems(b.getLocation(), getCraftOutputSlots());
                    blockMenu.dropItems(b.getLocation(), getPatternSlot());
                    blockMenu.dropItems(b.getLocation(), getPatternOutputSlot());
                }
            }
        };
    }
}
