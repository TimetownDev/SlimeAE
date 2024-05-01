package me.ddggdd135.slimeae.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ItemStorage implements IStorage {
    @NonNull private Map<ItemStack, Integer> storage = new ItemHashMap<>();

    private void trim(@NonNull ItemStack template) {
        if (storage.containsKey(template) && storage.get(template) == 0) {
            storage.remove(template);
        }
    }

    public ItemStorage() {}

    public ItemStorage(@NonNull ItemStack... itemStacks) {
        storage = ItemUtils.getAmounts(itemStacks);
    }

    public ItemStorage(@NonNull IStorage storage) {
        this(storage.getStorage());
    }

    public ItemStorage(@NonNull Map<ItemStack, Integer> items) {
        storage = new ItemHashMap<>(items);
    }

    @Override
    public void pushItem(@Nonnull @NonNull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (storage.containsKey(itemStack)) {
                int amount = storage.get(itemStack);
                amount += itemStack.getAmount();
                storage.put(itemStack, amount);
            } else {
                storage.put(itemStack, itemStack.getAmount());
            }
            itemStack.setAmount(0);
            trim(itemStack);
        }
    }

    public void addItem(@NonNull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (storage.containsKey(itemStack)) {
                int amount = storage.get(itemStack);
                amount += itemStack.getAmount();
                storage.put(itemStack, amount);
            } else {
                storage.put(itemStack, itemStack.getAmount());
            }
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull ItemStack itemStack) {
        addItem(new ItemStack[] {itemStack});
    }

    @Override
    public boolean contains(@Nonnull @NonNull ItemRequest[] requests) {
        return ItemUtils.contains(storage, requests);
    }

    @Nonnull
    @Override
    @NonNull public ItemStack[] tryTakeItem(@Nonnull @NonNull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            if (storage.containsKey(request.getTemplate())) {
                int amount = storage.get(request.getTemplate());
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
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    @NonNull public Map<ItemStack, Integer> getStorage() {
        return new ItemHashMap<>(storage);
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }

    @Override
    public boolean canHasEmptySlots() {
        return false;
    }

    @Nonnull
    public ItemStack[] toItemStacks() {
        return ItemUtils.createItems(storage);
    }
}
