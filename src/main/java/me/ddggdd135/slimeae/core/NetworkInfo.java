package me.ddggdd135.slimeae.core;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IDisposable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class NetworkInfo implements IDisposable {
    private Location controller;
    private Set<Location> children = new HashSet<>();
    private Set<Location> craftingHolders = new HashSet<>();
    private Map<Location, Set<CraftingRecipe>> recipeMap = new HashMap<>();
    private IStorage storage = new StorageCollection();

    @Nonnull
    public Location getController() {
        return controller;
    }

    @Nonnull
    public Set<Location> getChildren() {
        return children;
    }

    public NetworkInfo(@Nonnull Location controller) {
        this.controller = controller;
    }

    public NetworkInfo(@Nonnull Location controller, @Nonnull Set<Location> children) {
        this.controller = controller;
        this.children = children;
    }

    @Nonnull
    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(@Nonnull IStorage storage) {
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
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkInfo that = (NetworkInfo) o;
        return controller.equals(that.controller);
    }

    @Nonnull
    public Set<Location> getCraftingHolders() {
        return craftingHolders;
    }

    @Nonnull
    public Set<CraftingRecipe> getRecipes(@Nonnull Block holder) {
        return recipeMap.get(holder.getLocation());
    }

    @Nonnull
    public Map<Location, Set<CraftingRecipe>> getRecipeMap() {
        return recipeMap;
    }

    @Nonnull
    public Set<CraftingRecipe> getRecipes() {
        Set<CraftingRecipe> recipes = new HashSet<>();
        for (Location location : craftingHolders) {
            recipes.addAll(recipeMap.get(location));
        }
        return recipes;
    }

    @Nullable public CraftingRecipe getRecipeFor(@Nonnull ItemStack output) {
        for (CraftingRecipe recipe : getRecipes()) {
            if (Arrays.asList(recipe.getOutput()).contains(output)) return recipe;
        }

        return null;
    }
}
