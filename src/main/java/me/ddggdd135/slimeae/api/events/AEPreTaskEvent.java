package me.ddggdd135.slimeae.api.events;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.enums.AETaskType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AEPreTaskEvent extends Event implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private boolean cancelled;
    private AETaskType taskType;

    public AEPreTaskEvent(@Nonnull AETaskType taskType) {
        super(true);
        this.taskType = taskType;
    }

    @Override
    public @Nonnull HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Nonnull
    public AETaskType getTaskType() {
        return taskType;
    }
}
