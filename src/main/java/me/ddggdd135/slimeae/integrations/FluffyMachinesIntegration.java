package me.ddggdd135.slimeae.integrations;

import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class FluffyMachinesIntegration implements Integration {
    @Override
    public boolean isLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("FluffyMachines");
    }
}
