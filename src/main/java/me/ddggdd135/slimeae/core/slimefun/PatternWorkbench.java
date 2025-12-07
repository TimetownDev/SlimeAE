package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class PatternWorkbench extends SlimefunItem implements InventoryBlock {

    public PatternWorkbench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    public int[] getCraftSlots() {
        return new int[] {3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 21, 22, 23, 24, 25, 26};
    }

    public int[] getBorderSlots() {
        return new int[] {30, 31, 32, 33, 34, 35, 40, 44, 48, 49, 50, 52, 53};
    }

    @Override
    public int[] getInputSlots() {
        return getCraftSlots();
    }

    @Override
    public int[] getOutputSlots() {
        return getCraftOutputSlots();
    }

    public int[] getCraftOutputSlots() {
        return new int[] {0, 1, 9, 10, 18, 19, 27, 28, 36, 37, 45, 46};
    }

    public int[] getCraftOutputBorderSlots() {
        return new int[] {2, 11, 20, 29, 38, 47};
    }

    public int getCraftButtonSlot() {
        return 42;
    }

    public int getCraftTypeSlot() {
        return 51;
    }

    public int getPatternSlot() {
        return 41;
    }

    public int getPatternOutputSlot() {
        return 43;
    }

    public int getAllSupportedRecipeSlot() {
        return 39;
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : getCraftOutputBorderSlots()) {
            preset.addItem(slot, MenuItems.CRAFTING_OUTPUT_BORDER);
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(getCraftButtonSlot(), MenuItems.CRAFT_ITEM);

        preset.addItem(getAllSupportedRecipeSlot(), MenuItems.PATTERN_WORKBENCH_ALL_SUPPORTED_RECIPE);
        preset.addMenuClickHandler(getAllSupportedRecipeSlot(), ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuClickHandler(getCraftButtonSlot(), (player, i, itemStack, clickAction) -> {
            makePattern(block);
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
    }

    private void makePattern(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        ItemStack out = blockMenu.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());
        if (in == null || in.getType().isAir() || !(SlimefunItem.getByItem(in) instanceof Pattern)) return;
        ItemStack craftingTypeItem = blockMenu.getItemInSlot(getCraftTypeSlot());
        ItemStack toOut = SlimeAEItems.ENCODED_PATTERN.clone();

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

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                blockMenu.dropItems(b.getLocation(), getCraftSlots());
                blockMenu.dropItems(b.getLocation(), getOutputSlots());
                blockMenu.dropItems(b.getLocation(), getPatternSlot());
                blockMenu.dropItems(b.getLocation(), getPatternOutputSlot());
            }
        };
    }
}
