package me.ddggdd135.slimeae.integrations.networksexpansion;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.InitializeSafeProvider;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemInfo;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class DrawerStorage implements IStorage {
    private StorageUnitData data;
    private VarHandle ID_HANDLE = (new InitializeSafeProvider<>(() -> {
                try {
                    return MethodHandles.privateLookupIn(StorageUnitData.class, MethodHandles.lookup())
                            .findVarHandle(StorageUnitData.class, "id", int.class);
                } catch (NoSuchFieldException | IllegalAccessException var1) {
                    return null;
                }
            }))
            .v();
    private Block block;
    private boolean isReadOnly;

    public DrawerStorage(@Nonnull Block block) {
        this(block, false);
    }

    public DrawerStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded())
            throw new RuntimeException("NetworksExpansion is not loaded");
        this.block = block;
        data = NetworksDrawer.getStorageData(block.getLocation());
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        if (!isReadOnly && data != null)
            data.depositItemStack(itemStackCache.getItemStack(), NetworksDrawer.isLocked(block.getLocation()));
    }

    @Override
    public void pushItem(@Nonnull ItemInfo itemInfo) {
        if (!isReadOnly && data != null) {
            Map.Entry<ItemStack, Integer> entry =
                    new AbstractMap.SimpleEntry<>(itemInfo.getItemKey().getItemStack(), (int) itemInfo.getAmount());
            data.depositItemStack(entry, NetworksDrawer.isLocked(block.getLocation()));
            itemInfo.setAmount(entry.getValue());
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (data == null) return false;
        List<ItemContainer> items = data.getStoredItems();
        for (ItemRequest request : requests) {
            boolean found = false;
            for (ItemContainer itemContainer : items) {
                if (SlimefunUtils.isItemSimilar(
                        request.getKey().getItemStack(), itemContainer.getSample(), true, false)) {
                    if (itemContainer.getAmount() < request.getAmount()) return false;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        if (data == null) return new ItemStorage();

        ItemStorage storage = new ItemStorage();
        for (ItemRequest request : requests) {
            int amount = (int) request.getAmount();

            for (ItemContainer itemContainer : data.getStoredItemsDirectly()) {
                int containerAmount = itemContainer.getAmount();
                if (itemContainer.getSample().isSimilar(request.getKey().getItemStack())) {
                    int take = Math.min(amount, containerAmount);
                    if (take > 0) {
                        itemContainer.removeAmount(take);
                        DataStorage.setStoredAmount(
                                (Integer) ID_HANDLE.get(data), itemContainer.getId(), itemContainer.getAmount());

                        storage.addItem(request.getKey(), take);
                    }
                    break;
                }
            }
        }
        return storage;
    }

    @Override
    @Nonnull
    public ItemHashMap<Long> getStorageUnsafe() {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        if (data == null) return storage;
        for (ItemContainer itemContainer : data.getStoredItemsDirectly()) {
            storage.put(itemContainer.getSample(), (long) itemContainer.getAmount());
        }

        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        if (data == null) return -1;

        for (ItemContainer itemContainer : data.getStoredItemsDirectly()) {
            if (itemStack.getItemStack().getType() == itemContainer.getSample().getType()) {
                return 3000;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawerStorage that)) return false;
        return Objects.equals(data.getId(), that.data.getId());
    }

    @Override
    public int hashCode() {
        if (data == null) return 0;
        return data.getId();
    }
}
