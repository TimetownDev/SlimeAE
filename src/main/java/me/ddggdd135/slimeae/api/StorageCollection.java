package me.ddggdd135.slimeae.api;

import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageCollection implements IStorage {
    private List<IStorage> storages = new ArrayList<>();

    public StorageCollection(IStorage... storages) {
        this.storages = new ArrayList<>(List.of(storages));
    }

    public void addStorage(IStorage storage) {
        storages.add(storage);
    }

    public boolean removeStorage(IStorage storage) {
        return storages.remove(storage);
    }

    @Override
    public void pushItem(ItemStack[] itemStacks) {
        for (IStorage storage : storages) {
            storage.pushItem(itemStacks);
            itemStacks = ItemUtils.trimItems(itemStacks);
            if (itemStacks.length == 0) return;
        }
    }

    @Override
    public boolean contains(ItemRequest[] requests) {
        return ItemUtils.contains(getStorage(), requests);
    }

    @Override
    public ItemStack[] tryTakeItem(ItemRequest[] requests) {
        Map<ItemStack, Integer> rest = new HashMap<>();
        ItemStorage found = new ItemStorage();
        //init rest
        for (ItemRequest request : requests) {
            if (rest.containsKey(request.getItemStack())) {
                rest.put(request.getItemStack(), rest.get(request.getItemStack()) + request.getAmount());
            } else {
                rest.put(request.getItemStack(), request.getAmount());
            }
        }
        ItemUtils.trim(rest);

        for (IStorage storage : storages) {
            ItemStack[] itemStacks = storage.tryTakeItem(ItemUtils.createRequests(rest));
            rest = ItemUtils.takeItems(rest, ItemUtils.getAmounts(itemStacks));
            ItemUtils.trim(rest);
            found.addItem(itemStacks);
            if (rest.keySet().isEmpty()) {
                break;
            }
        }

        return found.toItemStacks();
    }

    @Override
    public Map<ItemStack, Integer> getStorage() {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (IStorage storage : storages) {
            Map<ItemStack, Integer> tmp = storage.getStorage();
            for (ItemStack itemStack : tmp.keySet()) {
                if (result.containsKey(itemStack)) {
                    result.put(itemStack, result.get(itemStack) + tmp.get(itemStack));
                } else {
                    result.put(itemStack, tmp.get(itemStack));
                }
            }
        }
        return result;
    }

    @Override
    public int getEmptySlots() {
        if (canHasEmptySlots()) return 0;
        int found = 0;
        for (IStorage storage : storages) {
            found += storage.getEmptySlots();
        }
        return found;
    }

    @Override
    public boolean canHasEmptySlots() {
        for (IStorage storage : storages) {
            if (storage.canHasEmptySlots()) return true;
        }
        return false;
    }
}
