package me.ddggdd135.slimeae.core.slimefun.cards;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.core.slimefun.MEIOPort;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AccelerationCard extends Card {

    protected final int accelerationMultiplier;

    public AccelerationCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        this(itemGroup, item, recipeType, recipe, 1);
    }

    public AccelerationCard(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int accelerationMultiplier) {
        super(itemGroup, item, recipeType, recipe);
        this.accelerationMultiplier = accelerationMultiplier;
    }

    @Override
    public void onTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        if (item instanceof MachineProcessHolder<?> processorHolder) {
            MachineOperation operation = processorHolder.getMachineProcessor().getOperation(block);
            if (operation != null && !operation.isFinished()) {
                operation.addProgress(accelerationMultiplier);
            }
            return;
        }

        if (item instanceof MEBus meBus) {
            for (int i = 0; i < accelerationMultiplier; i++) {
                meBus.onMEBusTick(block, item, data);
            }
        }

        if (item instanceof MEIOPort meioPort) {
            for (int i = 0; i < accelerationMultiplier; i++) {
                meioPort.onMEIOPortTick(block, item, data);
            }
        }
    }
}
