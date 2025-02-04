package me.ddggdd135.slimeae.core.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.ArrayList;
import java.util.List;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MenuItems {
    public static final SlimefunItemStack Empty = new SlimefunItemStack(
            "_AE_MN_EMPTY_", new AdvancedCustomItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
    public static final SlimefunItemStack Setting = new SlimefunItemStack(
            "_AE_MN_SETTING_", new AdvancedCustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "{#Spring_Green}物品设置槽位"));
    public static final SlimefunItemStack Pattern = new SlimefunItemStack(
            "_AE_MN_PATTERN_", new AdvancedCustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "{#Spring_Green}编码样板槽位"));
    public static final SlimefunItemStack Card = new SlimefunItemStack(
            "_AE_MN_PATTERN_", new AdvancedCustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "{#Spring_Green}升级卡槽位"));

    public static final SlimefunItemStack PAGE_PREVIOUS_STACK = new SlimefunItemStack(
            "_AE_MN_PAGE_PREVIOUS_STACK_", new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c上一页"));

    public static final SlimefunItemStack PAGE_NEXT_STACK = new SlimefunItemStack(
            "_AE_MN_PAGE_NEXT_STACK_", new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a下一页"));
    public static final SlimefunItemStack INPUT_MODE = new SlimefunItemStack(
            "_AE_MN_INPUT_MODE_",
            new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a输入模式", "", "&e左侧存储元件的物品将会输入AE网络"));

    public static final SlimefunItemStack OUTPUT_MODE = new SlimefunItemStack(
            "_AE_MN_OUTPUT_MODE_",
            new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&a输出模式", "", "&eAE网络中的物品将会输出到左侧存储元件"));

    public static final SlimefunItemStack CHANGE_SORT_STACK = new SlimefunItemStack(
            "_AE_MN_CHANGE_SORT_STACK_", new AdvancedCustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "&e更改排序方式"));

    public static final SlimefunItemStack FILTER_STACK = new SlimefunItemStack(
            "_AE_MN_FILTER_STACK_", new AdvancedCustomItemStack(Material.NAME_TAG, "&e设置过滤器 (右键点击以清除)"));
    public static final SlimefunItemStack PUSH_BACK = new SlimefunItemStack(
            "_AE_MN_PUSH_BACK_", new AdvancedCustomItemStack(Material.BARRIER, "&c&l点击将物品返回ME网络"));
    public static final ItemStack CRAFT_ITEM =
            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a合成按钮", "", "&7> 单击合成");
    public static final ItemStack CRAFTING_TABLE;
    public static final ItemStack COOKING = new CustomItemStack(Material.FURNACE, "&e流程配方", "", "&7> 单击切换为工作台配方");
    public static final SlimefunItemStack ACCEPT = new SlimefunItemStack(
            "_AE_MN_ACCEPT_", new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a&l确认"));
    public static final SlimefunItemStack CANCEL = new SlimefunItemStack(
            "_AE_MN_CANCEL_", new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c&l取消"));
    public static final SlimefunItemStack ME_SECURITY_TERMINAL_DESCRIPTION = new SlimefunItemStack(
            "_AE_MN_ME_SECURITY_TERMINAL_DESCRIPTION_",
            new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a&l将无线终端放在上方以绑定"));
    public static final NamespacedKey MENU_ITEM = new NamespacedKey(SlimeAEPlugin.getInstance(), "menu_item");

    static {
        List<String> CRAFTING_TABLE_LORE = new ArrayList<>(List.of(
                "",
                "&e支持以下合成配方:",
                "&e  - 原版工作台",
                "&b  - 增强型工作台",
                "&e  - 充能器",
                "&7  - 压印机",
                "&d  - 魔法工作台",
                "&6  - 盔甲锻造台",
                "&e  - 冶炼炉",
                "&a  - 压缩机",
                "&7  - 磨石",
                "&5  - 榨汁机",
                "&6  - 矿物粉碎机",
                "&f  - 压力机"));

        if (SlimeAEPlugin.getTranscEndenceIntegration().isLoaded()) {
            CRAFTING_TABLE_LORE.add("{#299482}  - 纳米工作台");
        }

        CRAFTING_TABLE_LORE.add("&7> 单击切换为流程配方");
        CRAFTING_TABLE = new AdvancedCustomItemStack(Material.CRAFTING_TABLE, "&e工作台配方", CRAFTING_TABLE_LORE);
    }
}
