package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.*;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = new HashSet<>();
    public final Set<Location> AllNetworkBlocks = new HashSet<>();
    public final Set<Location> AllControllers = new HashSet<>();
    public final Map<Location, IMEStorageObject> AllNetworkStorageBlocks = new HashMap<>();
    public final Map<Location, IMECraftHolder> AllNetworkCraftHolders = new HashMap<>();

    public NetworkInfo getNetworkInfo(Location location) {
        for (NetworkInfo info : AllNetworkData) {
            if (info.getChildren().contains(location)) return info;
        }
        return null;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        BlockDataController blockDataController = Slimefun.getDatabaseManager().getBlockDataController();
        SlimefunBlockData controllerBlockData = blockDataController.getBlockData(controller);
        if (!(controllerBlockData != null
                && SlimefunItem.getById(controllerBlockData.getSfId()) instanceof IMEController)) return null;
        NetworkInfo info = getNetworkInfo(controller);
        if (!AllControllers.contains(controller)) {
            return scanAll(controller);
        }
        if (info == null) {
            return scanAll(controller);
        }
        info.getChildren().clear();
        info.getChildren().addAll(NetworkUtils.scan(controller));
        info.getRecipeMap().clear();
        Set<Location> craftingHolders = new HashSet<>();
        Map<Location, Set<CraftingRecipe>> recipeMap = new HashMap<>();
        StorageCollection storageCollection = new StorageCollection();
        for (Location location : info.getChildren()) {
            if (AllControllers.contains(location) && !location.equals(controller)) {
                info.dispose();
                return null;
            }
            if (AllNetworkStorageBlocks.containsKey(location)) {
                IMEStorageObject IMEStorageObject = AllNetworkStorageBlocks.get(location);
                IStorage storage = IMEStorageObject.getStorage(location.getBlock());
                if (storage != null) storageCollection.addStorage(storage);
            }
            if (AllNetworkCraftHolders.containsKey(location)) {
                craftingHolders.add(location);
                recipeMap.put(
                        location,
                        new HashSet<>(List.of(
                                AllNetworkCraftHolders.get(location).getSupportedRecipes(location.getBlock()))));
            }
        }

        info.setStorage(storageCollection);
        info.getCraftingHolders().addAll(craftingHolders);
        for (Location key : recipeMap.keySet()) {
            info.getRecipeMap().put(key, recipeMap.get(key));
        }

        return info;
    }

    @Nullable public NetworkInfo scanAll(Location controller) {
        NetworkInfo info = getNetworkInfo(controller);
        if (info != null) info.dispose();
        Set<Location> scanned = NetworkUtils.scanDirectly(controller.getBlock());
        AllNetworkBlocks.addAll(scanned);
        StorageCollection storageCollection = new StorageCollection();
        Set<Location> craftingHolders = new HashSet<>();
        Map<Location, Set<CraftingRecipe>> recipeMap = new HashMap<>();
        boolean failed = false;
        for (Location location : scanned) {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(location);
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMEStorageObject IMEStorageObject) {
                AllNetworkStorageBlocks.put(location, IMEStorageObject);
                IStorage storage = IMEStorageObject.getStorage(location.getBlock());
                if (storage != null)
                    storageCollection.addStorage(storage);
            }
            if (slimefunItem instanceof IMECraftHolder IMECraftHolder) {
                AllNetworkCraftHolders.put(location, IMECraftHolder);
                craftingHolders.add(location);
                recipeMap.put(
                        location, new HashSet<>(List.of(IMECraftHolder.getSupportedRecipes(location.getBlock()))));
            }
            if (slimefunItem instanceof IMEController && !location.equals(controller)) failed = true;
        }
        if (!failed) {
            AllControllers.add(controller);
            info = new NetworkInfo(controller, scanned);
            info.setStorage(storageCollection);
            info.getCraftingHolders().addAll(craftingHolders);
            for (Location key : recipeMap.keySet()) {
                info.getRecipeMap().put(key, recipeMap.get(key));
            }
            AllNetworkData.add(info);
            return info;
        }
        return null;
    }

    public void clearData(Location location) {
        NetworkInfo info = getNetworkInfo(location);
        if (info != null) {
            Location controller = info.getController();
            if (location.equals(controller)) {
                info.dispose();
            }
            AllControllers.remove(controller);
            AllNetworkCraftHolders.remove(location);
            AllNetworkStorageBlocks.remove(location);
            AllNetworkBlocks.remove(location);

            info.getChildren().remove(location);
        }
    }
}
