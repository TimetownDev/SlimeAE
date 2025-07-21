package me.ddggdd135.slimeae.integrations;

import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class FluffyMachinesIntegration implements Integration {
    private boolean cache = false;
    private boolean isCached = false;

    @Override
    public boolean isLoaded() {
        if (!isCached) {
            cache = Bukkit.getPluginManager().isPluginEnabled("FluffyMachines")
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("support-fluffy-machines");
            isCached = true;
        }
        return cache;
    }
}
