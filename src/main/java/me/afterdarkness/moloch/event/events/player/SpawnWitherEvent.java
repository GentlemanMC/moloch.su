package me.afterdarkness.moloch.event.events.player;

import net.minecraft.entity.boss.EntityWither;
import net.spartanb312.base.event.EventCenter;

public class SpawnWitherEvent extends EventCenter {
    public EntityWither wither;

    public SpawnWitherEvent(EntityWither wither) {
        this.wither = wither;
    }
}
