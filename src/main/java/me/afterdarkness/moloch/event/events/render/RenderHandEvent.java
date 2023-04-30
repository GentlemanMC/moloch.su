package me.afterdarkness.moloch.event.events.render;

import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.event.EventCenter;

public class RenderHandEvent extends EventCenter {
    public VoidTask renderArm;

    public RenderHandEvent() {}

    public RenderHandEvent(VoidTask renderArm) {
        this.renderArm = renderArm;
    }

    public static class Post extends RenderHandEvent {
        public Post(VoidTask renderArm) {
            super(renderArm);
        }
    }
}
