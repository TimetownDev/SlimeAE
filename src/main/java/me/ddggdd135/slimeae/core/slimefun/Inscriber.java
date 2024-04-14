package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.AbstractMachineBlock;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Inscriber extends AbstractMachineBlock {
    public Inscriber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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
        return "ME_INSCRIBER";
    }

    @Override
    public int getCapacity() {
        return 1024;
    }

    @Override
    public int getEnergyConsumption() {
        return 64;
    }

    @Override
    public int getSpeed() {
        return 1;
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

        registerRecipe(
                20,
                new ItemStack[] {
                    SlimefunAEItems.PRINTED_LOGIC_CIRCUIT,
                    new ItemStack(Material.REDSTONE),
                    SlimefunAEItems.PRINTED_SILICON
                },
                new ItemStack[] {SlimefunAEItems.LOGIC_PROCESSOR});

        registerRecipe(
                20,
                new ItemStack[] {
                    SlimefunAEItems.PRINTED_CALCULATION_CIRCUIT,
                    new ItemStack(Material.REDSTONE),
                    SlimefunAEItems.PRINTED_SILICON
                },
                new ItemStack[] {SlimefunAEItems.CALCULATION_PROCESSOR});

        registerRecipe(
                20,
                new ItemStack[] {
                    SlimefunAEItems.PRINTED_ENGINEERING_CIRCUIT,
                    new ItemStack(Material.REDSTONE),
                    SlimefunAEItems.PRINTED_SILICON
                },
                new ItemStack[] {SlimefunAEItems.ENGINEERING_PROCESSOR});
    }

    @Override
    public int[] getBorderIn() {
        return new int[] {9, 10, 11, 12, 21, 27, 28, 29, 30};
    }

    @Override
    public int[] getBorderOut() {
        return new int[] {14, 15, 16, 17, 23, 32, 33, 34, 35};
    }

    @Override
    public int[] getInputSlots() {
        return new int[] {18, 19, 20};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] {24, 25, 26};
    }
}
