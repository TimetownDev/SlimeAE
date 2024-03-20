package me.ddggdd135.slimeae.core.items;

import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.groups.DummyItemGroup;
import me.ddggdd135.slimeae.core.groups.MainItemGroup;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SlimefunAEItemGroups {
    public static final ItemStack MAIN_ITEM_GROUP_CURSOR = new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Spring_Green}能源与应用");
    public static final ItemStack MACHINE_CURSOR = new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "{#Sky_Blue}机器");
    public static final MainItemGroup MAIN_ITEM_GROUP = new MainItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "main"), MAIN_ITEM_GROUP_CURSOR);
    public static final DummyItemGroup MACHINE = new DummyItemGroup(new NamespacedKey(SlimeAEPlugin.getInstance(), "machine"), MACHINE_CURSOR);
    public static void onSetup(SlimeAEPlugin plugin) {
        MAIN_ITEM_GROUP.register(plugin);
        MACHINE.register(plugin);
        MAIN_ITEM_GROUP.addItemGroup(MACHINE);
    }
}
