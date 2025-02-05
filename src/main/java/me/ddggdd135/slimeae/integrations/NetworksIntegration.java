package me.ddggdd135.slimeae.integrations;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import java.util.Arrays;
import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class NetworksIntegration implements Integration {
    private boolean cache = false;
    private boolean isCached = false;

    @Override
    public boolean isLoaded() {
        if (!isCached) {
            if (Bukkit.getPluginManager().isPluginEnabled("Networks-Changed")) {
                cache = true;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("Networks")) {
                try {
                    Class.forName("com.ytdd9527.networksexpansion.utils.JavaUtil");
                    cache = false;
                } catch (ClassNotFoundException e) {
                    cache = true;
                }
            }
            isCached = true;
        }
        return cache;
    }

    public ItemRequest[] asNetworkRequests(me.ddggdd135.slimeae.api.ItemRequest[] requests) {
        return Arrays.stream(requests)
                .map(x ->
                        new io.github.sefiraat.networks.network.stackcaches.ItemRequest(x.getTemplate(), x.getAmount()))
                .toArray(io.github.sefiraat.networks.network.stackcaches.ItemRequest[]::new);
    }
}
