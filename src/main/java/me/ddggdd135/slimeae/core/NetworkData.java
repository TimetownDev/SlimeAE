package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;

import javax.annotation.Nullable;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = new HashSet<>();
    public final Set<Location> AllNetworkBlocks = new HashSet<>();
    public final Set<Location> AllControllers = new HashSet<>();
    public final Set<Location> AllStorageObjects = new HashSet<>();
    public final Set<Location> AllCraftHolders = new HashSet<>();
    public final Set<Location> BannedScanSet = new HashSet<>();

    @Nullable
    public NetworkInfo getNetworkInfo(Location location) {
        for (NetworkInfo info : AllNetworkData) {
            if (info.getChildren().contains(location)) return info;
        }
        return null;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        if (!AllControllers.contains(controller)) return null;
        NetworkInfo info = getNetworkInfo(controller);
        if (info == null) {
            info = new NetworkInfo(controller);
            AllNetworkData.add(info);
        }
        if (info.getChildren().size() == 1 || info.getChildren().isEmpty()) {
            Set<Location> children = NetworkUtils.scan(controller.getBlock());
            for (Location location : children) {
                if (AllControllers.contains(location)
                        && !location.equals(controller)) {
                    info.dispose();
                    return null;
                }
            }

            info.getChildren().clear();
            info.getChildren().addAll(children);
        }

        StorageCollection networkStorage = new StorageCollection();
        for (Location location : info.getChildren()) {
            if (!AllStorageObjects.contains(location)) continue;
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMEStorageObject IMEStorageObject) {
                IStorage storage = IMEStorageObject.getStorage(location.getBlock());
                if (storage != null) networkStorage.addStorage(storage);
            }
        }
        info.setStorage(networkStorage);

        info.getCraftingHolders().clear();
        for (Location location : info.getChildren()) {
            if (!AllCraftHolders.contains(location)) continue;
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftHolder IMECraftHolder) {
                info.getCraftingHolders().add(location);
                info.getRecipeMap()
                        .put(location, new HashSet<>(List.of(IMECraftHolder.getSupportedRecipes(location.getBlock()))));
            }
        }


        return info;
    }
}
