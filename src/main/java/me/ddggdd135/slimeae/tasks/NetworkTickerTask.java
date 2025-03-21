package me.ddggdd135.slimeae.tasks;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingSession;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.entity.HumanEntity;
import org.bukkit.scheduler.BukkitScheduler;

public class NetworkTickerTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private volatile boolean running = false;

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay") / 2;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, 100L, tickRate);
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
                    new HashSet<>(networkInfo.getChildren()).forEach(x -> {
                        IMEObject slimefunItem =
                                SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(x);
                        if (slimefunItem == null) return;
                        slimefunItem.onNetworkTick(x.getBlock(), networkInfo);
                    });

                    // tick autoCrafting
                    Set<AutoCraftingSession> sessions = new HashSet<>(networkInfo.getCraftingSessions());
                    for (AutoCraftingSession session : sessions) {
                        if (!session.hasNext()) {
                            networkInfo.getCraftingSessions().remove(session);
                            Slimefun.runSync(() -> {
                                session.getMenu().getInventory().getViewers().forEach(HumanEntity::closeInventory);
                            });
                        } else session.moveNext(1024);
                    }
                    networkInfo.updateAutoCraftingMenu();
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(
                            Level.SEVERE,
                            x,
                            () -> "An Exception was caught while ticking the Network Tickers Task for SlimeAE");
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
