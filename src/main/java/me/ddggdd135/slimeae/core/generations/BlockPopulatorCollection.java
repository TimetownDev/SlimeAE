package me.ddggdd135.slimeae.core.generations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

public class BlockPopulatorCollection extends BlockPopulator {
    private final List<BlockPopulator> populators;

    public BlockPopulatorCollection(BlockPopulator... populators) {
        this.populators = new ArrayList<>();
        this.populators.addAll(List.of(populators));
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk source) {
        for (BlockPopulator populator : populators) populator.populate(world, random, source);
    }

    public void addPopulator(BlockPopulator populator) {
        populators.add(populator);
    }

    public boolean removePopulator(BlockPopulator populator) {
        return populators.remove(populator);
    }
}
