package me.ddggdd135.slimeae.core.generations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

public class SlimefunBlockPopulator extends BlockPopulator {
    private static boolean enableOreGen;

    public static void reloadConfig() {
        enableOreGen = SlimeAEPlugin.getInstance().getConfig().getBoolean("world-gen.enable-ore-gen", true);
    }

    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk source) {
        // 使用缓存的配置值
        if (!enableOreGen) {
            return;
        }

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
