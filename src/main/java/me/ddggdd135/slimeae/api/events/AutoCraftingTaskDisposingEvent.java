package me.ddggdd135.slimeae.api.events;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AutoCraftingTaskDisposingEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private final AutoCraftingTask autoCraftingTask;

    public AutoCraftingTaskDisposingEvent(@Nonnull AutoCraftingTask autoCraftingTask) {
        super(!Bukkit.isPrimaryThread());
        this.autoCraftingTask = autoCraftingTask;
    }

    @Nonnull
    public AutoCraftingTask getAutoCraftingSession() {
        return autoCraftingTask;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlerList;
    }
}
