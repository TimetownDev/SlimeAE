package me.ddggdd135.slimeae.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.integrations.networks.NetworksStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = new HashSet<>();
    public final Map<Location, IMEObject> AllNetworkBlocks = new ConcurrentHashMap<>();
    public final Map<Location, IMEController> AllControllers = new ConcurrentHashMap<>();
    public final Map<Location, IMEStorageObject> AllStorageObjects = new ConcurrentHashMap<>();
    public final Map<Location, IMECraftHolder> AllCraftHolders = new ConcurrentHashMap<>();

    @Nullable public NetworkInfo getNetworkInfo(Location location) {
        NetworkInfo re = null;
        Set<NetworkInfo> toDispose = new HashSet<>();
        for (NetworkInfo info : AllNetworkData) {
            if (info.getChildren().contains(location)) return info;
            if (info.getController().equals(location)) return info;
        }

        toDispose.forEach(NetworkInfo::dispose);
        return re;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        if (!AllControllers.containsKey(controller)) return null;
        NetworkInfo info = getNetworkInfo(controller);
        if (info == null) {
            info = new NetworkInfo(controller);
            AllNetworkData.add(info);
        }
        if (info.getChildren().size() == 1 || info.getChildren().isEmpty()) {
            Set<Location> children = NetworkUtils.scan(controller.getBlock());
            for (Location location : children) {
                if (AllControllers.containsKey(location) && !location.equals(controller)) {
                    info.dispose();
                    return null;
                }
            }

            info.getChildren().clear();
            info.getChildren().addAll(children);
        }

        info.getChildren().removeIf(x -> !AllNetworkBlocks.containsKey(x));

        StorageCollection storageNoNetworks = new StorageCollection();
        NetworksStorage networksStorage = null;
        for (Location location : info.getChildren()) {
            if (!AllStorageObjects.containsKey(location)) continue;
            IMEStorageObject slimefunItem = AllStorageObjects.get(location);
            IStorage storage = slimefunItem.getStorage(location.getBlock());
            if (storage instanceof NetworksStorage networksStorage1) {
                if (networksStorage != null) continue;

                networksStorage = networksStorage1;
                continue;
            }
            if (storage != null) storageNoNetworks.addStorage(storage);
        }
        storageNoNetworks.addStorage(info.getTempStorage());
        info.setStorageNoNetworks(storageNoNetworks);
        StorageCollection storageCollection = new StorageCollection(storageNoNetworks);
        storageCollection.addStorage(networksStorage);
        info.setStorage(storageCollection);

        Set<Location> newCraftingHolders = new ConcurrentHashSet<>();
        Map<Location, Set<CraftingRecipe>> newRecipeMap = new ConcurrentHashMap<>();
        for (Location location : info.getChildren()) {
            if (!AllCraftHolders.containsKey(location)) continue;
            IMECraftHolder slimefunItem = AllCraftHolders.get(location);
            newCraftingHolders.add(location);
            newRecipeMap.put(location, new HashSet<>(List.of(slimefunItem.getSupportedRecipes(location.getBlock()))));
        }

        info.getCraftingHolders().clear();
        info.getCraftingHolders().addAll(newCraftingHolders);
        info.getRecipeMap().clear();
        info.getRecipeMap().putAll(newRecipeMap);

        return info;
    }
}
