package me.ddggdd135.slimeae.core.generations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public class SlimefunBlockPopulator extends BlockPopulator {
    @Override
    public void populate(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull LimitedRegion limitedRegion) {
        BlockPopulatorCollection populatorCollection = new BlockPopulatorCollection();
        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (slimefunItem.getRecipeType() == SlimefunAERecipeTypes.WORLD_GENERATING) {
                populatorCollection.addPopulator(new OreGenerator(slimefunItem.getItem(), 8, 64));
            }
        }
        populatorCollection.populate(worldInfo, random, chunkX, chunkZ, limitedRegion);
    }
}
