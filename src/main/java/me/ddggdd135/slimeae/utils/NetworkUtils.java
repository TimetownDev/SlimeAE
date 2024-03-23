package me.ddggdd135.slimeae.utils;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class NetworkUtils {
    public static void scan(Block block, Set<Location> blocks) {
        BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (blocks.contains(testLocation)) continue;
            SlimefunBlockData blockData = controller.getBlockData(testLocation);
            if (blockData != null
                    && SlimefunItem.getById(blockData.getSfId()) instanceof IMEObject<?>
                    && !testLocation.getBlock().getType().isAir()) {
                blocks.add(testLocation);
                scan(testLocation.getBlock(), blocks);
            }
        }
    }

    public static Set<Location> scan(Block block) {
        Set<Location> result = new HashSet<>();
        scan(block, result);
        return result;
    }
}
