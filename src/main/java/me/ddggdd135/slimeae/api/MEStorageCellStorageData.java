package me.ddggdd135.slimeae.api;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.items.CreativeItemMap;
import me.ddggdd135.slimeae.core.slimefun.MECreativeItemStorageCell;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellStorageData {
    private static final Map<UUID, MEStorageCellStorageData> cache = new ConcurrentHashMap<>();
    private UUID uuid;
    private ItemHashMap<Long> storages;
    private long stored;
    private long size;

    public MEStorageCellStorageData() {}

    public MEStorageCellStorageData(@Nonnull ItemStack itemStack) {
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

    public UUID getUuid() {
        return uuid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Nonnull
    public ItemHashMap<Long> getStorage() {
        return storages;
    }

    public void setStorages(@Nonnull ItemHashMap<Long> storages) {
        this.storages = storages;
    }

    public long getStored() {
        return stored;
    }

    public void setStored(long stored) {
        this.stored = stored;
    }

    @Nullable public static MEStorageCellStorageData getMEStorageCellStorageData(@Nonnull UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    @Nullable public static MEStorageCellStorageData getMEStorageCellStorageData(@Nonnull ItemStack itemStack) {
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
            if (getMEStorageCellStorageData(uuid) != null) return getMEStorageCellStorageData(uuid);
        }

        return SlimeAEPlugin.getStorageCellStorageDataController().loadData(itemStack);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MEStorageCellStorageData that)) return false;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
