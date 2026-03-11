package me.ddggdd135.slimeae.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.lucasgithuber.obsidianexpansion.Items;
import me.lucasgithuber.obsidianexpansion.machines.ObsidianForge;

public final class ObsidianExpansionRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.OBSIDIAN_FORGE, ObsidianForge.TYPE, SlimefunItem.getByItem(Items.OBSIDIAN_FORGE));
    }
}
