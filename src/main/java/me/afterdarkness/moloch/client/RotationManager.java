package me.afterdarkness.moloch.client;

import me.afterdarkness.moloch.core.LockTask;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.event.Priority;
import net.spartanb312.base.event.events.render.RenderModelEvent;
import net.spartanb312.base.utils.RotationUtil;
import net.spartanb312.base.utils.Timer;

import static net.spartanb312.base.BaseCenter.mc;

public class RotationManager {

    public static float prevYaw;
    public static float prevPitch;
    public static float renderPitch;
    public static final Timer rotateOverrideTimeout = new Timer();
    public static final Timer rotateOverrideTimeoutXP = new Timer();
    public static float newYaw;
    public static final LockTask normalizeYawTask = new LockTask(() -> newYaw = RotationUtil.normalizeAngle(mc.player.rotationYawHead));

    public static void init() {
        BaseCenter.EVENT_BUS.register(new RotationManager());
    }

    @Listener(priority = Priority.HIGHEST)
    public void renderModelRotation(RenderModelEvent event) {
        event.pitch = renderPitch;
    }

    public static void resetRotation() {
        normalizeYawTask.setLocked(false);
    }

    public static void setYawAndPitchLowPriority(OnUpdateWalkingPlayerEvent.Pre event, float yaw, float pitch) {
        if (rotateOverrideTimeout.passed(200)) {
            setYawAndPitchMotionEvent(event, yaw, pitch);
        }
    }

    public static void setYawAndPitchMotionEvent(OnUpdateWalkingPlayerEvent.Pre event, BlockPos pos, EnumFacing facing) {
        rotateOverrideTimeout.reset();
        rotateOverrideTimeoutXP.reset();
        float[] r = RotationUtil.getRotationsBlock(pos, facing, true);
        setYawAndPitchMotionEvent(event, r[0], r[1]);
    }

    public static void setYawAndPitchMotionEvent(OnUpdateWalkingPlayerEvent.Pre event, Vec3d from, Vec3d to) {
        rotateOverrideTimeout.reset();
        rotateOverrideTimeoutXP.reset();
        float[] r = RotationUtil.getRotations(from, to);
        setYawAndPitchMotionEvent(event, r[0], r[1]);
    }

    public static void setYawAndPitchMotionEvent(OnUpdateWalkingPlayerEvent.Pre event, float yaw, float pitch) {
        mc.player.renderYawOffset = yaw;
        event.yaw = yaw;
        event.pitch = pitch;
        event.posX = mc.player.posX;
        event.posY = mc.player.getEntityBoundingBox().minY;
        event.posZ = mc.player.posZ;
        event.onGround = mc.player.onGround;
        event.cancel();
    }

    public static void setYawAndPitchBlock(float yaw, float pitch) {
        rotateOverrideTimeout.reset();
        rotateOverrideTimeoutXP.reset();
        prevYaw = mc.player.rotationYaw;
        prevPitch = mc.player.rotationPitch;
        mc.player.renderYawOffset = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, true));
    }

    public static void lookAtEntity(OnUpdateWalkingPlayerEvent.Pre event, Entity entity, boolean slowRotate, float degreesPerTick) {
        float[] v = RotationUtil.getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());

        if (slowRotate) {
            float normalizedYaw = RotationUtil.normalizeAngle(v[0]);
            normalizeYawTask.invokeLock();
            float angleDiff = RotationUtil.calcNormalizedAngleDiff(normalizedYaw, newYaw);

            if (Math.abs(angleDiff) > 1.0f) {
                newYaw += Math.abs(angleDiff) < degreesPerTick * (angleDiff > 0.0f ? 1 : -1) ? angleDiff
                        : degreesPerTick * (angleDiff > 0.0f ? 1 : -1);
                newYaw = RotationUtil.normalizeAngle(newYaw);
            }

            setYawAndPitchLowPriority(event, newYaw, v[1]);
        }
        else setYawAndPitchLowPriority(event, v[0], v[1]);
    }

    public static void lookAtVec3d(Vec3d vec3d) {
        float[] v = RotationUtil.getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec3d);

        setYawAndPitchBlock(v[0], v[1]);
    }

    public static void resetRotationBlock() {
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(prevYaw, prevPitch, true));
    }
}
