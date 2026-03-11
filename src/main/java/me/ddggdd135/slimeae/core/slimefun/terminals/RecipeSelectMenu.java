package me.ddggdd135.slimeae.core.slimefun.terminals;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftTypeRegistry;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.ddggdd135.slimeae.utils.VanillaRecipeUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class RecipeSelectMenu {
    private static final int[] ITEM_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    private static final int PREV_SLOT = 48;
    private static final int BACK_SLOT = 49;
    private static final int NEXT_SLOT = 50;
    private static final int[] BORDER_SLOTS = {45, 46, 47, 51, 52, 53};
    private static final int SIZE = 54;

    private RecipeSelectMenu() {}

    public static void open(
            @Nonnull Player player,
            @Nonnull ItemStack[] inputs,
            @Nonnull CraftType selectedType,
            @Nonnull Runnable backAction,
            @Nonnull RecipeSelectCallback callback) {
        List<CraftingRecipe> matches = findMatchingRecipes(inputs, selectedType);
        if (matches.isEmpty()) {
            player.sendMessage("§c没有找到匹配的配方");
            return;
        }
        open(player, 0, matches, backAction, callback);
    }

    private static void open(
            @Nonnull Player player,
            int page,
            @Nonnull List<CraftingRecipe> recipes,
            @Nonnull Runnable backAction,
            @Nonnull RecipeSelectCallback callback) {
        int maxPage = Math.max(0, (recipes.size() - 1) / ITEM_SLOTS.length);
        int safePage = Math.min(Math.max(page, 0), maxPage);

        ChestMenu menu = new ChestMenu("样板合成 - 选择配方");
        menu.setSize(SIZE);

        for (int slot : BORDER_SLOTS) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        menu.addItem(BACK_SLOT, new AdvancedCustomItemStack(Material.BARRIER, "&c&l返回"), (p, s, i, a) -> {
            backAction.run();
            return false;
        });

        if (safePage > 0) {
            menu.addItem(
                    PREV_SLOT, new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c上一页"), (p, s, i, a) -> {
                        open(p, safePage - 1, recipes, backAction, callback);
                        return false;
                    });
        } else {
            menu.addItem(PREV_SLOT, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage < maxPage) {
            menu.addItem(
                    NEXT_SLOT, new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a下一页"), (p, s, i, a) -> {
                        open(p, safePage + 1, recipes, backAction, callback);
                        return false;
                    });
        } else {
            menu.addItem(NEXT_SLOT, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        int start = safePage * ITEM_SLOTS.length;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int slot = ITEM_SLOTS[i];
            int index = start + i;
            if (index < recipes.size()) {
                CraftingRecipe recipe = recipes.get(index);
                ItemStack icon = createRecipeIcon(recipe);
                menu.addItem(slot, icon, (p, s, item, a) -> {
                    p.closeInventory();
                    callback.onSelect(recipe);
                    return false;
                });
            } else {
                menu.addItem(slot, null, ChestMenuUtils.getEmptyClickHandler());
            }
        }

        menu.open(player);
    }

    @Nonnull
    static List<CraftingRecipe> findMatchingRecipes(@Nonnull ItemStack[] inputs, @Nonnull CraftType selectedType) {
        List<CraftingRecipe> matches = new ArrayList<>();
        ItemStack[] trimmed = ItemUtils.trimItems(inputs);
        if (trimmed.length == 0) return matches;

        if (selectedType.isVanilla()) {
            findVanillaMatches(inputs, trimmed, selectedType, matches);
        } else {
            findSlimefunMatches(inputs, selectedType, matches);
        }

        return matches;
    }

    private static void findVanillaMatches(
            @Nonnull ItemStack[] inputs,
            @Nonnull ItemStack[] trimmed,
            @Nonnull CraftType type,
            @Nonnull List<CraftingRecipe> matches) {
        boolean hasSlimefunItem = Arrays.stream(trimmed).anyMatch(x -> SlimefunItem.getByItem(x) != null);
        if (hasSlimefunItem) return;

        if (type == CraftType.VANILLA_CRAFTING_TABLE) {
            CraftingRecipe recipe = VanillaRecipeUtils.getCraftingTableRecipe(inputs);
            if (recipe != null) matches.add(recipe);
        } else {
            for (ItemStack input : trimmed) {
                matches.addAll(VanillaRecipeUtils.getRecipesForType(input, type));
            }
        }
    }

    private static void findSlimefunMatches(
            @Nonnull ItemStack[] inputs, @Nonnull CraftType selectedType, @Nonnull List<CraftingRecipe> matches) {
        RecipeType recipeType = CraftTypeRegistry.getRecipeType(selectedType);
        if (recipeType == null) return;

        List<ItemStack[]> allInputs = RecipeUtils.getInputs(recipeType);
        for (ItemStack[] recipeInput : allInputs) {
            if (matchesInput(inputs, recipeInput)) {
                ItemStack[] output = RecipeUtils.getOutputs(recipeType, recipeInput);
                if (output.length > 0) {
                    matches.add(new CraftingRecipe(selectedType, recipeInput, output));
                }
            }
        }
    }

    private static boolean matchesInput(@Nonnull ItemStack[] playerInput, @Nonnull ItemStack[] recipeInput) {
        for (int i = 0; i < Math.max(playerInput.length, recipeInput.length); i++) {
            ItemStack player = i < playerInput.length ? playerInput[i] : null;
            ItemStack recipe = i < recipeInput.length ? recipeInput[i] : null;

            boolean playerEmpty = player == null || player.getType().isAir();
            boolean recipeEmpty = recipe == null || recipe.getType().isAir();

            if (playerEmpty && recipeEmpty) continue;
            if (playerEmpty || recipeEmpty) return false;
            if (!SlimefunUtils.isItemSimilar(player, recipe, true, true)) return false;
        }
        return true;
    }

    @Nonnull
    private static ItemStack createRecipeIcon(@Nonnull CraftingRecipe recipe) {
        ItemStack[] outputs = recipe.getOutput();
        if (outputs.length == 0) {
            return new AdvancedCustomItemStack(Material.BARRIER, "&c未知产物");
        }

        ItemStack display = outputs[0].clone();
        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&a输入:");
        for (ItemStack input : recipe.getInput()) {
            if (input == null || input.getType().isAir()) continue;
            extraLore.add("  &e- &f" + ItemUtils.getItemName(input) + " &7x" + input.getAmount());
        }
        extraLore.add("&e输出:");
        for (ItemStack output : outputs) {
            if (output == null || output.getType().isAir()) continue;
            extraLore.add("  &e- &f" + ItemUtils.getItemName(output) + " &7x" + output.getAmount());
        }
        extraLore.add("");
        extraLore.add("&7> 单击编码样板");

        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.addAll(CMIChatColor.translate(extraLore));
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    @FunctionalInterface
    public interface RecipeSelectCallback {
        void onSelect(@Nonnull CraftingRecipe recipe);
    }
}
