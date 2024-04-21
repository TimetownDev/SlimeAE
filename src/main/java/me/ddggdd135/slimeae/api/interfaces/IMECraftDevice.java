package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.CraftingRecipe;

public interface IMECraftDevice extends IMEObject {
    boolean isSupport(@Nonnull CraftingRecipe recipe);

    boolean canStartCrafting(@Nonnull CraftingRecipe recipe);

    boolean isFinished();

    @Nonnull
    CraftingRecipe getFinishedCraftingRecipe();
}
