package me.ddggdd135.slimeae.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.matl114.logitech.core.Machines.WorkBenchs.BugCrafter;

public final class LogiTechRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(CraftType.BUG_CRAFTER, BugCrafter.TYPE, SlimefunItem.getById("BUG_CRAFTER"));
    }
}
