package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.module.modules.visuals.ESP;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.monster.EntityCreeper;
import net.spartanb312.base.client.ModuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderCreeper.class)
public class MixinRenderCreeper {

    //this exists bc whenever a creeper charges up it breaks the brightness of the whole screen whenever an outlineesp is on
    @Inject(method = "getColorMultiplier(Lnet/minecraft/entity/monster/EntityCreeper;FF)I", at = @At("HEAD"), cancellable = true)
    public void getColorMultiplierHook(EntityCreeper entitylivingbaseIn, float lightBrightness, float partialTickTime, CallbackInfoReturnable<Integer> cir) {
        if (ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.renderingVanillaEntityFlag) {
            cir.setReturnValue(0);
        }
    }
}
