package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.event.events.render.RenderBossHealthEvent;
import net.minecraft.client.gui.GuiBossOverlay;
import net.spartanb312.base.BaseCenter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiBossOverlay.class)
public class MixinGuiBossOverlay {
    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealthHookPre(CallbackInfo ci) {
        RenderBossHealthEvent event = new RenderBossHealthEvent();
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
