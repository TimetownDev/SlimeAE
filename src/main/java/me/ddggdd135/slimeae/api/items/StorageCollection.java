package me.ddggdd135.slimeae.api.items;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.guguslimefunlib.items.ItemType;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCollection implements IStorage {
    private final Set<IStorage> storages;
    private final Map<ItemType, IStorage> takeCache;
    private final Map<ItemType, IStorage> pushCache;
    private final ItemHashSet notIncluded;

    // === F3: getStorageUnsafe() 结果缓存 ===
    private volatile ItemHashMap<Long> cachedStorage = null;
    private volatile long lastCacheTime = 0;
    private static final long STORAGE_CACHE_INTERVAL = 500; // 500ms 刷新一次

    public StorageCollection(@Nonnull IStorage... storages) {
        this.storages = new ConcurrentHashSet<>();
        this.takeCache = new HashMap<>();
        this.pushCache = new HashMap<>();
        this.notIncluded = new ItemHashSet();
        for (IStorage storage : storages) {
            addStorage(storage);
        }
    }

    public Set<IStorage> getStorages() {
        return storages;
    }

    public void addStorage(@Nullable IStorage storage) {
        if (storage == null) return;
        if (storage instanceof StorageCollection storageCollection) {
            storages.addAll(storageCollection.getStorages());
            invalidateStorageCache();
            return;
        }
        storages.add(storage);
        notIncluded.clear();
        invalidateStorageCache();
    }

    public boolean removeStorage(@Nonnull IStorage storage) {
        if (storage instanceof StorageCollection storageCollection) {
            boolean result = false;
            for (IStorage iStorage : storageCollection.getStorages()) {
                result |= removeStorage(iStorage);
            }

            return result;
        }
        Map.Entry<ItemType, IStorage> toRemove = null;
        for (Map.Entry<ItemType, IStorage> entry : takeCache.entrySet()) {
            if (entry.getValue() == storage) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            takeCache.remove(toRemove.getKey());
        }

        toRemove = null;
        for (Map.Entry<ItemType, IStorage> entry : pushCache.entrySet()) {
            if (entry.getValue() == storage) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            pushCache.remove(toRemove.getKey());
        }

        boolean removed = storages.remove(storage);
        if (removed) invalidateStorageCache();
        return removed;
    }

    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        ItemStack itemStack = itemStackCache.getItemStack();
        ItemKey key = itemStackCache.getItemKey();

        // 物品被推入时，从负面缓存中移除，因为该物品在存储中已有可用量
        if (notIncluded.contains(key)) notIncluded.remove(key);

        IStorage pushStorage = pushCache.get(key.getType());
        if (pushStorage != null) {
            pushStorage.pushItem(itemStackCache);
            invalidateStorageCache();
        }

        if (itemStack.getType().isAir() || itemStack.getAmount() == 0) return;

        List<IStorage> tmp = new ArrayList<>(storages);
        List<ObjectIntImmutablePair<IStorage>> sorted = new ArrayList<>(tmp.size());

        // 计算每个 storage 的 tier 并缓存结果
        for (IStorage storage : tmp) {
            int totalTier = storage.getTier(key);
            if (totalTier < 0) continue;
            sorted.add(new ObjectIntImmutablePair<>(storage, totalTier));
        }

        // 根据缓存的 tier 排序
        sorted.sort(Comparator.<ObjectIntImmutablePair<IStorage>>comparingInt(ObjectIntImmutablePair::rightInt)
                .reversed());

        for (ObjectIntImmutablePair<IStorage> storage : sorted) {
            storage.left().pushItem(itemStackCache);
            pushCache.put(key.getType(), storage.left());
            invalidateStorageCache();
            if (itemStack.getType().isAir() || itemStack.getAmount() == 0) return;
        }
    }

    public void pushItem(@Nonnull ItemInfo itemInfo) {
        ItemKey key = itemInfo.getItemKey();

        // 物品被推入时，从负面缓存中移除，因为该物品在存储中已有可用量
        if (notIncluded.contains(key)) notIncluded.remove(key);

        IStorage pushStorage = pushCache.get(key.getType());
        if (pushStorage != null) {
            pushStorage.pushItem(itemInfo);
            invalidateStorageCache();
        }

        if (itemInfo.isEmpty()) return;

        List<IStorage> tmp = new ArrayList<>(storages);
        List<ObjectIntImmutablePair<IStorage>> sorted = new ArrayList<>(tmp.size());

        // 计算每个 storage 的 tier 并缓存结果
        for (IStorage storage : tmp) {
            int totalTier = storage.getTier(key);
            if (totalTier < 0) continue;
            sorted.add(new ObjectIntImmutablePair<>(storage, totalTier));
        }

        // 根据缓存的 tier 排序
        sorted.sort(Comparator.<ObjectIntImmutablePair<IStorage>>comparingInt(ObjectIntImmutablePair::rightInt)
                .reversed());

        for (ObjectIntImmutablePair<IStorage> storage : sorted) {
            storage.left().pushItem(itemInfo);
            invalidateStorageCache();
            if (itemInfo.isEmpty()) return;
        }
    }

    public boolean contains(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> storage = getStorageUnsafe();
        for (ItemRequest request : requests) {
            if (notIncluded.contains(request.getKey())) return false;
            if (!ItemUtils.contains(storage, request)) {
                notIncluded.add(request.getKey());
                return false;
            }
        }
        return requests.length != 0;
    }

    @Nonnull
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        ItemStorage found = new ItemStorage();

        ItemHashMap<Long> rest = new ItemHashMap<>();
        // init rest
        for (ItemRequest request : requests) {
            if (notIncluded.contains(request.getKey())) continue;

            long amount = rest.getOrDefault(request.getKey(), 0L);
            amount += request.getAmount();
            rest.putKey(request.getKey(), amount);
        }
        ItemUtils.trim(rest);
        for (Map.Entry<ItemKey, Long> entry : rest.keyEntrySet()) {
            if (takeCache.containsKey(entry.getKey())) {
                IStorage storage = takeCache.get(entry.getKey().getType());
                ItemStorage itemStacks = storage.takeItem(ItemUtils.createRequests(rest));
                found.addItem(itemStacks.getStorageUnsafe());
            }
        }

        rest = ItemUtils.takeItems(rest, found.getStorageUnsafe());
        ItemUtils.trim(rest);
        if (rest.isEmpty()) return found;

        for (IStorage storage : storages) {
            ItemStorage itemStacks = storage.takeItem(ItemUtils.createRequests(rest));
            for (ItemKey key : itemStacks.getStorageUnsafe().sourceKeySet()) {
                ItemStack itemStack = key.getItemStack();
                if (itemStack != null && !itemStack.getType().isAir()) {
                    takeCache.put(key.getType(), storage);
                }
            }

            found.addItem(itemStacks.getStorageUnsafe());
            rest = ItemUtils.takeItems(rest, itemStacks.getStorageUnsafe());
            ItemUtils.trim(rest);
            if (rest.keySet().isEmpty()) break;
        }
        notIncluded.addAll(rest.sourceKeySet());
        invalidateStorageCache();

        return found;
    }

    /**
     * F3: 使存储缓存失效
     */
    public void invalidateStorageCache() {
        cachedStorage = null;
    }

    /**
     * 清除负面缓存（notIncluded 集合）。
     * 当物品通过非 pushItem 路径（如 tempStorage.addItem）归还到子存储后，
     * 需要调用此方法以确保后续 takeItem/contains 不会错误地跳过这些物品。
     */
    public void clearNotIncluded() {
        notIncluded.clear();
    }

    /**
     * 清除 takeCache 和 pushCache 路由缓存。
     * 当存储内容发生重大变化（如 dispose 归还大量物品）时，
     * 需要调用此方法以确保后续 takeItem/pushItem 不会使用过时的路由缓存。
     */
    public void clearTakeAndPushCache() {
        takeCache.clear();
        pushCache.clear();
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        // F3: 时间窗口内复用缓存结果
        long now = System.currentTimeMillis();
        ItemHashMap<Long> cached = cachedStorage;
        if (cached != null && (now - lastCacheTime) < STORAGE_CACHE_INTERVAL) {
            return cached;
        }

        ItemHashMap<Long> result = new ItemHashMap<>();

        for (IStorage storage : storages) {
            ItemHashMap<Long> tmp = storage.getStorageUnsafe();
            if (tmp instanceof CreativeItemMap) return tmp;
            for (ItemKey itemKey : tmp.sourceKeySet()) {
                Long currentValue = tmp.getKey(itemKey);
                if (currentValue == null) {
                    continue;
                }

                Long existingValue = result.getKey(itemKey);
                if (existingValue != null) {
                    result.putKey(itemKey, existingValue + currentValue);
                } else {
                    result.putKey(itemKey, currentValue);
                }
            }
        }

        cachedStorage = result;
        lastCacheTime = now;
        return result;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        int tier = Integer.MIN_VALUE;
        for (IStorage storage : storages) {
            tier = Math.max(tier, storage.getTier(itemStack));
        }

        return tier;
    }
}
