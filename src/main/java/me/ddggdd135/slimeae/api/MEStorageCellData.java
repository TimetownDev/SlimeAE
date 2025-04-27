package me.ddggdd135.slimeae.api;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.api.items.CreativeItemMap;
import me.ddggdd135.slimeae.core.slimefun.MECreativeItemStorageCell;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellData {
    private static final Map<UUID, MEStorageCellData> cache = new ConcurrentHashMap<>();
    private ItemHashMap<Long> storages;
    private long stored;
    private long size;
    private UUID uuid;

    public MEStorageCellData() {}

    public MEStorageCellData(@Nonnull ItemStack itemStack) {
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

    public void setUuid(@Nonnull UUID uuid) {
        this.uuid = uuid;
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

    @Nullable public static MEStorageCellData getMEStorageCellData(@Nonnull UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }
}
