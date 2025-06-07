package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.AEMachineBlock;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Charger extends AEMachineBlock {
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
        registerRecipe(2, new ItemStack(Material.QUARTZ), SlimeAEItems.CRYSTAL_CERTUS_QUARTZ);
        registerRecipe(2, SlimeAEItems.CRYSTAL_CERTUS_QUARTZ, SlimeAEItems.CHARGED_CRYSTAL_CERTUS_QUARTZ);

        registerRecipe(1, SlimeAEItems.QUARTZ_DUST, SlimeAEItems.CERTUS_QUARTZ_DUST);
        registerRecipe(1, SlimeAEItems.CERTUS_QUARTZ_DUST, SlimeAEItems.FLUIX_DUST);
        registerRecipe(1, SlimeAEItems.FLUIX_DUST, SlimeAEItems.SKY_STONE_DUST);

        registerRecipe(3, SlimeAEItems.CHARGED_CRYSTAL_CERTUS_QUARTZ, SlimeAEItems.CRYSTAL_FLUIX);
    }
}
