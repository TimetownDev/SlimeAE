package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeUtils {
    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem != null) {
            if (slimefunItem.getRecipeType() == RecipeType.ENHANCED_CRAFTING_TABLE) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE, slimefunItem.getRecipe(), slimefunItem.getRecipeOutput());
            } else {
                return new CraftingRecipe(CraftType.COOKING, slimefunItem.getRecipe(), slimefunItem.getRecipeOutput());
            }
        }
        List<Recipe> minecraftRecipe = Bukkit.getRecipesFor(itemStack);
        for (Recipe recipe : minecraftRecipe) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        shapedRecipe.getIngredientMap().values().stream()
                                .filter(Objects::nonNull)
                                .map(x -> new ItemStack(x.getType(), x.getAmount()))
                                .toArray(ItemStack[]::new),
                        new ItemStack(
                                shapedRecipe.getResult().getType(),
                                shapedRecipe.getResult().getAmount()));
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        Arrays.stream(shapelessRecipe.getIngredientList().toArray(new ItemStack[0]))
                                .filter(Objects::nonNull)
                                .map(x -> new ItemStack(x.getType(), x.getAmount()))
                                .toArray(ItemStack[]::new),
                        new ItemStack(
                                shapelessRecipe.getResult().getType(),
                                shapelessRecipe.getResult().getAmount()));
            }
            if (recipe instanceof CookingRecipe cookingRecipe)
                return new CraftingRecipe(
                        CraftType.COOKING,
                        new ItemStack[] {
                            new ItemStack(
                                    cookingRecipe.getInput().getType(),
                                    cookingRecipe.getInput().getAmount())
                        },
                        new ItemStack(
                                cookingRecipe.getResult().getType(),
                                cookingRecipe.getResult().getAmount()));
        }
        return null;
    }
}
