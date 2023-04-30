package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.event.events.render.RenderThrowableEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.spartanb312.base.BaseCenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSnowball.class)
public class MixinRenderSnowball<T extends Entity> extends Render<T> {

    @Mutable
    @Final
    @Shadow protected Item item;

    protected MixinRenderSnowball(RenderManager renderManager, Item itemIn) {
        super(renderManager);
        this.item = itemIn;
    }

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void doRenderHookHead(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        RenderThrowableEvent event = new RenderThrowableEvent.Head(item);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void doRenderHookInvoke(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        RenderThrowableEvent event = new RenderThrowableEvent.Invoke(item);
        BaseCenter.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            if (this.renderOutlines) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
