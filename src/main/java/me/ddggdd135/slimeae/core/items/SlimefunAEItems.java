package me.ddggdd135.slimeae.core.items;

import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.version.Version;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.abstracts.MEObject;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import me.ddggdd135.slimeae.core.slimefun.*;
import me.ddggdd135.slimeae.core.slimefun.buses.*;
import me.ddggdd135.slimeae.core.slimefun.cards.AccelerationCard;
import me.ddggdd135.slimeae.core.slimefun.cards.CraftingCard;
import me.ddggdd135.slimeae.core.slimefun.cards.RedstoneCard;
import me.ddggdd135.slimeae.core.slimefun.terminals.*;
import me.ddggdd135.slimeae.core.slimefun.tools.*;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SlimefunAEItems {
    // INFO
    public static final SlimefunItemStack INFO = new SlimefunItemStack(
            "AE_INFO",
            new AdvancedCustomItemStack(Material.PAPER),
            "&e信息",
            "",
            "&e版本 " + SlimeAEPlugin.getInstance().getPluginVersion());
    public static final SlimefunItemStack CONTRIBUTOR1 = new SlimefunItemStack(
            "AE_CONTRIBUTOR1",
            "85fffd0a33794006c5bacfc3082e70d59e96153f247ea3f787b559004515a02",
            "&e开发者 &bJWJUN233233");

    public static final SlimefunItemStack DEBUGGER1 = new SlimefunItemStack(
            "AE_DEBUGGER1", "6e3818350104939ea10e184c25c60193e6d409c431ea92b6af3a91a745112bbc", "&e测试者 &cZombie_2333");

    public static final SlimefunItemStack DEBUGGER2 = new SlimefunItemStack(
            "AE_DEBUGGER2", "73112785f64d814103931505ace00048c087337785550c99a67449c392b39772", "&e测试者 &6balugaq");

    // CABLE
    public static final SlimefunItemStack ME_GLASS_CABLE = new SlimefunItemStack(
            "ME_GLASS_CABLE",
            new AdvancedCustomItemStack(Material.GLASS, "{#Vanilla_Ice}ME玻璃线缆", "", "{#3366ff>}用于连接AE设备{#33ccf3<}"));
    public static final SlimefunItemStack ME_DENSE_CABLE = new SlimefunItemStack(
            "ME_DENSE_CABLE",
            new AdvancedCustomItemStack(
                    SlimefunItems.ENERGY_CONNECTOR, "{#Vanilla_Ice}ME致密线缆", "", "&e用于连接AE设备", "&e和ME玻璃线缆相比 仅仅是外观不同"));
    // MACHINE
    public static final SlimefunItemStack ME_CONTROLLER = new SlimefunItemStack(
            "ME_CONTROLLER",
            new AdvancedCustomItemStack(
                    Material.BLACK_STAINED_GLASS, "{#Bright_Gray}ME控制器", "", "&e&lAE网络的核心", "&eAE不允许同一个网络中存在多个控制器"));
    public static final SlimefunItemStack ME_UNIT = new SlimefunItemStack(
            "ME_UNIT",
            new AdvancedCustomItemStack(
                    Material.BLACK_STAINED_GLASS, "{#Starship}ME单元", "", "{#Bright_Red}此物品性能极差 仅供测试使用"));
    public static final SlimefunItemStack ME_INTERFACE = new SlimefunItemStack(
            "ME_INTERFACE",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1)
                            ? Material.TARGET
                            : Material.CHISELED_STONE_BRICKS,
                    "{#Vanilla_Ice}ME接口",
                    "",
                    "{#3366ff>}用它来与货运网络交互{#33ccf3<}",
                    "{#3366ff>}合成设备需要放在它旁边运行{#33ccf3<}",
                    "{#3366ff>}在里面放入蓝图 就可以在合成计划终端中自动合成{#33ccf3<}"));
    public static final SlimefunItemStack ME_DRIVE = new SlimefunItemStack(
            "ME_DRIVE",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.CHISELED_BOOKSHELF
                            : Material.BOOKSHELF,
                    "&fME驱动器",
                    "",
                    "{#3366ff>}用于放置ME存储元件{#33ccf3<}"));
    public static final SlimefunItemStack ME_CELL_WORKBENCH = new SlimefunItemStack(
            "ME_CELL_WORKBENCH",
            new AdvancedCustomItemStack(
                    Material.CRAFTING_TABLE, "&dME元件工作台", "", "{#3366ff>}用于修改存储元件的 黑/白 名单等{#33ccf3<}"));
    public static final SlimefunItemStack ME_IO_PORT = new SlimefunItemStack(
            "ME_IO_PORT",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.CHISELED_BOOKSHELF
                            : Material.BOOKSHELF,
                    "&fME IO端口",
                    "",
                    "{#3366ff>}将物品存储元件里的物品输入至ME网络{#33ccf3<}",
                    "{#3366ff>}或者将ME网络里的物品输出至物品元件{#33ccf3<}"));
    public static final SlimefunItemStack MOLECULAR_ASSEMBLER = new SlimefunItemStack(
            "MOLECULAR_ASSEMBLER",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1) ? Material.TINTED_GLASS : Material.GLASS,
                    "&f分子装配室",
                    "",
                    "{#3366ff>}在自动合成中充当原版和粘液合成台{#33ccf3<}",
                    "{#3366ff>}需要放在ME接口旁边 且只能用于AE的自动合成{#33ccf3<}",
                    "&a每个AE网络中 每个合成任务最多只会使用1024个合成设备"));
    public static final SlimefunItemStack LARGE_MOLECULAR_ASSEMBLER = new SlimefunItemStack(
            "LARGE_MOLECULAR_ASSEMBLER",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1) ? Material.TINTED_GLASS : Material.GLASS,
                    "&f大型分子装配室",
                    "",
                    "{#3366ff>}合成大型配方{#33ccf3<}"));
    public static final SlimefunItemStack COOKING_ALLOCATOR = new SlimefunItemStack(
            "COOKING_ALLOCATOR",
            new AdvancedCustomItemStack(Material.OAK_WOOD, "&f流程分配器", "", "{#3366ff>}自动合成时将物品放于指定的容器{#33ccf3<}"));
    public static final SlimefunItemStack CRAFTING_MONITOR = new SlimefunItemStack(
            "ME_CRAFTING_MONITOR",
            new AdvancedCustomItemStack(Material.SEA_LANTERN, "&f合成监视器", "", "{#3366ff>}右键它就能管理进行的自动合成任务{#33ccf3<}"));
    public static final SlimefunItemStack ENERGY_CELL =
            new SlimefunItemStack("ME_ENERGY_CELL", new AdvancedCustomItemStack(Material.AMETHYST_BLOCK, "&f能源元件"));
    public static final SlimefunItemStack INSCRIBER = new SlimefunItemStack(
            "ME_INSCRIBER", new AdvancedCustomItemStack(Material.LODESTONE, "&f压印机", "", "{#3366ff>}用于生产板材{#33ccf3<}"));
    public static final SlimefunItemStack CHARGER = new SlimefunItemStack(
            "ME_CHARGER", new AdvancedCustomItemStack(Material.LECTERN, "&f充能器", "", "{#3366ff>}用于制造赛特斯石英{#33ccf3<}"));
    public static final SlimefunItemStack ME_IMPORT_BUS = new SlimefunItemStack(
            "ME_IMPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.LIME_STAINED_GLASS,
                    "{#Bright_Green}&lME输入总线",
                    "",
                    "{#3366ff>}抽取指定容器中的物品 不支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_VANILLA_IMPORT_BUS = new SlimefunItemStack(
            "ME_VANILLA_IMPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.LIME_STAINED_GLASS,
                    "{#Bright_Green}&lME原版输入总线",
                    "",
                    "{#3366ff>}抽取指定容器中的物品 支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_ADVANCED_IMPORT_BUS = new SlimefunItemStack(
            "ME_ADVANCED_IMPORT_BUS",
            new AdvancedCustomItemStack(
                            Material.LIME_STAINED_GLASS,
                            "{#Bright_Green}&lME高级输入总线",
                            "",
                            "{#3366ff>}可选择多方向进行输入{#33ccf3<}",
                            "{#3366ff>}抽取指定容器中的物品 不支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_ADVANCED_VANILLA_IMPORT_BUS = new SlimefunItemStack(
            "ME_ADVANCED_VANILLA_IMPORT_BUS",
            new AdvancedCustomItemStack(
                            Material.LIME_STAINED_GLASS,
                            "{#Bright_Green}&lME高级原版输入总线",
                            "",
                            "{#3366ff>}可选择多方向进行输入{#33ccf3<}",
                            "{#3366ff>}抽取指定容器中的物品 支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_CHAINED_IMPORT_BUS = new SlimefunItemStack(
            "ME_CHAINED_IMPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.LIME_STAINED_GLASS,
                    "{#Bright_Green}&lME链式输入总线",
                    "",
                    "{#3366ff>}工作方向：选择的方向{#33ccf3<}",
                    "{#3366ff>}检测范围：1-64格[可配置]{#33ccf3<}",
                    "{#3366ff>}抽取指定容器中的物品 ❗不支持原版容器{#33ccf3<}",
                    "",
                    "{#FF5555>}❗ 严格链式连接：{#FF8888<}",
                    "{#FF5555>}若路径中有阻断/非机器方块{#FF8888<}",
                    "{#FF5555>}阻断点之后的容器将失效{#FF8888<}"));
    public static final SlimefunItemStack ME_EXPORT_BUS = new SlimefunItemStack(
            "ME_EXPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.RED_STAINED_GLASS,
                    "{#Bright_Red}&lME输出总线",
                    "",
                    "{#3366ff>}输出指定的物品到指定的容器中 不支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_VANILLA_EXPORT_BUS = new SlimefunItemStack(
            "ME_VANILLA_EXPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.RED_STAINED_GLASS,
                    "{#Bright_Red}&lME原版输出总线",
                    "",
                    "{#3366ff>}输出指定的物品到指定的容器中 支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_ADVANCED_EXPORT_BUS = new SlimefunItemStack(
            "ME_ADVANCED_EXPORT_BUS",
            new AdvancedCustomItemStack(
                            Material.RED_STAINED_GLASS,
                            "{#Bright_Red}&lME高级输出总线",
                            "",
                            "{#3366ff>}可选择多方向进行输出{#33ccf3<}",
                            "{#3366ff>}输出指定的物品到指定的容器中 不支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_ADVANCED_VANILLA_EXPORT_BUS = new SlimefunItemStack(
            "ME_ADVANCED_VANILLA_EXPORT_BUS",
            new AdvancedCustomItemStack(
                            Material.RED_STAINED_GLASS,
                            "{#Bright_Red}&lME高级原版输出总线",
                            "",
                            "{#3366ff>}可选择多方向进行输出{#33ccf3<}",
                            "{#3366ff>}输出指定的物品到指定的容器中 支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_CHAINED_EXPORT_BUS = new SlimefunItemStack(
            "ME_CHAINED_EXPORT_BUS",
            new AdvancedCustomItemStack(
                    Material.RED_STAINED_GLASS,
                    "{#Bright_Red}&lME链式输出总线",
                    "",
                    "{#3366ff>}工作方向：选择的方向{#33ccf3<}",
                    "{#3366ff>}检测范围：1-64格[可配置]{#33ccf3<}",
                    "{#3366ff>}抽取指定容器中的物品 ❗不支持原版容器{#33ccf3<}",
                    "",
                    "{#FF5555>}❗ 严格链式连接：{#FF8888<}",
                    "{#FF5555>}若路径中有阻断/非机器方块{#FF8888<}",
                    "{#FF5555>}阻断点之后的容器将失效{#FF8888<}"));
    public static final SlimefunItemStack ME_IE_BUS = new SlimefunItemStack(
            "ME_IE_BUS",
            new AdvancedCustomItemStack(
                    Material.YELLOW_STAINED_GLASS,
                    "{#Starship}&lME输入输出总线",
                    "",
                    "{#3366ff>}同时抽取和输出物品到指定的容器中 不支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_VANILLA_IE_BUS = new SlimefunItemStack(
            "ME_VANILLA_IE_BUS",
            new AdvancedCustomItemStack(
                    Material.YELLOW_STAINED_GLASS,
                    "{#Starship}&lME原版输入输出总线",
                    "",
                    "{#3366ff>}同时抽取和输出物品到指定的容器中 支持原版容器{#33ccf3<}"));
    public static final SlimefunItemStack ME_ADVANCED_IE_BUS = new SlimefunItemStack(
            "ME_ADVANCED_IE_BUS",
            new AdvancedCustomItemStack(
                            Material.YELLOW_STAINED_GLASS,
                            "{#Starship}&lME高级输入输出总线",
                            "",
                            "{#3366ff>}可选择多方向进行输入输出{#33ccf3<}",
                            "{#3366ff>}同时抽取和输出物品到指定的容器中 不支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_ADVANCED_VANILLA_IE_BUS = new SlimefunItemStack(
            "ME_ADVANCED_VANILLA_IE_BUS",
            new AdvancedCustomItemStack(
                            Material.YELLOW_STAINED_GLASS,
                            "{#Starship}&lME高级原版输入输出总线",
                            "",
                            "{#3366ff>}可选择多方向进行输入输出{#33ccf3<}",
                            "{#3366ff>}同时抽取和输出物品到指定的容器中 支持原版容器{#33ccf3<}")
                    .doGlow());
    public static final SlimefunItemStack ME_CHAINED_IE_BUS = new SlimefunItemStack(
            "ME_CHAINED_IE_BUS",
            new AdvancedCustomItemStack(
                    Material.YELLOW_STAINED_GLASS,
                    "{#Starship}&lME链式输入输出总线",
                    "",
                    "{#3366ff>}工作方向：选择的方向{#33ccf3<}",
                    "{#3366ff>}检测范围：1-64格[可配置]{#33ccf3<}",
                    "{#3366ff>}抽取指定容器中的物品 ❗不支持原版容器{#33ccf3<}",
                    "",
                    "{#FF5555>}❗ 严格链式连接：{#FF8888<}",
                    "{#FF5555>}若路径中有阻断/非机器方块{#FF8888<}",
                    "{#FF5555>}阻断点之后的容器将失效{#FF8888<}"));
    public static final SlimefunItemStack ME_STORAGE_BUS = new SlimefunItemStack(
            "ME_STORAGE_BUS",
            new AdvancedCustomItemStack(
                    Material.WHITE_STAINED_GLASS,
                    "&f&lME存储总线",
                    "",
                    "{#3366ff>}将方块的存储接入AE网络{#33ccf3<}",
                    "&e支持蓬松桶和无尽存储单元",
                    "&e支持网络量子存储和网络抽屉",
                    "&e不支持原版容器"));
    public static final SlimefunItemStack ME_LEVEL_EMITTER = new SlimefunItemStack(
            "ME_LEVEL_EMITTER",
            new AdvancedCustomItemStack(
                    Material.TARGET, "&f&lME标准发信器", "", "{#3366ff>}当AE网络中 设定物品大于设定的数量 发出红石信号{#33ccf3<}"));
    public static final SlimefunItemStack ME_TERMINAL = new SlimefunItemStack(
            "ME_TERMINAL",
            new AdvancedCustomItemStack(Material.SHROOMLIGHT, "&fME终端", "", "{#3366ff>}管理AE网络中的物品{#33ccf3<}"));
    public static final SlimefunItemStack ME_CRAFTING_TERMINAL = new SlimefunItemStack(
            "ME_CRAFTING_TERMINAL", new AdvancedCustomItemStack(Material.CRAFTING_TABLE, "&fME合成终端"));
    public static final SlimefunItemStack ME_CRAFT_PLANNING_TERMINAL = new SlimefunItemStack(
            "ME_CRAFT_PLANNING_TERMINAL", new AdvancedCustomItemStack(Material.SEA_LANTERN, "&fME合成计划终端"));
    public static final SlimefunItemStack ME_PATTERN_TERMINAL = new SlimefunItemStack(
            "ME_PATTERN_TERMINAL",
            new AdvancedCustomItemStack(Material.LOOM, "&fME样板终端", "", "{#3366ff>}可以制作编码样板{#33ccf3<}"));
    public static final SlimefunItemStack ME_SECURITY_TERMINAL = new SlimefunItemStack(
            "ME_SECURITY_TERMINAL",
            new AdvancedCustomItemStack(Material.ENDER_CHEST, "&fME安全终端", "", "{#3366ff>}用于连接无线终端{#33ccf3<}"));
    public static final SlimefunItemStack ME_CONVERSATION_MONITOR = new SlimefunItemStack(
            "ME_CONVERSATION_MONITOR",
            new AdvancedCustomItemStack(
                    Material.GLASS,
                    "&eME交换监控器",
                    "",
                    "{#3366ff>}可以直接存取其显示的物品{#33ccf3<}",
                    "{#3366ff>}锁定设定后 左键取出物品 右键放回物品{#33ccf3<}"));
    public static final SlimefunItemStack ME_CLEANER = new SlimefunItemStack(
            "ME_CLEANER",
            new AdvancedCustomItemStack(Material.OBSERVER, "&fME清除器", "", "{#3366ff>}用于清除ME存储元件中多余的物品{#33ccf3<}"));
    public static final SlimefunItemStack ME_CRAFTING_TRIGGER = new SlimefunItemStack(
            "ME_CRAFTING_TRIGGER",
            new AdvancedCustomItemStack(
                    Material.CRAFTING_TABLE, "&fME合成触发器", "", "{#3366ff>}当AE中物品缺少时, 下发自动合成任务{#33ccf3<}"));
    public static final SlimefunItemStack ENERGY_ACCEPTOR = new SlimefunItemStack(
            "ME_ENERGY_ACCEPTOR",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&f能源接收器", "", "&c现在它还没用 敬请期待"));
    public static final SlimefunItemStack NETWORKS_EXPANSION_SWITCH = new SlimefunItemStack(
            "NETWORKS_EXPANSION_SWITCH",
            new AdvancedCustomItemStack(Material.MAGENTA_GLAZED_TERRACOTTA, "&a网络交换机", "", "&c用它将AE网络和网络拓展连接"));
    public static final SlimefunItemStack PATTERN_WORKBENCH = new SlimefunItemStack(
            "PATTERN_WORKBENCH",
            new AdvancedCustomItemStack(
                    Material.PRISMARINE_BRICKS, "&e样板工作台", "", "&a在里面放入数量对应的配方所需物品和输出 就可以合成样板", "&a支持超大型配方 不支持原版配方"));

    // MATERIAL
    public static final SlimefunItemStack CRYSTAL_CERTUS_QUARTZ = new SlimefunItemStack(
            "CRYSTAL_CERTUS_QUARTZ", new AdvancedCustomItemStack(Material.QUARTZ, "{#Zumthor}赛特斯石英水晶"));

    public static final SlimefunItemStack CERTUS_QUARTZ_ORE = new SlimefunItemStack(
            "CERTUS_QUARTZ_ORE", new AdvancedCustomItemStack(Material.NETHER_QUARTZ_ORE, "{#Zumthor}赛特斯石英矿石"));
    public static final SlimefunItemStack CHARGED_CRYSTAL_CERTUS_QUARTZ = new SlimefunItemStack(
            "CHARGED_CRYSTAL_CERTUS_QUARTZ", new AdvancedCustomItemStack(Material.QUARTZ, "{#Zumthor}充能赛特斯石英水晶"));
    public static final SlimefunItemStack CRYSTAL_FLUIX = new SlimefunItemStack(
            "CRYSTAL_FLUIX",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1)
                            ? Material.AMETHYST_CLUSTER
                            : Material.QUARTZ,
                    "{#Fuchsia_Pink}福鲁伊克斯水晶"));
    public static final SlimefunItemStack CERTUS_QUARTZ_DUST = new SlimefunItemStack(
            "CERTUS_QUARTZ_DUST", new AdvancedCustomItemStack(Material.GLOWSTONE_DUST, "{#Zumthor}赛特斯石英粉"));
    public static final SlimefunItemStack FLUIX_DUST = new SlimefunItemStack(
            "FLUIX_DUST", new AdvancedCustomItemStack(Material.BLAZE_POWDER, "{#Fuchsia_Pink}福鲁伊克斯粉"));
    public static final SlimefunItemStack QUARTZ_DUST =
            new SlimefunItemStack("QUARTZ_DUST", new AdvancedCustomItemStack(Material.SUGAR, "{#ffffff}下界石英粉"));
    public static final SlimefunItemStack SKY_STONE_DUST = new SlimefunItemStack(
            "SKY_STONE_DUST", new AdvancedCustomItemStack(Material.GUNPOWDER, "{#Bright_Gray}陨石粉"));
    public static final SlimefunItemStack FLUIX_PEARL =
            new SlimefunItemStack("FLUIX_PEARL", new AdvancedCustomItemStack(Material.ENDER_EYE, "&d福鲁伊克斯珍珠"));
    public static final SlimefunItemStack PRINTED_SILICON =
            new SlimefunItemStack("PRINTED_SILICON", new AdvancedCustomItemStack(Material.ORANGE_DYE, "{#Sky_Blue}硅板"));
    public static final SlimefunItemStack PRINTED_LOGIC_CIRCUIT = new SlimefunItemStack(
            "PRINTED_LOGIC_CIRCUIT", new AdvancedCustomItemStack(Material.ORANGE_DYE, "{#Sky_Blue}逻辑电路板"));
    public static final SlimefunItemStack PRINTED_CALCULATION_CIRCUIT = new SlimefunItemStack(
            "PRINTED_CALCULATION_CIRCUIT", new AdvancedCustomItemStack(Material.LIGHT_BLUE_DYE, "{#Sky_Blue}运算电路板"));
    public static final SlimefunItemStack PRINTED_ENGINEERING_CIRCUIT = new SlimefunItemStack(
            "PRINTED_ENGINEERING_CIRCUIT", new AdvancedCustomItemStack(Material.BLUE_DYE, "{#Sky_Blue}工程电路板"));
    public static final SlimefunItemStack LOGIC_PROCESSOR = new SlimefunItemStack(
            "LOGIC_PROCESSOR",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#Sky_Blue}逻辑处理器")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack CALCULATION_PROCESSOR = new SlimefunItemStack(
            "CALCULATION_PROCESSOR",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#Sky_Blue}运算处理器")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack ENGINEERING_PROCESSOR = new SlimefunItemStack(
            "ENGINEERING_PROCESSOR",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#Sky_Blue}工程处理器")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack WIRELESS_RECEIVER =
            new SlimefunItemStack("WIRELESS_RECEIVER", new AdvancedCustomItemStack(Material.REDSTONE_TORCH, "&d无线接收器"));
    public static final SlimefunItemStack ME_STORAGE_HOUSING = new SlimefunItemStack(
            "ME_STORAGE_HOUSING",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#Bright_Gray}ME存储外壳")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_1K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_1K", new AdvancedCustomItemStack(Material.SLIME_BALL, "{#3366ff}1k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_4K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_4K", new AdvancedCustomItemStack(Material.FIREWORK_STAR, "{#3388fb}4k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_16K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_16K", new AdvancedCustomItemStack(Material.FIRE_CHARGE, "{#33aaf7}16k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_64K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_64K",
            new AdvancedCustomItemStack(Material.POPPED_CHORUS_FRUIT, "{#33ccf3}64k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_256K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_256K", new AdvancedCustomItemStack(Material.GRAY_DYE, "{#33aaf7}256k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_1M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_1M", new AdvancedCustomItemStack(Material.ENDER_PEARL, "{#3388fb}1m-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_4M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_4M", new AdvancedCustomItemStack(Material.ENDER_EYE, "{#3366ff}4m-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_16M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_16M", new AdvancedCustomItemStack(Material.GUNPOWDER, "{#3388fb}16m-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_64M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_64M", new AdvancedCustomItemStack(Material.BLAZE_POWDER, "{#3366ff}64m-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_256M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_256M",
            new AdvancedCustomItemStack(Material.NETHER_STAR, "{#33aaf7}256m-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_1G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_1G", new AdvancedCustomItemStack(Material.FIRE_CHARGE, "{#33aaf7}1g-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_4G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_4G",
            new AdvancedCustomItemStack(Material.POPPED_CHORUS_FRUIT, "{#33ccf3}4g-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_16G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_16G", new AdvancedCustomItemStack(Material.GRAY_DYE, "{#33aaf7}16g-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_64G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_64G", new AdvancedCustomItemStack(Material.ENDER_PEARL, "{#3388fb}64g-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_256G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_256G", new AdvancedCustomItemStack(Material.ENDER_EYE, "{#3366ff}256g-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_1T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_1T", new AdvancedCustomItemStack(Material.GUNPOWDER, "{#3388fb}1t-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_4T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_4T", new AdvancedCustomItemStack(Material.BLAZE_POWDER, "{#3366ff}4t-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_16T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_16T", new AdvancedCustomItemStack(Material.NETHER_STAR, "{#33aaf7}16t-ME存储组件"));

    // CELL
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_1K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_1K", new AdvancedCustomItemStack(Material.MUSIC_DISC_13, "{#3366ff}1k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_4K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_4K", new AdvancedCustomItemStack(Material.MUSIC_DISC_CAT, "{#3388fb}4k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_16K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_16K", new AdvancedCustomItemStack(Material.MUSIC_DISC_BLOCKS, "{#33aaf7}16k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_64K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_64K", new AdvancedCustomItemStack(Material.MUSIC_DISC_CHIRP, "{#33ccf3}64k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_256K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_256K", new AdvancedCustomItemStack(Material.MUSIC_DISC_FAR, "{#33aaf7}256k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_1M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_1M", new AdvancedCustomItemStack(Material.MUSIC_DISC_MALL, "{#3388fb}1m-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_4M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_4M", new AdvancedCustomItemStack(Material.MUSIC_DISC_RELIC, "{#3366ff}4m-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_16M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_16M",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_OTHERSIDE, "{#3388fb}16m-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_64M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_64M", new AdvancedCustomItemStack(Material.MUSIC_DISC_13, "{#3366ff}64m-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_256M = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_256M",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_PIGSTEP, "{#33aaf7}256m-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_1G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_1G", new AdvancedCustomItemStack(Material.MUSIC_DISC_BLOCKS, "{#33aaf7}1g-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_4G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_4G", new AdvancedCustomItemStack(Material.MUSIC_DISC_CHIRP, "{#33ccf3}4g-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_16G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_16G", new AdvancedCustomItemStack(Material.MUSIC_DISC_FAR, "{#33aaf7}16g-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_64G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_64G", new AdvancedCustomItemStack(Material.MUSIC_DISC_MALL, "{#3388fb}64g-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_256G = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_256G",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_RELIC, "{#3366ff}256g-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_1T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_1T",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_OTHERSIDE, "{#3388fb}1t-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_4T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_4T",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_OTHERSIDE, "{#3366ff}4t-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_16T = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_16T",
            new AdvancedCustomItemStack(Material.MUSIC_DISC_PIGSTEP, "{#33aaf7}16t-ME存储元件"));
    public static final SlimefunItemStack ME_CREATIVE_ITEM_STORAGE_CELL = new SlimefunItemStack(
            "ME_CREATIVE_ITEM_STORAGE_CELL",
            new AdvancedCustomItemStack(
                    Material.MUSIC_DISC_PIGSTEP, "{#3366ff>}创造存储元件{#33ccf3<}", "", "&c&l可以从中拿取任意物品"));
    public static final SlimefunItemStack FORMATION_CORE =
            new SlimefunItemStack("FORMATION_CORE", new AdvancedCustomItemStack(Material.GOLD_NUGGET, "{#33ccf3}成型核心"));
    public static final SlimefunItemStack ANNIHILATION_CORE = new SlimefunItemStack(
            "ANNIHILATION_CORE", new AdvancedCustomItemStack(Material.IRON_NUGGET, "{#33ccf3}破坏核心"));
    // CARD
    public static final SlimefunItemStack BASIC_CARD = new SlimefunItemStack(
            "BASIC_CARD",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.MINER_POTTERY_SHERD
                            : Material.PAPER,
                    "{#33ccf3}基础卡"));
    public static final SlimefunItemStack ADVANCED_CARD = new SlimefunItemStack(
            "ADVANCED_CARD",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.BLADE_POTTERY_SHERD
                            : Material.PAPER,
                    "{#33ccf3}高级卡"));
    public static final SlimefunItemStack ACCELERATION_CARD = new SlimefunItemStack(
            "ACCELERATION_CARD",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#33ccf3}加速卡")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack CRAFTING_CARD = new SlimefunItemStack(
            "CRAFTING_CARD",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "{#33ccf3}合成卡")
                    .addFlags(ItemFlag.values()));
    public static final SlimefunItemStack REDSTONE_CARD = new SlimefunItemStack(
            "REDSTONE_CARD",
            new AdvancedCustomItemStack(
                            Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                                    ? Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE
                                    : Material.SHULKER_SHELL,
                            "&c红石卡")
                    .addFlags(ItemFlag.values()));
    //    public static final SlimefunItemStack CAPACITY_CARD = new SlimefunItemStack(
    //            "CAPACITY_CARD",
    //            new AdvancedCustomItemStack(
    //                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
    //                            ? Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE
    //                            : Material.SHULKER_SHELL,
    //                    "{#33ccf3}容量卡"));

    public static final SlimefunItemStack QUARTZ_GLASS =
            new SlimefunItemStack("QUARTZ_GLASS", new AdvancedCustomItemStack(Material.GLASS, "{#33ccf3}石英玻璃"));
    public static final SlimefunItemStack BLANK_PATTERN = new SlimefunItemStack(
            "BLANK_PATTERN",
            new AdvancedCustomItemStack(
                    Material.PAPER, "{#33ccf3}空白样板", "", "{#3366ff>}在ME样板终端中用它制造编码样板 用于自动合成{#33ccf3<}"));
    public static final SlimefunItemStack ENCODED_PATTERN = new SlimefunItemStack(
            "ENCODED_PATTERN",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1) ? Material.PRISMARINE_SHARD : Material.PAPER,
                    "{#33ccf3}编码样板"));

    // TOOLS
    public static final SlimefunItemStack MEMORY_CARD = new SlimefunItemStack(
            "MEMORY_CARD",
            new AdvancedCustomItemStack(Material.PAPER, "&e内存卡", "", "&e复制ME设备的设置", "", "&eShift右键 复制设置", "&e右键 应用设置"));
    public static final SlimefunItemStack CARD_REPLICATOR = new SlimefunItemStack(
            "CARD_REPLICATOR",
            new AdvancedCustomItemStack(
                    Material.PAPER, "&e升级卡复制器", "", "&e复制ME设备的升级卡设置", "", "&eShift右键 复制设置", "&e右键 应用设置"));
    public static final SlimefunItemStack WIRELESS_TERMINAL = new SlimefunItemStack(
            "WIRELESS_TERMINAL",
            new AdvancedCustomItemStack(Material.ITEM_FRAME, "{#Sky_Blue}无线终端", "", "&e便捷的访问AE网络", "&e需要使用ME安全终端进行绑定"));
    public static final SlimefunItemStack CERTUS_QUARTZ_WRENCH = new SlimefunItemStack(
            "CERTUS_QUARTZ_WRENCH",
            new AdvancedCustomItemStack(Material.DIAMOND_HOE, "{#Zumthor}赛特斯石英扳手", "", "&e右键可将大部分AE设备拆卸为掉落物形式"));
    public static final SlimefunItemStack QUARTZ_WRENCH = new SlimefunItemStack(
            "QUARTZ_WRENCH",
            new AdvancedCustomItemStack(Material.IRON_HOE, "&f下界石英扳手", "", "&e手持扳手右键可将大部分AE设备拆卸为掉落物形式"));
    public static final SlimefunItemStack NETWORK_TOOL = new SlimefunItemStack(
            "NETWORK_TOOL",
            new AdvancedCustomItemStack(
                    Material.NETHERITE_HOE,
                    "{#Fuchsia_Pink}网络工具",
                    "",
                    "&e右键可查看整个AE网络的各个网络元件",
                    "&eShift右键可将大部分AE设备拆卸为掉落物形式"));
    public static final SlimefunItemStack AE_TERMINAL_TOPPER = new SlimefunItemStack(
            "AE_TERMINAL_TOPPER",
            new AdvancedCustomItemStack(Material.ENDER_EYE, "&e置顶器", "", "&2&l用于在终端中置顶物品", "&6用法: 在终端中拿起此物品，在显示物品上点击"));
    public static final SlimefunItemStack ENTROPY_MANIPULATOR = new SlimefunItemStack(
            "ENTROPY_MANIPULATOR",
            new AdvancedCustomItemStack(
                    Material.IRON_HOE,
                    "&6熵变机械臂",
                    "",
                    "{#3366ff>}充满电可处理125个方块{#33ccf3<}",
                    "",
                    "&a右键 &7- {#3366ff>}产生高温效应{#33ccf3<}",
                    "&aShift+右键 &7- {#3366ff>}产生低温效应{#33ccf3<}",
                    "&c攻击命中 &7- {#3366ff>}点燃目标生物{#3366ff<}",
                    "",
                    "&c&o&8\u21E8 &e\u26A1 &70 / 200000 J"));

    public static final SlimefunItemStack CHARGED_STAFF = new SlimefunItemStack(
            "CHARGED_STAFF",
            new AdvancedCustomItemStack(
                    Material.BLAZE_ROD,
                    "{#33ccf3}&l充能法杖",
                    "",
                    "{#3366ff>}⚡ 储能容量: &b8000J{#33aaf7<}",
                    "{#33aaf7>}❖ 单次消耗: &c300J{#3388ff<}",
                    "",
                    "{#ffd700>}⚔ 攻击属性:{#ffa500<}",
                    "&8➥ {#ff6b6b}3.5❤ &7真实伤害 &8(无视防御)",
                    "&8➥ {#4ecdc4}10格穿透射线",
                    "&8➥ {#a855f7}击退",
                    "",
                    "{#00cec9>}✦ 使用方式:{#0984e3<}",
                    "&8➥ {#dfe6e9}右键发射能量束",
                    "&8➥ {#fd79a8}命中生成粒子",
                    "&8➥ {#ff7675}电力消耗提示",
                    "",
                    "&c&o&8\u21E8 &e\u26A1 &70 / 8000 J"));

    public static void onSetup(SlimeAEPlugin plugin) {
        // Infos

        new SlimefunItem(SlimefunAEItemGroups.INFO, INFO, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.INFO, CONTRIBUTOR1, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.INFO, DEBUGGER1, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.INFO, DEBUGGER2, RecipeType.NULL, new ItemStack[0]).register(plugin);
        // Cables

        ItemUtils.setRecipeOutput(
                        new MEObject(
                                SlimefunAEItemGroups.CABLE,
                                ME_GLASS_CABLE,
                                RecipeType.ENHANCED_CRAFTING_TABLE,
                                new ItemStack[] {new ItemStack(Material.GLASS), CRYSTAL_FLUIX, CRYSTAL_FLUIX}),
                        new AdvancedCustomItemStack(ME_GLASS_CABLE, 4))
                .register(plugin);
        ItemUtils.setRecipeOutput(
                        new MEObject(
                                SlimefunAEItemGroups.CABLE,
                                ME_DENSE_CABLE,
                                RecipeType.ENHANCED_CRAFTING_TABLE,
                                new ItemStack[] {SlimefunItems.ENERGY_CONNECTOR, CRYSTAL_CERTUS_QUARTZ, CRYSTAL_FLUIX}),
                        new AdvancedCustomItemStack(ME_DENSE_CABLE, 4))
                .register(plugin);
        // Machines

        new MEController(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CONTROLLER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            SKY_STONE_DUST, CRYSTAL_FLUIX, SKY_STONE_DUST,
                            CRYSTAL_FLUIX, ENGINEERING_PROCESSOR, CRYSTAL_FLUIX,
                            SKY_STONE_DUST, CRYSTAL_FLUIX, SKY_STONE_DUST
                        })
                .register(plugin);
        new MEUnit(SlimefunAEItemGroups.MACHINE, ME_UNIT, RecipeType.NULL, new ItemStack[] {
                    null, null, null, new AdvancedCustomItemStack(Material.BARRIER, "&c&l调试物品")
                })
                .register(plugin);
        new MEInterface(
                        SlimefunAEItemGroups.MACHINE,
                        ME_INTERFACE,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT),
                            ANNIHILATION_CORE,
                            null,
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEDrive(SlimefunAEItemGroups.MACHINE, ME_DRIVE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    new ItemStack(Material.IRON_INGOT),
                    ENGINEERING_PROCESSOR,
                    new ItemStack(Material.IRON_INGOT),
                    ME_GLASS_CABLE,
                    null,
                    ME_GLASS_CABLE,
                    new ItemStack(Material.IRON_INGOT),
                    ENGINEERING_PROCESSOR,
                    new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
        new MECellWorkbench(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CELL_WORKBENCH,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.WHITE_WOOL),
                            LOGIC_PROCESSOR,
                            new ItemStack(Material.WHITE_WOOL),
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.CHEST),
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEIOPort(SlimefunAEItemGroups.MACHINE, ME_IO_PORT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    new ItemStack(Material.GLASS),
                    new ItemStack(Material.GLASS),
                    new ItemStack(Material.GLASS),
                    ME_DRIVE,
                    ME_GLASS_CABLE,
                    ME_DRIVE,
                    new ItemStack(Material.IRON_INGOT),
                    LOGIC_PROCESSOR,
                    new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
        new MolecularAssembler(
                        SlimefunAEItemGroups.MACHINE,
                        MOLECULAR_ASSEMBLER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT),
                            ANNIHILATION_CORE,
                            new ItemStack(Material.CRAFTING_TABLE),
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new LargeMolecularAssembler(
                        SlimefunAEItemGroups.MACHINE,
                        LARGE_MOLECULAR_ASSEMBLER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            ANNIHILATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            MOLECULAR_ASSEMBLER,
                            MOLECULAR_ASSEMBLER,
                            MOLECULAR_ASSEMBLER,
                            new ItemStack(Material.IRON_INGOT),
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new CookingAllocator(
                        SlimefunAEItemGroups.MACHINE,
                        COOKING_ALLOCATOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT),
                            ANNIHILATION_CORE,
                            ME_IE_BUS,
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MACHINE, ENERGY_CELL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                            CRYSTAL_CERTUS_QUARTZ, FLUIX_DUST, CRYSTAL_CERTUS_QUARTZ,
                            FLUIX_DUST, new ItemStack(Material.GLASS), FLUIX_DUST,
                            CRYSTAL_CERTUS_QUARTZ, FLUIX_DUST, CRYSTAL_CERTUS_QUARTZ
                        })
                .register(plugin);
        new Inscriber(SlimefunAEItemGroups.MACHINE, INSCRIBER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.STICKY_PISTON),
                    new ItemStack(Material.IRON_INGOT),
                    CRYSTAL_FLUIX,
                    null,
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.STICKY_PISTON),
                    new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
        new Charger(SlimefunAEItemGroups.MACHINE, CHARGER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    new ItemStack(Material.IRON_INGOT), SlimefunItems.FERROSILICON, new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.IRON_INGOT), null, null,
                    new ItemStack(Material.IRON_INGOT), SlimefunItems.FERROSILICON, new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
        new MEImportBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_IMPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            ANNIHILATION_CORE,
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.STICKY_PISTON),
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEVanillaImportBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_VANILLA_IMPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            new ItemStack(Material.HOPPER),
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            ME_IMPORT_BUS,
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEAdvancedImportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_IMPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_IMPORT_BUS,
                            ANNIHILATION_CORE,
                            ME_IMPORT_BUS,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.STICKY_PISTON),
                            new ItemStack(Material.IRON_INGOT),
                            ME_IMPORT_BUS,
                            ANNIHILATION_CORE,
                            ME_IMPORT_BUS,
                        })
                .register(plugin);
        new MEAdvancedVanillaImportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_VANILLA_IMPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            new ItemStack(Material.HOPPER),
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            ME_ADVANCED_IMPORT_BUS,
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEChainedImportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_CHAINED_IMPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_IMPORT_BUS, LOGIC_PROCESSOR, ME_IMPORT_BUS,
                            ME_IMPORT_BUS, ME_INTERFACE, ME_IMPORT_BUS,
                            ME_IMPORT_BUS, LOGIC_PROCESSOR, ME_IMPORT_BUS
                        })
                .register(plugin);
        new MEExportBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_EXPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.STICKY_PISTON)
                        })
                .register(plugin);
        new MEVanillaExportBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_VANILLA_EXPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            null,
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            ME_EXPORT_BUS,
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.HOPPER),
                            null
                        })
                .register(plugin);
        new MEAdvancedExportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_EXPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            FORMATION_CORE,
                            new ItemStack(Material.IRON_INGOT),
                            ME_EXPORT_BUS,
                            new ItemStack(Material.STICKY_PISTON),
                            ME_EXPORT_BUS,
                            ME_EXPORT_BUS,
                            FORMATION_CORE,
                            ME_EXPORT_BUS,
                        })
                .register(plugin);
        new MEAdvancedVanillaExportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_VANILLA_EXPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            null,
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            ME_ADVANCED_EXPORT_BUS,
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.HOPPER),
                        })
                .register(plugin);
        new MEChainedExportBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_CHAINED_EXPORT_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_EXPORT_BUS, LOGIC_PROCESSOR, ME_EXPORT_BUS,
                            ME_EXPORT_BUS, ME_INTERFACE, ME_EXPORT_BUS,
                            ME_EXPORT_BUS, LOGIC_PROCESSOR, ME_EXPORT_BUS
                        })
                .register(plugin);
        new MEIEBus(SlimefunAEItemGroups.MACHINE, ME_IE_BUS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    ME_IMPORT_BUS, LOGIC_PROCESSOR, ME_EXPORT_BUS
                })
                .register(plugin);
        new MEVanillaIEBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_VANILLA_IE_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_VANILLA_IMPORT_BUS, LOGIC_PROCESSOR, ME_VANILLA_EXPORT_BUS})
                .register(plugin);
        new MEAdvancedIEBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_IE_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_ADVANCED_IMPORT_BUS, LOGIC_PROCESSOR, ME_ADVANCED_EXPORT_BUS,
                            ME_ADVANCED_IMPORT_BUS, ME_IE_BUS, ME_ADVANCED_EXPORT_BUS,
                            ME_ADVANCED_IMPORT_BUS, LOGIC_PROCESSOR, ME_ADVANCED_EXPORT_BUS
                        })
                .register(plugin);
        new MEAdvancedVanillaIEBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_ADVANCED_VANILLA_IE_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_ADVANCED_VANILLA_IMPORT_BUS, LOGIC_PROCESSOR, ME_ADVANCED_VANILLA_EXPORT_BUS,
                            ME_ADVANCED_VANILLA_IMPORT_BUS, ME_VANILLA_IE_BUS, ME_ADVANCED_VANILLA_EXPORT_BUS,
                            ME_ADVANCED_VANILLA_IMPORT_BUS, LOGIC_PROCESSOR, ME_ADVANCED_VANILLA_EXPORT_BUS
                        })
                .register(plugin);
        new MEChainedIEBus(
                        SlimefunAEItemGroups.ADVANCED_MACHINE,
                        ME_CHAINED_IE_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_IE_BUS, LOGIC_PROCESSOR, ME_IE_BUS,
                            ME_IE_BUS, ME_INTERFACE, ME_IE_BUS,
                            ME_IE_BUS, LOGIC_PROCESSOR, ME_IE_BUS
                        })
                .register(plugin);
        new MEStorageBus(
                        SlimefunAEItemGroups.MACHINE,
                        ME_STORAGE_BUS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_INTERFACE, new ItemStack(Material.STICKY_PISTON), new ItemStack(Material.PISTON)
                        })
                .register(plugin);
        new MELevelEmitter(
                        SlimefunAEItemGroups.MACHINE,
                        ME_LEVEL_EMITTER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_INTERFACE, REDSTONE_CARD, CALCULATION_PROCESSOR})
                .register(plugin);
        new METerminal(SlimefunAEItemGroups.MACHINE, ME_TERMINAL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    QUARTZ_GLASS, FORMATION_CORE, ANNIHILATION_CORE, LOGIC_PROCESSOR
                })
                .register(plugin);
        new MECraftingTerminal(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CRAFTING_TERMINAL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_TERMINAL, new ItemStack(Material.CRAFTING_TABLE), CALCULATION_PROCESSOR})
                .register(plugin);
        new MECraftPlanningTerminal(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CRAFT_PLANNING_TERMINAL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_CRAFTING_TERMINAL, BLANK_PATTERN, CALCULATION_PROCESSOR})
                .register(plugin);
        new MEPatternTerminal(
                        SlimefunAEItemGroups.MACHINE,
                        ME_PATTERN_TERMINAL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_CRAFTING_TERMINAL, ENGINEERING_PROCESSOR})
                .register(plugin);
        new MESecurityTerminal(
                        SlimefunAEItemGroups.MACHINE,
                        ME_SECURITY_TERMINAL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT),
                            ME_DRIVE,
                            new ItemStack(Material.IRON_INGOT),
                            ME_GLASS_CABLE,
                            ME_ITEM_STORAGE_COMPONENT_16K,
                            ME_GLASS_CABLE,
                            new ItemStack(Material.IRON_INGOT),
                            ENGINEERING_PROCESSOR,
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new MEConversionMonitor(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CONVERSATION_MONITOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {FORMATION_CORE, ME_TERMINAL, ANNIHILATION_CORE})
                .register(plugin);
        new MECleaner(SlimefunAEItemGroups.MACHINE, ME_CLEANER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    null,
                    ME_INTERFACE,
                    null,
                    new ItemStack(Material.IRON_INGOT),
                    SlimefunItems.PORTABLE_DUSTBIN,
                    new ItemStack(Material.IRON_INGOT),
                    null,
                    LOGIC_PROCESSOR,
                })
                .register(plugin);
        new MECraftingTrigger(
                        SlimefunAEItemGroups.MACHINE,
                        ME_CRAFTING_TRIGGER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.CRAFTING_TABLE),
                            ME_INTERFACE,
                            new ItemStack(Material.CRAFTING_TABLE),
                            new ItemStack(Material.IRON_INGOT),
                            ME_CRAFT_PLANNING_TERMINAL,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.CRAFTING_TABLE),
                            LOGIC_PROCESSOR,
                            new ItemStack(Material.CRAFTING_TABLE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MACHINE,
                        ENERGY_ACCEPTOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GLASS),
                                    new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GLASS), CRYSTAL_FLUIX, new ItemStack(Material.GLASS),
                            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GLASS),
                                    new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new CraftingMonitor(
                        SlimefunAEItemGroups.MACHINE,
                        CRAFTING_MONITOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            null,
                            null,
                            new ItemStack(Material.REDSTONE_TORCH),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.LAPIS_LAZULI)
                        })
                .register(plugin);
        if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded())
            new NetworksExpansionSwitch(
                            SlimefunAEItemGroups.MACHINE,
                            NETWORKS_EXPANSION_SWITCH,
                            RecipeType.ENHANCED_CRAFTING_TABLE,
                            new ItemStack[] {
                                new ItemStack(Material.IRON_INGOT),
                                new ItemStack(Material.GLASS),
                                new ItemStack(Material.IRON_INGOT),
                                ME_INTERFACE,
                                NetworkSlimefunItems.NETWORK_CONTROLLER.getItem(),
                                ME_INTERFACE,
                                new ItemStack(Material.IRON_INGOT),
                                new ItemStack(Material.GLASS),
                                new ItemStack(Material.IRON_INGOT)
                            })
                    .register(plugin);
        new PatternWorkbench(
                        SlimefunAEItemGroups.MACHINE,
                        PATTERN_WORKBENCH,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_PATTERN_TERMINAL, ENGINEERING_PROCESSOR, new ItemStack(Material.CHEST)})
                .register(plugin);
        // Materials

        SlimefunItem crystal_certus_quartz = new SlimefunItem(
                SlimefunAEItemGroups.MATERIAL,
                CRYSTAL_CERTUS_QUARTZ,
                SlimefunAERecipeTypes.BLOCK_DESTROY,
                new ItemStack[] {null, null, null, null, CERTUS_QUARTZ_ORE});
        crystal_certus_quartz.setRecipeOutput(CRYSTAL_CERTUS_QUARTZ.asQuantity(5));
        crystal_certus_quartz.register(plugin);
        CrystalCertusQuartz crystalCertusQuartz = new CrystalCertusQuartz(plugin, CRYSTAL_CERTUS_QUARTZ);
        crystalCertusQuartz.register();
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        CHARGED_CRYSTAL_CERTUS_QUARTZ,
                        SlimefunAERecipeTypes.CHARGER,
                        new ItemStack[] {CRYSTAL_CERTUS_QUARTZ})
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CRYSTAL_FLUIX, SlimefunAERecipeTypes.CHARGER, new ItemStack[] {
                    CHARGED_CRYSTAL_CERTUS_QUARTZ
                })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        CERTUS_QUARTZ_ORE,
                        SlimefunAERecipeTypes.WORLD_GENERATING,
                        new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CERTUS_QUARTZ_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    CRYSTAL_CERTUS_QUARTZ
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, FLUIX_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    CRYSTAL_FLUIX
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, QUARTZ_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    new ItemStack(Material.QUARTZ)
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, SKY_STONE_DUST, SlimefunAERecipeTypes.CHARGER, new ItemStack[] {
                    FLUIX_DUST
                })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        FLUIX_PEARL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            FLUIX_DUST,
                            CRYSTAL_FLUIX,
                            FLUIX_DUST,
                            CRYSTAL_FLUIX,
                            new ItemStack(Material.ENDER_PEARL),
                            CRYSTAL_FLUIX,
                            FLUIX_DUST,
                            CRYSTAL_FLUIX,
                            FLUIX_DUST
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        PRINTED_SILICON,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {SlimefunItems.SILICON, new ItemStack(Material.IRON_INGOT)})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        PRINTED_LOGIC_CIRCUIT,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_SILICON, new ItemStack(Material.GOLD_INGOT)})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        PRINTED_CALCULATION_CIRCUIT,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_SILICON, CRYSTAL_CERTUS_QUARTZ})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        PRINTED_ENGINEERING_CIRCUIT,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_SILICON, new ItemStack(Material.DIAMOND)})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        LOGIC_PROCESSOR,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_LOGIC_CIRCUIT, new ItemStack(Material.REDSTONE), PRINTED_SILICON})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        CALCULATION_PROCESSOR,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_CALCULATION_CIRCUIT, new ItemStack(Material.REDSTONE), PRINTED_SILICON
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ENGINEERING_PROCESSOR,
                        SlimefunAERecipeTypes.INSCRIBER,
                        new ItemStack[] {PRINTED_ENGINEERING_CIRCUIT, new ItemStack(Material.REDSTONE), PRINTED_SILICON
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        WIRELESS_RECEIVER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            null,
                            FLUIX_PEARL,
                            null,
                            new ItemStack(Material.IRON_INGOT),
                            QUARTZ_GLASS,
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.IRON_INGOT),
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_STORAGE_HOUSING,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.GLASS), new ItemStack(Material.REDSTONE),
                                    new ItemStack(Material.GLASS),
                            new ItemStack(Material.REDSTONE), null, new ItemStack(Material.REDSTONE),
                            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT),
                                    new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_1K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CRYSTAL_CERTUS_QUARTZ,
                            new ItemStack(Material.REDSTONE),
                            CRYSTAL_CERTUS_QUARTZ,
                            LOGIC_PROCESSOR,
                            CRYSTAL_CERTUS_QUARTZ,
                            new ItemStack(Material.REDSTONE),
                            CRYSTAL_CERTUS_QUARTZ,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_4K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1K,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_1K,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1K,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_16K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4K,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_4K,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4K,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_64K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16K,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_16K,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16K,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_256K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64K,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_64K,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64K,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_1M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256K,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_256K,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256K,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_4M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1M,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_1M,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1M,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_16M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4M,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_4M,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4M,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_64M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16M,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_16M,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16M,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_256M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64M,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_64M,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64M,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_1G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256M,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_256M,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256M,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_4G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1G,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_1G,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1G,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_16G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4G,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_4G,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4G,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_64G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16G,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_16G,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_16G,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_256G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64G,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_64G,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_64G,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_1T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256G,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_256G,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_256G,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_4T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1T,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_1T,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_1T,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ME_ITEM_STORAGE_COMPONENT_16T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4T,
                            new ItemStack(Material.GLASS),
                            ME_ITEM_STORAGE_COMPONENT_4T,
                            new ItemStack(Material.REDSTONE),
                            ME_ITEM_STORAGE_COMPONENT_4T,
                            new ItemStack(Material.REDSTONE)
                        })
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        FORMATION_CORE,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {CRYSTAL_CERTUS_QUARTZ, FLUIX_DUST, LOGIC_PROCESSOR})
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        ANNIHILATION_CORE,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {new ItemStack(Material.QUARTZ), FLUIX_DUST, LOGIC_PROCESSOR})
                .register(plugin);
        new Card(
                        SlimefunAEItemGroups.MATERIAL,
                        BASIC_CARD,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.GOLD_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GOLD_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                        },
                        new SlimefunItemStack(SlimefunAEItems.BASIC_CARD, 2))
                .register(plugin);
        new Card(
                        SlimefunAEItemGroups.MATERIAL,
                        ADVANCED_CARD,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            new ItemStack(Material.DIAMOND),
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            new ItemStack(Material.REDSTONE),
                            CALCULATION_PROCESSOR,
                            new ItemStack(Material.DIAMOND),
                            new ItemStack(Material.GOLD_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                        },
                        new SlimefunItemStack(SlimefunAEItems.ADVANCED_CARD, 2))
                .register(plugin);
        new AccelerationCard(
                        SlimefunAEItemGroups.MATERIAL,
                        ACCELERATION_CARD,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ADVANCED_CARD, CRYSTAL_FLUIX})
                .register(plugin);
        new CraftingCard(
                        SlimefunAEItemGroups.MATERIAL,
                        CRAFTING_CARD,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {BASIC_CARD, new ItemStack(Material.CRAFTING_TABLE)})
                .register(plugin);
        new RedstoneCard(
                        SlimefunAEItemGroups.MATERIAL,
                        REDSTONE_CARD,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {BASIC_CARD, new ItemStack(Material.REDSTONE_TORCH)})
                .register(plugin);
        //        new SlimefunItem(
        //                        SlimefunAEItemGroups.MATERIAL,
        //                        CAPACITY_CARD,
        //                        RecipeType.ENHANCED_CRAFTING_TABLE,
        //                        new ItemStack[] {BASIC_CARD, CRYSTAL_CERTUS_QUARTZ, null, null, null, null, null,
        // null, null})
        //                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        QUARTZ_GLASS,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            QUARTZ_DUST,
                            new ItemStack(Material.GLASS),
                            QUARTZ_DUST,
                            new ItemStack(Material.GLASS),
                            QUARTZ_DUST,
                            new ItemStack(Material.GLASS),
                            QUARTZ_DUST,
                            new ItemStack(Material.GLASS),
                            QUARTZ_DUST
                        },
                        new SlimefunItemStack(SlimefunAEItems.QUARTZ_GLASS, 4))
                .register(plugin);
        new Pattern(SlimefunAEItemGroups.MATERIAL, BLANK_PATTERN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    QUARTZ_GLASS,
                    new ItemStack(Material.GLOWSTONE_DUST),
                    QUARTZ_GLASS,
                    new ItemStack(Material.GLOWSTONE_DUST),
                    CRYSTAL_CERTUS_QUARTZ,
                    new ItemStack(Material.GLOWSTONE_DUST),
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
        new Pattern(SlimefunAEItemGroups.MATERIAL, ENCODED_PATTERN, RecipeType.NULL, new ItemStack[0]).register(plugin);
        // Cells

        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_1K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_1K},
                        1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_4K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_4K},
                        4 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_16K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_16K},
                        16 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_64K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_64K},
                        64 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_256K,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_256K},
                        256 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_1M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_1M},
                        1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_4M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_4M},
                        4 * 1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_16M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_16M},
                        16 * 1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_64M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_64M},
                        64 * 1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_256M,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_256M},
                        256 * 1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_1G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_1G},
                        1024 * 1024 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_4G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_4G},
                        4L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_16G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_16G},
                        16L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_64G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_64G},
                        64L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_256G,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_256G},
                        256L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_1T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_1T},
                        1024L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_4T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_4T},
                        4L * 1024L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_16T,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {ME_STORAGE_HOUSING, ME_ITEM_STORAGE_COMPONENT_16T},
                        16L * 1024L * 1024L * 1024L * 1024L)
                .register(plugin);
        new MECreativeItemStorageCell(
                        SlimefunAEItemGroups.CELL, ME_CREATIVE_ITEM_STORAGE_CELL, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        // TOOLS

        new MemoryCard(SlimefunAEItemGroups.TOOL, MEMORY_CARD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    LOGIC_PROCESSOR,
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.IRON_INGOT),
                    new ItemStack(Material.GOLD_INGOT),
                    new ItemStack(Material.REDSTONE),
                    new ItemStack(Material.GOLD_INGOT)
                })
                .register(plugin);
        new CardReplicator(
                        SlimefunAEItemGroups.TOOL,
                        CARD_REPLICATOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            LOGIC_PROCESSOR,
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.IRON_INGOT),
                            new ItemStack(Material.GOLD_INGOT),
                            MEMORY_CARD,
                            new ItemStack(Material.GOLD_INGOT)
                        })
                .register(plugin);
        new WirelessTerminal(
                        SlimefunAEItemGroups.TOOL,
                        WIRELESS_TERMINAL,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            WIRELESS_RECEIVER,
                            null,
                            null,
                            ME_TERMINAL,
                            null,
                            null,
                            SlimefunItems.CARBONADO_EDGED_CAPACITOR
                        })
                .register(plugin);
        new Wrench(
                        SlimefunAEItemGroups.TOOL,
                        CERTUS_QUARTZ_WRENCH,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            CRYSTAL_CERTUS_QUARTZ,
                            null,
                            CRYSTAL_CERTUS_QUARTZ,
                            null,
                            CRYSTAL_CERTUS_QUARTZ,
                            null,
                            CRYSTAL_CERTUS_QUARTZ,
                            null,
                            CRYSTAL_CERTUS_QUARTZ
                        })
                .register(plugin);
        new Wrench(SlimefunAEItemGroups.TOOL, QUARTZ_WRENCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    new ItemStack(Material.QUARTZ),
                    null,
                    new ItemStack(Material.QUARTZ),
                    null,
                    new ItemStack(Material.QUARTZ),
                    null,
                    new ItemStack(Material.QUARTZ),
                    null,
                    new ItemStack(Material.QUARTZ)
                })
                .register(plugin);
        new NetworkTool(SlimefunAEItemGroups.TOOL, NETWORK_TOOL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    CERTUS_QUARTZ_WRENCH, QUARTZ_GLASS, null, CALCULATION_PROCESSOR, new ItemStack(Material.CHEST)
                })
                .register(plugin);
        RecipeType.ENHANCED_CRAFTING_TABLE.register(
                new ItemStack[] {QUARTZ_WRENCH, QUARTZ_GLASS, null, CALCULATION_PROCESSOR, new ItemStack(Material.CHEST)
                },
                NETWORK_TOOL);
        new SlimefunItem(
                        SlimefunAEItemGroups.TOOL,
                        AE_TERMINAL_TOPPER,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            ME_GLASS_CABLE,
                            CHARGED_CRYSTAL_CERTUS_QUARTZ,
                            ME_GLASS_CABLE,
                            CHARGED_CRYSTAL_CERTUS_QUARTZ,
                            CHARGED_CRYSTAL_CERTUS_QUARTZ,
                            CHARGED_CRYSTAL_CERTUS_QUARTZ,
                            ME_GLASS_CABLE,
                            CHARGED_CRYSTAL_CERTUS_QUARTZ,
                            ME_GLASS_CABLE
                        })
                .register(plugin);
        new EntropyManipulator(
                        SlimefunAEItemGroups.TOOL,
                        ENTROPY_MANIPULATOR,
                        RecipeType.ENHANCED_CRAFTING_TABLE,
                        new ItemStack[] {
                            CRYSTAL_FLUIX,
                            ENERGY_CELL,
                            null,
                            PRINTED_ENGINEERING_CIRCUIT,
                            new ItemStack(Material.IRON_INGOT),
                            null,
                            null,
                            null,
                            new ItemStack(Material.IRON_INGOT)
                        })
                .register(plugin);
        new ChargedStaff(SlimefunAEItemGroups.TOOL, CHARGED_STAFF, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    CHARGED_CRYSTAL_CERTUS_QUARTZ,
                    ENERGY_CELL,
                    null,
                    null,
                    null,
                    new ItemStack(Material.IRON_INGOT),
                    null,
                    null,
                    null,
                    new ItemStack(Material.IRON_INGOT)
                })
                .register(plugin);
    }
}
