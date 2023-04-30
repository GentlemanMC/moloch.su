package me.afterdarkness.moloch.event.decentralized;

import net.spartanb312.base.core.event.decentralization.DecentralizedEvent;
import me.afterdarkness.moloch.event.events.render.RenderWorldPostEvent;

public class DecentralizedRenderWorldPostEvent extends DecentralizedEvent<RenderWorldPostEvent> {
    public static DecentralizedRenderWorldPostEvent instance = new DecentralizedRenderWorldPostEvent();
}
