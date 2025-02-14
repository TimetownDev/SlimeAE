package me.ddggdd135.slimeae.core.recipes;

import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;

public class CraftingOperation implements MachineOperation {
    private final int ticks;
    private int process = 0;
    private CraftingRecipe recipe;

    public CraftingOperation(int ticks, @Nonnull CraftingRecipe recipe) {
        this.ticks = ticks;
        this.recipe = recipe;
    }

    @Override
    public void addProgress(int i) {
        process += i;
    }

    @Override
    public int getProgress() {
        return process;
    }

    @Override
    public int getTotalTicks() {
        return ticks;
    }

    @Nonnull
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftingOperation that = (CraftingOperation) o;
        return ticks == that.ticks && Objects.equals(recipe, that.recipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticks, recipe);
    }
}
