package net.spartanb312.base.mixin.mixins.render;

import me.afterdarkness.moloch.module.modules.visuals.Chams;
import me.afterdarkness.moloch.module.modules.visuals.ESP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.spartanb312.base.command.Command.mc;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.GL_CONSTANT;

@Mixin(RenderEntityItem.class)
public class MixinRenderEntityItem {

    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void doRenderHook1(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!Chams.instance.itemsCancelVanillaRender.getValue()) return;
        else {
            ci.cancel();
        }

        ItemStack itemstack = entity.getItem();
        IBakedModel ibakedmodel = mc.getItemRenderer().itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);

        if ((ModuleManager.getModule(Chams.class).isEnabled() && Chams.instance.items.getValue() && !(Chams.instance.itemsRangeLimit.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), Chams.instance.itemsRange.getValue()))) || (ESP.INSTANCE.espTargetItems.getValue() && ESP.INSTANCE.espModeItems.getValue() == ESP.ModeItems.Wireframe && ModuleManager.getModule(ESP.class).isEnabled() && !(ESP.INSTANCE.espRangeLimitItems.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), ESP.INSTANCE.espRangeItems.getValue())))) {
            renderItem(itemstack, ibakedmodel, (ESP.INSTANCE.espTargetItems.getValue() && ESP.INSTANCE.espModeItems.getValue() == ESP.ModeItems.Wireframe && ModuleManager.getModule(ESP.class).isEnabled() && !(ESP.INSTANCE.espRangeLimitItems.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), ESP.INSTANCE.espRangeItems.getValue()))), (ModuleManager.getModule(Chams.class).isEnabled() && Chams.instance.items.getValue() && !(Chams.instance.itemsRangeLimit.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), Chams.instance.itemsRange.getValue()))));

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", shift = At.Shift.AFTER))
    public void doRenderHook2(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (Chams.instance.itemsCancelVanillaRender.getValue()) return;

        ItemStack itemstack = entity.getItem();
        IBakedModel ibakedmodel = mc.getItemRenderer().itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);

        if ((ModuleManager.getModule(Chams.class).isEnabled() && Chams.instance.items.getValue() && !(Chams.instance.itemsRangeLimit.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), Chams.instance.itemsRange.getValue()))) || (ESP.INSTANCE.espTargetItems.getValue() && ESP.INSTANCE.espModeItems.getValue() == ESP.ModeItems.Wireframe && ModuleManager.getModule(ESP.class).isEnabled() && !(ESP.INSTANCE.espRangeLimitItems.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), ESP.INSTANCE.espRangeItems.getValue()))))
            renderItem(itemstack, ibakedmodel, (ESP.INSTANCE.espTargetItems.getValue() && ESP.INSTANCE.espModeItems.getValue() == ESP.ModeItems.Wireframe && ModuleManager.getModule(ESP.class).isEnabled() && !(ESP.INSTANCE.espRangeLimitItems.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), ESP.INSTANCE.espRangeItems.getValue()))), (ModuleManager.getModule(Chams.class).isEnabled() && Chams.instance.items.getValue() && !(Chams.instance.itemsRangeLimit.getValue() && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), Chams.instance.itemsRange.getValue()))));
    }

    private void newRenderModel(IBakedModel model, ItemStack stack, boolean lines, boolean chams) {
        GL11.glEnable(GL_POLYGON_SMOOTH);
        GL11.glEnable(GL_LINE_SMOOTH);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        if (Chams.instance.itemBlend.getValue() && chams) GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);
        else GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (chams && Chams.instance.itemTexture.getValue()) {
            GlStateManager.enableTexture2D();
            if (!Chams.instance.itemLighting.getValue()) GlStateManager.disableLighting();
            renderModel(model, Chams.instance.itemColor.getValue().getColor(), stack);
            GlStateManager.disableTexture2D();
        }

        if (lines || (chams && !Chams.instance.itemTexture.getValue())) {
            GL11.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
            GL11.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
            GL11.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, GL_CONSTANT);
            GlStateManager.disableLighting();

            if (chams && !Chams.instance.itemTexture.getValue()) {
                SpartanTessellator.BUF_FLOAT_4.put(0, ColorUtil.getRed(Chams.instance.itemColor.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(1, ColorUtil.getGreen(Chams.instance.itemColor.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(2, ColorUtil.getBlue(Chams.instance.itemColor.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(3, ColorUtil.getAlpha(Chams.instance.itemColor.getValue().getColor()) / 255.0f);
                GL11.glTexEnv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, SpartanTessellator.BUF_FLOAT_4);
                renderModel(model, Chams.instance.itemColor.getValue().getColor(), stack);
            }

            if (lines) {
                SpartanTessellator.BUF_FLOAT_4.put(0, ColorUtil.getRed(ESP.INSTANCE.espColorItems.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(1, ColorUtil.getGreen(ESP.INSTANCE.espColorItems.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(2, ColorUtil.getBlue(ESP.INSTANCE.espColorItems.getValue().getColor()) / 255.0f);
                SpartanTessellator.BUF_FLOAT_4.put(3, ColorUtil.getAlpha(ESP.INSTANCE.espColorItems.getValue().getColor()) / 255.0f);
                GL11.glTexEnv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, SpartanTessellator.BUF_FLOAT_4);
                GL11.glLineWidth(ESP.INSTANCE.espItemWidth.getValue());
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                renderModel(model, ESP.INSTANCE.espColorItems.getValue().getColor(), stack);

                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            }

            GlStateManager.enableLighting();
            GlStateManager.disableOutlineMode();
        }

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GL11.glEnable(GL_LIGHTING);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GL11.glDisable(GL_POLYGON_SMOOTH);
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    private void renderModel(IBakedModel model, int color, ItemStack stack) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values()) {
            mc.getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), color, stack);
        }

        mc.getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, null, 0L), color, stack);
        tessellator.draw();
    }

    public void renderItem(ItemStack stack, IBakedModel model, boolean lines, boolean chams) {
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            if (model.isBuiltInRenderer()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
            } else {
                this.newRenderModel(model, stack, lines, chams);
            }
            GlStateManager.popMatrix();
        }
    }
}
