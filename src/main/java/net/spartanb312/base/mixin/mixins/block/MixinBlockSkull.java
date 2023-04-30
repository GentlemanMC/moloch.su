package net.spartanb312.base.mixin.mixins.block;

import me.afterdarkness.moloch.event.events.player.SpawnWitherEvent;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.spartanb312.base.BaseCenter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockSkull.class)
public class MixinBlockSkull {

    @Inject(method = "checkWitherSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/EntityWither;setLocationAndAngles(DDDFF)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void checkWitherSpawnHook(World worldIn, BlockPos pos, TileEntitySkull te, CallbackInfo ci, BlockPattern blockpattern, BlockPattern.PatternHelper blockpattern$patternhelper, BlockPos blockpos, EntityWither entitywither, BlockPos blockpos1) {
        SpawnWitherEvent event = new SpawnWitherEvent(entitywither);
        BaseCenter.EVENT_BUS.post(event);
    }
}
