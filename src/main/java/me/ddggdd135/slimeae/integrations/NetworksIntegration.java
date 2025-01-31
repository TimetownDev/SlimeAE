package me.ddggdd135.slimeae.integrations;

import me.ddggdd135.slimeae.api.interfaces.Integration;
import org.bukkit.Bukkit;

public class NetworksIntegration implements Integration {
    @Override
    public boolean isLoaded() {
        if (Bukkit.getPluginManager().isPluginEnabled("Networks-Changed")) {
            return true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Networks")) {
            try {
                Class.forName("com.ytdd9527.networksexpansion.utils.JavaUtil");
                return false;
            } catch (ClassNotFoundException e) {
                return true;
            }
        }

        return false;
    }
}
