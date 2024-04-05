package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class MenuItems {
    public static final SlimefunItemStack Empty = new SlimefunItemStack(
            "_AE_MN_EMPTY_", new AdvancedCustomItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
    public static final SlimefunItemStack Setting = new SlimefunItemStack(
            "_AE_MN_SETTING_", new AdvancedCustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "{#Spring_Green}物品设置槽位"));
    public static final NamespacedKey MENU_ITEM = new NamespacedKey(SlimeAEPlugin.getInstance(), "menu_item");
}
