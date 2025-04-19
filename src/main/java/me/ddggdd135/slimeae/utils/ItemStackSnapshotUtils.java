package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemStackSnapshot;
import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;

public class ItemStackSnapshotUtils {
    @Nonnull
    public static ItemStack clone(@Nonnull ItemStackSnapshot itemStackSnapshot) {
        ItemStack itemStack = new ItemStack(itemStackSnapshot.getType(), itemStackSnapshot.getAmount());
        itemStack.setItemMeta(itemStackSnapshot.getItemMeta());

        return itemStack;
    }
}
