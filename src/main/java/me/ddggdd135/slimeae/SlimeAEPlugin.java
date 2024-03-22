package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private NetworkData networkData = new NetworkData();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        SlimefunAEItemGroups.onSetup(this);
        SlimefunAEItems.onSetup(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return instance;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return null;
    }

    @Nonnull
    public static SlimeAEPlugin getInstance() {
        return instance;
    }

    @Nonnull
    public static NetworkData getNetworkData() {
        return getInstance().networkData;
    }
}
