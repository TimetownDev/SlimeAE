package me.ddggdd135.slimeae.core.items;

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

    public static void onSetup(SlimeAEPlugin plugin) {
        new MEController(SlimefunAEItemGroups.MACHINE, ME_CONTROLLER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEUnit(SlimefunAEItemGroups.MACHINE, ME_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEInterface(SlimefunAEItemGroups.MACHINE, ME_INTERFACE, RecipeType.NULL, new ItemStack[0]).register(plugin);
    }
}
