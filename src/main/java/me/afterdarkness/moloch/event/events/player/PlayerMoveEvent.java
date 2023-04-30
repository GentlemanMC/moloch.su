package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class PlayerMoveEvent extends EventCenter {
    public double motionX;
    public double motionY;
    public double motionZ;

    public PlayerMoveEvent(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }
}
