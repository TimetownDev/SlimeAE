package me.ddggdd135.slimeae.integrations;

import io.github.thebusybiscuit.exoticgarden.ExoticGardenRecipeTypes;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;

public final class ExoticGardenRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(CraftType.KITCHEN, ExoticGardenRecipeTypes.KITCHEN, SlimefunItem.getById("KITCHEN"));
    }
}
