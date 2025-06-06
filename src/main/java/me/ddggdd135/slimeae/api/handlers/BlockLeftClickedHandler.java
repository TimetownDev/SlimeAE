package me.ddggdd135.slimeae.api.handlers;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.exceptions.IncompatibleItemHandlerException;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class BlockLeftClickedHandler implements BlockUseHandler {
    @Override
    public final void onRightClick(PlayerRightClickEvent playerRightClickEvent) {}

    public void onLeftClick(PlayerInteractEvent e) {}

    @Override
    @Nonnull
    public Optional<IncompatibleItemHandlerException> validate(@Nonnull SlimefunItem item) {
        return BlockUseHandler.super.validate(item);
    }

    @Override
    @Nonnull
    public Class<? extends ItemHandler> getIdentifier() {
        return BlockLeftClickedHandler.class;
    }
}
