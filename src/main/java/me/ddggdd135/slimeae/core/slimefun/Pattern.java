package me.ddggdd135.slimeae.core.slimefun;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import org.bukkit.inventory.ItemStack;

public class Pattern extends SlimefunItem {
    public Pattern(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (!nbtItem.hasTag("recipe", NBTType.NBTTagCompound)) return null;
        NBTCompound compound = nbtItem.getOrCreateCompound("recipe");
        return new CraftingRecipe(
                compound.getEnum("crafting_type", CraftType.class),
                compound.getItemStackArray("input"),
                compound.getItemStackArray("output"));
    }

    public static void setRecipe(@Nonnull ItemStack itemStack, @Nonnull CraftingRecipe recipe) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasTag("recipe")) nbtItem.removeKey("recipe");
        NBTCompound compound = nbtItem.getOrCreateCompound("recipe");
        compound.setEnum("crafting_type", recipe.getCraftType());
        compound.setItemStackArray("input", recipe.getInput());
        compound.setItemStackArray("output", recipe.getOutput());
        nbtItem.applyNBT(itemStack);
    }
}
