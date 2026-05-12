package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.block.Block;

public interface IMERealCraftDevice extends IMECraftDevice {
    boolean canStartCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe);

    boolean startCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe);

    boolean isFinished(@Nonnull Block block);

    @Nullable CraftingRecipe getFinishedCraftingRecipe(@Nonnull Block block);

    @Nonnull
    ItemStorage finishCrafting(@Nonnull Block block);
}
