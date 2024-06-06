package me.ddggdd135.slimeae.utils;

import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerBoxUtils {
    public static boolean isEmpty(@Nullable ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta)) {
            return false; // 不是潜影盒
        }

        if (!(blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox)) {
            return false; // 不是潜影盒
        }

        Inventory inventory = shulkerBox.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) {
                return false; // 如果任何一个槽位有物品，则不为空
            }
        }

        return true; // 如果所有槽位都为空，则为空
    }

    public static boolean isShulkerBox(ItemStack item) {
        if (item == null) {
            return false;
        }

        Material type = item.getType();
        return type == Material.SHULKER_BOX
                || type == Material.BLACK_SHULKER_BOX
                || type == Material.BLUE_SHULKER_BOX
                || type == Material.BROWN_SHULKER_BOX
                || type == Material.CYAN_SHULKER_BOX
                || type == Material.GRAY_SHULKER_BOX
                || type == Material.GREEN_SHULKER_BOX
                || type == Material.LIGHT_BLUE_SHULKER_BOX
                || type == Material.LIGHT_GRAY_SHULKER_BOX
                || type == Material.LIME_SHULKER_BOX
                || type == Material.MAGENTA_SHULKER_BOX
                || type == Material.ORANGE_SHULKER_BOX
                || type == Material.PINK_SHULKER_BOX
                || type == Material.PURPLE_SHULKER_BOX
                || type == Material.RED_SHULKER_BOX
                || type == Material.WHITE_SHULKER_BOX
                || type == Material.YELLOW_SHULKER_BOX;
    }
}
