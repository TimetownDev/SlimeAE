package me.ddggdd135.slimeae.integrations.networks;

import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import java.util.*;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.integrations.networksexpansion.DrawerStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class NetworksStorage extends StorageCollection {
    private final NetworkRoot networkRoot;

    public NetworksStorage(NetworkRoot networkRoot) {
        this.networkRoot = networkRoot;

        Set<Location> barrels = new HashSet<>();
        for (BarrelIdentity barrelIdentity : networkRoot.getInputAbleBarrels()) {
            barrels.add(barrelIdentity.getLocation());
        }

        for (BarrelIdentity barrelIdentity : networkRoot.getOutputAbleBarrels()) {
            barrels.add(barrelIdentity.getLocation());
        }

        for (Location location : barrels) {
            addStorage(ItemUtils.getStorage(location.getBlock(), true));
        }

        Set<Location> drawers = new HashSet<>();

        drawers.addAll(networkRoot.getInputAbleCargoStorageUnitDatas().values());
        drawers.addAll(networkRoot.getOutputAbleCargoStorageUnitDatas().values());

        for (Location location : drawers) {
            if (NetworksDrawer.getStorageData(location) == null) continue;
            Block block = location.getBlock();
            addStorage(new DrawerStorage(block));
        }
    }

    public NetworkRoot getNetworkRoot() {
        return networkRoot;
    }
}
