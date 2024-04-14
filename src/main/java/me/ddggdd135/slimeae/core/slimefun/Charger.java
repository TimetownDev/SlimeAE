package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.ddggdd135.slimeae.api.abstracts.AbstractMachineBlock;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class Charger extends AbstractMachineBlock {
    public Charger(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.PISTON);
    }

    @Nonnull
    @Override
    public String getMachineIdentifier() {
        return "ME_CHARGER";
    }

    @Override
    public int getCapacity() {
        return 2048;
    }

    @Override
    public int getEnergyConsumption() {
        return 128;
    }

    @Override
    public int getSpeed() {
        return 1;
    }

    @Override
    protected void registerDefaultRecipes() {
        registerRecipe(2, new ItemStack(Material.QUARTZ), SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ);
        registerRecipe(2, SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ, SlimefunAEItems.CHARGED_CRYSTAL_CERTUS_QUARTZ);

        registerRecipe(1, SlimefunAEItems.QUARTZ_DUST, SlimefunAEItems.CERTUS_QUARTZ_DUST);
        registerRecipe(1, SlimefunAEItems.CERTUS_QUARTZ_DUST, SlimefunAEItems.FLUIX_DUST);
        registerRecipe(1, SlimefunAEItems.FLUIX_DUST, SlimefunAEItems.SKY_STONE_DUST);

        registerRecipe(3, SlimefunAEItems.CHARGED_CRYSTAL_CERTUS_QUARTZ, SlimefunAEItems.CRYSTAL_FLUIX);
    }
}
