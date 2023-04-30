package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class UpdateTimerEvent extends EventCenter {
    public float timerSpeed;

    public UpdateTimerEvent(float timerSpeed) {
        this.timerSpeed = timerSpeed;
    }
}
