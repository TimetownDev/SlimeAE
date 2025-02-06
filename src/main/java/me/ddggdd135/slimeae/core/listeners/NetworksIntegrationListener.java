package me.ddggdd135.slimeae.core.listeners;

import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.api.events.NetworkRootReadyEvent;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Set;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.slimefun.NetworksExpansionSwitch;
import me.ddggdd135.slimeae.integrations.networks.StorageToBarrelWrapper;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NetworksIntegrationListener implements Listener {
    @EventHandler
    public void onNetworkRootReady(NetworkRootReadyEvent e) {
        //        NetworkRoot root = e.getRoot();
        //        for (Location location : root.getMonitors()) {
        //            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        //            if (slimefunBlockData == null) continue;
        //            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        //            if (slimefunItem instanceof NetworksExpansionSwitch networksExpansionSwitch) {
        //                NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(location);
        //                if (networkInfo == null) return;
        //                Set<BarrelIdentity> barrelIdentities =
        //                        networksExpansionSwitch.wrapIStorageAsBarrelIdentities(location,
        // networkInfo.getStorage());
        //                root.getInputAbleBarrels().addAll(barrelIdentities);
        //                root.getOutputAbleBarrels().addAll(barrelIdentities);
        //                return;
        //            }
        //        }
    }

    @EventHandler
    public void onNetworkRootLocateStorage(NetworkRootLocateStorageEvent e) {
        if (!(e.getStorageType() == StorageType.BARREL)) {
            return;
        }

        NetworkRoot root = e.getRoot();

        for (Location location : root.getMonitors()) {
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
            if (slimefunBlockData == null) continue;
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
            if (slimefunItem instanceof NetworksExpansionSwitch networksExpansionSwitch) {
                NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(location);
                if (networkInfo == null) return;
                Set<BarrelIdentity> barrelIdentities = networksExpansionSwitch.wrapIStorageAsBarrelIdentities(
                        location, networkInfo.getStorageNoNetworks());
                if (e.isInputAble()) {
                    root.getInputAbleBarrels().removeIf(x -> x instanceof StorageToBarrelWrapper);
                    root.getInputAbleBarrels().addAll(barrelIdentities);
                }
                if (e.isOutputAble()) {
                    root.getOutputAbleBarrels().removeIf(x -> x instanceof StorageToBarrelWrapper);
                    root.getOutputAbleBarrels().addAll(barrelIdentities);
                }
                return;
            }
        }
    }
}
