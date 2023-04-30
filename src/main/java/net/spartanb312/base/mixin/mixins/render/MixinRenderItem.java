package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.event.events.render.RenderHeldItemEvent;
import me.afterdarkness.moloch.module.modules.visuals.ESP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ModuleManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    @Shadow protected abstract void renderModel(IBakedModel model, int color, ItemStack stack);

    @Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
    public void renderEffectHook(IBakedModel model, CallbackInfo ci) {
        if (ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.renderProjectileFlag) ci.cancel();
    }

    @Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    public void renderModelHook(IBakedModel model, ItemStack stack, CallbackInfo ci) {
        if (ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.renderProjectileFlag) {
            GL11.glEnable(GL_LINE_SMOOTH);
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GL11.glDisable(GL_LIGHTING);
            GL11.glLineWidth(ESP.INSTANCE.espProjectileWidth.getValue());
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            renderModel(model, ESP.INSTANCE.espColorProjectiles.getValue().getColor(), stack);

            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GL11.glEnable(GL_LIGHTING);
            GlStateManager.enableBlend();

            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V", at = @At("HEAD"), cancellable = true)
    public void renderItemHook(ItemStack stack, EntityLivingBase entitylivingbaseIn, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        RenderHeldItemEvent event = new RenderHeldItemEvent(stack, entitylivingbaseIn, transform, leftHanded);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.cancel();
    }
}
