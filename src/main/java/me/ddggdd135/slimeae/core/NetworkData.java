package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.integrations.networks.NetworksStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = new HashSet<>();
    public final Map<Location, IMEObject> AllNetworkBlocks = new ConcurrentHashMap<>();
    public final Map<Location, IMEController> AllControllers = new ConcurrentHashMap<>();
    public final Map<Location, IMEStorageObject> AllStorageObjects = new ConcurrentHashMap<>();
    public final Map<Location, IMECraftHolder> AllCraftHolders = new ConcurrentHashMap<>();

    @Nullable public NetworkInfo getNetworkInfo(Location location) {
        NetworkInfo re = null;

        for (NetworkInfo info : AllNetworkData) {
            if (info.getChildren().contains(location)) return info;
            if (info.getController().equals(location)) return info;
        }

        return re;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        if (!AllControllers.containsKey(controller)) return null;
        NetworkInfo info = getNetworkInfo(controller);
        if (info == null) {
            info = new NetworkInfo(controller);
            AllNetworkData.add(info);
        }

        if (!updateChildren(info)) return null;
        if (!updateStorage(info)) return null;
        if (!updateAutoCraft(info)) return null;

        return info;
    }

    public boolean updateChildren(@Nonnull NetworkInfo info) {
        Location controller = info.getController();

        if (info.getChildren().size() == 1 || info.getChildren().isEmpty()) {
            Set<Location> children = NetworkUtils.scan(controller.getBlock());
            for (Location location : children) {
                if (AllControllers.containsKey(location) && !location.equals(controller)) {
                    info.dispose();
                    return false;
                }
            }

            info.getChildren().clear();
            info.getChildren().addAll(children);
        }

        info.getChildren().removeIf(x -> !AllNetworkBlocks.containsKey(x));

        return true;
    }

    public boolean updateStorage(@Nonnull NetworkInfo info) {
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

        return true;
    }

    public boolean updateAutoCraft(@Nonnull NetworkInfo info) {
        Set<Location> newCraftingHolders = new ConcurrentHashSet<>();
        Map<Location, Set<CraftingRecipe>> newRecipeMap = new ConcurrentHashMap<>();
        for (Location location : info.getChildren()) {
            if (!AllCraftHolders.containsKey(location)) continue;
            IMECraftHolder slimefunItem = AllCraftHolders.get(location);
            newCraftingHolders.add(location);
            newRecipeMap.put(location, new HashSet<>(List.of(slimefunItem.getSupportedRecipes(location.getBlock()))));
        }

        Map<CraftType, Integer> newSpeeds = new ConcurrentHashMap<>();
        for (Location location : info.getRecipeMap().keySet()) {
            IMECraftHolder holder =
                    SlimeAEPlugin.getNetworkData().AllCraftHolders.get(location);
            if (holder == null) continue;
            for (Block deviceBlock : holder.getCraftingDevices(location.getBlock())) {
                IMECraftDevice imeCraftDevice = (IMECraftDevice) SlimefunItem.getById(
                        StorageCacheUtils.getBlock(deviceBlock.getLocation()).getSfId());
                if (!(imeCraftDevice instanceof IMEVirtualCraftDevice device)) continue;
                CraftType craftType = device.getCraftingType();
                int speed = newSpeeds.getOrDefault(craftType, 0);
                speed += device.getSpeed(deviceBlock);
                newSpeeds.put(craftType, speed);
            }
        }

        info.getCraftingHolders().clear();
        info.getCraftingHolders().addAll(newCraftingHolders);
        info.getRecipeMap().clear();
        info.getRecipeMap().putAll(newRecipeMap);
        info.getVirtualCraftingDeviceSpeeds().clear();
        info.getVirtualCraftingDeviceSpeeds().putAll(newSpeeds);

        return true;
    }
}
