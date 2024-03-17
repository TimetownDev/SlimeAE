package me.ddggdd135.slimeae.api;

import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStorage implements IStorage {
    private Map<ItemStack, Integer> storage = new HashMap<>();

    private void trim(ItemStack template) {
        if (storage.containsKey(template) && storage.get(template) == 0) {
            storage.remove(template);
        }
    }

    public ItemStorage() {

    }

    public ItemStorage(ItemStack... itemStacks) {
        storage = ItemUtils.getAmounts(itemStacks);
    }

    public ItemStorage(Map<ItemStack, Integer> items) {
        storage = new HashMap<>(items);
    }

    @Override
    public void pushItem(ItemStack[] itemStacks) {
        for (ItemStack itemstack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemstack);
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemstack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(ItemUtils.createTemplateItem(itemstack), itemstack.getAmount());
            }
            itemstack.setAmount(0);
            trim(template);
        }
    }

    public void addItem(ItemStack[] itemStacks) {
        for (ItemStack itemstack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemstack);
            if (storage.containsKey(template)) {
                int amount = storage.get(template);
                amount += itemstack.getAmount();
                storage.put(template, amount);
            } else {
                storage.put(ItemUtils.createTemplateItem(itemstack), itemstack.getAmount());
            }
            trim(template);
        }
    }

    @Override
    public boolean contains(ItemRequest[] requests) {
        return ItemUtils.contains(storage, requests);
    }

    @Override
    public ItemStack[] tryTakeItem(ItemRequest[] requests) {
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
