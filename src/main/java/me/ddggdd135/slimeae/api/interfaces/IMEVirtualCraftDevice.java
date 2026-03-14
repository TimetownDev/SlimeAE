package me.ddggdd135.slimeae.api.interfaces;

import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import org.bukkit.block.Block;

public interface IMEVirtualCraftDevice extends IMECraftDevice {
    int getSpeed(@Nonnull Block block);

    CraftType getCraftingType();

    @Nonnull
    default Set<CraftType> getSupportedCraftTypes() {
        return Set.of(getCraftingType());
    }

    @Override
    default boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return getSupportedCraftTypes().contains(recipe.getCraftType());
    }
}
