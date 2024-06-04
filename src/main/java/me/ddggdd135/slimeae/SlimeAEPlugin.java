package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.commands.CraftCommand;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.BlockListener;
import me.ddggdd135.slimeae.integrations.FluffyMachinesIntegration;
import me.ddggdd135.slimeae.integrations.InfinityIntegration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private final NetworkData networkData = new NetworkData();
    private int slimefunTickCount;
    private final InfinityIntegration infinityIntegration = new InfinityIntegration();
    private final FluffyMachinesIntegration fluffyMachinesIntegration = new FluffyMachinesIntegration();

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        SlimefunAEItemGroups.onSetup(this);
        SlimefunAEItems.onSetup(this);

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        if (infinityIntegration.isLoaded()) getLogger().info("无尽贪婪已支持");
        if (fluffyMachinesIntegration.isLoaded()) getLogger().info("蓬松科技已支持");

        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().add(new SlimefunBlockPopulator());
        }

        CraftCommand craftCommand = new CraftCommand();
        getCommand("ae_craft").setExecutor(craftCommand);
        getCommand("ae_craft").setTabCompleter(craftCommand);

        Bukkit.getScheduler()
                .runTaskTimer(
                        this,
                        () -> slimefunTickCount++,
                        1,
                        Slimefun.getTickerTask().getTickRate());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().removeIf(x -> x instanceof SlimefunBlockPopulator);
        }
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

    @Nonnull
    public static InfinityIntegration getInfinityIntegration() {
        return getInstance().infinityIntegration;
    }

    @Nonnull
    public static FluffyMachinesIntegration getFluffyMachinesIntegration() {
        return getInstance().fluffyMachinesIntegration;
    }
}
