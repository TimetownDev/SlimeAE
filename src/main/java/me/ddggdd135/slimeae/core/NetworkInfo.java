package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IDisposable;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import org.bukkit.Location;

public class NetworkInfo implements IDisposable {
    private Location controller;
    private Set<Location> children = new HashSet<>();
    private Set<Location> craftingHolders = new HashSet<>();
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

    public Set<Location> getCraftingHolders() {
        return craftingHolders;
    }

    public Set<CraftingRecipe> getRecipes() {
        Set<CraftingRecipe> recipes = new HashSet<>();
        for (Location location : craftingHolders) {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(location);
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftHolder IMECraftHolder) {
                Collections.addAll(recipes, IMECraftHolder.getSupportedRecipes(location.getBlock()));
            }
        }
        return recipes;
    }
}
