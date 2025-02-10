package me.ddggdd135.slimeae.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfinityLibObjectFactory {
    @Nullable public CraftInfinityLibObject getObject(@Nonnull Object o) {
        if (InfinityLibUtils.isCraftingBlock(o)) return new CraftCraftingBlock(o);
        if (InfinityLibUtils.isCraftingBlockRecipe(o)) return new CraftCraftingBlockRecipe(o);
        return null;
    }
}
