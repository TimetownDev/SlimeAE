package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import org.bukkit.block.Block;

public interface IMECraftHolder extends IMEObject {
    @Nonnull
    Block[] getCraftingDevices(@Nonnull Block block);

    @Nonnull
    CraftingRecipe[] getSupportedRecipes(@Nonnull Block block);
}
