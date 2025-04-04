package me.ddggdd135.slimeae.utils;

import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockUtils {
    public static void breakBlock(@Nonnull Block block, @Nonnull Player player) {
        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (!breakEvent.isCancelled()) {
            block.setType(Material.AIR);
        }
    }
}
