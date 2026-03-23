package me.ddggdd135.slimeae.core.slimefun;

import com.balugaq.jeg.utils.GuideUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.listeners.JEGCompatibleListener;
import me.ddggdd135.slimeae.core.slimefun.terminals.CraftTypeSelector;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class PatternWorkbench extends SlimefunItem implements InventoryBlock {
    private static final String CRAFT_TYPE_KEY = "craft_type";

    public PatternWorkbench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    public int[] getCraftSlots() {
        return new int[] {3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 21, 22, 23, 24, 25, 26};
    }

    public int[] getBorderSlots() {
        return new int[] {30, 31, 32, 33, 34, 35, 44, 48, 49, 50, 52, 53};
    }

    public int getGuideEncodeSlot() {
        return 40;
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
    }

    @Override
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuClickHandler(getCraftButtonSlot(), (player, i, itemStack, clickAction) -> {
            makePattern(block);
            return false;
        });

        if (SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded()) {
            blockMenu.replaceExistingItem(getGuideEncodeSlot(), MenuItems.JEG_PATTERN_ENCODE_BUTTON);
            blockMenu.addMenuClickHandler(getGuideEncodeSlot(), (player, i, itemStack, clickAction) -> {
                GuideUtil.openMainMenuAsync(player, SlimefunGuideMode.SURVIVAL_MODE, 1);
                JEGCompatibleListener.addCallback(player.getUniqueId(), ((event, profile) -> {
                    BlockMenu actualMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (actualMenu == null) return;
                    if (!actualMenu
                            .getPreset()
                            .getID()
                            .equals(blockMenu.getPreset().getID())) return;

                    encodePatternFromGuide(actualMenu, event.getClickedItem());
                    event.setCancelled(true);

                    player.updateInventory();
                    actualMenu.open(player);
                }));
                JEGCompatibleListener.tagGuideOpen(player);
                return false;
            });
        }

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
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());
        if (in == null
                || in.getType().isAir()
                || me.ddggdd135.slimeae.utils.ItemUtils.getSlimefunItemFast(in, Pattern.class) == null) return;

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

    private void encodePatternFromGuide(@Nonnull BlockMenu blockMenu, @Nullable ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        ItemStack out = blockMenu.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());
        if (in == null
                || in.getType().isAir()
                || me.ddggdd135.slimeae.utils.ItemUtils.getSlimefunItemFast(in, Pattern.class) == null) return;
        CraftingRecipe recipe = RecipeUtils.getRecipe(clickedItem);
        if (recipe == null) return;
        ItemStack toOut = SlimeAEItems.ENCODED_PATTERN.clone();
        toOut.setAmount(1);
        in.subtract();
        Pattern.setRecipe(toOut, recipe);
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
