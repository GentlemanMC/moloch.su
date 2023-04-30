package net.spartanb312.base.mixin.mixins.entity;

import com.mojang.authlib.GameProfile;
import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.event.events.player.*;
import me.afterdarkness.moloch.module.modules.other.Freecam;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.event.events.client.ChatEvent;
import net.spartanb312.base.module.modules.movement.Velocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.spartanb312.base.utils.ItemUtils.mc;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends EntityPlayer {
    @Shadow public MovementInput movementInput;
    @Shadow public boolean prevOnGround;
    @Shadow public float lastReportedYaw;
    @Shadow public float lastReportedPitch;
    @Shadow public int positionUpdateTicks;
    @Shadow public double lastReportedPosX;
    @Shadow public double lastReportedPosY;
    @Shadow public double lastReportedPosZ;
    @Shadow public boolean autoJumpEnabled;
    @Shadow public boolean serverSprintState;
    @Shadow public boolean serverSneakState;
    private OnUpdateWalkingPlayerEvent updateWalkingevent;

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Shadow
    protected abstract void updateAutoJump(float p_189810_1_, float p_189810_2_);

    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"), cancellable = true)
    public void sendChatPacket(String message, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = {"pushOutOfBlocks"}, at = {@At(value="HEAD")}, cancellable = true)
    private void pushHook(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        if (ModuleManager.getModule(Velocity.class).isEnabled() && Velocity.instance.pushing.getValue())
            ci.setReturnValue(false);
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V"), cancellable = true)
    private void onMoveStateUpdate(CallbackInfo ci) {
        PlayerUpdateMoveEvent event = new PlayerUpdateMoveEvent(movementInput);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "isCurrentViewEntity", at = @At("HEAD"), cancellable = true)
    private void isCurrentViewEntityHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getModule(Freecam.class).isEnabled() && Freecam.INSTANCE.camera != null)
            cir.setReturnValue(mc.getRenderViewEntity() == Freecam.INSTANCE.camera);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.BEFORE))
    public void onUpdateHook1(CallbackInfo ci) {
        updateWalkingevent = new OnUpdateWalkingPlayerEvent.Pre(this.onGround, this.posX, this.getEntityBoundingBox().minY, this.posY, this.rotationYaw, this.rotationPitch);
        BaseCenter.EVENT_BUS.post(updateWalkingevent);
        if (updateWalkingevent.isCancelled()) {
            RotationManager.renderPitch = updateWalkingevent.pitch;
            newOnUpdateWalkingPlayer();
        } else {
            RotationManager.renderPitch = mc.player.prevRotationPitch + (mc.player.rotationPitch - mc.player.prevRotationPitch) * mc.getRenderPartialTicks();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayerHook(CallbackInfo ci) {
        if (updateWalkingevent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.AFTER))
    public void onUpdateHook2(CallbackInfo ci) {
        OnUpdateWalkingPlayerEvent event = new OnUpdateWalkingPlayerEvent(this.onGround, this.posX, this.getEntityBoundingBox().minY, this.posY, this.rotationYaw, this.rotationPitch);
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void moveHook(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (type == MoverType.SELF && mc.player != null) {
            PlayerMoveEvent event = new PlayerMoveEvent(x, y, z);
            BaseCenter.EVENT_BUS.post(event);

            if (event.isCancelled()) {
                ci.cancel();
                double d0 = this.posX;
                double d1 = this.posZ;
                super.move(type, event.motionX, event.motionY, event.motionZ);
                this.updateAutoJump((float)(this.posX - d0), (float)(this.posZ - d1));
            }
        }
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    public void isSneakingHook(CallbackInfoReturnable<Boolean> cir) {
        SetSneakEvent event = new SetSneakEvent(this.getFlag(1));
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(event.isSneaking);
        }
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;handleMovement(Lnet/minecraft/util/MovementInput;)V", shift = At.Shift.BEFORE))
    public void onLivingUpdateHook(CallbackInfo ci) {
        MovementInputEvent event = new MovementInputEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    private void newOnUpdateWalkingPlayer() {
        if (updateWalkingevent == null) return;

        boolean flag = this.isSprinting();

        if (flag != serverSprintState) {
            if (flag) {
                mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != serverSneakState) {
            if (flag1) {
                mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            serverSneakState = flag1;
        }

        if (mc.getRenderViewEntity() == mc.player) {
            double d0 = updateWalkingevent.posX - lastReportedPosX;
            double d1 = updateWalkingevent.posY - lastReportedPosY;
            double d2 = updateWalkingevent.posZ - lastReportedPosZ;
            double d3 = updateWalkingevent.yaw - lastReportedYaw;
            double d4 = updateWalkingevent.pitch - lastReportedPitch;
            ++positionUpdateTicks;
            boolean moving = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || positionUpdateTicks >= 20;
            boolean rotating = d3 != 0.0D || d4 != 0.0D;

            if (this.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, updateWalkingevent.yaw, updateWalkingevent.pitch, updateWalkingevent.onGround));
                moving = false;
            } else if (moving && rotating) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(updateWalkingevent.posX, updateWalkingevent.posY, updateWalkingevent.posZ, updateWalkingevent.yaw, updateWalkingevent.pitch, updateWalkingevent.onGround));
            } else if (moving) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(updateWalkingevent.posX, updateWalkingevent.posY, updateWalkingevent.posZ, updateWalkingevent.onGround));
            } else if (rotating) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(updateWalkingevent.yaw, updateWalkingevent.pitch, updateWalkingevent.onGround));
            } else if (prevOnGround != updateWalkingevent.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(updateWalkingevent.onGround));
            }

            if (moving) {
                lastReportedPosX = updateWalkingevent.posX;
                lastReportedPosY = updateWalkingevent.posY;
                lastReportedPosZ = updateWalkingevent.posZ;
                positionUpdateTicks = 0;
            }

            if (rotating) {
                lastReportedYaw = updateWalkingevent.yaw;
                lastReportedPitch = updateWalkingevent.pitch;
            }

            prevOnGround = updateWalkingevent.onGround;
            autoJumpEnabled = mc.gameSettings.autoJump;
        }
    }
}
