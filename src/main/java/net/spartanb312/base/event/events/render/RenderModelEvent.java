package net.spartanb312.base.event.events.render;

import net.spartanb312.base.event.EventCenter;

public class RenderModelEvent extends EventCenter {
    public float pitch;

    public RenderModelEvent(float pitch) {
        this.pitch = pitch;
    }
}
