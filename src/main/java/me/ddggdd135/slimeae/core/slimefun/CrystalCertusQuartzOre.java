package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrystalCertusQuartzOre extends SimpleSlimefunItem<BlockBreakHandler> {
    protected CrystalCertusQuartzOre(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @NotNull @Override
    public BlockBreakHandler getItemHandler() {
        return new BlockBreakHandler(true, true) {
            @Override
            public void onPlayerBreak(
                    @Nonnull BlockBreakEvent blockBreakEvent,
                    @Nonnull ItemStack itemStack,
                    @Nonnull List<ItemStack> list) {}
        };
    }
}
