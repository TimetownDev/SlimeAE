package me.ddggdd135.slimeae.core.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

public class BlockListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent e) {
        if (e.getBlock().getType() == Material.NETHER_QUARTZ_ORE) {
            Random random = new Random();
            //TODO
        }
    }
}
