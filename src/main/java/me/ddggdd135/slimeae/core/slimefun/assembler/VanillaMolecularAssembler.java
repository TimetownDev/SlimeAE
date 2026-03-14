package me.ddggdd135.slimeae.core.slimefun.assembler;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import org.bukkit.inventory.ItemStack;

public class VanillaMolecularAssembler extends MolecularAssembler {
    private static final Set<CraftType> VANILLA_CRAFT_TYPES =
            Arrays.stream(CraftType.values()).filter(CraftType::isVanilla).collect(Collectors.toUnmodifiableSet());

    public VanillaMolecularAssembler(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public CraftType getCraftingType() {
        return CraftType.VANILLA_CRAFTING_TABLE;
    }

    @Nonnull
    @Override
    public Set<CraftType> getSupportedCraftTypes() {
        return VANILLA_CRAFT_TYPES;
    }
}
