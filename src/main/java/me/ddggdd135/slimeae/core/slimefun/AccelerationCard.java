package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import me.ddggdd135.slimeae.api.abstracts.Card;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AccelerationCard extends Card {

    public AccelerationCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        if (!(item instanceof MachineProcessHolder processorHolder)) return;
        MachineOperation operation = processorHolder.getMachineProcessor().getOperation(block);
        if (operation == null) {
            return;
        }
        if (!operation.isFinished()) operation.addProgress(1);
    }
}
