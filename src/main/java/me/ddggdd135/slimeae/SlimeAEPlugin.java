package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.database.StorageCellDataController;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.commands.SlimeAECommand;
import me.ddggdd135.slimeae.core.commands.subcommands.*;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.BlockListener;
import me.ddggdd135.slimeae.core.listeners.NetworkListener;
import me.ddggdd135.slimeae.core.listeners.NetworksIntegrationListener;
import me.ddggdd135.slimeae.core.slimefun.CraftingCard;
import me.ddggdd135.slimeae.integrations.*;
import me.ddggdd135.slimeae.tasks.NetworkCheckTask;
import me.ddggdd135.slimeae.tasks.NetworkRefreshTask;
import me.ddggdd135.slimeae.tasks.NetworkTickerTask;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.guizhanss.minecraft.guizhanlib.updater.GuizhanUpdater;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SlimeAE插件的主类
 * @author JWJUN233233
 */
public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private final NetworkData networkData = new NetworkData();
    private int slimefunTickCount;
    private final InfinityIntegration infinityIntegration = new InfinityIntegration();
    private final FluffyMachinesIntegration fluffyMachinesIntegration = new FluffyMachinesIntegration();
    private final NetworksIntegration networksIntegration = new NetworksIntegration();
    private final NetworksExpansionIntegration networksExpansionIntegration = new NetworksExpansionIntegration();
    private final TranscEndenceIntegration transcEndenceIntegration = new TranscEndenceIntegration();
    private final StorageCellDataController storageCellDataController = new StorageCellDataController();
    private final NetworkTickerTask networkTicker = new NetworkTickerTask();
    private final NetworkCheckTask networkChecker = new NetworkCheckTask();
    private final NetworkRefreshTask networkRefresher = new NetworkRefreshTask();
    private final SlimeAECommand slimeAECommand = new SlimeAECommand();

    @Override
    public void onEnable() {
        instance = this;

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getConsoleSender().sendMessage("############################################");
        Bukkit.getConsoleSender()
                .sendMessage(CMIChatColor.translate("               &aSlime&dAE &f- &a粘液&dAE              "));
        Bukkit.getConsoleSender().sendMessage(" 作者: JWJUN233233 测试: Zombie_2333,HolderSea");
        Bukkit.getConsoleSender().sendMessage("############################################");
        tryUpdate();

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
        if (networksExpansionIntegration.isLoaded())
            Bukkit.getPluginManager().registerEvents(new NetworksIntegrationListener(), this);

        if (infinityIntegration.isLoaded()) getLogger().info("无尽贪婪已支持");
        if (fluffyMachinesIntegration.isLoaded()) getLogger().info("蓬松科技已支持");
        if (networksIntegration.isLoaded()) getLogger().info("网络已支持");
        if (networksExpansionIntegration.isLoaded()) getLogger().info("网络拓展已支持");
        if (transcEndenceIntegration.isLoaded()) getLogger().info("末地科技已支持");

        storageCellDataController.init();

        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().add(new SlimefunBlockPopulator());
        }

        Bukkit.getScheduler()
                .runTaskTimer(
                        this,
                        () -> slimefunTickCount++,
                        1,
                        Slimefun.getTickerTask().getTickRate());
        networkTicker.start(this);
        networkChecker.start(this);
        networkRefresher.start(this);

        slimeAECommand.addSubCommand(new HelpCommand());
        slimeAECommand.addSubCommand(new CleardataCommand());
        slimeAECommand.addSubCommand(new ApplyUUIDCommand());
        slimeAECommand.addSubCommand(new UuidCommand());
        slimeAECommand.addSubCommand(new ViewitemsCommand());

        getCommand("SlimeAE").setExecutor(slimeAECommand);
        getCommand("SlimeAE").setTabCompleter(slimeAECommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().removeIf(x -> x instanceof SlimefunBlockPopulator);
        }

        networkTicker.setPaused(true);
        networkTicker.halt();

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

    public void tryUpdate() {
        if (getConfig().getBoolean("auto-update")
                && getDescription().getVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), "TimetownDev", "SlimeAE", "master");
        }
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

    /**
     * 获取网络集成实例
     * @return 网络集成实例
     */
    @Nonnull
    public static NetworksIntegration getNetworksIntegration() {
        return getInstance().networksIntegration;
    }

    /**
     * 获取网络拓展集成实例
     * @return 网络拓展集成实例
     */
    @Nonnull
    public static NetworksExpansionIntegration getNetworksExpansionIntegration() {
        return getInstance().networksExpansionIntegration;
    }

    /**
     * 获取末地科技集成实例
     * @return 末地科技集成实例
     */
    @Nonnull
    public static TranscEndenceIntegration getTranscEndenceIntegration() {
        return getInstance().transcEndenceIntegration;
    }

    @Nonnull
    public static StorageCellDataController getStorageCellDataController() {
        return getInstance().storageCellDataController;
    }

    @Nonnull
    public static NetworkTickerTask getNetworkTicker() {
        return getInstance().networkTicker;
    }

    @Nonnull
    public static NetworkCheckTask getNetworkChecker() {
        return getInstance().networkChecker;
    }

    @Nonnull
    public static NetworkRefreshTask getNetworkRefresher() {
        return getInstance().networkRefresher;
    }

    @Nonnull
    public static SlimeAECommand getSlimeAECommand() {
        return getInstance().slimeAECommand;
    }
}
