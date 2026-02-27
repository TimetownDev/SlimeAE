package me.ddggdd135.slimeae.api.exceptions;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;

public class NoEnoughMaterialsException extends RuntimeException {
    private final ItemHashMap<Long> missingMaterials;

    public NoEnoughMaterialsException(@Nonnull ItemHashMap<Long> missingMaterials) {
        super(null, null, true, false); // 禁用栈跟踪捕获，避免性能开销
        this.missingMaterials = missingMaterials;
    }

    @Nonnull
    public ItemHashMap<Long> getMissingMaterials() {
        return missingMaterials;
    }
}
