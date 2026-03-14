package me.ddggdd135.slimeae.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import org.bukkit.Bukkit;
import org.bukkit.inventory.*;

public final class VanillaRecipeUtils {
    private VanillaRecipeUtils() {}

    @Nullable public static CraftingRecipe getCraftingTableRecipe(@Nonnull ItemStack[] input) {
        ItemStack[] padded = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            if (i < input.length && input[i] != null) {
                padded[i] = input[i];
            }
        }
        Recipe recipe = Bukkit.getCraftingRecipe(padded, Bukkit.getWorlds().get(0));
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            RecipeChoice[] perSlotChoices = expandShapedChoices(shapedRecipe);
            List<RecipeChoice> choiceList = new ArrayList<>();
            for (RecipeChoice c : perSlotChoices) {
                if (c != null) choiceList.add(c);
            }
            return new CraftingRecipe(
                    CraftType.VANILLA_CRAFTING_TABLE,
                    RecipeUtils.getRecipeInputs(choiceList, padded),
                    new ItemStack(
                            shapedRecipe.getResult().getType(),
                            shapedRecipe.getResult().getAmount()));
        }
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            ItemStack[] trimmed = ItemUtils.trimItems(input);
            ItemStack[] normalized = new ItemStack[trimmed.length];
            for (int i = 0; i < trimmed.length; i++) {
                normalized[i] = trimmed[i].asOne();
            }
            return new CraftingRecipe(
                    CraftType.VANILLA_CRAFTING_TABLE,
                    normalized,
                    new ItemStack(
                            shapelessRecipe.getResult().getType(),
                            shapelessRecipe.getResult().getAmount()));
        }
        return null;
    }

    @Nonnull
    private static RecipeChoice[] expandShapedChoices(@Nonnull ShapedRecipe shapedRecipe) {
        RecipeChoice[] result = new RecipeChoice[9];
        String[] shape = shapedRecipe.getShape();
        Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();
        for (int row = 0; row < shape.length; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c != ' ' && choiceMap.containsKey(c)) {
                    result[row * 3 + col] = choiceMap.get(c);
                }
            }
        }
        return result;
    }

    @Nonnull
    public static List<CraftingRecipe> getFurnaceRecipes(@Nonnull ItemStack input) {
        List<CraftingRecipe> results = new ArrayList<>();
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                if (!furnaceRecipe.getInputChoice().test(input)) continue;
                results.add(new CraftingRecipe(
                        CraftType.VANILLA_FURNACE,
                        new ItemStack[] {new ItemStack(input.getType(), 1)},
                        new ItemStack(
                                furnaceRecipe.getResult().getType(),
                                furnaceRecipe.getResult().getAmount())));
            }
        }
        return results;
    }

    @Nonnull
    public static List<CraftingRecipe> getBlastingRecipes(@Nonnull ItemStack input) {
        List<CraftingRecipe> results = new ArrayList<>();
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof BlastingRecipe blastingRecipe) {
                if (!blastingRecipe.getInputChoice().test(input)) continue;
                results.add(new CraftingRecipe(
                        CraftType.VANILLA_BLAST_FURNACE,
                        new ItemStack[] {new ItemStack(input.getType(), 1)},
                        new ItemStack(
                                blastingRecipe.getResult().getType(),
                                blastingRecipe.getResult().getAmount())));
            }
        }
        return results;
    }

    @Nonnull
    public static List<CraftingRecipe> getSmokingRecipes(@Nonnull ItemStack input) {
        List<CraftingRecipe> results = new ArrayList<>();
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof SmokingRecipe smokingRecipe) {
                if (!smokingRecipe.getInputChoice().test(input)) continue;
                results.add(new CraftingRecipe(
                        CraftType.VANILLA_SMOKER,
                        new ItemStack[] {new ItemStack(input.getType(), 1)},
                        new ItemStack(
                                smokingRecipe.getResult().getType(),
                                smokingRecipe.getResult().getAmount())));
            }
        }
        return results;
    }

    @Nonnull
    public static List<CraftingRecipe> getStonecutterRecipes(@Nonnull ItemStack input) {
        List<CraftingRecipe> results = new ArrayList<>();
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof StonecuttingRecipe scr) {
                if (scr.getInputChoice().test(input)) {
                    results.add(new CraftingRecipe(
                            CraftType.VANILLA_STONECUTTER,
                            new ItemStack[] {new ItemStack(input.getType(), 1)},
                            new ItemStack(
                                    scr.getResult().getType(), scr.getResult().getAmount())));
                }
            }
        }
        return results;
    }

    @Nonnull
    public static List<CraftingRecipe> getRecipesForType(@Nonnull ItemStack input, @Nonnull CraftType type) {
        return switch (type) {
            case VANILLA_FURNACE -> getFurnaceRecipes(input);
            case VANILLA_BLAST_FURNACE -> getBlastingRecipes(input);
            case VANILLA_SMOKER -> getSmokingRecipes(input);
            case VANILLA_STONECUTTER -> getStonecutterRecipes(input);
            default -> List.of();
        };
    }

    @Nullable public static CraftingRecipe findRecipeByOutput(
            @Nonnull ItemStack input, @Nonnull ItemStack output, @Nonnull CraftType type) {
        List<CraftingRecipe> recipes = getRecipesForType(input, type);
        for (CraftingRecipe recipe : recipes) {
            ItemStack[] outputs = recipe.getOutput();
            if (outputs.length > 0 && outputs[0].getType() == output.getType()) {
                return recipe;
            }
        }
        return null;
    }
}
