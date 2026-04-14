package me.ddggdd135.slimeae.tasks;

import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TaskBackupTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private volatile boolean running = false;
    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        int seconds = plugin.getConfig().getInt("auto-crafting.persistence.backup-interval-seconds", 60);
        if (seconds <= 0) return;
        this.tickRate = seconds * 20;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, tickRate, tickRate);
    }

    @Override
    public void run() {
        if (paused || halted) return;
        synchronized (this) {
            if (running) return;
            running = true;
        }
        try {
            if (SlimeAEPlugin.getCraftTaskPersistence() != null) {
                SlimeAEPlugin.getCraftTaskPersistence().backupAll();
            }
        } catch (Exception | LinkageError e) {
            SlimeAEPlugin.getInstance().getLogger().log(Level.SEVERE, "Exception while backing up craft tasks", e);
        } finally {
            synchronized (this) {
                running = false;
            }
        }
    }

    public void halt() {
        halted = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isRunning() {
        return running;
    }
}
