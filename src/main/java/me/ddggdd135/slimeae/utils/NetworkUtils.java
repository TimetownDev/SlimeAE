package me.ddggdd135.slimeae.utils;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class NetworkUtils {
    public static void scanDirectly(@Nonnull Block block, @Nonnull Set<Location> blocks) {
        BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (blocks.contains(testLocation)) continue;
            SlimefunBlockData blockData = controller.getBlockData(testLocation);
            if (blockData != null
                    && SlimefunItem.getById(blockData.getSfId()) instanceof IMEObject
                    && !testLocation.getBlock().getType().isAir()) {
                blocks.add(testLocation);
                scanDirectly(testLocation.getBlock(), blocks);
            }
        }
    }

    @Nonnull
    public static Set<Location> scanDirectly(@Nonnull Block block) {
        Set<Location> result = new HashSet<>();
        scanDirectly(block, result);
        return result;
    }

    @Nonnull
    public static Set<Location> scan(Location location) {
        Set<Location> result = new HashSet<>();
        scan(location, result);
        return result;
    }

    public static void scan(@Nonnull Location location, @Nonnull Set<Location> blocks) {
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = location.add(blockFace.getDirection());
            if (blocks.contains(testLocation)) continue;
            if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.contains(testLocation)) {
                blocks.add(testLocation);
                scanDirectly(testLocation.getBlock(), blocks);
            }
        }
    }
}
