package me.ddggdd135.slimeae.core.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Random;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
    private static void doDrop(Location location, ItemStack itemStack) {
        Random random = new Random();
        int i = random.nextInt(0, 100);
        if (i <= 35) {
            ItemStack result = itemStack.clone();
            result.setAmount(random.nextInt(1, itemStack.getAmount()));
            location.getWorld().dropItem(location, result);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent e) {
        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (slimefunItem.getRecipeType() == SlimefunAERecipeTypes.BLOCK_DESTROY) {
                for (ItemStack itemStack : slimefunItem.getRecipe()) {
                    if (itemStack == null || itemStack.getType().isAir()) continue;
                    SlimefunItem sfItem = SlimefunItem.getByItem(itemStack);
                    if (sfItem != null) {
                        SlimefunItem b =
                                StorageCacheUtils.getSfItem(e.getBlock().getLocation());
                        if (b == sfItem) {
                            doDrop(e.getBlock().getLocation(), itemStack);
                        }
                    } else if (e.getBlock().getType() == itemStack.getType()) {
                        doDrop(e.getBlock().getLocation(), itemStack);
                    }
                }
            }
        }
    }
}
