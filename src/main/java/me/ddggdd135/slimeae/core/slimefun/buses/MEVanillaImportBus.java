package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.operations.ImportOperation;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MEVanillaImportBus extends MEImportBus {

    @Override
    public boolean isSynchronized() {
        return true;
    }

    public MEVanillaImportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onMEBusTick(
            @Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data, BusTickContext context) {
        ImportOperation.executeSingleDirection(context, true, true);
    }
}
