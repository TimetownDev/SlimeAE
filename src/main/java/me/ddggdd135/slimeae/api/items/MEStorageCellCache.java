package me.ddggdd135.slimeae.api.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.MEStorageCellFilterData;
import me.ddggdd135.slimeae.api.MEStorageCellStorageData;
import me.ddggdd135.slimeae.api.annotation.Unsafe;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.ShulkerBoxUtils;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellCache implements IStorage {
    private static final Map<UUID, MEStorageCellCache> cache = new ConcurrentHashMap<>();
    private static volatile MEStorageCellCache creativeInstance;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final MEStorageCellStorageData storageData;
    private final MEStorageCellFilterData filterData;

    public MEStorageCellCache(ItemStack itemStack) {
        storageData = MEStorageCellStorageData.getMEStorageCellStorageData(itemStack);
        filterData = MEStorageCellFilterData.getMEStorageCellFilterData(itemStack);
        cache.put(storageData.getUuid(), this);
    }

    public MEStorageCellCache(
            @Nonnull MEStorageCellStorageData storageData, @Nonnull MEStorageCellFilterData filterData) {
        this.storageData = storageData;
        this.filterData = filterData;
        if (storageData.getUuid() != null) {
            cache.put(storageData.getUuid(), this);
        }
    }

    @Nonnull
    public MEStorageCellStorageData getStorageData() {
        return storageData;
    }

    @Nullable public static MEStorageCellCache getMEStorageCellCache(@Nonnull ItemStack itemStack) {
        if (!MEItemStorageCell.isCurrentServer(itemStack)) {
            return null;
        }

        UUID uuid = UUID.randomUUID();
        if (!NBT.get(itemStack, x -> {
            return x.hasTag(MEItemStorageCell.UUID_KEY, NBTType.NBTTagIntArray);
        })) {
            UUID finalUuid = uuid;
            NBT.modify(itemStack, x -> {
                x.setUUID(MEItemStorageCell.UUID_KEY, finalUuid);
            });
        } else {
            uuid = NBT.get(itemStack, x -> {
                return x.getUUID(MEItemStorageCell.UUID_KEY);
            });
            if (getMEStorageCellCache(uuid) != null) return getMEStorageCellCache(uuid);
        }

        return new MEStorageCellCache(itemStack);
    }

    @Nullable public static MEStorageCellCache getMEStorageCellCache(@Nonnull UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    @Nonnull
    public static MEStorageCellCache getCreativeInstance() {
        MEStorageCellCache inst = creativeInstance;
        if (inst != null) return inst;
        synchronized (MEStorageCellCache.class) {
            inst = creativeInstance;
            if (inst != null) return inst;
            MEStorageCellStorageData data = new MEStorageCellStorageData();
            data.setSize(Integer.MAX_VALUE);
            data.setStorages(new CreativeItemMap());
            MEStorageCellFilterData filter = new MEStorageCellFilterData();
            inst = new MEStorageCellCache(data, filter);
            creativeInstance = inst;
            return inst;
        }
    }

    public long getSize() {
        return storageData.getSize();
    }

    public long getStored() {
        return storageData.getStored();
    }

    private void trim(@Nonnull ItemKey key) {
        ItemHashMap<Long> storages = storageData.getStorage();

        if (storages.containsKey(key) && storages.getKey(key) == 0) {
            storages.removeKey(key);
            SlimeAEPlugin.getStorageCellStorageDataController().markDirty(storageData, key, 0);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        ItemKey dirtyKey = null;
        long dirtyAmount = 0;
        boolean needsDirty = false;

        rwLock.writeLock().lock();
        try {
            ItemStack itemStack = itemStackCache.getItemStack();
            ItemKey key = itemStackCache.getItemKey();
            ItemHashMap<Long> storages = storageData.getStorage();
            long stored = storageData.getStored();
            long size = storageData.getSize();

            if (storages instanceof CreativeItemMap) {
                itemStack.setAmount(0);
                return;
            }

            if (!filterData.matches(key)) return;

            if (SlimefunItem.getById(itemStackCache.getItemKey().getType().getId()) instanceof MEItemStorageCell
                    || (ShulkerBoxUtils.isShulkerBox(itemStack) && !ShulkerBoxUtils.isEmpty(itemStack))) return;

            long amount = storages.getOrDefault(key, 0L);
            long toAdd;
            if (stored + itemStack.getAmount() > size) toAdd = size - stored;
            else toAdd = itemStack.getAmount();
            stored += toAdd;
            storageData.setStored(stored);
            storages.putKey(key, amount + toAdd);
            dirtyKey = key;
            dirtyAmount = amount + toAdd;
            needsDirty = true;
            itemStack.setAmount((int) (itemStack.getAmount() - toAdd));
            trim(key);
        } finally {
            rwLock.writeLock().unlock();
        }

        if (needsDirty) {
            SlimeAEPlugin.getStorageCellStorageDataController().markDirty(storageData, dirtyKey, dirtyAmount);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemInfo itemInfo) {
        ItemKey dirtyKey = null;
        long dirtyAmount = 0;
        boolean needsDirty = false;

        rwLock.writeLock().lock();
        try {
            ItemKey key = itemInfo.getItemKey();
            ItemHashMap<Long> storages = storageData.getStorage();
            long stored = storageData.getStored();
            long size = storageData.getSize();

            if (storages instanceof CreativeItemMap) {
                itemInfo.setAmount(0);
                return;
            }

            ItemStack itemStack = key.getItemStack();

            if (!filterData.matches(key)) return;

            if (SlimefunItem.getById(key.getType().getId()) instanceof MEItemStorageCell
                    || (ShulkerBoxUtils.isShulkerBox(itemStack) && !ShulkerBoxUtils.isEmpty(itemStack))) return;

            long amount = storages.getOrDefault(key, 0L);
            long toAdd;
            if (stored + itemInfo.getAmount() > size) toAdd = size - stored;
            else toAdd = itemInfo.getAmount();
            stored += toAdd;
            storageData.setStored(stored);
            storages.putKey(key, amount + toAdd);
            itemInfo.setAmount(itemInfo.getAmount() - toAdd);
            dirtyKey = key;
            dirtyAmount = amount + toAdd;
            needsDirty = true;
            trim(key);
        } finally {
            rwLock.writeLock().unlock();
        }

        if (needsDirty) {
            SlimeAEPlugin.getStorageCellStorageDataController().markDirty(storageData, dirtyKey, dirtyAmount);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        rwLock.readLock().lock();
        try {
            ItemHashMap<Long> storages = storageData.getStorage();

            if (storages instanceof CreativeItemMap) return true;

            for (ItemRequest request : requests) {
                if (!storages.containsKey(request.getKey())
                        || storages.getOrDefault(request.getKey(), 0L) < request.getAmount()) return false;
            }
            return true;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        List<Map.Entry<ItemKey, Long>> dirtyBatch = null;
        ItemStorage itemStacks;

        rwLock.writeLock().lock();
        try {
            ItemHashMap<Long> storages = storageData.getStorage();
            long stored = storageData.getStored();

            if (storages instanceof CreativeItemMap) {
                return new ItemStorage(ItemUtils.getAmounts(requests));
            }

            itemStacks = new ItemStorage();
            dirtyBatch = new ArrayList<>();
            for (ItemRequest request : requests) {
                if (storages.containsKey(request.getKey())) {
                    long amount = storages.getKey(request.getKey());
                    if (amount >= request.getAmount()) {
                        itemStacks.addItem(request.getKey(), request.getAmount());
                        stored -= request.getAmount();
                        storages.putKey(request.getKey(), amount - request.getAmount());
                    } else if (amount > 0) {
                        itemStacks.addItem(request.getKey(), amount);
                        stored -= amount;
                        storages.removeKey(request.getKey());
                    }

                    long remaining = storages.getOrDefault(request.getKey(), 0L);
                    dirtyBatch.add(Map.entry(request.getKey(), remaining));
                    trim(request.getKey());
                }
            }
            storageData.setStored(stored);
        } finally {
            rwLock.writeLock().unlock();
        }

        if (dirtyBatch != null) {
            for (Map.Entry<ItemKey, Long> entry : dirtyBatch) {
                SlimeAEPlugin.getStorageCellStorageDataController()
                        .markDirty(storageData, entry.getKey(), entry.getValue());
            }
        }

        return itemStacks;
    }

    @Nonnull
    @Unsafe
    public ItemHashMap<Long> getStorageUnsafe() {
        rwLock.readLock().lock();
        try {
            return storageData.getStorage();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MEStorageCellCache cellCache = (MEStorageCellCache) o;
        return Objects.equals(storageData, cellCache.storageData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageData);
    }

    @Override
    public int getTier(@Nonnull ItemKey key) {
        if (storageData.getStorage().containsKey(key)) return 1000;

        return 0;
    }

    public UUID getUuid() {
        return storageData.getUuid();
    }

    public void setStored(long stored) {
        storageData.setStored(stored);
    }

    @Nonnull
    public MEStorageCellFilterData getFilterData() {
        return filterData;
    }
}
