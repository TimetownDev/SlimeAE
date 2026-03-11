package me.ddggdd135.slimeae.integrations;

import io.github.mooy1.infinityexpansion.items.blocks.Blocks;
import io.github.mooy1.infinityexpansion.items.blocks.InfinityWorkbench;
import io.github.mooy1.infinityexpansion.items.mobdata.MobData;
import io.github.mooy1.infinityexpansion.items.mobdata.MobDataInfuser;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;

public final class InfinityRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.INFINITY_WORKBENCH, InfinityWorkbench.TYPE, SlimefunItem.getByItem(Blocks.INFINITY_FORGE));
        RecipeUtils.registerType(
                CraftType.MOB_DATA_INFUSER, MobDataInfuser.TYPE, SlimefunItem.getByItem(MobData.INFUSER));
    }
}
