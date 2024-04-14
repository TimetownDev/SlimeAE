package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MenuItems {
    public static final SlimefunItemStack Empty = new SlimefunItemStack(
            "_AE_MN_EMPTY_", new AdvancedCustomItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
    public static final SlimefunItemStack Setting = new SlimefunItemStack(
            "_AE_MN_SETTING_", new AdvancedCustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "{#Spring_Green}物品设置槽位"));

    public static final SlimefunItemStack PAGE_PREVIOUS_STACK = new SlimefunItemStack(
            "_AE_MN_PAGE_PREVIOUS_STACK_", new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&b上一页"));

    public static final SlimefunItemStack PAGE_NEXT_STACK = new SlimefunItemStack(
            "_AE_MN_PAGE_NEXT_STACK_", new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c下一页"));

    public static final SlimefunItemStack CHANGE_SORT_STACK = new SlimefunItemStack(
            "_AE_MN_CHANGE_SORT_STACK_", new AdvancedCustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "&e更改排序方式"));

    public static final SlimefunItemStack FILTER_STACK = new SlimefunItemStack(
            "_AE_MN_FILTER_STACK_", new AdvancedCustomItemStack(Material.NAME_TAG, "&e设置过滤器 (右键点击以清除)"));
    public static final SlimefunItemStack PUSH_BACK = new SlimefunItemStack(
            "_AE_MN_PUSH_BACK_", new AdvancedCustomItemStack(Material.BARRIER, "&c&l点击将物品返回ME网络"));
    public static final ItemStack MULTI_INPUT_ITEM =
            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a多物品输入", "", "&7> 单击查看");
    public static final ItemStack MULTI_OUTPUT_ITEM =
            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a多物品输出", "", "&7> 单击查看");
    public static final NamespacedKey MENU_ITEM = new NamespacedKey(SlimeAEPlugin.getInstance(), "menu_item");
}
