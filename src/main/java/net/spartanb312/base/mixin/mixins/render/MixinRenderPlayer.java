package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.event.events.render.RenderHandEvent;
import me.afterdarkness.moloch.module.modules.other.Freecam;
import me.afterdarkness.moloch.module.modules.visuals.Nametags;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ModuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.spartanb312.base.utils.ItemUtils.mc;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {
    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Shadow
    public abstract ModelPlayer getMainModel();

    @Shadow
    protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);

    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo ci) {
        if (ModuleManager.getModule(Nametags.class).isEnabled()) {
            ci.cancel();
        }
    }

    //pasted from trollheck
    @Inject(method = "doRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderViewEntity:Lnet/minecraft/entity/Entity;"))
    public void doRenderGetRenderViewEntity(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (ModuleManager.getModule(Freecam.class).isEnabled() && mc.getRenderViewEntity() != mc.player) {
            double renderY = y;

            if (entity.isSneaking())
                renderY = y - 0.125D;

            this.setModelVisibilities(entity);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender(entity, x, renderY, z, entityYaw, partialTicks);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        }
    }

    @Inject(method = "renderRightArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFF)V", shift = At.Shift.AFTER))
    public void renderRightArmHookPre(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "renderRightArm", at = @At("TAIL"))
    public void renderRightArmHookPost(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent.Post(() -> {
            ModelPlayer modelplayer = this.getMainModel();
            this.setModelVisibilities(clientPlayer);
            GlStateManager.enableBlend();
            modelplayer.swingProgress = 0.0F;
            modelplayer.isSneak = false;
            modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
            modelplayer.bipedRightArm.rotateAngleX = 0.0F;
            modelplayer.bipedRightArm.render(0.0625F);
            modelplayer.bipedRightArmwear.rotateAngleX = 0.0F;
            modelplayer.bipedRightArmwear.render(0.0625F);
            GlStateManager.disableBlend();
        });
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "renderLeftArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFF)V", shift = At.Shift.AFTER))
    public void renderLeftArmHookPre(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent();
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    public void renderLeftArmHookPost(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent.Post(() -> {
            ModelPlayer modelplayer = this.getMainModel();
            this.setModelVisibilities(clientPlayer);
            GlStateManager.enableBlend();
            modelplayer.isSneak = false;
            modelplayer.swingProgress = 0.0F;
            modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
            modelplayer.bipedLeftArm.rotateAngleX = 0.0F;
            modelplayer.bipedLeftArm.render(0.0625F);
            modelplayer.bipedLeftArmwear.rotateAngleX = 0.0F;
            modelplayer.bipedLeftArmwear.render(0.0625F);
            GlStateManager.disableBlend();
        });
        BaseCenter.EVENT_BUS.post(event);
    }
}
