package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.event.events.player.BlockInteractionEvent;
import me.afterdarkness.moloch.event.events.render.FOVItemModifyEvent;
import me.afterdarkness.moloch.module.modules.visuals.CameraClip;
import me.afterdarkness.moloch.module.modules.visuals.HeldModelTweaks;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.event.decentraliized.DecentralizedRenderWorldEvent;
import net.spartanb312.base.event.events.render.HudOverlayEvent;
import net.spartanb312.base.event.events.render.RenderWorldEvent;
import net.spartanb312.base.module.modules.visuals.NoRender;
import me.afterdarkness.moloch.event.events.render.RenderWorldPostEvent;
import me.afterdarkness.moloch.event.decentralized.DecentralizedRenderWorldPostEvent;
import me.afterdarkness.moloch.mixinotherstuff.IEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements IEntityRenderer {
    @Shadow
    protected abstract void setupCameraTransform(float partialTicks, int pass);

    @Shadow @Final public Minecraft mc;

    private boolean isRenderingHeldStuff = false;

    /**
     * Mixin have bugs,sometimes we may inject failed,so we use ASM
     *
     * @club.eridani.cursa.asm.impl.PatchEntityRenderer
     */
    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float partialTicks, CallbackInfo ci) {
        HudOverlayEvent event = new HudOverlayEvent(HudOverlayEvent.Type.HURTCAM);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target = "net/minecraft/profiler/Profiler.endStartSection(Ljava/lang/String;)V", args = "ldc=hand"))
    public void onStartHand(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ModuleManager.getModule(HeldModelTweaks.class).isDisabled() || !HeldModelTweaks.INSTANCE.render3dOnTop.getValue()) {
            RenderWorldEvent event = new RenderWorldEvent(partialTicks);
            DecentralizedRenderWorldEvent.instance.post(event);
            BaseCenter.EVENT_BUS.post(event);
        }
    }

    @Inject(method = "renderWorldPass", at = @At(value = "TAIL"))
    public void renderWorldPassTail(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        RenderWorldPostEvent event = new RenderWorldPostEvent(partialTicks, pass);
        DecentralizedRenderWorldPostEvent.instance.post(event);
        BaseCenter.EVENT_BUS.post(event);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "RETURN"))
    public void renderWorldPassHookPost(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ModuleManager.getModule(HeldModelTweaks.class).isEnabled() && HeldModelTweaks.INSTANCE.render3dOnTop.getValue()) {
            mc.entityRenderer.setupCameraTransform(partialTicks, pass);

            RenderWorldEvent event = new RenderWorldEvent(partialTicks);
            DecentralizedRenderWorldEvent.instance.post(event);
            BaseCenter.EVENT_BUS.post(event);
        }
        GL11.glDisable(GL_LIGHTING);
    }

    @Inject(method = "hurtCameraEffect", at = @At(value = "HEAD"), cancellable = true)
    public void hurtCameraEffectHook(float partialTicks, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isEnabled() && NoRender.INSTANCE.hurtCam.getValue()) ci.cancel();
    }

    @Inject(method = "setupFog", at = @At(value = "RETURN"))
    public void setupFogHook(int startCoords, float partialTicks, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isEnabled() && NoRender.INSTANCE.fog.getValue())
            GlStateManager.disableFog();
    }

    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    public void displayItemActivationHook(ItemStack stack, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isEnabled() && NoRender.INSTANCE.totemPop.getValue() == NoRender.TotemMode.NoRender)
            ci.cancel();
    }

    @Inject(method = "renderItemActivation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    public void renderItemActivationHook(int p_190563_1_, int p_190563_2_, float p_190563_3_, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isEnabled() && NoRender.INSTANCE.totemPop.getValue() == NoRender.TotemMode.Scale) {

            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int scaledWidth = scaledResolution.getScaledWidth();
            int scaledHeight = scaledResolution.getScaledHeight();

            GL11.glTranslatef((scaledWidth / 2.0f) * (1.0f - NoRender.INSTANCE.totemSize.getValue()), (scaledHeight / 2.0f) * (1.0f - NoRender.INSTANCE.totemSize.getValue()), 0.0f);
            GL11.glScalef(NoRender.INSTANCE.totemSize.getValue(), NoRender.INSTANCE.totemSize.getValue(), NoRender.INSTANCE.totemSize.getValue());
        }
    }

    @Override
    public void invokeSetupCameraTransform(float partialTicks, int pass) {
        setupCameraTransform(partialTicks, pass);
    }

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    public void getFOVModifierHook(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        FOVItemModifyEvent event = new FOVItemModifyEvent(cir.getReturnValue());
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled() && isRenderingHeldStuff)
            cir.setReturnValue(event.fov);
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    public void renderHandPre(float partialTicks, int pass, CallbackInfo ci) {
        isRenderingHeldStuff = true;
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    public void renderHandTail(float partialTicks, int pass, CallbackInfo ci) {
        isRenderingHeldStuff = false;
    }

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.BEFORE), cancellable = true)
    public void getMouseOverHook(float partialTicks, CallbackInfo ci) {
        BlockInteractionEvent event = new BlockInteractionEvent();
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            mc.profiler.endSection();
        }
    }

    @ModifyVariable(method = "orientCamera", ordinal = 3, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCameraModify(double d) {
        if (ModuleManager.getModule(CameraClip.class).isEnabled()) {
            return CameraClip.INSTANCE.cameraDistance.getValue();
        }
        else {
            return d;
        }
    }

    @ModifyVariable(method = "orientCamera", ordinal = 7, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCameraModify2(double d) {
        if (ModuleManager.getModule(CameraClip.class).isEnabled()) {
            return CameraClip.INSTANCE.cameraDistance.getValue();
        }
        else {
            return d;
        }
    }
}
