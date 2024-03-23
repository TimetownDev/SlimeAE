package me.ddggdd135.slimeae.api;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class MEItemCellStorage implements IStorage {
    @Nonnull
    private final NBTCompound nbt;

    @Nonnull
    private final ItemStack itemStack;

    private final int size;
    private int storaged;

    private void trim(@Nonnull ItemStack template) {
        if (containsKey(template) && get(template) == 0) {
            remove(template);
        }
    }

    public boolean containsKey(@Nonnull ItemStack template) {
        return nbt.getKeys().contains(String.valueOf(template.hashCode()));
    }

    public int get(@Nonnull ItemStack template) {
        if (containsKey(template)) {
            ReadWriteNBT compound = nbt.getCompound(String.valueOf(template.hashCode()));
            return compound.getInteger("amount");
        }
        return 0;
    }

    private boolean remove(@Nonnull ItemStack template) {
        if (containsKey(template)) {
            nbt.removeKey(String.valueOf(template.hashCode()));
            return true;
        }
        return false;
    }

    private void put(@Nonnull ItemStack template, int amount) {
        if (containsKey(template)) {
            ReadWriteNBT compound = nbt.getCompound(String.valueOf(template.hashCode()));
            compound.setInteger("amount", amount);
        } else {
            ReadWriteNBT compound = nbt.getOrCreateCompound(String.valueOf(template.hashCode()));
            compound.setItemStack("item", template);
            compound.setInteger("amount", amount);
        }
    }

    public MEItemCellStorage(@Nonnull ItemStack itemStack) {
        if (MEItemStorageCell.getSize(itemStack) == 0) throw new RuntimeException("ItemStack is not MEItemStorageCell");
        NBTItem nbtItem = new NBTItem(itemStack, true);
        nbt = nbtItem.getOrCreateCompound("item_storage");
        this.itemStack = itemStack;
        size = MEItemStorageCell.getSize(itemStack);
        Map<ItemStack, Integer> storage = ItemUtils.toStorage(nbt);
        for (ItemStack key : storage.keySet()) {
            storaged += storage.get(key);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemStack);
            if (SlimefunItem.getByItem(template) instanceof MEItemStorageCell) continue;
            int amount = get(template);
            int toAdd;
            if (storaged + itemStack.getAmount() > size) toAdd = size - storaged;
            else toAdd = itemStack.getAmount();
            storaged += toAdd;
            put(template, amount + toAdd);
            itemStack.setAmount(itemStack.getAmount() - toAdd);
            trim(template);
        }
        MEItemStorageCell.updateLore(itemStack);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        for (ItemRequest request : requests) {
            ItemStack template = ItemUtils.createTemplateItem(request.getItemStack());
            if (!containsKey(template) || get(template) < request.getAmount()) return false;
        }
        return true;
    }

    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            if (containsKey(request.getItemStack())) {
                int amount = get(request.getItemStack());
                if (amount >= request.getAmount()) {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), request.getAmount());
                    itemStacks.addAll(List.of(tmp));
                    put(request.getItemStack(), amount - request.getAmount());
                } else {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), amount);
                    itemStacks.addAll(List.of(tmp));
                    put(request.getItemStack(), 0);
                }
                trim(request.getItemStack());
            }
        }
        MEItemStorageCell.updateLore(itemStack);
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        return ItemUtils.toStorage(nbt);
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }
}
