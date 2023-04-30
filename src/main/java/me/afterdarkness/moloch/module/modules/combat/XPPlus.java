package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.ServerManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.event.events.player.ProcessRightClickEvent;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.*;
import org.lwjgl.input.Mouse;

@Parallel(runnable = true)
@ModuleInfo(name = "XP+", category = Category.COMBAT, description = "Make throwing experience bottles more efficient")
public class XPPlus extends Module {

    Setting<Boolean> rotate = setting("Rotate", true).des("Rotates down when throwing xp");
    Setting<Boolean> rotatePredict = setting("RotatePredict", false).des("Predicts your motion to calculate where to throw").whenTrue(rotate);
    Setting<Integer> rotateTimeout = setting("RotateTimeout", 200, 1, 700).des("Milliseconds to stop rotating after throwing an xp bottle").whenTrue(rotate);
    Setting<Float> predictFactor = setting("PredictFactor", 20.0f, 1.0f, 40.0f).des("How much to predict your movement to calculate where to throw xp").whenTrue(rotatePredict).whenTrue(rotate);
    Setting<Boolean> autoThrow = setting("AutoThrow", false).des("Automatically throws xp");
    Setting<Boolean> silentSwap = setting("SilentSwap", true).des("Spoofs hotbar slot").whenTrue(autoThrow);
    Setting<Integer> delay = setting("Delay", 50, 0, 200).des("Milliseconds between each xp throw attempt");
    Setting<Boolean> extraPackets = setting("ExtraPackets", false).des("Adds CPacketPlayerTryUseItem on throwing xp").whenFalse(autoThrow);
    Setting<Integer> packetCount = setting("PacketCount", 1, 1, 64).des("Amount of CPacketPlayerTryUseItem packets to send on throw").whenTrue(extraPackets).whenFalse(autoThrow);

    private final Timer delayTimer = new Timer();
    private final Timer xpTimer = new Timer();
    private boolean packetXPFlag;
    private boolean isRotating;

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        CPacketPlayerTryUseItem packet;
        if (event.getPacket() instanceof CPacketPlayerTryUseItem
                && mc.player.getHeldItem((packet = ((CPacketPlayerTryUseItem) event.getPacket())).getHand()).getItem() == Items.EXPERIENCE_BOTTLE
                && !packetXPFlag) {
            xpTimer.reset();

            if (extraPackets.getValue() && !autoThrow.getValue()) {
                packetXPFlag = true;
                for (int i = 0; i < packetCount.getValue(); i++) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(packet.getHand()));
                }
                packetXPFlag = false;
            }
        }
    }

    @Override
    public void onTick() {
        if (autoThrow.getValue()) {
            int slot = ItemUtils.findItemInHotBar(Items.EXPERIENCE_BOTTLE);
            if (slot == -1) {
                toggle();
                ChatUtil.sendNoSpamErrorMessage("No xp in hotbar!");
                return;
            }

            xpTimer.reset();

            if (delayTimer.passed(delay.getValue()) && !(rotate.getValue() && !isRotating)) {
                boolean isHoldingXP = mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE;
                SwapManager.swapInvoke(this.name, false, true, () -> {
                    int prevSlot = mc.player.inventory.currentItem;
                    if (!isHoldingXP && slot != prevSlot)
                        ItemUtils.switchToSlot(slot, silentSwap.getValue());

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

                    if (!isHoldingXP && slot != prevSlot)
                        ItemUtils.switchToSlot(prevSlot, silentSwap.getValue());
                });
                delayTimer.reset();
            }
        } else {
            if (Mouse.isButtonDown(1) && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || ServerManager.isServerSideHoldingMain(Items.EXPERIENCE_BOTTLE))
                    && !(rotate.getValue() && !isRotating) && delayTimer.passed(delay.getValue())) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                delayTimer.reset();
            }
        }
    }

    @Listener
    public void onRightClick(ProcessRightClickEvent event) {
        if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || ServerManager.isServerSideHoldingMain(Items.EXPERIENCE_BOTTLE)) {
            xpTimer.reset();
            event.cancel();
        }
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (rotate.getValue() && RotationManager.rotateOverrideTimeoutXP.passed(200) && !xpTimer.passed(rotateTimeout.getValue())) {
            isRotating = true;
            RotationManager.rotateOverrideTimeout.reset();

            if (rotatePredict.getValue()) {
                float[] r = RotationUtil.getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), EntityUtil.predict(mc.player, predictFactor.getValue(), false));
                RotationManager.renderPitch = r[1];
                event.pitch = r[1];
            } else {
                RotationManager.renderPitch = 90.0f;
                event.pitch = 90.0f;
            }

            event.posX = mc.player.posX;
            event.posY = mc.player.getEntityBoundingBox().minY;
            event.posZ = mc.player.posZ;
            event.onGround = mc.player.onGround;
            event.cancel();
        } else {
            isRotating = false;
        }
    }
}
