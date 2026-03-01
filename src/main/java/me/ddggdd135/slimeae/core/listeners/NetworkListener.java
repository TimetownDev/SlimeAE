package me.ddggdd135.slimeae.core.listeners;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class NetworkListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSlimefunBlockPlace(SlimefunBlockPlaceEvent e) {
        SlimeAEPlugin.getNetworkData()
                .AllNetworkBlocks
                .remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().AllControllers.remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData()
                .AllStorageObjects
                .remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(e.getBlockPlaced().getLocation());
        if (e.getSlimefunItem() instanceof IMEObject IMEObject) {
            SlimeAEPlugin.getNetworkData()
                    .AllNetworkBlocks
                    .put(e.getBlockPlaced().getLocation(), IMEObject);
        }

        if (e.getSlimefunItem() instanceof IMEController IMEController) {
            SlimeAEPlugin.getNetworkData().AllControllers.put(e.getBlockPlaced().getLocation(), IMEController);
        }

        if (e.getSlimefunItem() instanceof IMEStorageObject IMEStorageObject) {
            SlimeAEPlugin.getNetworkData()
                    .AllStorageObjects
                    .put(e.getBlockPlaced().getLocation(), IMEStorageObject);
        }

        if (e.getSlimefunItem() instanceof IMECraftHolder IMECraftHolder) {
            SlimeAEPlugin.getNetworkData()
                    .AllCraftHolders
                    .put(e.getBlockPlaced().getLocation(), IMECraftHolder);
        }
        Block block = e.getBlockPlaced();
        Set<NetworkInfo> networkInfos = new HashSet<>();
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.containsKey(testLocation)) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(testLocation);
                if (info != null) networkInfos.add(info);
            }
        }

        if (networkInfos.size() != 1) {
            for (NetworkInfo info : networkInfos) {
                info.dispose();
            }
        } else {
            NetworkInfo info = networkInfos.iterator().next();
            Location placed = e.getBlockPlaced().getLocation();
            info.getChildren().add(placed);
            SlimeAEPlugin.getNetworkData().locationToNetwork.put(placed, info);
            info.setNeedsStorageUpdate(true);
            info.setNeedsRecipeUpdate(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllControllers.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllStorageObjects.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(e.getBlock().getLocation());

        Location broken = e.getBlock().getLocation();
        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(broken);
        if (networkInfo != null) {
            networkInfo.getChildren().remove(broken);
            SlimeAEPlugin.getNetworkData().locationToNetwork.remove(broken, networkInfo);
            if (broken.equals(networkInfo.getController())) {
                networkInfo.dispose();
            } else {
                int adjacentCount = 0;
                for (BlockFace blockFace : Valid_Faces) {
                    Location adj = broken.clone().add(blockFace.getDirection());
                    if (networkInfo.getChildren().contains(adj)) {
                        adjacentCount++;
                    }
                }
                if (adjacentCount >= 2) {
                    networkInfo.dispose();
                } else {
                    networkInfo.setNeedsStorageUpdate(true);
                    networkInfo.setNeedsRecipeUpdate(true);
                }
            }
        }
    }
}
