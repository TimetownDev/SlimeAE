package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IRecipeCompletableWithGuide;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.PatternUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MEPatternTerminal extends METerminal implements IRecipeCompletableWithGuide {
    private static final String CRAFT_TYPE_KEY = "craft_type";

    public MEPatternTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int[] getBorderSlots() {
        return new int[] {0, 1, 3, 5, 12, 13, 14, 21, 23, 30, 31, 34, 35, 39, 41, 48, 49, 50, 51, 52, 53};
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

    public int getRecipeSelectSlot() {
        return 32;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);
        preset.addItem(getReturnItemSlot(), MenuItems.PUSH_BACK);
        preset.addItem(getCraftButtonSlot(), MenuItems.CRAFT_ITEM);
        preset.addItem(getRecipeSelectSlot(), MenuItems.RECIPE_SELECT);
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

        CraftType initialType = getSelectedCraftType(block);
        blockMenu.replaceExistingItem(getCraftTypeSlot(), CraftTypeSelector.createTypeIcon(initialType));
        blockMenu.addMenuClickHandler(getCraftTypeSlot(), (player, i, cursor, clickAction) -> {
            CraftTypeSelector.open(player, selectedType -> {
                StorageCacheUtils.setData(block.getLocation(), CRAFT_TYPE_KEY, selectedType.name());
                BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
                if (menu != null) {
                    menu.replaceExistingItem(getCraftTypeSlot(), CraftTypeSelector.createTypeIcon(selectedType));
                    menu.open(player);
                }
            });
            return false;
        });

        blockMenu.addMenuClickHandler(getCraftButtonSlot(), (player, i, itemStack, clickAction) -> {
            makePattern(block);
            return false;
        });

        blockMenu.addMenuClickHandler(getRecipeSelectSlot(), (player, i, itemStack, clickAction) -> {
            handleRecipeSelect(block, player, clickAction.isShiftClicked());
            return false;
        });

        addJEGRecipeButton(blockMenu, getJEGRecipeButtonSlot());
    }

    @Nonnull
    private CraftType getSelectedCraftType(@Nonnull Block block) {
        String stored = StorageCacheUtils.getData(block.getLocation(), CRAFT_TYPE_KEY);
        if (stored == null || stored.isEmpty()) {
            return CraftType.ENHANCED_CRAFTING_TABLE;
        }
        CraftType type = CraftType.fromName(stored);
        if (type == null) {
            return CraftType.ENHANCED_CRAFTING_TABLE;
        }
        if (type == CraftType.CRAFTING_TABLE) {
            return CraftType.ENHANCED_CRAFTING_TABLE;
        }
        return type;
    }

    private void makePattern(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        ItemStack out = blockMenu.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        if (!PatternUtils.tryAutoFillBlankPattern(blockMenu, getPatternSlot(), block)) return;
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());

        CraftType selectedType = getSelectedCraftType(block);
        ItemStack toOut = SlimeAEItems.ENCODED_PATTERN.clone();

        if (selectedType.isProcess()) {
            makeProcessPattern(blockMenu, in, toOut);
        } else if (selectedType.isLarge()) {
            makeLargePattern(blockMenu, in, toOut);
        } else {
            makeSmallPattern(blockMenu, in, toOut);
        }
    }

    private void makeProcessPattern(BlockMenu blockMenu, ItemStack patternIn, ItemStack toOut) {
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
        patternIn.subtract();
        CraftingRecipe recipe = new CraftingRecipe(CraftType.COOKING, input, output);
        Pattern.setRecipe(toOut, recipe);
        blockMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
    }

    private void makeLargePattern(BlockMenu blockMenu, ItemStack patternIn, ItemStack toOut) {
        ItemStack[] outputItems = Arrays.stream(getCraftOutputSlots())
                .mapToObj(blockMenu::getItemInSlot)
                .filter(Objects::nonNull)
                .filter(x -> !x.getType().isAir())
                .toArray(ItemStack[]::new);
        if (outputItems.length == 0) return;

        CraftingRecipe recipe = RecipeUtils.getRecipe(outputItems[0], RecipeUtils.LARGE_TYPES);
        if (recipe == null || !recipe.getCraftType().isLarge()) return;

        toOut.setAmount(1);
        patternIn.subtract();
        Pattern.setRecipe(toOut, recipe);
        blockMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
    }

    private void makeSmallPattern(BlockMenu blockMenu, ItemStack patternIn, ItemStack toOut) {
        List<ItemStack> inputList = new ArrayList<>();
        for (int slot : getCraftSlots()) {
            inputList.add(blockMenu.getItemInSlot(slot));
        }
        ItemStack[] inputs = inputList.toArray(ItemStack[]::new);

        List<ItemStack> outputList = new ArrayList<>();
        for (int slot : getCraftOutputSlots()) {
            outputList.add(blockMenu.getItemInSlot(slot));
        }
        ItemStack[] outputs = outputList.toArray(ItemStack[]::new);

        CraftingRecipe recipe;
        if (ItemUtils.trimItems(outputs).length != 0) {
            recipe = RecipeUtils.getRecipe(inputs, outputs);
        } else {
            recipe = RecipeUtils.getRecipe(inputs);
        }

        if (recipe == null) return;

        toOut.setAmount(1);
        patternIn.subtract();
        Pattern.setRecipe(toOut, recipe);
        blockMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
    }

    private void handleRecipeSelect(@Nonnull Block block, @Nonnull Player player, boolean shiftClick) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        CraftType selectedType = getSelectedCraftType(block);

        List<ItemStack> inputList = new ArrayList<>();
        for (int slot : getCraftSlots()) {
            inputList.add(blockMenu.getItemInSlot(slot));
        }
        ItemStack[] inputs = inputList.toArray(ItemStack[]::new);

        Runnable backAction = () -> {
            BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
            if (menu != null) menu.open(player);
        };

        RecipeSelectMenu.open(player, inputs, selectedType, backAction, recipe -> {
            BlockMenu actualMenu = StorageCacheUtils.getMenu(block.getLocation());
            if (actualMenu == null) return;

            if (shiftClick && info != null) {
                NetworkRecipeFetch.moveRecipeFromNetwork(actualMenu, info.getStorage(), recipe, getCraftSlots(), false);
            }

            if (!PatternUtils.tryAutoFillBlankPattern(actualMenu, getPatternSlot(), block)) {
                actualMenu.open(player);
                return;
            }
            ItemStack patternIn = actualMenu.getItemInSlot(getPatternSlot());

            ItemStack patternOut = actualMenu.getItemInSlot(getPatternOutputSlot());
            if (patternOut != null && !patternOut.getType().isAir()) {
                actualMenu.open(player);
                return;
            }

            ItemStack toOut = SlimeAEItems.ENCODED_PATTERN.clone();
            toOut.setAmount(1);
            patternIn.subtract();
            Pattern.setRecipe(toOut, recipe);
            actualMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
            actualMenu.open(player);
        });
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
                // 清理缓存，防止内存泄漏
                clearSortedItemsCache(b.getLocation());
            }
        };
    }

    @Override
    public boolean fastInsert() {
        return super.fastInsert();
    }

    @Override
    public int[] getIngredientSlots() {
        return getCraftSlots();
    }

    public int getJEGRecipeButtonSlot() {
        return 35;
    }

    @Override
    public int getJEGFindingButtonSlot() {
        return 12;
    }
}
