package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class BlockInteractionEvent extends EventCenter {
    public static class RightClickPre extends BlockInteractionEvent {
        public RightClickPre() {}
    }

    public static class RightClickPost extends BlockInteractionEvent {
        public RightClickPost() {}
    }
}
