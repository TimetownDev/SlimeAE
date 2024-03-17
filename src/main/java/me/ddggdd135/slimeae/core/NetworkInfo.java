package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IDisposable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class NetworkInfo implements IDisposable {
    private Location controller;
    private Set<Location> children = new HashSet<>();

    public Location getController() {
        return controller;
    }
    public Set<Location> getChildren() {
        return children;
    }
    public NetworkInfo(Location controller) {
        this.controller = controller;
    }

    public NetworkInfo(Location controller, Set<Location> children) {
        this.controller = controller;
        this.children = children;
    }

    public IStorage getStorage() {
        StorageCollection storageCollection = new StorageCollection();
        for (Location location : children) {
            SlimefunBlockData blockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(location);
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMEStorageObject<?> IMEStorageObject) {
                storageCollection.addStorage(IMEStorageObject.getStorage(location.getBlock()));
            }
        }
        return storageCollection;
    }

    @Override
    public void dispose() {
        NetworkData networkData = SlimeAEPlugin.getNetworkData();
        networkData.AllNetworkData.remove(this);
    }

    @Override
    public int hashCode() {
        return controller.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkInfo that = (NetworkInfo) o;
        return controller.equals(that.controller);
    }
}
