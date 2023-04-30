package net.spartanb312.base.mixin.mixins.entity;

import me.afterdarkness.moloch.event.events.player.FinishEatingEvent;
import me.afterdarkness.moloch.event.events.player.JumpEvent;
import me.afterdarkness.moloch.event.events.player.TravelEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.spartanb312.base.BaseCenter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.spartanb312.base.BaseCenter.mc;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void jumpHook(CallbackInfo ci) {
        if (mc.world.getEntityByID(this.getEntityId()) == mc.player) {
            JumpEvent event = new JumpEvent();
            BaseCenter.EVENT_BUS.post(event);
            if (event.isCancelled()) ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travelHook(float strafe, float vertical, float forward, CallbackInfo ci) {
        TravelEvent event = new TravelEvent();
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            ci.cancel();
        }
    }

    @Inject(method = "handleStatusUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;onItemUseFinish()V"))
    public void onItemUseFinishHook(CallbackInfo ci) {
        FinishEatingEvent event = new FinishEatingEvent();
        BaseCenter.EVENT_BUS.post(event);
    }
}
