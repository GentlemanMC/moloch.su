package net.spartanb312.base.mixin.mixins.entity;

import me.afterdarkness.moloch.event.events.player.BlockBreakDelayEvent;
import me.afterdarkness.moloch.event.events.player.DamageBlockEvent;
import me.afterdarkness.moloch.event.events.player.ProcessRightClickBlockEvent;
import me.afterdarkness.moloch.event.events.player.ProcessRightClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.spartanb312.base.BaseCenter;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDamageBlockHook(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        DamageBlockEvent event = new DamageBlockEvent(pos, side);
        BaseCenter.EVENT_BUS.post(event);

        if (event.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "clickBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;blockHitDelay:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void clickBlockHook(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakDelayEvent event = new BlockBreakDelayEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "onPlayerDamageBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;blockHitDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    public void onPlayerDamageBlockHook1(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakDelayEvent event = new BlockBreakDelayEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "onPlayerDamageBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;blockHitDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 2, shift = At.Shift.AFTER))
    public void onPlayerDamageBlockHook2(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakDelayEvent event = new BlockBreakDelayEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "processRightClick", at = @At("HEAD"), cancellable = true)
    public void processRightClickHook(EntityPlayer player, World worldIn, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        ProcessRightClickEvent event = new ProcessRightClickEvent();
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled())
            cir.setReturnValue(EnumActionResult.PASS);
    }
    @Inject(method={"processRightClickBlock"}, at={@At(value="HEAD")}, cancellable=true)
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        ProcessRightClickBlockEvent event = new ProcessRightClickBlockEvent(pos, hand, Minecraft.getMinecraft().player.getHeldItem(hand));
        MinecraftForge.EVENT_BUS.post((Event)event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }
}
