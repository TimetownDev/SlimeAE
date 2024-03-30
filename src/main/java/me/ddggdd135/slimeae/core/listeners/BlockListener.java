package me.ddggdd135.slimeae.core.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
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
                        if (b == sfItem) return; // TODO
                    }
                }
            }
        }
    }
}
