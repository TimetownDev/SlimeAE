package me.ddggdd135.slimeae.core.recipes;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.recipes.MinecraftRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class BlockDestroyRecipeType extends RecipeType {
    public BlockDestroyRecipeType(ItemStack item, String machine) {
        super(item, machine);
    }

    public BlockDestroyRecipeType(NamespacedKey key, SlimefunItemStack slimefunItem, String... lore) {
        super(key, slimefunItem, lore);
    }

    public BlockDestroyRecipeType(NamespacedKey key, ItemStack item, BiConsumer<ItemStack[], ItemStack> callback, String... lore) {
        super(key, item, callback, lore);
    }

    public BlockDestroyRecipeType(NamespacedKey key, ItemStack item, BiConsumer<ItemStack[], ItemStack> registerCallback, BiConsumer<ItemStack[], ItemStack> unregisterCallback, String... lore) {
        super(key, item, registerCallback, unregisterCallback, lore);
    }

    public BlockDestroyRecipeType(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    public BlockDestroyRecipeType(MinecraftRecipe<?> recipe) {
        super(recipe);
    }
}
