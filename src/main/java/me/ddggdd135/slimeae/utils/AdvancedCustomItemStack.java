package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import net.Zrips.CMILib.Colors.CMIChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AdvancedCustomItemStack extends CustomItemStack {
    public AdvancedCustomItemStack(ItemStack item) {
        super(item);
    }

    public AdvancedCustomItemStack(Material type) {
        super(type);
    }

    public AdvancedCustomItemStack(ItemStack item, Consumer<ItemMeta> meta) {
        super(item, meta);
    }

    public AdvancedCustomItemStack(Material type, Consumer<ItemMeta> meta) {
        super(type, meta);
    }

    public AdvancedCustomItemStack(ItemStack item, String name, String... lore) {
        super(item, CMIChatColor.translate(name), ColorUtils.translateAll(lore));
    }

    public AdvancedCustomItemStack(ItemStack item, Color color, String name, String... lore) {
        super(item, color, CMIChatColor.translate(name), ColorUtils.translateAll(lore));
    }

    public AdvancedCustomItemStack(Material type, String name, String... lore) {
        super(type, CMIChatColor.translate(name), ColorUtils.translateAll(lore));
    }

    public AdvancedCustomItemStack(Material type, String name, List<String> lore) {
        super(type, CMIChatColor.translate(name), ColorUtils.translateAll(String.valueOf(lore)));
    }

    public AdvancedCustomItemStack(ItemStack item, List<String> list) {
        super(item, CMIChatColor.translate(list));
    }

    public AdvancedCustomItemStack(Material type, List<String> list) {
        super(type, CMIChatColor.translate(list));
    }

    public AdvancedCustomItemStack(ItemStack item, int amount) {
        super(item, amount);
    }

    public AdvancedCustomItemStack(ItemStack item, Material type) {
        super(item, type);
    }
}
