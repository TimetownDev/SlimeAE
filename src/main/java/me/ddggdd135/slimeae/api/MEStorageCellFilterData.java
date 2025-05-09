package me.ddggdd135.slimeae.api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemType;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellFilterData {
    private static final Map<UUID, MEStorageCellFilterData> cache = new ConcurrentHashMap<>();
    private UUID uuid;
    private ItemHashSet filters = new ItemHashSet();
    private Set<ItemType> filterItemTypes = new HashSet<>();
    private boolean isReversed;
    private boolean isFuzzy;

    public MEStorageCellFilterData(@Nonnull ItemStack itemStack) {
        if (MEItemStorageCell.getSize(itemStack) == 0) throw new RuntimeException("ItemStack is not MEItemStorageCell");

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

    /*
     * 允许存入则返回true
     */
    public boolean matches(@Nonnull ItemKey itemKey) {
        if (filters.isEmpty()) return true;

        if (!isFuzzy) {
            if (!isReversed) return filters.contains(itemKey);
            return !filters.contains(itemKey);
        }

        if (!isReversed) return filterItemTypes.contains(itemKey.getType());
        return !filterItemTypes.contains(itemKey.getType());
    }

    @Nonnull
    public UUID getUuid() {
        return uuid;
    }

    public boolean isReversed() {
        return isReversed;
    }

    @Nonnull
    public ItemHashSet getFilters() {
        return filters;
    }

    public void setFilters(@Nonnull ItemHashSet filters) {
        this.filters = filters;
    }

    @Nonnull
    public Set<ItemType> getFilterItemTypes() {
        return filterItemTypes;
    }

    public void setFilterItemTypes(@Nonnull Set<ItemType> filterItemTypes) {
        this.filterItemTypes = filterItemTypes;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MEStorageCellFilterData that)) return false;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Nullable public static MEStorageCellFilterData getMEStorageCellFilterData(@Nonnull UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    @Nullable public static MEStorageCellFilterData getMEStorageCellFilterData(@Nonnull ItemStack itemStack) {
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
            if (getMEStorageCellFilterData(uuid) != null) return getMEStorageCellFilterData(uuid);
        }

        return SlimeAEPlugin.getStorageCellFilterDataController().loadData(itemStack);
    }

    public boolean isFuzzy() {
        return isFuzzy;
    }

    public void setFuzzy(boolean fuzzy) {
        isFuzzy = fuzzy;
    }

    public void updateItemTypes() {
        filterItemTypes.clear();

        for (ItemKey itemKey : filters) {
            filterItemTypes.add(itemKey.getType());
        }
    }
}
