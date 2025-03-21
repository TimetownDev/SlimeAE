package me.ddggdd135.slimeae.api.items;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCollection implements IStorage {
    private final Set<IStorage> storages;
    private final ItemHashMap<IStorage> takeCache;
    private final ItemHashMap<IStorage> pushCache;
    private final ItemHashSet notIncluded;

    public StorageCollection(@Nonnull IStorage... storages) {
        this.storages = new ConcurrentHashSet<>();
        this.takeCache = new ItemHashMap<>();
        this.pushCache = new ItemHashMap<>();
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
            return;
        }
        storages.add(storage);
        notIncluded.clear();
    }

    public boolean removeStorage(@Nonnull IStorage storage) {
        if (storage instanceof StorageCollection storageCollection) {
            boolean result = false;
            for (IStorage iStorage : storageCollection.getStorages()) {
                result |= removeStorage(iStorage);
            }

            return result;
        }
        Map.Entry<ItemStack, IStorage> toRemove = null;
        for (Map.Entry<ItemStack, IStorage> entry : takeCache.entrySet()) {
            if (entry.getValue() == storage) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            takeCache.remove(toRemove.getKey());
        }

        toRemove = null;
        for (Map.Entry<ItemStack, IStorage> entry : pushCache.entrySet()) {
            if (entry.getValue() == storage) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            pushCache.remove(toRemove.getKey());
        }

        return storages.remove(storage);
    }

    @Override
    public void pushItem(@Nonnull ItemStack itemStack) {
        ItemKey key = new ItemKey(itemStack);

        IStorage pushStorage = pushCache.getKey(key);
        if (pushStorage != null) pushStorage.pushItem(itemStack);

        if (itemStack.isEmpty()) return;

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
            storage.left().pushItem(itemStack);
            if (itemStack.isEmpty()) return;
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> storage = getStorage();
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
    @Override
    public ItemStorage tryTakeItem(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> rest = new ItemHashMap<>();
        ItemStorage found = new ItemStorage();
        // init rest
        for (ItemRequest request : requests) {
            if (notIncluded.contains(request.getKey())) continue;

            long amount = rest.getOrDefault(request.getKey(), 0L);
            amount += request.getAmount();
            rest.putKey(request.getKey(), amount);
        }
        ItemUtils.trim(rest);
        for (Map.Entry<ItemStack, Long> entry : rest.entrySet()) {
            if (takeCache.containsKey(entry.getKey())) {
                IStorage storage = takeCache.get(entry.getKey());
                ItemStorage itemStacks = storage.tryTakeItem(ItemUtils.createRequests(rest));
                found.addItem(itemStacks.getStorage());
                if (rest.keySet().isEmpty()) break;
            }
        }

        for (IStorage storage : storages) {
            ItemStorage itemStacks = storage.tryTakeItem(ItemUtils.createRequests(rest));
            for (ItemKey key : itemStacks.getStorage().sourceKeySet()) {
                ItemStack itemStack = key.getItemStack();
                if (itemStack != null && !itemStack.getType().isAir()) {
                    takeCache.putKey(key, storage);
                }
            }

            found.addItem(itemStacks.getStorage());
            if (rest.keySet().isEmpty()) break;
        }
        notIncluded.addAll(rest.sourceKeySet());

        return found;
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorage() {
        ItemHashMap<Long> result = new ItemHashMap<>();
        for (IStorage storage : storages) {
            ItemHashMap<Long> tmp = storage.getStorage();
            if (tmp instanceof CreativeItemMap) return tmp;
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
    public int getTier(@Nonnull ItemKey itemStack) {
        int tier = Integer.MIN_VALUE;
        for (IStorage storage : storages) {
            tier = Math.max(tier, storage.getTier(itemStack));
        }

        return tier;
    }
}
