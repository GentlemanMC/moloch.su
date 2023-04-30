package me.afterdarkness.moloch.module.modules.movement;

import me.afterdarkness.moloch.core.LockTask;
import me.afterdarkness.moloch.event.events.player.MovementInputEvent;
import me.afterdarkness.moloch.event.events.player.PlayerUpdateMoveEvent;
import me.afterdarkness.moloch.event.events.player.UpdateTimerEvent;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;

import java.util.List;
import java.util.stream.Collectors;

@Parallel(runnable = true)
@ModuleInfo(name = "NoSlow", category = Category.MOVEMENT, description = "Prevent slowing down")
public class NoSlow extends Module {


    Setting<ItemMode> itemMode = setting("ItemMode", ItemMode.Normal).des("No slow mode for items (2b2t bypass will disable swording or crystaling while eating)");
    Setting<Boolean> items = setting("Items", true).des("No slowing down on item use");
    public Setting<Boolean> soulSand = setting("SoulSand", false).des("No slowing down on soul sand");
    //see MixinBlockSoulSand
    Setting<Boolean> slime = setting("Slime", false).des("No slowing down on slime blocks");
    public Setting<CobWebMode> cobWebMode = setting("CobWebMode", CobWebMode.None).des("Ways to prevent webs from slowing you down");
    //see MixinBlockWeb
    Setting<Float> webHorizontalFactor = setting("WebHSpeed", 2.0f, 0.0f, 100.0f).des("Horizontal speed in web multiplier").whenAtMode(cobWebMode, CobWebMode.Motion);
    Setting<Float> webVerticalFactor = setting("WebVSpeed", 2.0f, 0.0f, 100.0f).des("Vertical speed in web multiplier").whenAtMode(cobWebMode, CobWebMode.Motion);
    Setting<Float> cobwebTimerSpeed = setting("CobwebTimerSpeed", 10.0f, 1.0f, 20.0f).des("Speed of timer in cobweb").whenAtMode(cobWebMode, CobWebMode.Timer);
    Setting<Boolean> sneak = setting("Sneak", false).des("No slowing down on sneaking");

    public static NoSlow instance;
    private final LockTask stopSneakTask = new LockTask(() -> mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING)));

    public NoSlow() {
        instance = this;
    }

    @Override
    public void onRenderTick() {
        if (slime.getValue()) Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.4945f);
        else Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.8f);
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && items.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            switch (itemMode.getValue()) {
                case NCPStrict: {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, EntityUtil.floorEntity(mc.player), EnumFacing.DOWN));
                    break;
                }

                case _2B2TBypass: {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                    break;
                }
            }
        }
    }

    @Listener
    public void onUpdateMove(PlayerUpdateMoveEvent event) {
        if (itemMode.getValue() == ItemMode._2B2TSneak && !mc.player.isSneaking() && !mc.player.isRiding()) {
            if (mc.player.isHandActive()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                stopSneakTask.setLocked(false);
            }
            else {
                stopSneakTask.invokeLock();
            }
        }

        if (cobWebMode.getValue() == CobWebMode.Motion && isInWeb(false)) {
            mc.player.motionX *= webHorizontalFactor.getValue();
            mc.player.motionZ *= webHorizontalFactor.getValue();
            mc.player.motionY *= webVerticalFactor.getValue();
        }
    }

    @Listener
    public void onUpdateTimer(UpdateTimerEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (cobWebMode.getValue() == CobWebMode.Timer && isInWeb(true)) {
            event.timerSpeed = cobwebTimerSpeed.getValue();
        }
    }

    @Listener
    public void onInput(MovementInputEvent event) {
        if ((items.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) || (sneak.getValue() && mc.player.isSneaking())) {
            mc.player.movementInput.moveForward /= 0.2f;
            mc.player.movementInput.moveStrafe /= 0.2f;
        }
    }

    private boolean isInWeb(boolean onlyFalling) {
        if (onlyFalling && mc.player.onGround) return false;

        List<BlockPos> webs = BlockUtil.getSphereRounded(EntityUtil.floorEntity(mc.player), 3, true)
                                    .stream()
                                    .filter(pos -> mc.world.getBlockState(pos).getBlock() == Blocks.WEB)
                                    .collect(Collectors.toList());

        for (BlockPos pos : webs) {
            if (mc.player.getEntityBoundingBox().intersects(new AxisAlignedBB(pos.x, pos.y, pos.z, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0)))
                return true;
        }
        return false;
    }

    enum ItemMode {
        Normal,
        NCPStrict,
        _2B2TSneak,
        _2B2TBypass
    }

    public enum CobWebMode {
        None,
        Cancel,
        Motion,
        Timer
    }
}
