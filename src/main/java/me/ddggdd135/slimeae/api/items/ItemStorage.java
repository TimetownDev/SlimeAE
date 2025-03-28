package me.ddggdd135.slimeae.api.items;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class ItemStorage implements IStorage {
    @Nonnull
    private ItemHashMap<Long> storage = new ItemHashMap<>();

    private boolean isReadonly;

    private void trim(@Nonnull ItemKey key) {
        if (storage.getOrDefault(key, 0L) == 0) {
            storage.removeKey(key);
        }
    }

    public ItemStorage() {}

    public ItemStorage(@Nonnull ItemStack... itemStacks) {
        storage = ItemUtils.getAmounts(itemStacks);
    }

    public ItemStorage(@Nonnull IStorage storage) {
        this(storage.getStorage());
    }

    public ItemStorage(@Nonnull ItemHashMap<Long> items) {
        storage = new ItemHashMap<>(items);
    }

    @Override
    public void pushItem(@Nonnull ItemStack itemStack) {
        if (isReadonly) return;
        ItemStack template = itemStack.asOne();
        long amount = storage.getOrDefault(template, 0L);
        amount += itemStack.getAmount();
        storage.put(template, amount);
        itemStack.setAmount(0);
        trim(new ItemKey(itemStack));
    }

    public void addItem(@Nonnull ItemStack[] itemStacks) {
        addItem(itemStacks, false);
    }

    public void addItem(@Nonnull ItemStack[] itemStacks, boolean force) {
        if (isReadonly && !force) return;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemStack template = itemStack.asOne();
            long amount = storage.getOrDefault(template, 0L);
            amount += itemStack.getAmount();
            storage.put(template, amount);
            trim(new ItemKey(itemStack));
        }
    }

    public void addItem(@Nonnull ItemHashMap<Long> storage) {
        addItem(storage, false);
    }

    public void addItem(@Nonnull ItemHashMap<Long> storage, boolean force) {
        if (isReadonly && !force) return;
        this.storage = ItemUtils.addItems(this.storage, storage);
    }

    public void addItem(@Nonnull ItemKey itemStack, long amount) {
        addItem(itemStack, amount, false);
    }

    public void addItem(@Nonnull ItemKey itemStack, long amount, boolean force) {
        if (isReadonly && !force) return;
        long a = storage.getOrDefault(itemStack, 0L);
        a += amount;
        storage.putKey(itemStack, a);
    }

    public void addItem(@Nonnull ItemStack itemStack) {
        addItem(itemStack, false);
    }

    public void addItem(@Nonnull ItemStack itemStack, boolean force) {
        if (isReadonly && !force) return;
        addItem(new ItemStack[] {itemStack});
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        return ItemUtils.contains(storage, requests);
    }

    @Override
    @Nonnull
    public ItemStorage tryTakeItem(@Nonnull ItemRequest[] requests) {
        ItemStorage itemStacks = new ItemStorage();
        for (ItemRequest request : requests) {
            long amount = storage.getOrDefault(request.getKey(), 0L);
            if (amount >= request.getAmount()) {
                itemStacks.addItem(request.getKey(), request.getAmount());
                storage.putKey(request.getKey(), amount - request.getAmount());
            } else {
                itemStacks.addItem(request.getKey(), amount);
                storage.putKey(request.getKey(), 0L);
            }
            trim(request.getKey());
        }
        return itemStacks;
    }

    @Override
    @Nonnull
    public ItemHashMap<Long> getStorage() {
        return new ItemHashMap<>(storage);
    }

    @Nonnull
    public ItemStack[] toItemStacks() {
        ItemUtils.trim(storage);
        return ItemUtils.createItems(storage);
    }

    public void setReadonly(boolean readonly) {
        isReadonly = readonly;
    }

    public boolean isReadonly() {
        return isReadonly;
    }
}
