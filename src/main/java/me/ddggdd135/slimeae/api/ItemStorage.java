package me.ddggdd135.slimeae.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ItemStorage implements IStorage {
    @NonNull private Map<ItemStack, Integer> storage = new HashMap<>();

    private void trim(@NonNull ItemStack template) {
        if (storage.containsKey(template) && storage.get(template) == 0) {
            storage.remove(template);
        }
    }

    public ItemStorage() {}

    public ItemStorage(@NonNull ItemStack... itemStacks) {
        storage = ItemUtils.getAmounts(itemStacks);
    }

    public ItemStorage(@NonNull Map<ItemStack, Integer> items) {
        storage = new HashMap<>(items);
    }

    @Override
    public void pushItem(@NonNull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemStack);
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemStack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(template, itemStack.getAmount());
            }
            itemStack.setAmount(0);
            trim(template);
        }
    }

    public void addItem(@NonNull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemStack);
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemStack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(template, itemStack.getAmount());
            }
            trim(template);
        }
    }

    public void addItem(@Nonnull ItemStack itemStack) {
        addItem(new ItemStack[]{itemStack});
    }

    @Override
    public boolean contains(@NonNull ItemRequest[] requests) {
        return ItemUtils.contains(storage, requests);
    }

    @Override
    @NonNull public ItemStack[] tryTakeItem(@NonNull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            if (storage.containsKey(request.getItemStack())) {
                int amount = storage.get(request.getItemStack());
                if (amount >= request.getAmount()) {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), request.getAmount());
                    itemStacks.addAll(List.of(tmp));
                    storage.put(request.getItemStack(), amount - request.getAmount());
                } else {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), amount);
                    itemStacks.addAll(List.of(tmp));
                    storage.put(request.getItemStack(), 0);
                }
                trim(request.getItemStack());
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    @NonNull public Map<ItemStack, Integer> getStorage() {
        return new HashMap<>(storage);
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
