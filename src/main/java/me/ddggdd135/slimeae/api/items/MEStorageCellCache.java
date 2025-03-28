package me.ddggdd135.slimeae.api.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.slimefun.MECreativeItemStorageCell;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.utils.ShulkerBoxUtils;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellCache implements IStorage {
    private static final Map<UUID, MEStorageCellCache> cache = new ConcurrentHashMap<>();
    private final ItemHashMap<Long> storages;
    private long stored;
    private final long size;
    private final UUID uuid;

    public MEStorageCellCache(ItemStack itemStack) {
        if (MEItemStorageCell.getSize(itemStack) == 0) throw new RuntimeException("ItemStack is not MEItemStorageCell");
        size = MEItemStorageCell.getSize(itemStack);
        if (SlimefunItem.getByItem(itemStack) instanceof MECreativeItemStorageCell) storages = new CreativeItemMap();
        else {
            storages = new ItemHashMap<>();
        }
        UUID tmp = NBT.get(itemStack, x -> {
            return x.getUUID(MEItemStorageCell.UUID_KEY);
        });
        if (tmp == null) {
            tmp = UUID.randomUUID();
            UUID finalTmp = tmp;
            NBT.modify(itemStack, x -> {
                x.setUUID(MEItemStorageCell.UUID_KEY, finalTmp);
            });
        }
        uuid = tmp;
        cache.put(uuid, this);
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

        return SlimeAEPlugin.getStorageCellDataController().loadData(itemStack);
    }

    @Nullable public static MEStorageCellCache getMEStorageCellCache(UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    public long getSize() {
        return size;
    }

    public long getStored() {
        return stored;
    }

    private void trim(@Nonnull ItemKey key) {
        if (storages.containsKey(key) && storages.getKey(key) == 0) {
            storages.removeKey(key);
            SlimeAEPlugin.getStorageCellDataController().markDirty(this);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStack itemStack) {
        if (storages instanceof CreativeItemMap) {
            itemStack.setAmount(0);
            return;
        }
        if (SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                || (ShulkerBoxUtils.isShulkerBox(itemStack) && !ShulkerBoxUtils.isEmpty(itemStack))) return;
        ItemKey key = new ItemKey(itemStack.asOne());
        long amount = storages.getOrDefault(key, 0L);
        long toAdd;
        if (stored + itemStack.getAmount() > size) toAdd = size - stored;
        else toAdd = itemStack.getAmount();
        stored += toAdd;
        storages.putKey(key, amount + toAdd);
        SlimeAEPlugin.getStorageCellDataController().markDirty(this);
        itemStack.setAmount((int) (itemStack.getAmount() - toAdd));
        trim(key);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (storages instanceof CreativeItemMap) return true;

        for (ItemRequest request : requests) {
            if (!storages.containsKey(request.getKey())
                    || storages.getOrDefault(request.getKey(), 0L) < request.getAmount()) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStorage tryTakeItem(@Nonnull ItemRequest[] requests) {
        if (storages instanceof CreativeItemMap) {
            return new ItemStorage(storages);
        }

        ItemStorage itemStacks = new ItemStorage();
        for (ItemRequest request : requests) {
            if (storages.containsKey(request.getKey())) {
                long amount = storages.getKey(request.getKey());
                if (amount >= request.getAmount()) {
                    itemStacks.addItem(request.getKey(), request.getAmount());
                    stored -= request.getAmount();
                    storages.putKey(request.getKey(), amount - request.getAmount());
                    SlimeAEPlugin.getStorageCellDataController().markDirty(this);
                } else {
                    itemStacks.addItem(request.getKey(), amount);
                    stored -= storages.getKey(request.getKey());
                    storages.putKey(request.getKey(), 0L);
                }
                trim(request.getKey());
            }
        }
        return itemStacks;
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorage() {
        if (storages instanceof CreativeItemMap) return storages;
        return new ItemHashMap<>(storages);
    }

    public ItemHashMap<Long> getSourceStorage() {
        return storages;
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

    @Override
    public int getTier(@Nonnull ItemKey key) {
        if (storages.containsKey(key)) return 1000;

        return 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void updateStored(long stored) {
        this.stored = stored;
    }
}
