package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class SetSneakEvent extends EventCenter {
    public boolean isSneaking;

    public SetSneakEvent(boolean isSneaking) {
        this.isSneaking = isSneaking;
    }
}
