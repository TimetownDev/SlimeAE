package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import me.ddggdd135.slimeae.core.slimefun.*;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.utils.ItemUtils;
import net.Zrips.CMILib.Version.Version;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SlimefunAEItems {
    public static final SlimefunItemStack ME_CONTROLLER = new SlimefunItemStack(
            "ME_CONTROLLER", new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Bright_Gray}ME控制器"));
    public static final SlimefunItemStack ME_UNIT = new SlimefunItemStack(
            "ME_UNIT", new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Starship}ME单元"));
    public static final SlimefunItemStack ME_INTERFACE = new SlimefunItemStack(
            "ME_INTERFACE",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1)
                            ? Material.TARGET
                            : Material.CHISELED_STONE_BRICKS,
                    "{#Vanilla_Ice}ME接口"));
    public static final SlimefunItemStack ME_DRIVE = new SlimefunItemStack(
            "ME_DRIVE",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.CHISELED_BOOKSHELF
                            : Material.BOOKSHELF,
                    "&fME驱动器"));
    public static final SlimefunItemStack MOLECULAR_ASSEMBLER = new SlimefunItemStack(
            "MOLECULAR_ASSEMBLER",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_17_R1) ? Material.TINTED_GLASS : Material.GLASS,
                    "&f分子装配室"));
    public static final SlimefunItemStack ONE_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "ONE_K_CRAFTING_STORAGE", new AdvancedCustomItemStack(Material.LIME_CONCRETE, "&f1K合成储存器"));
    public static final SlimefunItemStack FOUR_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "FOUR_K_CRAFTING_STORAGE", new AdvancedCustomItemStack(Material.GREEN_CONCRETE, "&f4K合成储存器"));
    public static final SlimefunItemStack SIXSTEEN_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "SIXSTEEN_K_CRAFTING_STORAGE", new AdvancedCustomItemStack(Material.CYAN_CONCRETE, "&f16K合成储存器"));
    public static final SlimefunItemStack SIXTY_FOUR_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "SIXTY_FOUR_K_CRAFTING_STORAGE", new AdvancedCustomItemStack(Material.LIGHT_BLUE_CONCRETE, "&f64K合成储存器"));
    public static final SlimefunItemStack CRAFTING_CO_PROCESSING_UNIT = new SlimefunItemStack(
            "CRAFTING_CO_PROCESSING_UNIT", new AdvancedCustomItemStack(Material.PURPLE_GLAZED_TERRACOTTA, "&f并行处理单元"));
    public static final SlimefunItemStack CRAFTING_UNIT = new SlimefunItemStack(
            "CRAFTING_UNIT", new AdvancedCustomItemStack(Material.CHISELED_STONE_BRICKS, "&f合成单元"));
    public static final SlimefunItemStack CRAFTING_MONITOR =
            new SlimefunItemStack("CRAFTING_MONITOR", new AdvancedCustomItemStack(Material.SEA_LANTERN, "&f合成监视器"));
    public static final SlimefunItemStack ENERGY_CELL =
            new SlimefunItemStack("ENERGY_CELL", new AdvancedCustomItemStack(Material.AMETHYST_BLOCK, "&f能源元件"));
    public static final SlimefunItemStack INSCRIBER =
            new SlimefunItemStack("INSCRIBER", new AdvancedCustomItemStack(Material.ANVIL, "&f压印机"));
    public static final SlimefunItemStack CHARGER =
            new SlimefunItemStack("CHARGER", new AdvancedCustomItemStack(Material.LECTERN, "&f充能器"));
    public static final SlimefunItemStack ME_IMPORT_BUS = new SlimefunItemStack(
            "ME_IMPORT_BUS", new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS, "{#Bright_Green}&lME输入总线"));
    public static final SlimefunItemStack ME_EXPORT_BUS = new SlimefunItemStack(
            "ME_EXPORT_BUS", new AdvancedCustomItemStack(Material.RED_STAINED_GLASS, "{#Bright_Red}&lME输出总线"));
    public static final SlimefunItemStack ME_STORAGE_BUS = new SlimefunItemStack(
            "ME_STORAGE_BUS", new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&fME存储总线"));
    public static final SlimefunItemStack ME_CRAFTING_TERMINAL = new SlimefunItemStack(
            "ME_CRAFTING_TERMINAL", new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&fME合成终端"));
    public static final SlimefunItemStack ME_PATTERN_TERMINAL = new SlimefunItemStack(
            "ME_PATTERN_TERMINAL", new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&fME样板终端"));
    public static final SlimefunItemStack ME_TERMINAL =
            new SlimefunItemStack("ME_TERMINAL", new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&fME终端"));
    public static final SlimefunItemStack ENERGY_ACCEPTOR = new SlimefunItemStack(
            "ENERGY_ACCEPTOR", new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS, "&f能源接收器"));

    // MATERIAL
    static final SlimefunItemStack CRYSTAL_CERTUS_QUARTZ = new SlimefunItemStack(
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
    public static final SlimefunItemStack LOGIC_PROCESSOR = new SlimefunItemStack(
            "LOGIC_PROCESSOR",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Material.SHULKER_SHELL,
                    "{#Sky_Blue}逻辑处理器"));
    public static final SlimefunItemStack CALCULATION_PROCESSOR = new SlimefunItemStack(
            "CALCULATION_PROCESSOR",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Material.SHULKER_SHELL,
                    "{#Sky_Blue}运算处理器"));
    public static final SlimefunItemStack ENGINEERING_PROCESSOR = new SlimefunItemStack(
            "ENGINEERING_PROCESSOR",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Material.SHULKER_SHELL,
                    "{#Sky_Blue}工程处理器"));
    public static final SlimefunItemStack PRINTED_LOGIC_CIRCUIT = new SlimefunItemStack(
            "PRINTED_LOGIC_CIRCUIT", new AdvancedCustomItemStack(Material.ORANGE_DYE, "{#Sky_Blue}逻辑电路板"));
    public static final SlimefunItemStack PRINTED_CALCULATION_CIRCUIT = new SlimefunItemStack(
            "PRINTED_CALCULATION_CIRCUIT", new AdvancedCustomItemStack(Material.LIGHT_BLUE_DYE, "{#Sky_Blue}运算电路板"));
    public static final SlimefunItemStack PRINTED_ENGINEERING_CIRCUIT = new SlimefunItemStack(
            "PRINTED_ENGINEERING_CIRCUIT", new AdvancedCustomItemStack(Material.BLUE_DYE, "{#Sky_Blue}工程电路板"));
    public static final SlimefunItemStack ME_STORAGE_HOUSING = new SlimefunItemStack(
            "ME_STORAGE_HOUSING",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Material.SHULKER_SHELL,
                    "{#Bright_Gray}ME存储外壳"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_1K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_1K", new AdvancedCustomItemStack(Material.SLIME_BALL, "{#3366ff}1k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_4K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_4K", new AdvancedCustomItemStack(Material.FIREWORK_STAR, "{#3388fb}4k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_16K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_16K", new AdvancedCustomItemStack(Material.FIRE_CHARGE, "{#33aaf7}16k-ME存储组件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_COMPONENT_64K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_COMPONENT_64K",
            new AdvancedCustomItemStack(Material.POPPED_CHORUS_FRUIT, "{#33ccf3}64k-ME存储组件"));

    // CELL
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_1K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_1K", new AdvancedCustomItemStack(Material.MUSIC_DISC_13, "{#3366ff}1k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_4K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_4K", new AdvancedCustomItemStack(Material.MUSIC_DISC_CAT, "{#3388fb}4k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_16K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_16K", new AdvancedCustomItemStack(Material.MUSIC_DISC_BLOCKS, "{#33aaf7}16k-ME存储元件"));
    public static final SlimefunItemStack ME_ITEM_STORAGE_CELL_64K = new SlimefunItemStack(
            "ME_ITEM_STORAGE_CELL_64K", new AdvancedCustomItemStack(Material.MUSIC_DISC_CHIRP, "{#33ccf3}64k-ME存储元件"));
    public static final SlimefunItemStack FORMATION_CORE =
            new SlimefunItemStack("FORMATION_CORE", new AdvancedCustomItemStack(Material.GOLD_NUGGET, "{#33ccf3}成型核心"));
    public static final SlimefunItemStack ANNIHILATION_CORE = new SlimefunItemStack(
            "ANNIHILATION_CORE", new AdvancedCustomItemStack(Material.IRON_NUGGET, "{#33ccf3}破坏核心"));
    // card
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
                    "{#33ccf3}加速卡"));
    public static final SlimefunItemStack CAPACITY_CARD = new SlimefunItemStack(
            "CAPACITY_CARD",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1)
                            ? Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Material.SHULKER_SHELL,
                    "{#33ccf3}容量卡"));

    public static final SlimefunItemStack QUARTZ_GLASS =
            new SlimefunItemStack("QUARTZ_GLASS", new AdvancedCustomItemStack(Material.GLASS, "{#33ccf3}石英玻璃"));
    public static final SlimefunItemStack BLANK_PATTERN =
            new SlimefunItemStack("BLANK_PATTERN", new AdvancedCustomItemStack(Material.PAPER, "{#33ccf3}空白样板"));
    public static final SlimefunItemStack ENCODED_PATTERN = new SlimefunItemStack(
            "ENCODED_PATTERN",
            new AdvancedCustomItemStack(
                    Version.getCurrent().isEqualOrHigher(Version.v1_20_R1) ? Material.PRISMARINE_SHARD : Material.PAPER,
                    "{#33ccf3}编码样板"));

    public static void onSetup(SlimeAEPlugin plugin) {
        // Machines
        new MEController(SlimefunAEItemGroups.MACHINE, ME_CONTROLLER, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new MEUnit(SlimefunAEItemGroups.MACHINE, ME_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEInterface(SlimefunAEItemGroups.MACHINE, ME_INTERFACE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEDrive(SlimefunAEItemGroups.MACHINE, ME_DRIVE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, MOLECULAR_ASSEMBLER, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ONE_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, FOUR_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, SIXSTEEN_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, SIXTY_FOUR_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_CO_PROCESSING_UNIT, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_UNIT, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_MONITOR, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ENERGY_CELL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, INSCRIBER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CHARGER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEImportBus(SlimefunAEItemGroups.MACHINE, ME_IMPORT_BUS, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new MEExportBus(SlimefunAEItemGroups.MACHINE, ME_EXPORT_BUS, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new MEStorageBus(SlimefunAEItemGroups.MACHINE, ME_STORAGE_BUS, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_CRAFTING_TERMINAL, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_PATTERN_TERMINAL, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_TERMINAL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ENERGY_ACCEPTOR, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        // Materials

        SlimefunItem crystal_certus_quartz = new SlimefunItem(
                SlimefunAEItemGroups.MATERIAL,
                CRYSTAL_CERTUS_QUARTZ,
                SlimefunAERecipeTypes.BLOCK_DESTROY,
                new ItemStack[] {null, null, null, null, CERTUS_QUARTZ_ORE});
        crystal_certus_quartz.setRecipeOutput(ItemUtils.createItems(CRYSTAL_CERTUS_QUARTZ, 5)[0]);
        crystal_certus_quartz.register(plugin);
        CrystalCertusQuartz crystalCertusQuartz = new CrystalCertusQuartz(plugin, CRYSTAL_CERTUS_QUARTZ);
        crystalCertusQuartz.register();
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL, CHARGED_CRYSTAL_CERTUS_QUARTZ, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CRYSTAL_FLUIX, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL,
                        CERTUS_QUARTZ_ORE,
                        SlimefunAERecipeTypes.WORLD_GENERATING,
                        new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CERTUS_QUARTZ_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    CRYSTAL_CERTUS_QUARTZ, null, null, null, null, null, null, null, null
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, FLUIX_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    CRYSTAL_FLUIX, null, null, null, null, null, null, null, null
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, QUARTZ_DUST, RecipeType.ORE_CRUSHER, new ItemStack[] {
                    new ItemStack(Material.QUARTZ), null, null, null, null, null, null, null, null
                })
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, SKY_STONE_DUST, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, LOGIC_PROCESSOR, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CALCULATION_PROCESSOR, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ENGINEERING_PROCESSOR, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, PRINTED_LOGIC_CIRCUIT, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, PRINTED_CALCULATION_CIRCUIT, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, PRINTED_ENGINEERING_CIRCUIT, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ME_STORAGE_HOUSING, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ME_ITEM_STORAGE_COMPONENT_1K, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ME_ITEM_STORAGE_COMPONENT_4K, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL, ME_ITEM_STORAGE_COMPONENT_16K, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(
                        SlimefunAEItemGroups.MATERIAL, ME_ITEM_STORAGE_COMPONENT_64K, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, FORMATION_CORE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ANNIHILATION_CORE, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, BASIC_CARD, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ADVANCED_CARD, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ACCELERATION_CARD, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, CAPACITY_CARD, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, QUARTZ_GLASS, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, BLANK_PATTERN, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MATERIAL, ENCODED_PATTERN, RecipeType.NULL, new ItemStack[0])
                .register(plugin);
        // Cells

        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL, ME_ITEM_STORAGE_CELL_1K, RecipeType.NULL, new ItemStack[0], 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL, ME_ITEM_STORAGE_CELL_4K, RecipeType.NULL, new ItemStack[0], 4 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_16K,
                        RecipeType.NULL,
                        new ItemStack[0],
                        16 * 1024)
                .register(plugin);
        new MEItemStorageCell(
                        SlimefunAEItemGroups.CELL,
                        ME_ITEM_STORAGE_CELL_64K,
                        RecipeType.NULL,
                        new ItemStack[0],
                        64 * 1024)
                .register(plugin);
    }
}
