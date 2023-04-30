package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class RubberbandEvent extends EventCenter {
    public double distance;
    public float[] rotationDif;

    public RubberbandEvent(double distance, float[] rotationDif) {
        this.distance = distance;
        this.rotationDif = rotationDif;
    }
}
