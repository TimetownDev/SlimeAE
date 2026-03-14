package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
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
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.ddggdd135.slimeae.utils.VanillaRecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MEVanillaPatternTerminal extends METerminal {
    private static final String CRAFT_TYPE_KEY = "vanilla_craft_type";
    private static final CraftType[] VANILLA_TYPES = {
        CraftType.VANILLA_CRAFTING_TABLE,
        CraftType.VANILLA_FURNACE,
        CraftType.VANILLA_BLAST_FURNACE,
        CraftType.VANILLA_SMOKER,
        CraftType.VANILLA_STONECUTTER
    };

    public MEVanillaPatternTerminal(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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
            NetworkInfo info =
                    me.ddggdd135.slimeae.SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
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

        blockMenu.replaceExistingItem(getCraftTypeSlot(), getVanillaTypeIcon(getStoredVanillaType(block)));
        blockMenu.addMenuClickHandler(getCraftTypeSlot(), (player, i, cursor, clickAction) -> {
            CraftType current = getStoredVanillaType(block);
            CraftType next = getNextVanillaType(current);
            StorageCacheUtils.setData(block.getLocation(), CRAFT_TYPE_KEY, next.name());
            blockMenu.replaceExistingItem(i, getVanillaTypeIcon(next));
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
    }

    @Nonnull
    private CraftType getStoredVanillaType(@Nonnull Block block) {
        String stored = StorageCacheUtils.getData(block.getLocation(), CRAFT_TYPE_KEY);
        if (stored == null || stored.isEmpty()) return CraftType.VANILLA_CRAFTING_TABLE;
        CraftType type = CraftType.fromName(stored);
        if (type == null || !type.isVanilla()) return CraftType.VANILLA_CRAFTING_TABLE;
        return type;
    }

    @Nonnull
    private CraftType getNextVanillaType(@Nonnull CraftType current) {
        for (int i = 0; i < VANILLA_TYPES.length; i++) {
            if (VANILLA_TYPES[i] == current) {
                return VANILLA_TYPES[(i + 1) % VANILLA_TYPES.length];
            }
        }
        return VANILLA_TYPES[0];
    }

    @Nonnull
    private ItemStack getVanillaTypeIcon(@Nonnull CraftType type) {
        return switch (type) {
            case VANILLA_FURNACE -> MenuItems.VANILLA_FURNACE.clone();
            case VANILLA_BLAST_FURNACE -> MenuItems.VANILLA_BLAST_FURNACE.clone();
            case VANILLA_SMOKER -> MenuItems.VANILLA_SMOKER.clone();
            case VANILLA_STONECUTTER -> MenuItems.VANILLA_STONECUTTER.clone();
            default -> MenuItems.VANILLA_CRAFTING_TABLE.clone();
        };
    }

    private void makePattern(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        ItemStack out = blockMenu.getItemInSlot(getPatternOutputSlot());
        if (out != null && !out.getType().isAir()) return;
        ItemStack in = blockMenu.getItemInSlot(getPatternSlot());
        if (in == null || in.getType().isAir() || !(SlimefunItem.getByItem(in) instanceof Pattern)) return;

        CraftType vanillaType = getStoredVanillaType(block);
        ItemStack toOut = SlimeAEItems.ENCODED_PATTERN.clone();
        toOut.setAmount(1);

        CraftingRecipe recipe = null;
        if (vanillaType == CraftType.VANILLA_CRAFTING_TABLE) {
            recipe = makeVanillaCraftingPattern(blockMenu);
        } else {
            recipe = makeProcessPattern(blockMenu, vanillaType);
        }

        if (recipe == null) return;
        in.subtract();
        Pattern.setRecipe(toOut, recipe);
        blockMenu.replaceExistingItem(getPatternOutputSlot(), toOut);
    }

    private CraftingRecipe makeVanillaCraftingPattern(BlockMenu blockMenu) {
        List<ItemStack> inputList = new ArrayList<>();
        for (int slot : getCraftSlots()) {
            inputList.add(blockMenu.getItemInSlot(slot));
        }
        ItemStack[] inputs = inputList.toArray(ItemStack[]::new);

        boolean hasSlimefunItem = Arrays.stream(inputs)
                .filter(Objects::nonNull)
                .filter(x -> !x.getType().isAir())
                .anyMatch(x -> SlimefunItem.getByItem(x) != null);
        if (hasSlimefunItem) return null;

        return VanillaRecipeUtils.getCraftingTableRecipe(inputs);
    }

    private CraftingRecipe makeProcessPattern(BlockMenu blockMenu, CraftType type) {
        ItemStack[] inputs = Arrays.stream(getCraftSlots())
                .mapToObj(blockMenu::getItemInSlot)
                .filter(Objects::nonNull)
                .filter(x -> !x.getType().isAir())
                .toArray(ItemStack[]::new);
        ItemStack[] outputs = Arrays.stream(getCraftOutputSlots())
                .mapToObj(blockMenu::getItemInSlot)
                .filter(Objects::nonNull)
                .filter(x -> !x.getType().isAir())
                .toArray(ItemStack[]::new);
        if (inputs.length == 0 || outputs.length == 0) return null;

        ItemStack input = inputs[0];
        ItemStack output = outputs[0];

        if (SlimefunItem.getByItem(input) != null) return null;

        CraftingRecipe found = VanillaRecipeUtils.findRecipeByOutput(input, output, type);
        if (found != null) return found;

        List<CraftingRecipe> recipes = VanillaRecipeUtils.getRecipesForType(input, type);
        if (recipes.isEmpty()) return null;
        for (CraftingRecipe recipe : recipes) {
            if (recipe.getOutput().length > 0 && recipe.getOutput()[0].getType() == output.getType()) {
                return recipe;
            }
        }
        return null;
    }

    private void handleRecipeSelect(@Nonnull Block block, @Nonnull Player player, boolean shiftClick) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = me.ddggdd135.slimeae.SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        CraftType vanillaType = getStoredVanillaType(block);

        List<ItemStack> inputList = new ArrayList<>();
        for (int slot : getCraftSlots()) {
            inputList.add(blockMenu.getItemInSlot(slot));
        }
        ItemStack[] inputs = inputList.toArray(ItemStack[]::new);

        Runnable backAction = () -> {
            BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
            if (menu != null) menu.open(player);
        };

        RecipeSelectMenu.open(player, inputs, vanillaType, backAction, recipe -> {
            BlockMenu actualMenu = StorageCacheUtils.getMenu(block.getLocation());
            if (actualMenu == null) return;

            if (shiftClick && info != null) {
                NetworkRecipeFetch.moveRecipeFromNetwork(actualMenu, info.getStorage(), recipe, getCraftSlots(), false);
            }

            ItemStack patternIn = actualMenu.getItemInSlot(getPatternSlot());
            if (patternIn == null
                    || patternIn.getType().isAir()
                    || !(SlimefunItem.getByItem(patternIn) instanceof Pattern)) {
                actualMenu.open(player);
                return;
            }

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
                clearSortedItemsCache(b.getLocation());
            }
        };
    }

    @Override
    public boolean fastInsert() {
        return super.fastInsert();
    }

    @Override
    public int getJEGFindingButtonSlot() {
        return 12;
    }
}
