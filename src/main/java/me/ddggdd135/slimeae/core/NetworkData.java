package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = new HashSet<>();

    public NetworkInfo getNetworkInfo(Location location) {
        for (NetworkInfo info : AllNetworkData) {
            if (info.getChildren().contains(location)) return info;
        }
        return null;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        BlockDataController blockDataController = Slimefun.getDatabaseManager().getBlockDataController();
        SlimefunBlockData controllerBlockData = blockDataController.getBlockData(controller);
        if (!(controllerBlockData != null && SlimefunItem.getById(controllerBlockData.getSfId()) instanceof IMEController<?>))
            return null;
        NetworkInfo info = getNetworkInfo(controller);
        Set<Location> children = NetworkUtils.scan(controller.getBlock());
        for (Location location : children) {
            SlimefunBlockData blockData = blockDataController.getBlockData(location);
            if (blockData != null && SlimefunItem.getById(blockData.getSfId()) instanceof IMEController<?> && !location.equals(controller)) {
                if (info != null) {
                    info.dispose();
                }
                return null;
            }
        }
        if (info != null) {
            info.getChildren().clear();
            info.getChildren().addAll(children);
        } else {
            info = new NetworkInfo(controller, children);
            AllNetworkData.add(info);
        }
        return info;
    }
}
