package me.afterdarkness.moloch.module.modules.movement;

import me.afterdarkness.moloch.event.events.player.GroundedStepEvent;
import me.afterdarkness.moloch.event.events.player.PlayerUpdateMoveEvent;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;

@Parallel(runnable = true)
@ModuleInfo(name = "Step", category = Category.MOVEMENT, description = "Allows you go up blocks")
public class Step extends Module {

    public Setting<Boolean> vanilla = setting("VanillaMode", true).des("Way to step over blocks");
    public Setting<Boolean> timer = setting("Timer", false).des("Slows you down when stepping").whenFalse(vanilla);
    //See MixinTimer
    Setting<Float> timerFactor = setting("TimerFactor", 0.5f, 0.3f, 1.0f).whenTrue(timer).whenFalse(vanilla);
    Setting<Boolean> entityStep = setting("EntityStep", false).des("Modifies entities' step height");
    Setting<Float> entityStepHeight = setting("EntityStepHeight", 100.0f, 1.0f, 256.0f).des("Max entity step height").whenTrue(entityStep);
    Setting<Float> height = setting("Height", 2.0f, 1.0f, 3.0f).des("Max height to be able to step over");
    Setting<Boolean> toggle = setting("Toggle", false).des("Automatically disables module when you've stepped over blocks once");

