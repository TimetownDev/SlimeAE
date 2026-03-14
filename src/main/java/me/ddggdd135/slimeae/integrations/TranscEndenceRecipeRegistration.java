package me.ddggdd135.slimeae.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.sfiguz7.transcendence.lists.TEItems;
import me.sfiguz7.transcendence.lists.TERecipeType;

public final class TranscEndenceRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.NANOBOT_CRAFTER,
                TERecipeType.NANOBOT_CRAFTER,
                SlimefunItem.getByItem(TEItems.NANOBOT_CRAFTER));
    }
}
