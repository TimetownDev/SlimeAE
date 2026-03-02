package me.ddggdd135.slimeae.core.listeners;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.logging.Level;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.reskin.ReskinApplier;
import me.ddggdd135.slimeae.api.reskin.ReskinDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ReskinListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlimefunBlockPlace(SlimefunBlockPlaceEvent e) {
        if (!isReskinEnabled()) return;

        ItemStack placedItem = e.getItemStack();

        String[] reskinData = ReskinDataManager.getReskinData(placedItem);
        if (reskinData == null) return;

        String type = reskinData[0];
        String value = reskinData[1];
        Block block = e.getBlockPlaced();
        Location blockLoc = block.getLocation().clone();

        SlimeAEPlugin.getInstance()
                .getLogger()
                .log(Level.INFO, "[Reskin] Detected reskin data on place: type={0}, value={1}, loc={2}", new Object[] {
                    type, value, blockLoc
                });

        Bukkit.getScheduler()
                .runTaskLater(
                        SlimeAEPlugin.getInstance(),
                        () -> {
                            SlimefunBlockData blockData = StorageCacheUtils.getBlock(blockLoc);
                            if (blockData == null) {
                                SlimeAEPlugin.getInstance()
                                        .getLogger()
                                        .log(
                                                Level.WARNING,
                                                "[Reskin] Block data is null at {0}, skip reskin apply",
                                                blockLoc);
                                return;
                            }

                            Block targetBlock = blockLoc.getBlock();
                            SlimeAEPlugin.getInstance()
                                    .getLogger()
                                    .log(
                                            Level.INFO,
                                            "[Reskin] Applying reskin: type={0}, value={1}, block={2}",
                                            new Object[] {type, value, targetBlock.getType()});
                            ReskinApplier.applyReskin(targetBlock, type, value);
                            SlimeAEPlugin.getInstance()
                                    .getLogger()
                                    .log(
                                            Level.INFO,
                                            "[Reskin] After apply: block type is now {0}",
                                            targetBlock.getType());

                            SlimeAEPlugin.getReskinDataController().setReskinData(blockLoc, type, value);
                        },
                        3L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!isReskinEnabled()) return;

        Block block = e.getBlock();
        Location loc = block.getLocation();

        String[] reskinData = SlimeAEPlugin.getReskinDataController().getReskinData(loc);
        if (reskinData == null) return;

        SlimefunBlockData sfBlockData = StorageCacheUtils.getBlock(loc);
        String sfId = sfBlockData != null ? sfBlockData.getSfId() : null;

        SlimeAEPlugin.getReskinDataController().removeReskinData(loc);

        String savedType = reskinData[0];
        String savedValue = reskinData[1];
        Bukkit.getScheduler()
                .runTaskLater(
                        SlimeAEPlugin.getInstance(),
                        () -> {
                            for (Entity entity : block.getWorld()
                                    .getNearbyEntities(loc.clone().add(0.5, 0.5, 0.5), 1.5, 1.5, 1.5)) {
                                if (!(entity instanceof Item itemEntity)) continue;
                                ItemStack drop = itemEntity.getItemStack();
                                SlimefunItem sfItem = SlimefunItem.getByItem(drop);
                                if (sfItem != null
                                        && sfId != null
                                        && sfItem.getId().equals(sfId)) {
                                    ReskinDataManager.addReskinData(drop, savedType, savedValue);
                                    itemEntity.setItemStack(drop);
                                }
                            }
                        },
                        1L);
    }

    private boolean isReskinEnabled() {
        return SlimeAEPlugin.getInstance().getConfig().getBoolean("reskin-machine.enable", true);
    }
}
