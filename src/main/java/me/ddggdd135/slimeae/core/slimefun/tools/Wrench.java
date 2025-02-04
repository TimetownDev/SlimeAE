package me.ddggdd135.slimeae.core.slimefun.tools;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.utils.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Wrench extends SlimefunItem {
    public static final Set<String> SUPPORTED_BLOCK_ID = new HashSet<>();

    public Wrench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
            e.cancel();
            if (e.getClickedBlock().isEmpty()) return;
            Block block = e.getClickedBlock().get();
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
            if (slimefunBlockData == null) return;
            if (!SUPPORTED_BLOCK_ID.contains(slimefunBlockData.getSfId())) return;

            BlockUtils.breakBlock(block, e.getPlayer());
        });
    }

    static {
        for (Field field : SlimefunAEItems.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                Object object = field.get(null);
                if (object instanceof ItemStack itemStack) {
                    SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
                    if (slimefunItem == null) continue;
                    SUPPORTED_BLOCK_ID.add(slimefunItem.getId());
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }
}
