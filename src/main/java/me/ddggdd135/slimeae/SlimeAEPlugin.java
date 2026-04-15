package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.api.abstracts.MEChainedBus;
import me.ddggdd135.slimeae.api.database.v3.CraftTaskPersistence;
import me.ddggdd135.slimeae.api.database.v3.StorageConfig;
import me.ddggdd135.slimeae.api.database.v3.V3DatabaseManager;
import me.ddggdd135.slimeae.api.database.v3.V3FilterController;
import me.ddggdd135.slimeae.api.database.v3.V3ReskinController;
import me.ddggdd135.slimeae.api.database.v3.V3StorageCellController;
import me.ddggdd135.slimeae.api.reskin.MaterialValidator;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.commands.SlimeAECommand;
import me.ddggdd135.slimeae.core.commands.subcommands.*;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimeAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.listeners.*;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.core.slimefun.MECleaner;
import me.ddggdd135.slimeae.core.slimefun.MECraftingTrigger;
import me.ddggdd135.slimeae.core.slimefun.MELevelEmitter;
import me.ddggdd135.slimeae.core.slimefun.NetworksExpansionSwitch;
import me.ddggdd135.slimeae.core.slimefun.cards.CraftingCard;
import me.ddggdd135.slimeae.integrations.*;
import me.ddggdd135.slimeae.tasks.*;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.ddggdd135.slimeae.utils.SlimefunItemUtils;
import net.guizhanss.minecraft.guizhanlib.updater.GuizhanUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SlimeAE插件的主类
 * @author JWJUN233233
 */
public final class SlimeAEPlugin extends JavaPlugin implements SlimefunAddon {
    private static SlimeAEPlugin instance;
    private NetworkData networkData;
    private InfinityIntegration infinityIntegration;
    private FluffyMachinesIntegration fluffyMachinesIntegration;
    private NetworksIntegration networksIntegration = new NetworksIntegration();
    private NetworksExpansionIntegration networksExpansionIntegration;
    private TranscEndenceIntegration transcEndenceIntegration;
    private JustEnoughGuideIntegration justEnoughGuideIntegration;
    private GalactifunIntegration galactifunIntegration;
    private ObsidianExpansionIntegration obsidianExpansionIntegration;
    private ExoticGardenIntegration exoticGardenIntegration;
    private LogiTechIntegration logiTechIntegration;
    private FinalTechIntegration finalTechIntegration;

    private V3DatabaseManager v3DatabaseManager;
    private CraftTaskPersistence craftTaskPersistence;

    private NetworkTickerTask networkTicker;
    private NetworkTimeConsumingTask networkTimeConsumingTask;
    private DataSavingTask dataSavingTask;
    private TaskBackupTask taskBackupTask;
    private SlimeAECommand slimeAECommand = new SlimeAECommand();
    private PinnedManager pinnedManager;
    private Metrics metrics;
    private static boolean debug = false;

    @Override
    public void onEnable() {
        instance = this;

        networkData = new NetworkData();
        infinityIntegration = new InfinityIntegration();
        fluffyMachinesIntegration = new FluffyMachinesIntegration();
        networksIntegration = new NetworksIntegration();
        networksExpansionIntegration = new NetworksExpansionIntegration();
        transcEndenceIntegration = new TranscEndenceIntegration();
        justEnoughGuideIntegration = new JustEnoughGuideIntegration();
        galactifunIntegration = new GalactifunIntegration();
        obsidianExpansionIntegration = new ObsidianExpansionIntegration();
        exoticGardenIntegration = new ExoticGardenIntegration();
        logiTechIntegration = new LogiTechIntegration();
        finalTechIntegration = new FinalTechIntegration();

        networkTicker = new NetworkTickerTask();
        networkTimeConsumingTask = new NetworkTimeConsumingTask();
        dataSavingTask = new DataSavingTask();
        taskBackupTask = new TaskBackupTask();
        slimeAECommand = new SlimeAECommand();

        pinnedManager = new PinnedManager();

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getConsoleSender().sendMessage("############################################");
        Bukkit.getConsoleSender()
                .sendMessage(CMIChatColor.translate("               &aSlime&dAE &f- &a粘液&dAE              "));
        Bukkit.getConsoleSender().sendMessage(" 作者: JWJUN233233 测试: Zombie_2333,balugaq");
        Bukkit.getConsoleSender().sendMessage("############################################");

        reloadConfig0();

        StorageConfig storageConfig = new StorageConfig(getConfig());
        v3DatabaseManager = new V3DatabaseManager(storageConfig);

        if (getConfig().getBoolean("auto-update")) {
            tryUpdate();
        }

        // Plugin startup logic
        SlimeAEItemGroups.onSetup(this);
        SlimeAEItems.onSetup(this);
        RecipeUtils.init();

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new CardListener(), this);
        Bukkit.getPluginManager().registerEvents(new NetworkListener(), this);
        if (networksExpansionIntegration.isLoaded())
            Bukkit.getPluginManager().registerEvents(new NetworksIntegrationListener(), this);
        if (justEnoughGuideIntegration.isLoaded())
            Bukkit.getPluginManager().registerEvents(new JEGCompatibleListener(), this);

