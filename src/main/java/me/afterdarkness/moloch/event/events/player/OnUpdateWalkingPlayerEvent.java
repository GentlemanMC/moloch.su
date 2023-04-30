package me.afterdarkness.moloch.event.events.player;

import net.spartanb312.base.event.EventCenter;

public class OnUpdateWalkingPlayerEvent extends EventCenter {
    public float yaw;
    public float pitch;
    public boolean onGround;
    public double posX;
    public double posY;
    public double posZ;

    public OnUpdateWalkingPlayerEvent(boolean onGround, double posX, double posY, double posZ, float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static class Pre extends OnUpdateWalkingPlayerEvent {
        public Pre(boolean onGround, double posX, double posY, double posZ, float yaw, float pitch) {
            super(onGround, posX, posY, posZ, yaw, pitch);
        }
    }
}
