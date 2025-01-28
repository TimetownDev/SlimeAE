package me.ddggdd135.slimeae.tasks;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class NetworkRefreshTask implements Runnable{
    private int tickRate;
    private boolean halted = false;
    private boolean running = false;

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay");

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, tickRate, tickRate);
    }

    private void reset() {
        running = false;
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        try {
            // If this method is actually still running... DON'T
            if (running) {
                return;
            }

            running = true;
            // Run our ticker code
            if (!halted) {

                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);

                for (NetworkInfo networkInfo : allNetworkData) {
                    SlimeAEPlugin.getNetworkData().refreshNetwork(networkInfo.getController());
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(
                            Level.SEVERE,
                            x,
                            () -> "An Exception was caught while refresh Networks for SlimeAE");
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
