package me.ddggdd135.slimeae.tasks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DataSavingTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = SlimeAEPlugin.getInstance().getConfig().getInt("auto-save-period", 300) * 20;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, tickRate, tickRate);
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

            if (!halted) {
                SlimeAEPlugin.getStorageCellDataController().saveAllAsync();
                SlimeAEPlugin.getInstance().getLogger().info("开始保存ME存储元件数据");
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, x, () -> "An Exception was caught while saving data for SlimeAE");
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
