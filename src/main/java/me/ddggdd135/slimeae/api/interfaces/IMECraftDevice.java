package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import org.bukkit.block.Block;

public interface IMECraftDevice extends IMEObject {
    boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe);
}
