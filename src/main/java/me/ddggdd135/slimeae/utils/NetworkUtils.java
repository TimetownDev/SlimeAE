package me.ddggdd135.slimeae.utils;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class NetworkUtils {
    public static void scan(Block block, Set<Location> blocks) {
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (blocks.contains(testLocation)) continue;
            if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.containsKey(testLocation)) {
                blocks.add(testLocation);
                scan(testLocation.getBlock(), blocks);
            } else {
                SlimefunBlockData blockData = StorageCacheUtils.getBlock(testLocation);
                if (blockData == null) {
                    continue;
                }
                SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
                if (slimefunItem instanceof IMEObject IMEObject) {
                    blocks.add(testLocation);
                    SlimeAEPlugin.getNetworkData().AllNetworkBlocks.put(testLocation, IMEObject);

                    if (slimefunItem instanceof IMEController IMEController) {
                        SlimeAEPlugin.getNetworkData().AllControllers.put(testLocation, IMEController);
                    }

                    if (slimefunItem instanceof IMEStorageObject IMEStorageObject) {
                        SlimeAEPlugin.getNetworkData().AllStorageObjects.put(testLocation, IMEStorageObject);
                    }

                    if (slimefunItem instanceof IMECraftHolder IMECraftHolder) {
                        SlimeAEPlugin.getNetworkData().AllCraftHolders.put(testLocation, IMECraftHolder);
                    }

                    scan(testLocation.getBlock(), blocks);
                }
            }
        }
    }

    public static Set<Location> scan(Block block) {
        Set<Location> result = new HashSet<>();
        scan(block, result);
        return result;
    }
}
