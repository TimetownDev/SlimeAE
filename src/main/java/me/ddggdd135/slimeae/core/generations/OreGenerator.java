package me.ddggdd135.slimeae.core.generations;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OreGenerator extends BlockPopulator {
    private final ItemStack item;
    private final int size;
    private final int height;

    public OreGenerator(ItemStack itemStack, int size, int height) {
        item = itemStack;
        this.size = size;
        this.height = height;
    }

    @Override
    public void populate(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull LimitedRegion limitedRegion) {
        for (int i = 0; i < 10; i++) { // 生成10个矿脉
            int centerX = (chunkX << 4) + random.nextInt(16);
            int centerY = random.nextInt(height);
            int centerZ = (chunkZ << 4) + random.nextInt(16);

            generateVein(limitedRegion, random, centerX, centerY, centerZ);
        }
    }

    private void generateVein(LimitedRegion limitedRegion, Random random, int centerX, int centerY, int centerZ) {
        int x = centerX;
        int y = centerY;
        int z = centerZ;
        int size = random.nextInt(3, this.size);
        for (int i = 0; i < size; i++) {
            int r = random.nextInt(0, 3);
            if (r == 0) {
                x++;
            } else if (r == 1) {
                y++;
            } else if (r == 2) {
                z++;
            }
            generateBlock(limitedRegion, x, y, z);
        }
    }

    private void generateBlock(LimitedRegion limitedRegion, int x, int y, int z) {
        if (!(limitedRegion.getType(new Location(limitedRegion.getWorld(), x, y, z)) == Material.STONE)) return;
        ;
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        if (slimefunItem != null) {
            BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
            controller.createBlock(new Location(limitedRegion.getWorld(), x, y, z), slimefunItem.getId());
        }
        limitedRegion.setType(x, y, z, item.getType());
    }
}
