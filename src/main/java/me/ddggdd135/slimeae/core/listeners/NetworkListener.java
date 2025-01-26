package me.ddggdd135.slimeae.core.listeners;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
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
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

public class NetworkListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSlimefunBlockPlace(SlimefunBlockPlaceEvent e) {
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().AllControllers.remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().AllStorageObjects.remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(e.getBlockPlaced().getLocation());
        SlimeAEPlugin.getNetworkData().BannedScanSet.remove(e.getBlockPlaced().getLocation());
        if (e.getSlimefunItem() instanceof IMEObject) {
            SlimeAEPlugin.getNetworkData().AllNetworkBlocks.add(e.getBlockPlaced().getLocation());
        }

        if (e.getSlimefunItem() instanceof IMEController) {
            SlimeAEPlugin.getNetworkData().AllControllers.add(e.getBlockPlaced().getLocation());
        }

        if (e.getSlimefunItem() instanceof IMEStorageObject) {
            SlimeAEPlugin.getNetworkData().AllStorageObjects.add(e.getBlockPlaced().getLocation());
        }

        if (e.getSlimefunItem() instanceof IMECraftHolder) {
            SlimeAEPlugin.getNetworkData().AllCraftHolders.add(e.getBlockPlaced().getLocation());
        }
        Block block = e.getBlockPlaced();
        Set<NetworkInfo> networkInfos = new HashSet<>();
        for (BlockFace blockFace : Valid_Faces) {
            Location testLocation = block.getLocation().add(blockFace.getDirection());
            if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.contains(testLocation)) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(testLocation);
                if (info != null)
                    networkInfos.add(info);
            }
        }

        if (networkInfos.size() != 1) {
            for (NetworkInfo info : networkInfos) {
                info.dispose();
            }
        } else {
            NetworkInfo info = networkInfos.toArray(NetworkInfo[]::new)[0];
            info.getChildren().clear();
            SlimeAEPlugin.getNetworkData().refreshNetwork(info.getController());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllControllers.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllStorageObjects.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(e.getBlock().getLocation());
        SlimeAEPlugin.getNetworkData().BannedScanSet.remove(e.getBlock().getLocation());
    }
}