    public static Step INSTANCE;
    private BlockPos prevPos;
    private float prevHeight;
    public float tickRate = 1.0f;
    //See MixinTimer
    public boolean timerFlag;
    //thank u aesthetical for the step heights for 0.875 - 0.75
    private final double[] offsetsPointSevenFive = new double[]{0.39, 0.753, 0.75};
    private final double[] offsetsPointEightOneTwoFive = new double[]{0.39, 0.7, 0.8125};
    private final double[] offsetsPointEightSevenFive = new double[]{0.39, 0.7, 0.875};
    private final double[] offsetsOne = new double[]{0.42, 0.753, 1.0};
    private final double[] offsetsOnePointOneTwoFive = new double[]{0.42, 0.75, 1.0, 1.16};
    private final double[] offsetsOnePointOneEightSevenFive = new double[]{0.42, 0.75, 1.0, 1.16};
    private final double[] offsetsOneAndQuarter = new double[]{0.42, 0.75, 1.0, 1.16, 1.23};
    private final double[] offsetsOneAndHalf = new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
    private final double[] offsetsTwo = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
    private final double[] offsetsTwoAndHalf = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
    private final double[] offsetsThree =  new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43, 1.78, 1.63, 1.51, 1.9, 2.21, 2.45, 2.43};
    private final float[] offsets = new float[]{3.0f, 2.5f, 2.25f, 2.1875f, 2.125f, 2.075f, 2.0f, 1.875f, 1.8125f, 1.75f, 1.5f, 1.25f, 1.1875f, 1.125f, 1.075f, 1.0f, 0.875f, 0.8125f, 0.75f};

    public Step() {
        INSTANCE = this;
    }

    @Override
    public String getModuleInfo() {
        if (vanilla.getValue()) {
            return "Vanilla";
        }
        else {
            return "Packet";
        }
    }

    @Override
    public void onDisable() {
        mc.player.stepHeight = 0.6f;
        if (mc.player.ridingEntity instanceof AbstractHorse || mc.player.ridingEntity instanceof EntityPig) mc.player.ridingEntity.stepHeight = 1.0f;
        else if (mc.player.ridingEntity != null) mc.player.ridingEntity.stepHeight = 0.0f;
    }

    @Override
    public void onTick() {
        if (!vanilla.getValue() && EntityUtil.canStep() && mc.player.onGround) {
            packetStep();
        }

        if (entityStep.getValue() && mc.player.ridingEntity != null) {
            mc.player.ridingEntity.stepHeight = entityStepHeight.getValue();
        }

        if ((vanilla.getValue() || entityStep.getValue()) && toggle.getValue() && mc.player.posY - mc.player.lastTickPosY >= 1.0f) {
            toggle();
        }
    }

    @Listener
    public void onUpdateMove(PlayerUpdateMoveEvent event) {
        if (vanilla.getValue() && EntityUtil.canStep()) {
            mc.player.stepHeight = height.getValue();
        }
        else {
            mc.player.stepHeight = 0.6f;
        }
    }

    @Listener
    public void onGroundedStep(GroundedStepEvent event) {
        if (mc.player.ridingEntity instanceof AbstractHorse || mc.player.ridingEntity instanceof EntityPig) mc.player.ridingEntity.stepHeight = entityStep.getValue() ? entityStepHeight.getValue() : 1.0f;
        else if (mc.player.ridingEntity != null) mc.player.ridingEntity.stepHeight = entityStep.getValue() ? entityStepHeight.getValue() : 0.0f;
    }

    public void timerStepTickUpdate() {
        if (isDisabled() || (prevPos != null && prevHeight > 0.0f && mc.player.getPosition().y - prevPos.y >= prevHeight) || !(!vanilla.getValue() && timer.getValue())) {
            timerFlag = false;
            prevPos = null;
        }
    }

    private void packetStep() {
        double[] extension = extend();
        prevHeight = 0.0f;

        if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(extension[0],  0.01, extension[1])).isEmpty()) return;
        for (float offset : offsets) {
            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(extension[0], offset + 0.01, extension[1])).isEmpty()) {
                prevHeight = offset;
            }
        }

        if (prevHeight > 0.0f && height.getValue() >= prevHeight) {
            prevPos = mc.player.getPosition();
            timerFlag = true;

            if (prevHeight == 0.75f || prevHeight == 0.8125f || prevHeight == 0.875f || prevHeight == 1.0f || prevHeight == 1.075f) {
                tickRate = timerFactor.getValue();
            }
            else if (prevHeight == 1.125f || prevHeight == 1.1875f || prevHeight == 1.25f) {
                tickRate = (float) Math.pow(timerFactor.getValue(), 2.0);
            }
            else {
                tickRate = (float) Math.pow(timerFactor.getValue(), 3.0);
            }
        }

        //不知道怎麼做 沒有float switch所以要用elseifelseifelseifelseifelseifelseifelseif
        for (float offset : offsets) {
            if (height.getValue() >= offset && prevHeight == offset) {
                if (offset == 0.75f) {
                    for (double d : offsetsPointSevenFive)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 0.8125f) {
                    for (double d : offsetsPointEightOneTwoFive)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 0.875f) {
                    for (double d : offsetsPointEightSevenFive)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 1.0f || offset == 1.075f) {
                    for (double d : offsetsOne)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 1.125f) {
                    for (double d : offsetsOnePointOneTwoFive)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 1.1875f) {
                    for (double d : offsetsOnePointOneEightSevenFive)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));

                } else if (offset == 1.25f) {
                    for (double d : offsetsOneAndQuarter) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));
                    }
                } else if (offset == 1.5f) {
                    for (double d : offsetsOneAndHalf) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));
                    }
                } else if (offset == 1.76f || offset == 1.8125f || offset == 1.875f || offset == 2.0f || offset == 2.075f || offset == 2.125f || offset == 2.1875f || offset == 2.25f) {
                    for (double d : offsetsTwo) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));
                    }
                } else if (offset == 2.5f) {
                    for (double d : offsetsTwoAndHalf) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));
                    }
                } else if (offset == 3.0f) {
                    for (double d : offsetsThree)
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + d, mc.player.posZ, mc.player.onGround));
                }

                mc.player.setPosition(mc.player.posX, mc.player.posY + offset, mc.player.posZ);

                if (toggle.getValue()) ModuleManager.getModule(Step.class).disable();
            }
        }
    }

    //from gamesense
    private static double[] extend() {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45.0f : 45.0f);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45.0f : -45.0f);
            }

            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin((yaw + 90.0f) * (Math.PI / 180.0f));
        final double cos = Math.cos((yaw + 90.0f) * (Math.PI / 180.0f));
        final double posX = forward * 0.1 * cos + (forward != 0.0f ? 0.0f : side) * 0.1 * sin;
        final double posZ = forward * 0.1 * sin - (forward != 0.0f ? 0.0f : side) * 0.1 * cos;
        return new double[]{posX, posZ};
    }
}
