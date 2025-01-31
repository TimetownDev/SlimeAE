package me.ddggdd135.slimeae.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class ItemStorage implements IStorage {
    @Nonnull
    private Map<ItemStack, Integer> storage = new HashMap<>();

    private void trim(@Nonnull ItemStack template) {
        if (storage.containsKey(template) && storage.get(template) == 0) {
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
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = itemStack.asOne();
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemStack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(template, itemStack.getAmount());
            }
            itemStack.setAmount(0);
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = itemStack.asOne();
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemStack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(template, itemStack.getAmount());
            }
            trim(itemStack);
        }
    }

    public void addItem(@Nonnull ItemStack itemStack) {
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
    @Nonnull
    public Map<ItemStack, Integer> getStorage() {
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
