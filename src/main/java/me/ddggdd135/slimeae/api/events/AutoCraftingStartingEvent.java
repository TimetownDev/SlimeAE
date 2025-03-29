package me.ddggdd135.slimeae.api.events;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingSession;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AutoCraftingStartingEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private final AutoCraftingSession autoCraftingSession;

    public AutoCraftingStartingEvent(@Nonnull AutoCraftingSession autoCraftingSession) {
        super(!Bukkit.isPrimaryThread());
        this.autoCraftingSession = autoCraftingSession;
    }

    @Nonnull
    public AutoCraftingSession getAutoCraftingSession() {
        return autoCraftingSession;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlerList;
    }
}
