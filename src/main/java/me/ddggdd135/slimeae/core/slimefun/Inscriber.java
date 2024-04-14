package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.InventoryListener;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Inscriber extends AContainer implements EnergyNetComponent, RecipeDisplayItem {
    public Inscriber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.PISTON);
    }

    @Nonnull
    @Override
    public String getMachineIdentifier() {
        return "ME_INSCRIBER";
    }

    public int getCapacity() {
        return 1024;
    }

    public int getEnergyConsumption() {
        return 64;
    }

    public int getSpeed() {
        return 1;
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> displayRecipes = new ArrayList<>(recipes.size() * 2);

        for (int i = 0; i < recipes.size(); i++) {
            MachineRecipe recipe = recipes.get(i);
            if (recipe.getInput().length == 2) {
                ItemStack itemStack = MenuItems.MULTI_INPUT_ITEM.clone();
                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(InventoryListener.INDEX_KEY, PersistentDataType.INTEGER, i);
                displayRecipes.add(itemStack);
            } else {
                displayRecipes.add(recipe.getInput()[0]);
            }

            if (recipe.getOutput().length == 2) {
                ItemStack itemStack = MenuItems.MULTI_OUTPUT_ITEM.clone();
                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(InventoryListener.INDEX_KEY, PersistentDataType.INTEGER, i);
                displayRecipes.add(itemStack);
            } else {
                displayRecipes.add(recipe.getOutput()[0]);
            }
        }

        return displayRecipes;
    }

    @Override
    protected void registerDefaultRecipes() {
        registerRecipe(5, new ItemStack[] {SlimefunItems.SILICON, new ItemStack(Material.IRON_INGOT)}, new ItemStack[] {
            SlimefunAEItems.PRINTED_SILICON
        });

        registerRecipe(
                10,
                new ItemStack[] {SlimefunAEItems.PRINTED_SILICON, new ItemStack(Material.GOLD_INGOT)},
                new ItemStack[] {SlimefunAEItems.PRINTED_LOGIC_CIRCUIT});

        registerRecipe(
                10,
                new ItemStack[] {SlimefunAEItems.PRINTED_SILICON, SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ},
                new ItemStack[] {SlimefunAEItems.PRINTED_CALCULATION_CIRCUIT});

        registerRecipe(
                10,
                new ItemStack[] {SlimefunAEItems.PRINTED_SILICON, new ItemStack(Material.DIAMOND)},
                new ItemStack[] {SlimefunAEItems.PRINTED_ENGINEERING_CIRCUIT});
    }
}
