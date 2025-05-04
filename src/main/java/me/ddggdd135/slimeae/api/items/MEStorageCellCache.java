package me.ddggdd135.slimeae.api.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        cache.put(storageData.getUuid(), this);
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

        return new MEStorageCellCache(
                SlimeAEPlugin.getStorageCellStorageDataController().loadData(itemStack),
                SlimeAEPlugin.getStorageCellFilterDataController().loadData(itemStack));
    }

    @Nullable public static MEStorageCellCache getMEStorageCellCache(@Nonnull UUID uuid) {
        return cache.getOrDefault(uuid, null);
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
            SlimeAEPlugin.getStorageCellStorageDataController().markDirty(storageData);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
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
        itemStack.setAmount((int) (itemStack.getAmount() - toAdd));
        trim(key);
    }

    @Override
    public void pushItem(@Nonnull ItemInfo itemInfo) {
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
        itemInfo.setAmount((int) (itemInfo.getAmount() - toAdd));
        trim(key);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> storages = storageData.getStorage();

        if (storages instanceof CreativeItemMap) return true;

        for (ItemRequest request : requests) {
            if (!storages.containsKey(request.getKey())
                    || storages.getOrDefault(request.getKey(), 0L) < request.getAmount()) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> storages = storageData.getStorage();
        long stored = storageData.getStored();

        if (storages instanceof CreativeItemMap) {
            return new ItemStorage(ItemUtils.getAmounts(requests));
        }

        ItemStorage itemStacks = new ItemStorage();
        for (ItemRequest request : requests) {
            if (storages.containsKey(request.getKey())) {
                long amount = storages.getKey(request.getKey());
                if (amount >= request.getAmount()) {
                    itemStacks.addItem(request.getKey(), request.getAmount());
                    stored -= request.getAmount();
                    storages.putKey(request.getKey(), amount - request.getAmount());
                } else {
                    itemStacks.addItem(request.getKey(), amount);
                    stored -= storages.getKey(request.getKey());
                    storages.putKey(request.getKey(), 0L);
                }
                SlimeAEPlugin.getStorageCellStorageDataController().markDirty(storageData);
                trim(request.getKey());
            }
        }
        storageData.setStored(stored);

        return itemStacks;
    }

    @Nonnull
    @Unsafe
    public ItemHashMap<Long> getStorageUnsafe() {
        return storageData.getStorage();
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
