package me.ddggdd135.slimeae.integrations.networks;

import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.integrations.networksexpansion.DrawerStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class NetworksStorage implements IStorage {
    private final NetworkRoot networkRoot;
    private final StorageCollection storage;

    public NetworksStorage(NetworkRoot networkRoot) {
        this.networkRoot = networkRoot;
        storage = new StorageCollection();

        Set<Location> barrels = new HashSet<>();
        for (BarrelIdentity barrelIdentity : networkRoot.getInputAbleBarrels()) {
            barrels.add(barrelIdentity.getLocation());
        }

        for (BarrelIdentity barrelIdentity : networkRoot.getOutputAbleBarrels()) {
            barrels.add(barrelIdentity.getLocation());
        }

        for (Location location : barrels) {
            storage.addStorage(ItemUtils.getStorage(location.getBlock(), true));
        }

        Set<Location> drawers = new HashSet<>();

        drawers.addAll(networkRoot.getInputAbleCargoStorageUnitDatas().values());
        drawers.addAll(networkRoot.getOutputAbleCargoStorageUnitDatas().values());

        for (Location location : drawers) {
            if (NetworksDrawer.getStorageData(location) == null) continue;
            Block block = location.getBlock();
            storage.addStorage(new DrawerStorage(block));
        }
    }

    public NetworkRoot getNetworkRoot() {
        return networkRoot;
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        storage.pushItem(itemStacks);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        return storage.contains(requests);
    }

    @Override
    @Nonnull
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        return storage.tryTakeItem(requests);
    }

    @Override
    @Nonnull
    public Map<ItemStack, Integer> getStorage() {
        return storage.getStorage();
    }

    @Override
    public int getEmptySlots() {
        return storage.getEmptySlots();
    }

    @Override
    public int getTier(@Nonnull ItemStack itemStack) {
        return storage.getTier(itemStack);
    }
}
