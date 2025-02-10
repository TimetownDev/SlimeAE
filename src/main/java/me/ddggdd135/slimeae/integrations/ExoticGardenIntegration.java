package me.ddggdd135.slimeae.integrations;

import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class ExoticGardenIntegration implements Integration {
    private boolean cache = false;
    private boolean isCached = false;

    @Override
    public boolean isLoaded() {
        if (!isCached) {
            cache = Bukkit.getPluginManager().isPluginEnabled("ExoticGarden");
            isCached = true;
        }
        return cache;
    }
}
