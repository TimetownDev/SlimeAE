package me.ddggdd135.slimeae.core.items;

import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.groups.DummyItemGroup;
import me.ddggdd135.slimeae.core.groups.MainItemGroup;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import net.Zrips.CMILib.Version.Version;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SlimefunAEItemGroups {
    public static final ItemStack MAIN_ITEM_GROUP_CURSOR =
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Spring_Green}能源应用2");
    public static final ItemStack MACHINE_CURSOR =
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Sky_Blue}机器");
    public static final ItemStack MATERIAL_CURSOR = new AdvancedCustomItemStack(
            Version.getCurrent().isEqualOrHigher(Version.v1_17_R1) ? Material.AMETHYST_CLUSTER : Material.QUARTZ,
            "{#Fuchsia_Pink}材料");
    public static final ItemStack CELL_CURSOR = new AdvancedCustomItemStack(Material.SLIME_BALL, "{#3366ff}元件");
    public static final MainItemGroup MAIN_ITEM_GROUP =
            new MainItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "main"), MAIN_ITEM_GROUP_CURSOR);
    public static final DummyItemGroup MACHINE =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "machine"), MACHINE_CURSOR);
    public static final DummyItemGroup MATERIAL =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "material"), MATERIAL_CURSOR);
    public static final DummyItemGroup CELL =
            new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "material"), CELL_CURSOR);

    public static void onSetup(SlimeAEPlugin plugin) {
        MAIN_ITEM_GROUP.register(plugin);
        MACHINE.register(plugin);
        MATERIAL.register(plugin);
        MAIN_ITEM_GROUP.addItemGroup(MACHINE);
        MAIN_ITEM_GROUP.addItemGroup(MATERIAL);
        MAIN_ITEM_GROUP.addItemGroup(CELL);
    }
}
