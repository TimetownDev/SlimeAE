package me.ddggdd135.slimeae.core;

import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IDisposable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import org.bukkit.Location;

public class NetworkInfo implements IDisposable {
    private Location controller;
    private Set<Location> children = new HashSet<>();

    private IStorage storage = new StorageCollection();

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
        return storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
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
