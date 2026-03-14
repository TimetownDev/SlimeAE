package me.ddggdd135.slimeae.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.setup.FinalTechRecipeTypes;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;

public final class FinalTechRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.MATRIX_CRAFTING_TABLE,
                FinalTechRecipeTypes.MATRIX_CRAFTING_TABLE,
                SlimefunItem.getByItem(FinalTechItemStacks.MATRIX_CRAFTING_TABLE));
    }

    public static void registerBedrockCraftTable() {
        RecipeUtils.registerType(
                CraftType.BEDROCK_CRAFT_TABLE,
                FinalTechRecipeTypes.BEDROCK_CRAFT_TABLE,
                SlimefunItem.getByItem(FinalTechItemStacks.BEDROCK_CRAFT_TABLE));
    }
}
