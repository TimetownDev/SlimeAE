package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.BlockListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private NetworkData networkData = new NetworkData();
    private int slimefunTickCount;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        SlimefunAEItemGroups.onSetup(this);
        SlimefunAEItems.onSetup(this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().add(new SlimefunBlockPopulator());
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> slimefunTickCount++, 1, Slimefun.getTickerTask().getTickRate());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @NotNull @Override
    public JavaPlugin getJavaPlugin() {
        return instance;
    }

    @Nullable @Override
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

    public static int getSlimefunTickCount() {
        return getInstance().slimefunTickCount;
    }
}
