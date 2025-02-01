package me.ddggdd135.slimeae.integrations.networks;

import io.github.sefiraat.networks.network.barrel.BarrelType;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class StorageToBarrelWrapper extends BarrelIdentity {
    protected final IStorage storage;

    public StorageToBarrelWrapper(@Nonnull Location location, @Nonnull IStorage storage, @Nonnull ItemStack itemStack) {
        super(
                location,
                itemStack,
                NetworkUtils.<Integer>doAntiNetworksTask(
                        storage, x -> x.getStorage().getOrDefault(itemStack, 0)),
                BarrelType.UNKNOWN);
        this.storage = storage;
    }

    @Override
    @Nullable public ItemStack requestItem(@Nonnull ItemRequest itemRequest) {
        if (itemRequest.getItemStack() == null
                || itemRequest.getItemStack().getType().isAir()) return null;
        if (!SlimefunUtils.isItemSimilar(getItemStack(), itemRequest.getItemStack(), true, false)) return null;
        Set<NetworksStorage> banned = new HashSet<>();
        if (storage instanceof StorageCollection storageCollection) {
            Set<IStorage> storages = storageCollection.getStorages();
            storages.removeIf(x -> {
                if (x instanceof NetworksStorage networksStorage) {
                    banned.add(networksStorage);
                    return true;
                }

                return false;
            });
        }

        ItemStack[] itemStacks = storage.tryTakeItem(new me.ddggdd135.slimeae.api.ItemRequest(
                itemRequest.getItemStack(),
                Math.min(itemRequest.getItemStack().getMaxStackSize(), itemRequest.getAmount())));
        if (storage instanceof StorageCollection storageCollection) {
            storageCollection.getStorages().addAll(banned);
        }
        if (itemStacks.length == 1) return itemStacks[0];
        return null;
    }

    @Override
    public void depositItemStack(ItemStack[] itemStacks) {
        Set<NetworksStorage> banned = new HashSet<>();
        if (storage instanceof StorageCollection storageCollection) {
            Set<IStorage> storages = storageCollection.getStorages();
            storages.removeIf(x -> {
                if (x instanceof NetworksStorage networksStorage) {
                    banned.add(networksStorage);
                    return true;
                }

                return false;
            });
        }
        storage.pushItem(itemStacks);
        if (storage instanceof StorageCollection storageCollection) {
            storageCollection.getStorages().addAll(banned);
        }
    }

    @Override
    public int[] getInputSlot() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlot() {
        return new int[0];
    }
}
