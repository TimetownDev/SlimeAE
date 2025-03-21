package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.ddggdd135.slimeae.utils.ItemUtils;
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
    public int[] getBorderSlots() {
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
            ItemStack[] input = Arrays.stream(getCraftSlots())
                    .mapToObj(blockMenu::getItemInSlot)
                    .filter(Objects::nonNull)
                    .filter(x -> !x.getType().isAir())
                    .toArray(ItemStack[]::new);
            ItemStack[] output = Arrays.stream(getCraftOutputSlots())
                    .mapToObj(blockMenu::getItemInSlot)
                    .filter(Objects::nonNull)
                    .filter(x -> !x.getType().isAir())
                    .toArray(ItemStack[]::new);
            if (input.length == 0 || output.length == 0) return;
            in.subtract();
            CraftingRecipe recipe = new CraftingRecipe(CraftType.COOKING, input, output);
            Pattern.setRecipe(toOut, recipe);
        } else {
            List<ItemStack> inputList = new ArrayList<>();
            for (int slot : getCraftSlots()) {
                inputList.add(blockMenu.getItemInSlot(slot));
            }

            ItemStack[] inputs = inputList.toArray(ItemStack[]::new);

            ItemStack[] outputs;
            List<ItemStack> outputList = new ArrayList<>();
            for (int slot : getCraftOutputSlots()) {
                outputList.add(blockMenu.getItemInSlot(slot));
            }

            outputs = outputList.toArray(ItemStack[]::new);

            CraftingRecipe recipe;
            if (ItemUtils.trimItems(outputs).length != 0) recipe = RecipeUtils.getRecipe(inputs, outputs);
            else recipe = RecipeUtils.getRecipe(inputs);

            if (recipe == null) return;

            toOut.setAmount(1);
            in.subtract();
            Pattern.setRecipe(toOut, recipe);
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
