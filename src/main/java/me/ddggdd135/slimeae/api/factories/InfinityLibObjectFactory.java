package me.ddggdd135.slimeae.api.factories;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.wrappers.CraftCraftingBlock;
import me.ddggdd135.slimeae.api.wrappers.CraftCraftingBlockRecipe;
import me.ddggdd135.slimeae.api.wrappers.CraftInfinityLibObject;
import me.ddggdd135.slimeae.utils.InfinityLibUtils;

public class InfinityLibObjectFactory {
    @Nullable public CraftInfinityLibObject getObject(@Nonnull Object o) {
        if (InfinityLibUtils.isCraftingBlock(o)) return new CraftCraftingBlock(o);
        if (InfinityLibUtils.isCraftingBlockRecipe(o)) return new CraftCraftingBlockRecipe(o);
        return null;
    }
}
