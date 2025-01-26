package me.ddggdd135.slimeae.utils;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;

import java.util.HashSet;
import java.util.Set;

import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class NetworkUtils {
    public static void scan(Block block, Set<Location> blocks) {
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (blocks.contains(testLocation)) continue;
            if (SlimeAEPlugin.getNetworkData().BannedScanSet.contains(testLocation)) return;
            if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.contains(testLocation)
                    && !testLocation.getBlock().getType().isAir()) {
                blocks.add(testLocation);
                scan(testLocation.getBlock(), blocks);
            } else {
                SlimefunBlockData blockData = StorageCacheUtils.getBlock(testLocation);
                if (blockData == null) continue;
                SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
                if (slimefunItem instanceof IMEObject) {
                    blocks.add(testLocation);
                    SlimeAEPlugin.getNetworkData().AllNetworkBlocks.add(testLocation);

                    if (slimefunItem instanceof IMEController) {
                        SlimeAEPlugin.getNetworkData().AllControllers.add(testLocation);
                    }

                    if (slimefunItem instanceof IMEStorageObject) {
                        SlimeAEPlugin.getNetworkData().AllStorageObjects.add(testLocation);
                    }

                    if (slimefunItem instanceof IMECraftHolder) {
                        SlimeAEPlugin.getNetworkData().AllCraftHolders.add(testLocation);
                    }

                    scan(testLocation.getBlock(), blocks);
                } else {
                    SlimeAEPlugin.getNetworkData().BannedScanSet.add(testLocation);
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
