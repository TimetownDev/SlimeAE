package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import org.bukkit.block.Block;

public interface IMECraftDevice extends IMEObject {
    boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe);

    boolean canStartCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe);

    boolean isFinished(@Nonnull Block block);

    @Nonnull
    CraftingRecipe getFinishedCraftingRecipe(@Nonnull Block block);
}
