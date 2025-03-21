package me.ddggdd135.slimeae.api.items;

import me.ddggdd135.guguslimefunlib.items.ItemKey;

public class ItemRequest {
    private ItemKey key;
    private long amount;

    public ItemRequest(ItemKey key, long amount) {
        this.key = key;
        this.amount = amount;
    }

    public ItemKey getKey() {
        return key;
    }

    public void setKey(ItemKey key) {
        this.key = key;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
