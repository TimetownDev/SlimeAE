package me.ddggdd135.slimeae.api.items;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCollection implements IStorage {
    private final Set<IStorage> storages;
    private final Map<ItemKey, IStorage> takeCache;
    private final Map<ItemKey, IStorage> pushCache;
    private final ItemHashSet notIncluded;

    private volatile ItemHashMap<Long> cachedStorage = null;
    private volatile Map<ItemKey, List<IStorage>> itemToStorageIndex = null;
    private volatile long lastCacheTime = 0;
    private static final long STORAGE_CACHE_INTERVAL = 200;
    private volatile long changeVersion = 0;
    private volatile long cachedVersion = -1;

    public StorageCollection(@Nonnull IStorage... storages) {
        this.storages = new ConcurrentHashSet<>();
        this.takeCache = new ConcurrentHashMap<>();
        this.pushCache = new ConcurrentHashMap<>();
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
        takeCache.values().removeIf(v -> v == storage);
        pushCache.values().removeIf(v -> v == storage);

        boolean removed = storages.remove(storage);
        if (removed) invalidateStorageCache();
        return removed;
    }

    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        ItemStack itemStack = itemStackCache.getItemStack();
        ItemKey key = itemStackCache.getItemKey();

        if (notIncluded.contains(key)) notIncluded.remove(key);

        int amountBefore = itemStack.getAmount();
        IStorage pushStorage = pushCache.get(key);
        if (pushStorage != null) {
            pushStorage.pushItem(itemStackCache);
            int pushed = amountBefore - itemStack.getAmount();
            if (pushed > 0) adjustCache(key, pushed);
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
            int beforeAmount = itemStack.getAmount();
            storage.left().pushItem(itemStackCache);
            pushCache.put(key, storage.left());
            int pushed = beforeAmount - itemStack.getAmount();
            if (pushed > 0) adjustCache(key, pushed);
            if (itemStack.getType().isAir() || itemStack.getAmount() == 0) return;
        }
    }

    public void pushItem(@Nonnull ItemInfo itemInfo) {
        ItemKey key = itemInfo.getItemKey();

        if (notIncluded.contains(key)) notIncluded.remove(key);

        long amountBefore = itemInfo.getAmount();
        IStorage pushStorage = pushCache.get(key);
        if (pushStorage != null) {
            pushStorage.pushItem(itemInfo);
            long pushed = amountBefore - itemInfo.getAmount();
            if (pushed > 0) adjustCache(key, pushed);
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
            long beforeAmount = itemInfo.getAmount();
            storage.left().pushItem(itemInfo);
            pushCache.put(key, storage.left());
            long pushed = beforeAmount - itemInfo.getAmount();
            if (pushed > 0) adjustCache(key, pushed);
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

        long[] remaining = new long[requests.length];
        for (int i = 0; i < requests.length; i++) {
            remaining[i] = requests[i].getAmount();
        }

        Map<IStorage, List<int[]>> cacheGroups = new HashMap<>();
        for (int i = 0; i < requests.length; i++) {
            if (remaining[i] <= 0) continue;
            IStorage cached = takeCache.get(requests[i].getKey());
            if (cached == null) continue;
            cacheGroups.computeIfAbsent(cached, k -> new ArrayList<>()).add(new int[] {i});
        }
        for (Map.Entry<IStorage, List<int[]>> group : cacheGroups.entrySet()) {
            List<int[]> idxList = group.getValue();
            ItemRequest[] subReqs = new ItemRequest[idxList.size()];
            for (int j = 0; j < idxList.size(); j++) {
                int idx = idxList.get(j)[0];
                subReqs[j] = new ItemRequest(requests[idx].getKey(), remaining[idx]);
            }
            ItemStorage result = group.getKey().takeItem(subReqs);
            ItemHashMap<Long> resultMap = result.getStorageUnsafe();
            for (int j = 0; j < idxList.size(); j++) {
                int idx = idxList.get(j)[0];
                long takenAmount = resultMap.getOrDefault(requests[idx].getKey(), 0L);
                if (takenAmount > 0) {
                    found.addItem(requests[idx].getKey(), takenAmount);
                    remaining[idx] -= takenAmount;
                }
            }
        }

        boolean allSatisfied = true;
        for (long r : remaining) {
            if (r > 0) {
                allSatisfied = false;
                break;
            }
        }

        if (!allSatisfied) {
            Map<ItemKey, List<IStorage>> index = itemToStorageIndex;
            Set<IStorage> relevantStorages = null;
            if (index != null) {
                relevantStorages = new LinkedHashSet<>();
                for (int i = 0; i < requests.length; i++) {
                    if (remaining[i] <= 0) continue;
                    List<IStorage> candidates = index.get(requests[i].getKey());
                    if (candidates != null) relevantStorages.addAll(candidates);
                }
            }
            Iterable<IStorage> toScan = relevantStorages != null ? relevantStorages : storages;
            for (IStorage storage : toScan) {
                List<ItemRequest> subRequests = new ArrayList<>();
                int[] indices = new int[requests.length];
                int subCount = 0;
                for (int i = 0; i < requests.length; i++) {
                    if (remaining[i] > 0) {
                        indices[subCount] = i;
                        subRequests.add(new ItemRequest(requests[i].getKey(), remaining[i]));
                        subCount++;
                    }
                }
                if (subRequests.isEmpty()) break;

                ItemStorage result = storage.takeItem(subRequests.toArray(new ItemRequest[0]));
                ItemHashMap<Long> resultStorage = result.getStorageUnsafe();

                for (int j = 0; j < subCount; j++) {
                    int idx = indices[j];
                    Long takenAmount = resultStorage.getKey(requests[idx].getKey());
                    if (takenAmount != null && takenAmount > 0) {
                        found.addItem(requests[idx].getKey(), takenAmount);
                        remaining[idx] -= takenAmount;
                        takeCache.put(requests[idx].getKey(), storage);
                    }
                }

                allSatisfied = true;
                for (long r : remaining) {
                    if (r > 0) {
                        allSatisfied = false;
                        break;
                    }
                }
                if (allSatisfied) break;
            }

            if (!allSatisfied && relevantStorages != null) {
                for (IStorage storage : storages) {
                    if (relevantStorages.contains(storage)) continue;
                    List<ItemRequest> subRequests = new ArrayList<>();
                    int[] indices = new int[requests.length];
                    int subCount = 0;
                    for (int i = 0; i < requests.length; i++) {
                        if (remaining[i] > 0) {
                            indices[subCount] = i;
                            subRequests.add(new ItemRequest(requests[i].getKey(), remaining[i]));
                            subCount++;
                        }
                    }
                    if (subRequests.isEmpty()) break;

                    ItemStorage result = storage.takeItem(subRequests.toArray(new ItemRequest[0]));
                    ItemHashMap<Long> resultStorage = result.getStorageUnsafe();

                    for (int j = 0; j < subCount; j++) {
                        int idx = indices[j];
                        Long takenAmount = resultStorage.getKey(requests[idx].getKey());
                        if (takenAmount != null && takenAmount > 0) {
                            found.addItem(requests[idx].getKey(), takenAmount);
                            remaining[idx] -= takenAmount;
                            takeCache.put(requests[idx].getKey(), storage);
                        }
                    }

                    allSatisfied = true;
                    for (long r : remaining) {
                        if (r > 0) {
                            allSatisfied = false;
                            break;
                        }
                    }
                    if (allSatisfied) break;
                }
            }
        }

        for (int i = 0; i < requests.length; i++) {
            if (remaining[i] > 0) {
                notIncluded.add(requests[i].getKey());
            } else {
                notIncluded.remove(requests[i].getKey());
            }
        }

        ItemHashMap<Long> foundStorage = found.getStorageUnsafe();
        for (ItemKey key : foundStorage.sourceKeySet()) {
            Long takenAmount = foundStorage.getKey(key);
            if (takenAmount != null && takenAmount > 0) {
                adjustCache(key, -takenAmount);
            }
        }

        return found;
    }

    public void invalidateStorageCache() {
        changeVersion++;
    }

    private final Object cacheLock = new Object();

    private void adjustCache(@Nonnull ItemKey key, long delta) {
        synchronized (cacheLock) {
            ItemHashMap<Long> cached = cachedStorage;
            if (cached == null || cached instanceof CreativeItemMap) return;
            Long current = cached.getKey(key);
            long newValue = (current != null ? current : 0L) + delta;
            if (newValue <= 0) {
                cached.removeKey(key);
            } else {
                cached.putKey(key, newValue);
            }
        }
    }

    public void clearNotIncluded() {
        notIncluded.clear();
    }

    public void clearTakeAndPushCache() {
        takeCache.clear();
        pushCache.clear();
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        long currentVersion = changeVersion;
        ItemHashMap<Long> cached = cachedStorage;
        if (cached != null && cachedVersion == currentVersion) {
            long now = System.currentTimeMillis();
            if ((now - lastCacheTime) < STORAGE_CACHE_INTERVAL) {
                return cached;
            }
        }

        ItemHashMap<Long> result = new ItemHashMap<>();
        Map<ItemKey, List<IStorage>> newIndex = new HashMap<>();

        for (IStorage storage : storages) {
            ItemHashMap<Long> tmp = storage.getStorageUnsafe();
            if (tmp instanceof CreativeItemMap) {
                itemToStorageIndex = null;
                return tmp;
            }
            for (ItemKey itemKey : tmp.sourceKeySet()) {
                Long currentValue = tmp.getKey(itemKey);
                if (currentValue == null) {
                    continue;
                }

                newIndex.computeIfAbsent(itemKey, k -> new ArrayList<>()).add(storage);

                Long existingValue = result.getKey(itemKey);
                if (existingValue != null) {
                    result.putKey(itemKey, existingValue + currentValue);
                } else {
                    result.putKey(itemKey, currentValue);
                }
            }
        }

        itemToStorageIndex = newIndex;
        cachedStorage = result;
        lastCacheTime = System.currentTimeMillis();
        cachedVersion = currentVersion;
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
