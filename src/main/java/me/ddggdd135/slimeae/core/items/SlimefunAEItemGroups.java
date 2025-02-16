package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.version.Version;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.groups.DummyItemGroup;
import me.ddggdd135.slimeae.core.groups.MainItemGroup;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SlimefunAEItemGroups {
    public static final ItemStack MAIN_ITEM_GROUP_CURSOR =
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Spring_Green}能源与应用2");
    public static final ItemStack INFO_CURSOR = new AdvancedCustomItemStack(Material.BOOK, "&eSlimeAE信息");
    public static final ItemStack CABLE_CURSOR =
            new AdvancedCustomItemStack(SlimefunItems.ENERGY_CONNECTOR, "{#Vanilla_Ice}线缆");
    public static final ItemStack MACHINE_CURSOR =
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Sky_Blue}机器");
    public static final ItemStack ADVANCED_MACHINE_CURSOR =
            new AdvancedCustomItemStack(Material.PURPLE_STAINED_GLASS, "{#9B30FF}高级机器").doGlow();
    public static final ItemStack MATERIAL_CURSOR = new AdvancedCustomItemStack(
            Version.getCurrent().isEqualOrHigher(Version.v1_17_R1) ? Material.AMETHYST_CLUSTER : Material.QUARTZ,
            "{#Fuchsia_Pink}材料");
    public static final ItemStack CELL_CURSOR = new AdvancedCustomItemStack(Material.SLIME_BALL, "{#3366ff}元件");
    public static final ItemStack TOOL_CURSOR = new AdvancedCustomItemStack(Material.BLAZE_ROD, "&e工具");
    public static final MainItemGroup MAIN_ITEM_GROUP =
            new MainItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "main"), MAIN_ITEM_GROUP_CURSOR);
    public static final DummyItemGroup INFO =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "info"), INFO_CURSOR);
    public static final DummyItemGroup CABLE =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "cable"), CABLE_CURSOR);
    public static final DummyItemGroup MACHINE =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "machine"), MACHINE_CURSOR);
    public static final DummyItemGroup ADVANCED_MACHINE =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "machine"), ADVANCED_MACHINE_CURSOR);
    public static final DummyItemGroup MATERIAL =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "material"), MATERIAL_CURSOR);
    public static final DummyItemGroup CELL =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "material"), CELL_CURSOR);
    public static final DummyItemGroup TOOL =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "tool"), TOOL_CURSOR);

    public static void onSetup(SlimeAEPlugin plugin) {
        MAIN_ITEM_GROUP.register(plugin);
        INFO.register(plugin);
        CABLE.register(plugin);
        MACHINE.register(plugin);
        ADVANCED_MACHINE.register(plugin);
        MATERIAL.register(plugin);
        CELL.register(plugin);
        TOOL.register(plugin);
        MAIN_ITEM_GROUP.addItemGroup(INFO);
        MAIN_ITEM_GROUP.addItemGroup(CABLE);
        MAIN_ITEM_GROUP.addItemGroup(MACHINE);
        MAIN_ITEM_GROUP.addItemGroup(ADVANCED_MACHINE);
        MAIN_ITEM_GROUP.addItemGroup(MATERIAL);
        MAIN_ITEM_GROUP.addItemGroup(CELL);
        MAIN_ITEM_GROUP.addItemGroup(TOOL);
    }
}
