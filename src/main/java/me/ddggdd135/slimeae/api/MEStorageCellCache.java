package me.ddggdd135.slimeae.api;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class MEStorageCellCache implements IStorage {
    private static final Map<UUID, MEStorageCellCache> cache = new HashMap<>();
    private final Map<ItemStack, Integer> storages = new HashMap<>();
    private int storaged;
    private int size;
    private UUID uuid;
    private MEStorageCellCache(ItemStack itemStack) {
        if (MEItemStorageCell.getSize(itemStack) == 0) throw new RuntimeException("ItemStack is not MEItemStorageCell");
        NBTItem nbtItem = new NBTItem(itemStack, true);
        NBTCompound nbt = nbtItem.getOrCreateCompound("item_storage");
        size = MEItemStorageCell.getSize(itemStack);
        Map<ItemStack, Integer> storage = ItemUtils.toStorage(nbt);
        for (ItemStack key : storage.keySet()) {
            storaged += storage.get(key);
        }
        if (!nbtItem.hasTag("uuid", NBTType.NBTTagIntArray)) nbtItem.setUUID("uuid", UUID.randomUUID());
        uuid = nbtItem.getUUID("uuid");
    }

    private MEStorageCellCache(UUID uuid, int size) {
        this.uuid = uuid;
        this.size = size;
    }

    public static MEStorageCellCache getMEStorageCellCache(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (!nbtItem.hasTag("uuid", NBTType.NBTTagIntArray)) {
            UUID uuid = UUID.randomUUID();
            nbtItem.setUUID("uuid", uuid);
            return new MEStorageCellCache(itemStack);
        } else {
            return getMEStorageCellCache(nbtItem.getUUID("uuid"), MEItemStorageCell.getSize(itemStack));
        }
    }

    public static MEStorageCellCache getMEStorageCellCache(UUID uuid, int size) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        else {
            MEStorageCellCache cellCache = new MEStorageCellCache(uuid, size);
            cache.put(uuid, cellCache);
            return cellCache;
        }
    }

    public int getSize() {
        return size;
    }

    public int getStoraged() {
        return storaged;
    }

    private void trim(@Nonnull ItemStack template) {
        if (storages.containsKey(template) && storages.getOrDefault(template, 0) == 0) {
            storages.remove(template);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemStack);
            if (SlimefunItem.getByItem(template) instanceof MEItemStorageCell) continue;
            int amount = storages.getOrDefault(template, 0);
            int toAdd;
            if (storaged + itemStack.getAmount() > size) toAdd = size - storaged;
            else toAdd = itemStack.getAmount();
            storaged += toAdd;
            storages.put(template, amount + toAdd);
            itemStack.setAmount(itemStack.getAmount() - toAdd);
            trim(template);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        for (ItemRequest request : requests) {
            ItemStack template = ItemUtils.createTemplateItem(request.getItemStack());
            if (!storages.containsKey(template) || storages.getOrDefault(template, 0) < request.getAmount()) return false;
        }
        return true;
    }

    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            if (storages.containsKey(request.getItemStack())) {
                int amount = storages.getOrDefault(request.getItemStack(), 0);
                if (amount >= request.getAmount()) {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), request.getAmount());
                    itemStacks.addAll(List.of(tmp));
                    storages.put(request.getItemStack(), amount - request.getAmount());
                } else {
                    ItemStack[] tmp = ItemUtils.createItems(request.getItemStack(), amount);
                    itemStacks.addAll(List.of(tmp));
                    storages.put(request.getItemStack(), 0);
                }
                trim(request.getItemStack());
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        return new HashMap<>(storages);
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MEStorageCellCache cellCache = (MEStorageCellCache) o;
        return Objects.equals(uuid, cellCache.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
