package me.afterdarkness.moloch.module.modules.movement;

import me.afterdarkness.moloch.event.events.player.PlayerMoveEvent;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

@Parallel(runnable = true)
@ModuleInfo(name = "SafeWalk", category = Category.MOVEMENT, description = "Stops you from walking off blocks")
public class SafeWalk extends Module {

    Setting<Double> minHeight = setting("MinHeight", 0.1, 0.1, 20.0).des("Minimum fall height to stop you from walking of block");

    public static SafeWalk INSTANCE;

    public SafeWalk() {
        INSTANCE = this;
    }

    @Listener
    public void onPlayerMove(PlayerMoveEvent event) {
        preventFall(event, minHeight.getValue(), true);
    }

    public void preventFall(PlayerMoveEvent event, double minHeight, boolean onlyOnGround) {
        event.cancel();

        if ((!onlyOnGround || mc.player.onGround) && !mc.player.noClip) {
            restrictMotion(event, true, false, minHeight);
            restrictMotion(event, false, true, minHeight);
            restrictMotion(event, true, true, minHeight);
        }
    }

    private boolean offsetBBEmpty(double xOffset, double yOffset, double zOffset) {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(xOffset, yOffset, zOffset)).isEmpty();
    }

    private void restrictMotion(PlayerMoveEvent event, boolean restrictX, boolean restrictZ, double minHeight) {
        while ((!restrictX || event.motionX != 0.0) && (!restrictZ || event.motionZ != 0.0) && offsetBBEmpty(restrictX ? event.motionX : 0.0, -minHeight, restrictZ ? event.motionZ : 0.0)) {
            if (restrictX) event.motionX = modifyMotion(event.motionX);
            if (restrictZ) event.motionZ = modifyMotion(event.motionZ);
        }
    }

    private double modifyMotion(double motion) {
        if (motion < 0.05 && motion >= -0.05) {
            motion = 0.0;
        } else if (motion > 0.0) {
            motion -= 0.05;
        } else {
            motion += 0.05;
        }
        return motion;
    }
}
