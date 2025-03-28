package me.ddggdd135.slimeae.api.exceptions;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;

public class NoEnoughMaterialsException extends RuntimeException {
    private final ItemHashMap<Long> missingMaterials;

    public NoEnoughMaterialsException(@Nonnull ItemHashMap<Long> missingMaterials) {
        this.missingMaterials = missingMaterials;
    }

    @Nonnull
    public ItemHashMap<Long> getMissingMaterials() {
        return missingMaterials;
    }
}
