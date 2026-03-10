package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.slimefun.MEDrive;
import me.ddggdd135.slimeae.core.slimefun.assembler.LargeMolecularAssembler;
import me.ddggdd135.slimeae.core.slimefun.assembler.MolecularAssembler;
import me.ddggdd135.slimeae.integrations.networks.NetworksStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class NetworkData {
    public final Set<NetworkInfo> AllNetworkData = ConcurrentHashMap.newKeySet();
    public final Map<Location, IMEObject> AllNetworkBlocks = new ConcurrentHashMap<>();
    public final Map<Location, IMEController> AllControllers = new ConcurrentHashMap<>();
    public final Map<Location, IMEStorageObject> AllStorageObjects = new ConcurrentHashMap<>();
    public final Map<Location, IMECraftHolder> AllCraftHolders = new ConcurrentHashMap<>();

    public final Map<Location, NetworkInfo> locationToNetwork = new ConcurrentHashMap<>();

    @Nullable public NetworkInfo getNetworkInfo(Location location) {
        NetworkInfo info = locationToNetwork.get(location);
        if (info != null) return info;

        for (NetworkInfo ni : AllNetworkData) {
            if (ni.getController().equals(location)) {
                locationToNetwork.put(location, ni);
                return ni;
            }
        }

        for (NetworkInfo ni : AllNetworkData) {
            if (ni.getChildren().contains(location)) {
                locationToNetwork.put(location, ni);
                return ni;
            }
        }
        return null;
    }

    public NetworkInfo refreshNetwork(Location controller) {
        if (!AllControllers.containsKey(controller)) return null;
        NetworkInfo info = getNetworkInfo(controller);
        if (info == null) {
            info = new NetworkInfo(controller);
            AllNetworkData.add(info);
            locationToNetwork.put(controller, info);
        }

        if (!updateChildren(info)) return null;
        if (!updateStorage(info)) return null;
        if (!updateAutoCraft(info)) return null;

        return info;
    }

    public boolean updateChildren(@Nonnull NetworkInfo info) {
        Location controller = info.getController();

        boolean needsScan = info.getChildren().size() <= 1;

        if (!needsScan) {
            for (Location child : info.getChildren()) {
                for (BlockFace face : IMEObject.Valid_Faces) {
                    Location adj = child.clone().add(face.getDirection());
                    if (AllNetworkBlocks.containsKey(adj) && !info.getChildren().contains(adj)) {
                        needsScan = true;
                        break;
                    }
                }
                if (needsScan) break;
            }
        }

        if (needsScan) {
            Set<Location> children = NetworkUtils.scan(controller.getBlock());
            for (Location location : children) {
                if (AllControllers.containsKey(location) && !location.equals(controller)) {
                    info.dispose();
                    return false;
                }
            }

            info.replaceChildren(children);
        }

        info.getChildren().removeIf(x -> {
            if (!AllNetworkBlocks.containsKey(x)) {
                locationToNetwork.remove(x, info);
                return true;
            }
            return false;
        });

        Set<Location> tickable = new HashSet<>();
        for (Location loc : info.getChildren()) {
            IMEObject obj = AllNetworkBlocks.get(loc);
            if (obj instanceof MEDrive || obj instanceof MolecularAssembler || obj instanceof LargeMolecularAssembler) {
                tickable.add(loc);
            }
        }
        info.setTickableChildren(tickable);

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
        Map<Location, Block[]> devicesCache = new HashMap<>();
        for (Location location : info.getChildren()) {
            if (!AllCraftHolders.containsKey(location)) continue;
            IMECraftHolder slimefunItem = AllCraftHolders.get(location);
            newCraftingHolders.add(location);
            Block[] devices = slimefunItem.getCraftingDevices(location.getBlock());
            devicesCache.put(location, devices);
            Set<CraftingRecipe> supported = new HashSet<>();
            CraftingRecipe[] holderRecipes;
            if (slimefunItem instanceof me.ddggdd135.slimeae.core.slimefun.MEInterface meInterface) {
                holderRecipes = meInterface.getRecipes(location.getBlock());
            } else if (slimefunItem
                    instanceof me.ddggdd135.slimeae.core.slimefun.MEPatternInterface mePatternInterface) {
                holderRecipes = mePatternInterface.getRecipes(location.getBlock());
            } else {
                holderRecipes = slimefunItem.getSupportedRecipes(location.getBlock());
            }
            for (Block device : devices) {
                SlimefunBlockData blockData =
                        Slimefun.getDatabaseManager().getBlockDataController().getBlockData(device.getLocation());
                if (blockData == null) continue;
                SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
                if (sfItem instanceof IMECraftDevice craftDevice) {
                    for (CraftingRecipe recipe : holderRecipes) {
                        if (!supported.contains(recipe) && craftDevice.isSupport(device, recipe)) {
                            supported.add(recipe);
                        }
                    }
                }
            }
            newRecipeMap.put(location, supported);
        }

        Map<CraftType, Integer> newSpeeds = new ConcurrentHashMap<>();
        for (Location location : newRecipeMap.keySet()) {
            Block[] devices = devicesCache.get(location);
            if (devices == null) continue;
            for (Block deviceBlock : devices) {
                IMECraftDevice imeCraftDevice = (IMECraftDevice) SlimefunItem.getById(
                        StorageCacheUtils.getBlock(deviceBlock.getLocation()).getSfId());
                if (!(imeCraftDevice instanceof IMEVirtualCraftDevice device)) continue;
                CraftType craftType = device.getCraftingType();
                int speed = newSpeeds.getOrDefault(craftType, 0);
                speed += device.getSpeed(deviceBlock);
                newSpeeds.put(craftType, speed);
            }
        }

        Map<ItemKey, CraftingRecipe> newOutputIndex = new HashMap<>();
        Map<CraftingRecipe, List<Location>> newRecipeToHolders = new HashMap<>();
        for (Map.Entry<Location, Set<CraftingRecipe>> entry : newRecipeMap.entrySet()) {
            Location loc = entry.getKey();
            for (CraftingRecipe r : entry.getValue()) {
                for (ItemStack output : r.getOutput()) {
                    newOutputIndex.putIfAbsent(new ItemKey(output.asOne()), r);
                }
                newRecipeToHolders.computeIfAbsent(r, k -> new ArrayList<>()).add(loc);
            }
        }

        // 先计算新的配方缓存快照
        Set<CraftingRecipe> newCachedRecipes = new HashSet<>();
        for (Set<CraftingRecipe> recipes : newRecipeMap.values()) {
            newCachedRecipes.addAll(recipes);
        }

        // 原子地替换底层数据：先设置 F9 缓存快照，使 getRecipes() 返回新结果，
        // 然后再更新底层数据结构。这避免了 clear()+putAll() 之间的竞态条件。
        info.setRecipeCache(Collections.unmodifiableSet(newCachedRecipes));
        info.setOutputIndex(newOutputIndex);
        info.setRecipeToHolders(newRecipeToHolders);
        info.setCachedCraftingDevices(devicesCache);

        info.getCraftingHolders().clear();
        info.getCraftingHolders().addAll(newCraftingHolders);
        info.getRecipeMap().clear();
        info.getRecipeMap().putAll(newRecipeMap);
        info.getVirtualCraftingDeviceSpeeds().clear();
        info.getVirtualCraftingDeviceSpeeds().putAll(newSpeeds);

        return true;
    }
}
