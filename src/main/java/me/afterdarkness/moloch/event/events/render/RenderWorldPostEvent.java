package me.afterdarkness.moloch.event.events.render;

import net.spartanb312.base.core.event.decentralization.EventData;
import net.spartanb312.base.event.EventCenter;

public final class RenderWorldPostEvent extends EventCenter implements EventData {

    private final float partialTicks;
    private final Pass pass;

    public RenderWorldPostEvent(float partialTicks, int pass) {
        this.partialTicks = partialTicks;
        this.pass = Pass.values()[pass];
    }

    public final Pass getPass() {
        return this.pass;
    }

    public final float getPartialTicks() {
        return partialTicks;
    }

    public enum Pass {
        ANAGLYPH_CYAN, ANAGLYPH_RED, NORMAL
    }

}
