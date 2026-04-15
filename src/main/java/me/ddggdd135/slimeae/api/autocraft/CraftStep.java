package me.ddggdd135.slimeae.api.autocraft;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.bukkit.Location;

public class CraftStep {
    private final CraftingRecipe recipe;
    private long amount;
    private final Set<Location> runningDevices = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int virtualRunning;
    private int virtualProcess;

    public CraftStep(@Nonnull CraftingRecipe recipe, long amount) {
        this.recipe = recipe;
        this.amount = amount;
    }

    @Nonnull
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void decreaseAmount(long value) {
        this.amount -= value;
    }

    public int getRunning() {
        return runningDevices.size();
    }

    public void addRunningDevice(@Nonnull Location location) {
        runningDevices.add(location);
    }

    public void removeRunningDevice(@Nonnull Location location) {
        runningDevices.remove(location);
    }

    @Nonnull
    public Set<Location> getRunningDevices() {
        return Collections.unmodifiableSet(runningDevices);
    }

    public void setRunning(int running) {
        // no-op kept for compatibility
    }

    public void incrementRunning() {
        // no-op - use addRunningDevice instead
    }

    public void decrementRunning() {
        // no-op - use removeRunningDevice instead
    }

    public int getVirtualRunning() {
        return virtualRunning;
    }

    public void setVirtualRunning(int virtualRunning) {
        this.virtualRunning = virtualRunning;
    }

    public int getVirtualProcess() {
        return virtualProcess;
    }

    public void setVirtualProcess(int virtualProcess) {
        this.virtualProcess = virtualProcess;
    }

    public void addVirtualProcess(int value) {
        this.virtualProcess += value;
    }

    public boolean isIdle() {
        return runningDevices.isEmpty() && virtualRunning <= 0;
    }

    public boolean isCompleted() {
        return amount <= 0 && isIdle();
    }
}
