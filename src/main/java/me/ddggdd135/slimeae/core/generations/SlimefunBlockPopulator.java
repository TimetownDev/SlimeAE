package me.ddggdd135.slimeae.core.generations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

public class SlimefunBlockPopulator extends BlockPopulator {
    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk source) {
        BlockPopulatorCollection populatorCollection = new BlockPopulatorCollection();
        try {
            for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems()) {
                if (slimefunItem.getRecipeType() == SlimefunAERecipeTypes.WORLD_GENERATING) {
                    populatorCollection.addPopulator(new OreGenerator(slimefunItem.getItem(), 8, 64));
                }
            }
            populatorCollection.populate(world, random, source);
        } catch (Exception ignored) {
        }
    }
}
