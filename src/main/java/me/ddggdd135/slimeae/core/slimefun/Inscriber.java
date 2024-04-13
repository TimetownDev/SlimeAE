package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Inscriber extends AContainer implements EnergyNetComponent, RecipeDisplayItem {
    public Inscriber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        setCapacity(1024);
        setProcessingSpeed(1);
        setEnergyConsumption(128);
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.PISTON);
    }

    @NotNull
    @Override
    public String getMachineIdentifier() {
        return "ME_INSCRIBER";
    }

    @Override
    protected void registerDefaultRecipes() {
        registerRecipe(5, new ItemStack[] {
                SlimefunItems.SILICON
        }, new ItemStack[] {SlimefunAEItems.PRINTED_SILICON});

        registerRecipe(10, new ItemStack[] {
                SlimefunAEItems.PRINTED_SILICON, new ItemStack(Material.GOLD_INGOT)
        }, new ItemStack[] {SlimefunAEItems.PRINTED_LOGIC_CIRCUIT});

        registerRecipe(10, new ItemStack[] {
                SlimefunAEItems.PRINTED_SILICON, SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ
        }, new ItemStack[] {SlimefunAEItems.PRINTED_CALCULATION_CIRCUIT});

        registerRecipe(10, new ItemStack[] {
                SlimefunAEItems.PRINTED_SILICON, new ItemStack(Material.DIAMOND)
        }, new ItemStack[] {SlimefunAEItems.PRINTED_ENGINEERING_CIRCUIT});
    }
}
