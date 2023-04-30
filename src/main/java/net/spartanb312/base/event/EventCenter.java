package net.spartanb312.base.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class EventCenter extends Event {
    private volatile boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

}
