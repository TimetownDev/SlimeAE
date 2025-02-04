package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import me.sfiguz7.transcendence.lists.TERecipeType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeUtils {
    public static final Map<RecipeType, MultiBlockMachine> SUPPORTED_RECIPE_TYPES = new HashMap<>();

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem != null) {
            if (SUPPORTED_RECIPE_TYPES.containsKey(slimefunItem.getRecipeType())) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE, slimefunItem.getRecipe(), slimefunItem.getRecipeOutput());
            } else {
                for (Map.Entry<RecipeType, MultiBlockMachine> entry : SUPPORTED_RECIPE_TYPES.entrySet()) {
                    if (entry.getValue() == null) continue;
                    for (ItemStack[] input : RecipeType.getRecipeInputList(entry.getValue())) {
                        ItemStack output = RecipeType.getRecipeOutputList(entry.getValue(), input);
                        if (SlimefunUtils.isItemSimilar(itemStack, output, true, false)) {
                            return new CraftingRecipe(CraftType.CRAFTING_TABLE, input, output);
                        }
                    }
                }
            }

            return new CraftingRecipe(CraftType.COOKING, slimefunItem.getRecipe(), slimefunItem.getRecipeOutput());
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

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack[] input) {
        for (Map.Entry<RecipeType, MultiBlockMachine> entry : SUPPORTED_RECIPE_TYPES.entrySet()) {
            if (entry.getValue() == null) continue;
            in:
            for (ItemStack[] input1 : RecipeType.getRecipeInputList(entry.getValue())) {
                for (int i = 0; i < Math.max(input.length, input1.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (input.length > i) {
                        x = input[i];
                    }
                    if (input1.length > i) {
                        y = input1[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue in;
                    }
                }

                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE, input1, RecipeType.getRecipeOutputList(entry.getValue(), input1));
            }
        }

        Recipe minecraftRecipe =
                Bukkit.getCraftingRecipe(input, Bukkit.getWorlds().get(0));
        if (minecraftRecipe instanceof ShapedRecipe shapedRecipe) {
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
        if (minecraftRecipe instanceof ShapelessRecipe shapelessRecipe) {
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
        if (minecraftRecipe instanceof CookingRecipe cookingRecipe)
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

        return null;
    }

    static {
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ENHANCED_CRAFTING_TABLE, (MultiBlockMachine)
                SlimefunItem.getByItem(SlimefunItems.ENHANCED_CRAFTING_TABLE));
        SUPPORTED_RECIPE_TYPES.put(SlimefunAERecipeTypes.CHARGER, null);
        SUPPORTED_RECIPE_TYPES.put(SlimefunAERecipeTypes.INSCRIBER, null);
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.MAGIC_WORKBENCH, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.MAGIC_WORKBENCH));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ARMOR_FORGE, null);
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.SMELTERY, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.SMELTERY));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ANCIENT_ALTAR, null);
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.COMPRESSOR, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.COMPRESSOR));
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.GRIND_STONE, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.GRIND_STONE));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.JUICER, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.JUICER));
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.ORE_CRUSHER, (MultiBlockMachine) SlimefunItem.getByItem(SlimefunItems.ORE_CRUSHER));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.PRESSURE_CHAMBER, (MultiBlockMachine)
                SlimefunItem.getByItem(SlimefunItems.PRESSURE_CHAMBER));

        if (SlimeAEPlugin.getTranscEndenceIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(TERecipeType.NANOBOT_CRAFTER, null);
        }
    }
}
