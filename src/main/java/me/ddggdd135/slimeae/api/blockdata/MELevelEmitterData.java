package me.ddggdd135.slimeae.api.blockdata;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IInventoryBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MELevelEmitterData implements IInventoryBlockData {
    private static final NamespacedKey key = new NamespacedKey(SlimeAEPlugin.getInstance(), "me_level_emitter");

    private long amount;
    private ItemStack itemStack;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MELevelEmitterData that = (MELevelEmitterData) o;
        return amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    @Nonnull
    public NamespacedKey getNamespacedKey() {
        return key;
    }

    @Nonnull
    @Override
    public ItemStack[] getItemStacks() {
        return new ItemStack[] {itemStack};
    }

    @Override
    public void setItemStacks(@Nonnull ItemStack[] itemStacks) {
        itemStack = itemStacks[0];
    }

    @Override
    public boolean needItems() {
        return false;
    }
}
