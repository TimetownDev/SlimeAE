package me.ddggdd135.slimeae.utils;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.matlib.nmsMirror.impl.CraftBukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftItemStackUtils {
    public static ItemStack[] asCraftCopy(@Nonnull ItemStack[] itemStacks) {
        ItemStack[] copy = new ItemStack[itemStacks.length];

        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStack = itemStacks[i];
            if (itemStack == null) {
                copy[i] = CraftBukkit.ITEMSTACK.createCraftItemStack(Material.AIR, 1);
                continue;
            }

            copy[i] = CraftBukkit.ITEMSTACK.asCraftCopy(itemStack);
        }

        return copy;
    }
}
