package me.ddggdd135.slimeae.api;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCollection implements IStorage {
    private final List<IStorage> storages;
    private final Map<ItemStack, IStorage> takeCache;
    private final Map<ItemStack, IStorage> pushCache;
    private final Set<ItemStack> notIncluded;

    public StorageCollection(@Nonnull IStorage... storages) {
        this.storages = new ArrayList<>(List.of(storages));
        this.takeCache = new HashMap<>();
        this.pushCache = new HashMap<>();
        this.notIncluded = new HashSet<>();
    }

    public void addStorage(@Nullable IStorage storage) {
        if (storage == null) return;
        storages.add(storage);
        notIncluded.clear();
    }

    public boolean removeStorage(@Nonnull IStorage storage) {
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
        ItemStack[] finalItemStacks = itemStacks;
        List<IStorage> sorted = new ArrayList<>(storages.stream()
                .sorted(Comparator.comparing(
                        x -> {
                            int tierx = 0;
                            for (ItemStack itemStack : finalItemStacks) {
                                tierx += x.getTier(itemStack);
                            }

                            return tierx;
                        },
                        Integer::compare))
                .toList());
        Collections.reverse(sorted);
        for (IStorage storage : sorted) {
            storage.pushItem(itemStacks);
            for (ItemStack itemStack : itemStacks) {
                if (itemStack.getAmount() == 0 && !itemStack.getType().isAir()) {
                    pushCache.put(itemStack.asOne(), storage);
                }
            }
            itemStacks = ItemUtils.trimItems(itemStacks);
            if (itemStacks.length == 0) return;
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        Map<ItemStack, Integer> storage = getStorage();
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
        Map<ItemStack, Integer> rest = new HashMap<>();
        ItemStorage found = new ItemStorage();
        // init rest
        for (ItemRequest request : requests) {
            if (rest.containsKey(request.getTemplate())) {
                rest.put(request.getTemplate(), rest.get(request.getTemplate()) + request.getAmount());
            } else {
                rest.put(request.getTemplate(), request.getAmount());
            }
        }
        ItemUtils.trim(rest);
        for (Map.Entry<ItemStack, Integer> entry : rest.entrySet()) {
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
                    takeCache.put(itemStack, storage);
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
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (IStorage storage : storages) {
            Map<ItemStack, Integer> tmp = storage.getStorage();
            if (tmp instanceof CreativeItemIntegerMap) return tmp;
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

    @Override
    public int getTier(@Nonnull ItemStack itemStack) {
        int tier = Integer.MIN_VALUE;
        for (IStorage storage : storages) {
            tier = Math.max(tier, storage.getTier(itemStack));
        }

        return tier;
    }
}
