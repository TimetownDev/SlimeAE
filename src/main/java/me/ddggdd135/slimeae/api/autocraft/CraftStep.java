package me.ddggdd135.slimeae.api.autocraft;

import javax.annotation.Nonnull;

public class CraftStep {
    private final CraftingRecipe recipe;
    private long amount;
    private int running;
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
        return running;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public void incrementRunning() {
        this.running++;
    }

    public void decrementRunning() {
        this.running--;
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
        return running <= 0 && virtualRunning <= 0;
    }

    public boolean isCompleted() {
        return amount <= 0 && isIdle();
    }
}
