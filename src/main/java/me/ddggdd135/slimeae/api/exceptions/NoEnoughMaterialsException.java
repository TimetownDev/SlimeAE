package me.ddggdd135.slimeae.api.exceptions;

import java.util.Map;
import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;

public class NoEnoughMaterialsException extends RuntimeException {
    private final Map<ItemStack, Integer> missingMaterials;

    public NoEnoughMaterialsException(@Nonnull Map<ItemStack, Integer> missingMaterials) {
        this.missingMaterials = missingMaterials;
    }

    @Nonnull
    public Map<ItemStack, Integer> getMissingMaterials() {
        return missingMaterials;
    }
}
