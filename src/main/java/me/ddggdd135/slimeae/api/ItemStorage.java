package me.ddggdd135.slimeae.api;

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
    private Map<ItemStack, Integer> storage = new ConcurrentHashMap<>();

    private boolean isReadonly;

    private void trim(@Nonnull ItemStack itemStack) {
        ItemStack template = itemStack.asOne();
        if (storage.getOrDefault(template, 0) == 0) {
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

    public ItemStorage(@Nonnull Map<ItemStack, Integer> items) {
        storage = new HashMap<>(items);
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (isReadonly) return;
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = itemStack.asOne();
            int amount = storage.getOrDefault(template, 0);
            amount += itemStack.getAmount();
            storage.put(template, amount);
            itemStack.setAmount(0);
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull ItemStack[] itemStacks) {
        if (isReadonly) return;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemStack template = itemStack.asOne();
            int amount = storage.getOrDefault(template, 0);
            amount += itemStack.getAmount();
            storage.put(template, amount);
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull Map<ItemStack, Integer> storage) {
        if (isReadonly) return;
        this.storage = ItemUtils.addItems(this.storage, storage);
    }

    public void addItem(@Nonnull ItemStack itemStack, int amount) {
        if (isReadonly) return;
        ItemStack template = itemStack.asOne();
        int a = storage.getOrDefault(template, 0);
        a += amount;
        storage.put(template, a);
    }

    public void addItem(@Nonnull ItemStack itemStack) {
        if (isReadonly) return;
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
            int amount = storage.getOrDefault(request.getTemplate(), 0);
            if (amount >= request.getAmount()) {
                ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), request.getAmount());
                itemStacks.addAll(List.of(tmp));
                storage.put(request.getTemplate(), amount - request.getAmount());
            } else {
                ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), amount);
                itemStacks.addAll(List.of(tmp));
                storage.put(request.getTemplate(), 0);
            }
            trim(request.getTemplate());
        }
        return itemStacks.toArray(ItemStack[]::new);
    }

    @Override
    @Nonnull
    public Map<ItemStack, Integer> getStorage() {
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
