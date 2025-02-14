package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;

public interface IInventoryBlockData extends IBlockData {
    @Nonnull
    ItemStack[] getItemStacks();

    void setItemStacks(@Nonnull ItemStack[] itemStacks);

    default boolean needItems() {
        return true;
    }
}
