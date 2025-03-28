package me.ddggdd135.slimeae.api.autocraft;

import javax.annotation.Nonnull;

public class CraftStep {
    private final CraftingRecipe recipe;
    private long amount;

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
}
