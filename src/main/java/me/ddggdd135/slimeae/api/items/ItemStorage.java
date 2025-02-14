package me.ddggdd135.slimeae.api.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class ItemStorage implements IStorage {
    @Nonnull
    private Map<ItemStack, Long> storage = new ConcurrentHashMap<>();

    private boolean isReadonly;

    private void trim(@Nonnull ItemStack itemStack) {
        ItemStack template = itemStack.asOne();
        if (storage.getOrDefault(template, 0L) == 0) {
            storage.remove(template);
        }
    }

    public ItemStorage() {}

    public ItemStorage(@Nonnull ItemStack... itemStacks) {
        storage = ItemUtils.getAmounts(itemStacks);
    }

    public ItemStorage(@Nonnull IStorage storage) {
        this(storage.getStorage());
    }

    public ItemStorage(@Nonnull Map<ItemStack, Long> items) {
        storage = new HashMap<>(items);
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (isReadonly) return;
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = itemStack.asOne();
            long amount = storage.getOrDefault(template, 0L);
            amount += itemStack.getAmount();
            storage.put(template, amount);
            itemStack.setAmount(0);
            trim(itemStack);
        }
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
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull Map<ItemStack, Long> storage) {
        addItem(storage, false);
    }

    public void addItem(@Nonnull Map<ItemStack, Long> storage, boolean force) {
        if (isReadonly && !force) return;
        this.storage = ItemUtils.addItems(this.storage, storage);
    }

    public void addItem(@Nonnull ItemStack itemStack, long amount) {
        addItem(itemStack, amount, false);
    }

    public void addItem(@Nonnull ItemStack itemStack, long amount, boolean force) {
        if (isReadonly && !force) return;
        ItemStack template = itemStack.asOne();
        long a = storage.getOrDefault(template, 0L);
        a += amount;
        storage.put(template, a);
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
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            long amount = storage.getOrDefault(request.getTemplate(), 0L);
            if (amount >= request.getAmount()) {
                ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), request.getAmount());
                itemStacks.addAll(List.of(tmp));
                storage.put(request.getTemplate(), amount - request.getAmount());
            } else {
                ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), amount);
                itemStacks.addAll(List.of(tmp));
                storage.put(request.getTemplate(), 0L);
            }
            trim(request.getTemplate());
        }
        return itemStacks.toArray(ItemStack[]::new);
    }

    @Override
    @Nonnull
    public Map<ItemStack, Long> getStorage() {
        return new HashMap<>(storage);
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }

    @Nonnull
    public ItemStack[] toItemStacks() {
        return ItemUtils.createItems(storage);
    }

    public void setReadonly(boolean readonly) {
        isReadonly = readonly;
    }

    public boolean isReadonly() {
        return isReadonly;
    }
}
