package me.ddggdd135.slimeae.core.generations;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import javax.annotation.Nonnull;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;

/**
 * 矿物生成器类,用于在世界中生成自定义矿物
 */
public class OreGenerator extends BlockPopulator {
    private final ItemStack item;
    private final int size;
    private final int height;

    /**
     * 构造一个新的矿物生成器
     *
     * @param itemStack 要生成的矿物物品
     * @param size 矿脉大小
     * @param height 生成的最大高度
     */
    public OreGenerator(ItemStack itemStack, int size, int height) {
        item = itemStack;
        this.size = size;
        this.height = height;
    }

    /**
     * 在指定区块中生成矿物
     *
     * @param world 目标世界
     * @param random 随机数生成器
     * @param source 目标区块
     */
    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk source) {
        int chunkX = source.getX();
        int chunkZ = source.getZ();
        for (int i = 0; i < 5; i++) { // 生成5个矿脉
            int centerX = (chunkX << 4) + random.nextInt(16);
            int centerY = random.nextInt(height);
            int centerZ = (chunkZ << 4) + random.nextInt(16);

            generateVein(world, random, centerX, centerY, centerZ, source);
        }
    }

    private void generateVein(World world, Random random, int centerX, int centerY, int centerZ, Chunk chunk) {
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
            int chunkX = chunk.getX() << 4; // 将Chunk的坐标左移4位，相当于乘以16
            int chunkZ = chunk.getZ() << 4; // 将Chunk的坐标左移4位，相当于乘以16
            Location location = new Location(world, x, y, z);
            if (location.getBlockX() >= chunkX
                    && location.getBlockX() < chunkX + 16
                    && location.getBlockZ() >= chunkZ
                    && location.getBlockZ() < chunkZ + 16) {
                // Location在指定的Chunk里
                generateBlock(world, x, y, z);
            }
        }
    }

    private void generateBlock(World world, int x, int y, int z) {
        if (world.getType(x, y, z) != Material.STONE) return;
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        if (slimefunItem != null) {
            BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
            controller.createBlock(new Location(world, x, y, z), slimefunItem.getId());
        }

        world.setType(x, y, z, item.getType());
    }
}
