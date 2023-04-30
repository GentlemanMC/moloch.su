package net.spartanb312.base.event.events.render;

import net.spartanb312.base.core.event.decentralization.EventData;
import net.spartanb312.base.event.EventCenter;

public final class RenderWorldEvent extends EventCenter implements EventData {

    private final float partialTicks;

    public RenderWorldEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public final float getPartialTicks() {
        return partialTicks;
    }
}
