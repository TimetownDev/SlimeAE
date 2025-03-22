package me.ddggdd135.slimeae.tasks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.scheduler.BukkitScheduler;

public class NetworkCheckTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay") / 2;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, 100L, tickRate);
    }

    private void reset() {
        running.set(false);
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        try {
            // If this method is actually still running... DON'T
            if (!running.compareAndSet(false, true)) {
                return;
            }

            // Run our ticker code
            if (!halted) {
                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);

                for (NetworkInfo networkInfo : allNetworkData) {
                    SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(networkInfo.getController());
                    if (slimefunBlockData == null) {
                        networkInfo.dispose();
                        continue;
                    }

                    SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
                    if (!(slimefunItem instanceof IMEController)) {
                        networkInfo.dispose();
                    }
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, x, () -> "An Exception was caught while checking the MEControllers for SlimeAE");
        } finally {
            reset();
        }
    }

    public boolean isHalted() {
        return halted;
    }

    public void halt() {
        halted = true;
    }

    public int getTickRate() {
        return tickRate;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
