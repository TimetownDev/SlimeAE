package me.ddggdd135.slimeae.integrations;

import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.items.AssemblyTable;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;

public final class GalactifunRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.ASSEMBLY_TABLE, AssemblyTable.TYPE, SlimefunItem.getByItem(BaseItems.ASSEMBLY_TABLE));
    }
}
