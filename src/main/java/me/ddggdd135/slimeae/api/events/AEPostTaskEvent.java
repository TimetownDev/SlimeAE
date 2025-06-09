package me.ddggdd135.slimeae.api.events;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.enums.AETaskType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AEPostTaskEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private AETaskType taskType;

    public AEPostTaskEvent(@Nonnull AETaskType taskType) {
        super(true);
        this.taskType = taskType;
    }

    @Override
    public @Nonnull HandlerList getHandlers() {
        return handlerList;
    }

    @Nonnull
    public AETaskType getTaskType() {
        return taskType;
    }
}
