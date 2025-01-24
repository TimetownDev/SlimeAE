package me.ddggdd135.slimeae.api;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTCompoundList;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTItem;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.slimefun.MECreativeItemStorageCell;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.ShulkerBoxUtils;
import org.bukkit.inventory.ItemStack;

public class MEStorageCellCache implements IStorage {
    private static final Map<UUID, MEStorageCellCache> cache = new HashMap<>();
    private final Map<ItemStack, Integer> storages;
    private int stored;
    private final int size;
    private final UUID uuid;

    public MEStorageCellCache(ItemStack itemStack) {
        if (MEItemStorageCell.getSize(itemStack) == 0) throw new RuntimeException("ItemStack is not MEItemStorageCell");
        NBTItem nbtItem = new NBTItem(itemStack, true);
        NBTCompoundList nbt = nbtItem.getCompoundList(MEItemStorageCell.ITEM_STORAGE_KEY);
        size = MEItemStorageCell.getSize(itemStack);
        if (SlimefunItem.getByItem(itemStack) instanceof MECreativeItemStorageCell)
            storages = new CreativeItemIntegerMap();
        else {
            if (nbt != null) {
                storages = ItemUtils.toStorage(nbt);
                for (ItemStack key : storages.keySet()) {
                    stored += storages.get(key);
                }

                nbt.clear();
            } else {
                storages = new HashMap<>();
            }
        }
        if (!nbtItem.hasTag(MEItemStorageCell.UUID_KEY, NBTType.NBTTagIntArray))
            nbtItem.setUUID(MEItemStorageCell.UUID_KEY, UUID.randomUUID());
        uuid = nbtItem.getUUID(MEItemStorageCell.UUID_KEY);
        cache.put(uuid, this);
    }

    public static MEStorageCellCache getMEStorageCellCache(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        UUID uuid = UUID.randomUUID();
        if (!nbtItem.hasTag(MEItemStorageCell.UUID_KEY, NBTType.NBTTagIntArray)) {
            nbtItem.setUUID(MEItemStorageCell.UUID_KEY, uuid);
        } else {
            uuid = nbtItem.getUUID(MEItemStorageCell.UUID_KEY);
            if (getMEStorageCellCache(uuid) != null) return getMEStorageCellCache(uuid);
        }

        return SlimeAEPlugin.getStorageCellDataController().loadData(itemStack);
    }

    @Nullable public static MEStorageCellCache getMEStorageCellCache(UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    public int getSize() {
        return size;
    }

    public int getStored() {
        return stored;
    }

    private void trim(@Nonnull ItemStack template) {
        if (storages.containsKey(template) && storages.getOrDefault(template, 0) == 0) {
            storages.remove(template);
            SlimeAEPlugin.getStorageCellDataController().deleteAsync(this, template);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (storages instanceof CreativeItemIntegerMap) {
            for (ItemStack itemStack : itemStacks) {
                itemStack.setAmount(0);
            }
            return;
        }
        for (ItemStack itemStack : itemStacks) {
            if (SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                    || (ShulkerBoxUtils.isShulkerBox(itemStack) && !ShulkerBoxUtils.isEmpty(itemStack))) continue;
            ItemStack template = itemStack.asOne();
            int amount = storages.getOrDefault(template, 0);
            int toAdd;
            if (stored + itemStack.getAmount() > size) toAdd = size - stored;
            else toAdd = itemStack.getAmount();
            stored += toAdd;
            storages.put(template, amount + toAdd);
            SlimeAEPlugin.getStorageCellDataController().updateAsync(this, template, amount + toAdd, amount == 0);
            itemStack.setAmount(itemStack.getAmount() - toAdd);
            trim(itemStack);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (storages instanceof CreativeItemIntegerMap) return true;

        for (ItemRequest request : requests) {
            if (!storages.containsKey(request.getTemplate())
                    || storages.getOrDefault(request.getTemplate(), 0) < request.getAmount()) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        if (storages instanceof CreativeItemIntegerMap) {
            return ItemUtils.createItems(requests);
        }

        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            if (storages.containsKey(request.getTemplate())) {
                int amount = storages.get(request.getTemplate());
                if (amount >= request.getAmount()) {
                    ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), request.getAmount());
                    itemStacks.addAll(List.of(tmp));
                    stored -= request.getAmount();
                    storages.put(request.getTemplate(), amount - request.getAmount());
                    SlimeAEPlugin.getStorageCellDataController()
                            .updateAsync(this, request.getTemplate(), amount - request.getAmount(), false);
                } else {
                    ItemStack[] tmp = ItemUtils.createItems(request.getTemplate(), amount);
                    itemStacks.addAll(List.of(tmp));
                    stored -= storages.get(request.getTemplate());
                    storages.put(request.getTemplate(), 0);
                }
                trim(request.getTemplate());
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        if (storages instanceof CreativeItemIntegerMap) return storages;
        return new HashMap<>(storages);
    }

    public Map<ItemStack, Integer> getSourceStorage() {
        return storages;
    }

    @Override
    public int getEmptySlots() {
        return 0;
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
    public int getTier(@Nonnull ItemStack itemStack) {
        if (storages.containsKey(itemStack)) return 200;

        return 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void updateStored(int stored) {
        this.stored = stored;
    }
}
