package me.ddggdd135.slimeae.integrations;

import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class NetworksExpansionIntegration implements Integration {
    @Override
    public boolean isLoaded() {
        if (Bukkit.getPluginManager().isPluginEnabled("Networks")) {
            try {
                Class.forName("com.ytdd9527.networksexpansion.utils.JavaUtil");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        return false;
    }
}
