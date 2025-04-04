package me.ddggdd135.slimeae.api.blockdata;

import java.util.Arrays;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IInventoryBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MEInterfaceData implements IInventoryBlockData {
    private static final NamespacedKey key = new NamespacedKey(SlimeAEPlugin.getInstance(), "me_interface");
    private ItemStack[] itemStacks = new ItemStack[0];

    @Override
    @Nonnull
    public NamespacedKey getNamespacedKey() {
        return key;
    }

    @Override
    @Nonnull
    public ItemStack[] getItemStacks() {
        return itemStacks;
    }

    @Override
    public void setItemStacks(@Nonnull ItemStack[] itemStacks) {
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean needItems() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MEInterfaceData that = (MEInterfaceData) o;
        return Arrays.equals(itemStacks, that.itemStacks);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(itemStacks);
    }
}
