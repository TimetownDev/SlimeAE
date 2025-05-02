package me.ddggdd135.slimeae.api.items;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.ItemKey;

public class ItemInfo {
    private final ItemKey itemKey;
    private long amount;

    public ItemInfo(@Nonnull ItemKey itemKey) {
        this.itemKey = itemKey;
    }

    public ItemInfo(@Nonnull ItemKey itemKey, long amount) {
        this(itemKey);
        this.amount = amount;
    }

    public ItemKey getItemKey() {
        return itemKey;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemInfo itemInfo)) return false;
        return amount == itemInfo.amount && Objects.equals(itemKey, itemInfo.itemKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemKey, amount);
    }
}
