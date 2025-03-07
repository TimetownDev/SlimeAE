package me.ddggdd135.slimeae.api.items;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCollection implements IStorage {
    private final Set<IStorage> storages;
    private final Map<ItemStack, IStorage> takeCache;
    private final Map<ItemStack, IStorage> pushCache;
    private final Set<ItemStack> notIncluded;

    public StorageCollection(@Nonnull IStorage... storages) {
        this.storages = new HashSet<>();
        this.takeCache = new HashMap<>();
        this.pushCache = new HashMap<>();
        this.notIncluded = new HashSet<>();
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
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = itemStack.asOne();
            if (pushCache.containsKey(template)) {
                IStorage storage = pushCache.get(template);
                storage.pushItem(itemStack);
            }
        }

        itemStacks = ItemUtils.trimItems(itemStacks);
        if (itemStacks.length == 0) return;

        List<IStorage> tmp = new ArrayList<>(storages);
        List<ObjectIntImmutablePair<IStorage>> sorted = new ArrayList<>(tmp.size());

        // 计算每个 storage 的 tier 并缓存结果
        for (IStorage storage : tmp) {
            int totalTier = 0;
            for (ItemStack itemStack : itemStacks) {
                totalTier += storage.getTier(itemStack);
            }
            if (totalTier < 0) continue;
            sorted.add(new ObjectIntImmutablePair<>(storage, totalTier));
        }

        // 根据缓存的 tier 排序
        sorted.sort(Comparator.<ObjectIntImmutablePair<IStorage>>comparingInt(ObjectIntImmutablePair::rightInt)
                .reversed());

        for (ObjectIntImmutablePair<IStorage> storage : sorted) {
            storage.left().pushItem(itemStacks);
            itemStacks = ItemUtils.trimItems(itemStacks);
            if (itemStacks.length == 0) return;
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        Map<ItemStack, Long> storage = getStorage();
        for (ItemRequest request : requests) {
            if (notIncluded.contains(request.getTemplate())) return false;
            if (!ItemUtils.contains(storage, request)) {
                notIncluded.add(request.getTemplate());
                return false;
            }
        }
        return requests.length != 0;
    }

    @Nonnull
    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        Map<ItemStack, Long> rest = new HashMap<>();
        ItemStorage found = new ItemStorage();
        // init rest
        for (ItemRequest request : requests) {
            if (notIncluded.contains(request.getTemplate())) continue;
            if (rest.containsKey(request.getTemplate())) {
                rest.put(request.getTemplate(), rest.get(request.getTemplate()) + request.getAmount());
            } else {
                rest.put(request.getTemplate(), request.getAmount());
            }
        }
        ItemUtils.trim(rest);
        for (Map.Entry<ItemStack, Long> entry : rest.entrySet()) {
            if (takeCache.containsKey(entry.getKey())) {
                IStorage storage = takeCache.get(entry.getKey());
                ItemStack[] itemStacks = storage.tryTakeItem(ItemUtils.createRequests(rest));
                rest = ItemUtils.takeItems(rest, ItemUtils.getAmounts(itemStacks));
                ItemUtils.trim(rest);
                found.addItem(itemStacks);
                if (rest.keySet().isEmpty()) break;
            }
        }

        for (IStorage storage : storages) {
            ItemStack[] itemStacks = storage.tryTakeItem(ItemUtils.createRequests(rest));
            for (ItemStack itemStack : itemStacks) {
                if (itemStack != null && !itemStack.getType().isAir()) {
                    takeCache.put(itemStack.asOne(), storage);
                }
            }
            rest = ItemUtils.takeItems(rest, ItemUtils.getAmounts(itemStacks));
            ItemUtils.trim(rest);
            found.addItem(itemStacks);
            if (rest.keySet().isEmpty()) break;
        }
        notIncluded.addAll(rest.keySet());

        return found.toItemStacks();
    }

    @Override
    public @Nonnull Map<ItemStack, Long> getStorage() {
        Map<ItemStack, Long> result = new HashMap<>();
        for (IStorage storage : storages) {
            Map<ItemStack, Long> tmp = storage.getStorage();
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
    public int getTier(@Nonnull ItemStack itemStack) {
        int tier = Integer.MIN_VALUE;
        for (IStorage storage : storages) {
            tier = Math.max(tier, storage.getTier(itemStack));
        }

        return tier;
    }
}