        if (infinityIntegration.isLoaded()) getLogger().info("无尽贪婪已接入");
        if (fluffyMachinesIntegration.isLoaded()) getLogger().info("蓬松科技已接入");
        if (networksIntegration.isLoaded()) getLogger().info("网络已接入");
        if (networksExpansionIntegration.isLoaded()) getLogger().info("网络拓展已接入");
        if (transcEndenceIntegration.isLoaded()) getLogger().info("末地科技已接入");
        if (justEnoughGuideIntegration.isLoaded()) getLogger().info("更好的粘液书已接入");
        if (galactifunIntegration.isLoaded()) getLogger().info("星系已接入");
        if (obsidianExpansionIntegration.isLoaded()) getLogger().info("黑曜石科技已接入");
        if (exoticGardenIntegration.isLoaded()) getLogger().info("异域花园已接入");
        if (logiTechIntegration.isLoaded()) getLogger().info("逻辑工艺已接入");
        if (finalTechIntegration.isLoaded()) getLogger().info("乱序技艺已接入");

        v3DatabaseManager.init();

        craftTaskPersistence = new CraftTaskPersistence(v3DatabaseManager);
        craftTaskPersistence.initSchema();

        Bukkit.getPluginManager().registerEvents(new ReskinListener(), this);

        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().add(new SlimefunBlockPopulator());
        }

        networkTicker.start(this);
        networkTimeConsumingTask.start(this);
        dataSavingTask.start(this);
        taskBackupTask.start(this);

        slimeAECommand.addSubCommand(new ApplyUUIDCommand());
        slimeAECommand.addSubCommand(new CleardataCommand());
        slimeAECommand.addSubCommand(new HelpCommand());
        slimeAECommand.addSubCommand(new ReloadCommand());
        slimeAECommand.addSubCommand(new SavedataCommand());
        slimeAECommand.addSubCommand(new UuidCommand());
        slimeAECommand.addSubCommand(new ViewitemsCommand());
        slimeAECommand.addSubCommand(new MigrateCommand());
        slimeAECommand.addSubCommand(new RollbackCommand());
        slimeAECommand.addSubCommand(new RepairCommand());
        slimeAECommand.addSubCommand(new ExportCommand());
        slimeAECommand.addSubCommand(new ImportCommand());
        slimeAECommand.addSubCommand(new ExportAllCommand());
        slimeAECommand.addSubCommand(new ImportAllCommand());
        slimeAECommand.addSubCommand(new StatusCommand());

        getCommand("SlimeAE").setExecutor(slimeAECommand);
        getCommand("SlimeAE").setTabCompleter(slimeAECommand);

        int pluginId = 24737;
        metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        if (metrics != null) metrics.shutdown();
        // Plugin shutdown logic
        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().removeIf(x -> x instanceof SlimefunBlockPopulator);
        }

        networkTicker.setPaused(true);
        networkTicker.halt();
        networkTimeConsumingTask.setPaused(true);
        networkTimeConsumingTask.halt();
        dataSavingTask.setPaused(true);
        dataSavingTask.halt();
        taskBackupTask.setPaused(true);
        taskBackupTask.halt();

        if (craftTaskPersistence != null) {
            for (NetworkInfo info : new java.util.HashSet<>(networkData.AllNetworkData)) {
                for (me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask task :
                        new java.util.HashSet<>(info.getAutoCraftingSessions())) {
                    task.suspend();
                }
            }
        }

        int waitCount = 0;
        while (dataSavingTask.isRunning() && waitCount < 30) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            waitCount++;
        }

        v3DatabaseManager.shutdown();

        SlimefunItemUtils.unregisterItemGroups(this);
        SlimefunItemUtils.unregisterAllItems(this);
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

    /**
     * 获取jeg集成实例
     * @return jeg集成实例
     */
    @Nonnull
    public static JustEnoughGuideIntegration getJustEnoughGuideIntegration() {
        return getInstance().justEnoughGuideIntegration;
    }

    /**
     * 获取星系集成实例
     * @return 星系集成实例
     */
    @Nonnull
    public static GalactifunIntegration getGalactifunIntegration() {
        return getInstance().galactifunIntegration;
    }

    /**
     * 获取黑曜石科技集成实例
     * @return 黑曜石科技集成实例
     */
    @Nonnull
    public static ObsidianExpansionIntegration getObsidianExpansionIntegration() {
        return getInstance().obsidianExpansionIntegration;
    }

    /**
     * 获取异域花园集成实例
     * @return 异域花园集成实例
     */
    @Nonnull
    public static ExoticGardenIntegration getExoticGardenIntegration() {
        return getInstance().exoticGardenIntegration;
    }

    @Nonnull
    public static LogiTechIntegration getLogiTechIntegration() {
        return getInstance().logiTechIntegration;
    }

    @Nonnull
    public static FinalTechIntegration getFinalTechIntegration() {
        return getInstance().finalTechIntegration;
    }

    @Nonnull
    public static V3StorageCellController getStorageCellStorageDataController() {
        return getInstance().v3DatabaseManager.getStorageController();
    }

    @Nonnull
    public static V3FilterController getStorageCellFilterDataController() {
        return getInstance().v3DatabaseManager.getFilterController();
    }

    /**
     * 获取材质转换数据控制器
     * @return 材质转换数据控制器实例
     */
    @Nonnull
    public static V3ReskinController getReskinDataController() {
        return getInstance().v3DatabaseManager.getReskinController();
    }

    @Nonnull
    public static V3DatabaseManager getV3DatabaseManager() {
        return getInstance().v3DatabaseManager;
    }

    @javax.annotation.Nullable public static CraftTaskPersistence getCraftTaskPersistence() {
        return getInstance().craftTaskPersistence;
    }

    @Nonnull
    public static NetworkTickerTask getNetworkTicker() {
        return getInstance().networkTicker;
    }

    @Nonnull
    public static NetworkTimeConsumingTask getNetworkTimeConsumingTask() {
        return getInstance().networkTimeConsumingTask;
    }

    @Nonnull
    public static DataSavingTask getDataSavingTask() {
        return getInstance().dataSavingTask;
    }

    @Nonnull
    public static SlimeAECommand getSlimeAECommand() {
        return getInstance().slimeAECommand;
    }

    @Nonnull
    public static PinnedManager getPinnedManager() {
        return getInstance().pinnedManager;
    }

    /**
     * 判断是否开启了调试模式
     * @return 是否开启调试模式
     */
    public static boolean isDebug() {
        return debug;
    }

    public void reloadConfig0() {
        // 保存默认配置
        saveDefaultConfig();

        reloadConfig();

        // 重载调试模式配置
        debug = getConfig().getBoolean("debug", false);

        // 重载网络配置
        NetworkInfo.reloadConfig();

        // 重载矿石生成配置
        SlimefunBlockPopulator.reloadConfig();

        // 重载合成卡冷却时间配置
        CraftingCard.reloadConfig();

        // 重载网络交换机配置
        if (networksExpansionIntegration.isLoaded()) NetworksExpansionSwitch.reloadConfig();

        // 重载链式总线配置
        MEChainedBus.reloadConfig();

        // 重载ME清除器配置
        MECleaner.reloadConfig();

        // 重载ME合成触发器配置
        MECraftingTrigger.reloadConfig();

        // 重载ME标准发信器配置
        MELevelEmitter.reloadConfig();

        // 重载材质转换机配置
        MaterialValidator.reloadConfig();
    }
}
