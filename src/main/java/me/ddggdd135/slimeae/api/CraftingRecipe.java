package me.ddggdd135.slimeae.api;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import org.bukkit.inventory.ItemStack;

public class CraftingRecipe {
    private final CraftType craftType;
    private final ItemStack[] input;
    private final ItemStack[] output;

    public CraftingRecipe(@Nonnull CraftType craftType, @Nonnull ItemStack[] input, @Nonnull ItemStack[] output) {
        this.craftType = craftType;
        this.input = input;
        this.output = output;
    }

    public CraftingRecipe(@Nonnull CraftType craftType, @Nonnull ItemStack[] input, @Nonnull ItemStack output) {
        this(craftType, input, new ItemStack[] {output});
    }

    @Nonnull
    public CraftType getCraftType() {
        return craftType;
    }

    @Nonnull
    public ItemStack[] getInput() {
        return input.clone();
    }

    @Nonnull
    public ItemStack[] getOutput() {
        return output.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftingRecipe that = (CraftingRecipe) o;
        return craftType == that.craftType && Arrays.equals(input, that.input) && Arrays.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(craftType);
        result = 31 * result + Arrays.hashCode(input);
        result = 31 * result + Arrays.hashCode(output);
        return result;
    }
}
