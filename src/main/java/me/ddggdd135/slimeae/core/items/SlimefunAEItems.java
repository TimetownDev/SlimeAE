package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.slimefun.MEController;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
import me.ddggdd135.slimeae.core.slimefun.MEUnit;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

public class SlimefunAEItems {
    public static final SlimefunItemStack ME_CONTROLLER = new SlimefunItemStack(
            "ME_CONTROLLER",
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS,
            "{#Bright_Gray}ME控制器"));
    public static final SlimefunItemStack ME_UNIT = new SlimefunItemStack(
            "ME_UNIT",
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS,
            "{#Starship}ME单元"));
    public static final SlimefunItemStack ME_INTERFACE = new SlimefunItemStack(
            "ME_INTERFACE",
            new AdvancedCustomItemStack(Material.TARGET,
            "{#Vanilla_Ice}ME接口"));
    public static final SlimefunItemStack ME_DRIVER = new SlimefunItemStack(
            "ME_DRIVE",
            new AdvancedCustomItemStack(Material.CHISELED_BOOKSHELF),
            "&fME驱动器"
    );
    public static final SlimefunItemStack MOLECULAR_ASSEMBLER = new SlimefunItemStack(
            "MOLECULAR_ASSEMBLER",
            new AdvancedCustomItemStack(Material.TINTED_GLASS),
            "&f分子装配室"
    );
    public static final SlimefunItemStack ONE_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "ONE_K_CRAFTING_STORAGE",
            new AdvancedCustomItemStack(Material.LIME_CONCRETE),
            "&f1K合成储存器"
    );
    public static final SlimefunItemStack FOUR_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "FOUR_K_CRAFTING_STORAGE",
            new AdvancedCustomItemStack(Material.GREEN_CONCRETE),
            "&f4K合成储存器"
    );
    public static final SlimefunItemStack SIXSTEEN_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "SIXSTEEN_K_CRAFTING_STORAGE",
            new AdvancedCustomItemStack(Material.CYAN_CONCRETE),
            "&f16K合成储存器"
    );
    public static final SlimefunItemStack SIXTY_FOUR_K_CRAFTING_STORAGE = new SlimefunItemStack(
            "SIXTY_FOUR_K_CRAFTING_STORAGE",
            new AdvancedCustomItemStack(Material.LIGHT_BLUE_CONCRETE),
            "&f64K合成储存器"
    );
    public static final SlimefunItemStack CRAFTING_CO_PROCESSING_UNIT = new SlimefunItemStack(
            "CRAFTING_CO_PROCESSING_UNIT",
            new AdvancedCustomItemStack(Material.PURPLE_GLAZED_TERRACOTTA),
            "&f并行处理单元"
    );
    public static final SlimefunItemStack CRAFTING_UNIT = new SlimefunItemStack(
            "CRAFTING_UNIT",
            new AdvancedCustomItemStack(Material.CHISELED_STONE_BRICKS),
            "&f合成单元"
    );
    public static final SlimefunItemStack CRAFTING_MONITOR = new SlimefunItemStack(
            "CRAFTING_MONITOR",
            new AdvancedCustomItemStack(Material.SEA_LANTERN),
            "&f合成监视器"
    );
    public static final SlimefunItemStack ENERGY_CELL = new SlimefunItemStack(
            "ENERGY_CELL",
            new AdvancedCustomItemStack(Material.AMETHYST_BLOCK),
            "&f能源元件"
    );
    public static final SlimefunItemStack INSCRIBER = new SlimefunItemStack(
            "INSCRIBER",
            new AdvancedCustomItemStack(Material.ANVIL),
            "&f压印机"
    );
    public static final SlimefunItemStack CHARGER = new SlimefunItemStack(
            "CHARGER",
            new AdvancedCustomItemStack(Material.LECTERN),
            "&f充能器"
    );
    public static final SlimefunItemStack ME_IMPORT_BUS = new SlimefunItemStack(
            "ME_IMPORT_BUS",
            new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS),
            "&fME输入总线"
    );
    public static final SlimefunItemStack ME_EXPORT_BUS = new SlimefunItemStack(
            "ME_EXPORT_BUS",
            new AdvancedCustomItemStack(Material.RED_STAINED_GLASS),
            "&fME输出总线"
    );
    public static final SlimefunItemStack ME_STORAGE_BUS = new SlimefunItemStack(
            "ME_STORAGE_BUS",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS),
            "&fME存储总线"
    );
    public static final SlimefunItemStack ME_CRAFTING_TERMINAL = new SlimefunItemStack(
            "ME_CRAFTING_TERMINAL",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS),
            "&fME合成终端"
    );
    public static final SlimefunItemStack ME_PATTERN_TERMINAL = new SlimefunItemStack(
            "ME_PATTERN_TERMINAL",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS),
            "&fME样板终端"
    );
    public static final SlimefunItemStack ME_TERMINAL = new SlimefunItemStack(
            "ME_TERMINAL",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS),
            "&fME终端"
    );
    public static final SlimefunItemStack ENERGY_ACCEPTOR = new SlimefunItemStack(
            "ENERGY_ACCEPTOR",
            new AdvancedCustomItemStack(Material.WHITE_STAINED_GLASS),
            "&f能源接收器"
    );

    public static void onSetup(SlimeAEPlugin plugin) {
        new MEController(SlimefunAEItemGroups.MACHINE, ME_CONTROLLER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEUnit(SlimefunAEItemGroups.MACHINE, ME_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEInterface(SlimefunAEItemGroups.MACHINE, ME_INTERFACE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_DRIVER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, MOLECULAR_ASSEMBLER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ONE_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, FOUR_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, SIXSTEEN_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, SIXTY_FOUR_K_CRAFTING_STORAGE, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_CO_PROCESSING_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CRAFTING_MONITOR, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ENERGY_CELL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, INSCRIBER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, CHARGER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_IMPORT_BUS, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_EXPORT_BUS, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_STORAGE_BUS, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_CRAFTING_TERMINAL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_PATTERN_TERMINAL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ME_TERMINAL, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new SlimefunItem(SlimefunAEItemGroups.MACHINE, ENERGY_ACCEPTOR, RecipeType.NULL, new ItemStack[0]).register(plugin);
    }
}
