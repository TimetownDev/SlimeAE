package me.ddggdd135.slimeae.core.generations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public class BlockPopulatorCollection extends BlockPopulator {
    private final List<BlockPopulator> populators;

    public BlockPopulatorCollection(BlockPopulator... populators) {
        this.populators = new ArrayList<>();
        this.populators.addAll(List.of(populators));
    }

    @Override
    public void populate(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull LimitedRegion limitedRegion) {
        for (BlockPopulator populator : populators)
            populator.populate(worldInfo, random, chunkX, chunkZ, limitedRegion);
    }

    public void addPopulator(BlockPopulator populator) {
        populators.add(populator);
    }

    public boolean removePopulator(BlockPopulator populator) {
        return populators.remove(populator);
    }
}
