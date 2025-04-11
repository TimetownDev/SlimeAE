package me.ddggdd135.slimeae;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.api.abstracts.MEChainedBus;
import me.ddggdd135.slimeae.api.database.StorageCellDataController;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.commands.SlimeAECommand;
import me.ddggdd135.slimeae.core.commands.subcommands.*;
import me.ddggdd135.slimeae.core.generations.SlimefunBlockPopulator;
import me.ddggdd135.slimeae.core.items.SlimefunAEItemGroups;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.listeners.BlockListener;
import me.ddggdd135.slimeae.core.listeners.CardListener;
import me.ddggdd135.slimeae.core.listeners.NetworkListener;
import me.ddggdd135.slimeae.core.listeners.NetworksIntegrationListener;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.core.slimefun.MECleaner;
import me.ddggdd135.slimeae.core.slimefun.MECraftingTrigger;
import me.ddggdd135.slimeae.core.slimefun.MELevelEmitter;
import me.ddggdd135.slimeae.core.slimefun.NetworksExpansionSwitch;
import me.ddggdd135.slimeae.core.slimefun.cards.CraftingCard;
import me.ddggdd135.slimeae.integrations.*;
import me.ddggdd135.slimeae.tasks.*;
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
    private final NetworkData networkData = new NetworkData();
    private final InfinityIntegration infinityIntegration = new InfinityIntegration();
    private final FluffyMachinesIntegration fluffyMachinesIntegration = new FluffyMachinesIntegration();
    private final NetworksIntegration networksIntegration = new NetworksIntegration();
    private final NetworksExpansionIntegration networksExpansionIntegration = new NetworksExpansionIntegration();
    private final TranscEndenceIntegration transcEndenceIntegration = new TranscEndenceIntegration();
    private final JustEnoughGuideIntegration justEnoughGuideIntegration = new JustEnoughGuideIntegration();
    private final GalactifunIntegration galactifunIntegration = new GalactifunIntegration();
    private final ObsidianExpansionIntegration obsidianExpansionIntegration = new ObsidianExpansionIntegration();
    private final ExoticGardenIntegration exoticGardenIntegration = new ExoticGardenIntegration();

    private final StorageCellDataController storageCellDataController = new StorageCellDataController();

    private final NetworkTickerTask networkTicker = new NetworkTickerTask();
    private final NetworkTimeConsumingTask networkTimeConsumingTask = new NetworkTimeConsumingTask();
    private final DataSavingTask dataSavingTask = new DataSavingTask();
    private final SlimeAECommand slimeAECommand = new SlimeAECommand();
    private PinnedManager pinnedManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        instance = this;
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
        tryUpdate();

        reloadConfig0();

        // Plugin startup logic
        SlimefunAEItemGroups.onSetup(this);
        SlimefunAEItems.onSetup(this);

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new CardListener(), this);
        Bukkit.getPluginManager().registerEvents(new NetworkListener(), this);
        if (networksExpansionIntegration.isLoaded())
            Bukkit.getPluginManager().registerEvents(new NetworksIntegrationListener(), this);

        if (infinityIntegration.isLoaded()) getLogger().info("无尽贪婪已接入");
        if (fluffyMachinesIntegration.isLoaded()) getLogger().info("蓬松科技已接入");
        if (networksIntegration.isLoaded()) getLogger().info("网络已接入");
        if (networksExpansionIntegration.isLoaded()) getLogger().info("网络拓展已接入");
        if (transcEndenceIntegration.isLoaded()) getLogger().info("末地科技已接入");
        if (justEnoughGuideIntegration.isLoaded()) getLogger().info("更好的粘液书已接入");
        if (galactifunIntegration.isLoaded()) getLogger().info("星系已接入");
        if (obsidianExpansionIntegration.isLoaded()) getLogger().info("黑曜石科技已接入");
        if (exoticGardenIntegration.isLoaded()) getLogger().info("异域花园已接入");

        storageCellDataController.init();

        for (World world : Bukkit.getWorlds()) {
            world.getPopulators().add(new SlimefunBlockPopulator());
        }

        networkTicker.start(this);
        networkTimeConsumingTask.start(this);
        dataSavingTask.start(this);

        slimeAECommand.addSubCommand(new ApplyUUIDCommand());
        slimeAECommand.addSubCommand(new CleardataCommand());
        slimeAECommand.addSubCommand(new HelpCommand());
        slimeAECommand.addSubCommand(new ReloadCommand());
        slimeAECommand.addSubCommand(new SavedataCommand());
        slimeAECommand.addSubCommand(new UuidCommand());
        slimeAECommand.addSubCommand(new ViewitemsCommand());

        getCommand("SlimeAE").setExecutor(slimeAECommand);
        getCommand("SlimeAE").setTabCompleter(slimeAECommand);

        int pluginId = 24737;
        metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
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
    public static StorageCellDataController getStorageCellDataController() {
        return getInstance().storageCellDataController;
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

    public void reloadConfig0() {
        // 保存默认配置
        saveDefaultConfig();

        reloadConfig();

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
    }
}
