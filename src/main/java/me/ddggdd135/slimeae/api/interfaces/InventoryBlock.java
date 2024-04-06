package me.ddggdd135.slimeae.api.interfaces;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface InventoryBlock {
    int[] getInputSlots();

    int[] getOutputSlots();

    @OverridingMethodsMustInvokeSuper
    void init(@Nonnull BlockMenuPreset preset);

    @OverridingMethodsMustInvokeSuper
    void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block);

    default int[] getSlotsAccessedByItemTransport(@Nonnull ItemTransportFlow flow) {
        return flow == ItemTransportFlow.INSERT ? getInputSlots() : getOutputSlots();
    }

    default int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        return this.getSlotsAccessedByItemTransport(flow);
    }

    default boolean canOpen(@Nonnull SlimefunItem item, @Nonnull Block block, @Nonnull Player player) {
        if (player.hasPermission("slimefun.inventory.bypass")) {
            return true;
        } else {
            return item.canUse(player, false)
                    && Slimefun.getProtectionManager()
                            .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
        }
    }

    default void createPreset(@Nonnull SlimefunItem item) {
        this.createPreset(item, item.getItemName());
    }

    default void createPreset(@Nonnull SlimefunItem item, @Nonnull String title) {
        new BlockMenuPreset(item.getId(), title) {
            @Override
            public void init() {
                InventoryBlock.this.init(this);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return InventoryBlock.this.getSlotsAccessedByItemTransport(flow);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return InventoryBlock.this.canOpen(item, block, player);
            }

            @Override
            public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
                InventoryBlock.this.newInstance(menu, block);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                return InventoryBlock.this.getSlotsAccessedByItemTransport(menu, flow, item);
            }
        };
    }
}
