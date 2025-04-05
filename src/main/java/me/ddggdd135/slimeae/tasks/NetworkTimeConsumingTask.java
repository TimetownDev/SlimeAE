package me.ddggdd135.slimeae.tasks;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.Bukkit;

public class NetworkTimeConsumingTask implements Runnable {
    private int tickRate;
    private boolean halted = false;

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay");
        run();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        run0();

        long elapsed = System.currentTimeMillis() - startTime;
        long nextDelay = Math.max(tickRate - elapsed, 0);
        Bukkit.getScheduler().runTaskLaterAsynchronously(SlimeAEPlugin.getInstance(), this, nextDelay);
    }

    public void run0() {
        if (paused) {
            return;
        }

        try {
            // Run our ticker code
            if (!halted) {
                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);

                for (NetworkInfo networkInfo : allNetworkData) {
                    networkInfo.getChildren().forEach(x -> {
                        IMEObject slimefunItem =
                                SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(x);
                        if (slimefunItem == null) return;
                        slimefunItem.onNetworkTimeConsumingTick(x.getBlock(), networkInfo);
                    });
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, x, () -> "An Exception was caught while ticking Networks for SlimeAE");
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
