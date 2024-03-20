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

public class SlimefunAEItems {
    public static final SlimefunItemStack ME_CONTROLLER = new SlimefunItemStack("ME_CONTROLLER", new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Bright_Gray}ME控制器"));
    public static final SlimefunItemStack ME_UNIT = new SlimefunItemStack("ME_UNIT", new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Starship}ME单元"));
    public static final SlimefunItemStack ME_INTERFACE = new SlimefunItemStack("ME_INTERFACE", new AdvancedCustomItemStack(Material.TARGET, "{#Vanilla_Ice}ME接口"));
    public static void onSetup(SlimeAEPlugin plugin) {
        new MEController(SlimefunAEItemGroups.MACHINE, ME_CONTROLLER, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEUnit(SlimefunAEItemGroups.MACHINE, ME_UNIT, RecipeType.NULL, new ItemStack[0]).register(plugin);
        new MEInterface(SlimefunAEItemGroups.MACHINE, ME_INTERFACE, RecipeType.NULL, new ItemStack[0]).register(plugin);
    }
}
