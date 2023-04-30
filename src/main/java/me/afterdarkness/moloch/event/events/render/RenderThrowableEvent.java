package me.afterdarkness.moloch.event.events.render;

import net.minecraft.item.Item;
import net.spartanb312.base.event.EventCenter;

public class RenderThrowableEvent extends EventCenter {
    public Item item;

    public RenderThrowableEvent(Item item) {
        this.item = item;
    }

    public static class Head extends RenderThrowableEvent {
        public Head(Item item) {
            super(item);
        }
    }

    public static class Invoke extends RenderThrowableEvent {
        public Invoke(Item item) {
            super(item);
        }
    }
}
