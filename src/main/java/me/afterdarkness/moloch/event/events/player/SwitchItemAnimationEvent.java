package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class SwitchItemAnimationEvent extends EventCenter {
    public float progressFactor;

    public SwitchItemAnimationEvent(float progressFactor) {
        this.progressFactor = progressFactor;
    }
}
