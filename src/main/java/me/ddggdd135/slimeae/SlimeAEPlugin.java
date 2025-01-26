package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.database.StorageCellDataController;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.commands.CraftCommand;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.BlockListener;
import me.ddggdd135.slimeae.core.listeners.NetworkListener;
import me.ddggdd135.slimeae.core.slimefun.CraftingCard;
import me.ddggdd135.slimeae.integrations.FluffyMachinesIntegration;
import me.ddggdd135.slimeae.integrations.InfinityIntegration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SlimeAE插件的主类
 */
public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private final NetworkData networkData = new NetworkData();
    private int slimefunTickCount;
    private final InfinityIntegration infinityIntegration = new InfinityIntegration();
    private final FluffyMachinesIntegration fluffyMachinesIntegration = new FluffyMachinesIntegration();
    private final StorageCellDataController storageCellDataController = new StorageCellDataController();

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 重载网络配置
        NetworkInfo.reloadConfig();

        // 重载矿石生成配置
        SlimefunBlockPopulator.reloadConfig();

        // 重载合成卡冷却时间配置
        CraftingCard.reloadConfig();

        // Plugin startup logic
        SlimefunAEItemGroups.onSetup(this);
        SlimefunAEItems.onSetup(this);

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new NetworkListener(), this);

        if (infinityIntegration.isLoaded()) getLogger().info("无尽贪婪已支持");
        if (fluffyMachinesIntegration.isLoaded()) getLogger().info("蓬松科技已支持");

        storageCellDataController.init();

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

        storageCellDataController.shutdown();
    }

    @Override
    @Nonnull
    public JavaPlugin getJavaPlugin() {
        return instance;
    }

    @Override
    @Nullable public String getBugTrackerURL() {
        return null;
    }

    @Nonnull
    public static SlimeAEPlugin getInstance() {
        return instance;
    }

    /**
     * 获取网络数据管理器
     * @return 网络数据管理器实例
     */
    @Nonnull
    public static NetworkData getNetworkData() {
        return getInstance().networkData;
    }

    /**
     * 获取Slimefun计时器计数
     * @return 当前的计时器计数值
     */
    public static int getSlimefunTickCount() {
        return getInstance().slimefunTickCount;
    }

    /**
     * 获取无尽贪婪集成实例
     * @return 无尽贪婪集成实例
     */
    @Nonnull
    public static InfinityIntegration getInfinityIntegration() {
        return getInstance().infinityIntegration;
    }

    /**
     * 获取蓬松科技集成实例
     * @return 蓬松科技集成实例
     */
    @Nonnull
    public static FluffyMachinesIntegration getFluffyMachinesIntegration() {
        return getInstance().fluffyMachinesIntegration;
    }

    public static StorageCellDataController getStorageCellDataController() {
        return getInstance().storageCellDataController;
    }
}
