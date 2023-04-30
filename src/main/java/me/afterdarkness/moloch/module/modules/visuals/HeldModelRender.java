package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.render.RenderHeldItemEvent;
import me.afterdarkness.moloch.event.events.render.RenderHandEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.ForgeHooksClient;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

@Parallel(runnable = true)
@ModuleInfo(name = "HeldModelRender", category = Category.VISUALS, description = "Cool effects on held items and hand")
public class HeldModelRender extends Module {

    Setting<Page> page = setting("Page", Page.Items);

    Setting<Integer> itemAlpha = setting("ItemAlpha", 255, 0, 255).des("Alpha of held items").whenAtMode(page, Page.Items);
    Setting<Boolean> itemsColorOverlay = setting("ItemsColorOverlay", false).des("Overlays a color on held items").whenAtMode(page, Page.Items);
    Setting<Color> itemsOverlayColor = setting("ItemsOverlayColor", new Color(new java.awt.Color(153, 125, 255, 90).getRGB())).whenTrue(itemsColorOverlay).whenAtMode(page, Page.Items);
    Setting<Lines> itemsLines = setting("ItemsLines", Lines.None).des("What type of lines render for held items").whenAtMode(page, Page.Items);
    Setting<Boolean> itemLinesTransparency = setting("ItemsLinesTransparent", true).des("Be able to modify the alpha of item lines (texture alpha affects the lines alpha so if you want the lines to be solid, turn this off)").when(() -> itemsLines.getValue() != Lines.None).whenAtMode(page, Page.Items);
    Setting<Float> itemsLinesWidth = setting("ItemsLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of held item wireframe lines").when(() -> itemsLines.getValue() != Lines.None).whenAtMode(page, Page.Items);
    Setting<Color> itemsLinesColor = setting("ItemsLinesColor", new Color(new java.awt.Color(169, 156, 238, 150).getRGB())).when(() -> itemsLines.getValue() != Lines.None).whenAtMode(page, Page.Items);

    Setting<Boolean> handTexture = setting("HandTexture", false).des("Draw texture on hand").whenAtMode(page, Page.Hand);
    Setting<Boolean> handLighting = setting("HandLighting", true).des("Use lighting on hand").whenAtMode(page, Page.Hand);
    Setting<Color> handColor = setting("HandColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).whenAtMode(page, Page.Hand);
    Setting<Boolean> handColorOverlay = setting("HandColorOverlay", false).des("Overlays a color on hand").whenAtMode(page, Page.Hand);
    Setting<Color> handOverlayColor = setting("HandOverlayColor", new Color(new java.awt.Color(153, 125, 255, 90).getRGB())).whenTrue(handColorOverlay).whenAtMode(page, Page.Hand);
    Setting<Lines> handLines = setting("HandLines", Lines.None).des("What type of lines render for hand").whenAtMode(page, Page.Hand);
    Setting<Float> handLinesWidth = setting("HandLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of hand wireframe lines").when(() -> handLines.getValue() != Lines.None).whenAtMode(page, Page.Hand);
    Setting<Color> handLinesColor = setting("HandLinesColor", new Color(new java.awt.Color(169, 156, 238, 150).getRGB())).when(() -> handLines.getValue() != Lines.None).whenAtMode(page, Page.Hand);

    @Listener
    public void onRenderHeldItem(RenderHeldItemEvent event) {
        if (!event.stack.isEmpty() && (event.transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || event.transform == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                && event.holdingEntity != null && event.holdingEntity == mc.player) {
            IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(event.stack, event.holdingEntity.world, event.holdingEntity);
            model = ForgeHooksClient.handleCameraTransforms(model, event.transform, event.leftHanded);
            event.cancel();

            SpartanTessellator.renderItemModelPre(event.stack);
            SpartanTessellator.renderItemModelVanilla(event.stack, model, itemAlpha.getValue() / 255.0f, true);
            if (itemsColorOverlay.getValue())
                SpartanTessellator.renderItemModelColorOverlay(event.stack, model, itemsOverlayColor.getValue().getColor(), true);

            if (itemsLines.getValue() != Lines.None) {
                GL11.glLineWidth(itemsLinesWidth.getValue());
                SpartanTessellator.renderItemModelLines(event.stack, model, itemsLines.getValue() == Lines.Outline, !itemLinesTransparency.getValue(), itemsLinesColor.getValue().getColor(), true);
            }
            SpartanTessellator.renderItemModelPost(event.stack);
        }
    }

    @Listener
    public void onRenderHand(RenderHandEvent event) {
        GL11.glEnable(GL_CULL_FACE);
        if (!handTexture.getValue()) GL11.glDisable(GL_TEXTURE_2D);
        if (!handLighting.getValue()) GL11.glDisable(GL_LIGHTING);
        GL11.glColor4f(handColor.getValue().getColorColor().getRed() / 255.0f, handColor.getValue().getColorColor().getGreen() / 255.0f, handColor.getValue().getColorColor().getBlue() / 255.0f, handColor.getValue().getAlpha() / 255.0f);
    }

    @Listener
    public void onRenderHandPost(RenderHandEvent.Post event) {
        if (handLines.getValue() != Lines.None) {
            GlStateManager.disableDepth();
            GL11.glDisable(GL_CULL_FACE);
            GL11.glDisable(GL_LIGHTING);
            GL11.glDisable(GL_TEXTURE_2D);
            GL11.glLineWidth(handLinesWidth.getValue());
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glColor4f(handLinesColor.getValue().getColorColor().getRed() / 255.0f, handLinesColor.getValue().getColorColor().getGreen() / 255.0f, handLinesColor.getValue().getColorColor().getBlue() / 255.0f, handLinesColor.getValue().getAlpha() / 255.0f);
            if (handLines.getValue() == Lines.Outline) {
                SpartanTessellator.outline1();
                event.renderArm.invoke();
                SpartanTessellator.outline2();
                event.renderArm.invoke();
                SpartanTessellator.outline3();
                event.renderArm.invoke();
                SpartanTessellator.outlineRelease();
            } else {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                event.renderArm.invoke();
            }
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            GL11.glDisable(GL_LINE_SMOOTH);
            GlStateManager.enableDepth();
        }

        if (handColorOverlay.getValue()) {
            GlStateManager.depthMask(false);
            GL11.glDepthFunc(GL_EQUAL);
            GL11.glDisable(GL_TEXTURE_2D);
            GL11.glDisable(GL_LIGHTING);
            GL11.glColor4f(handOverlayColor.getValue().getColorColor().getRed() / 255.0f, handOverlayColor.getValue().getColorColor().getGreen() / 255.0f, handOverlayColor.getValue().getColorColor().getBlue() / 255.0f, handOverlayColor.getValue().getAlpha() / 255.0f);
            event.renderArm.invoke();
            GL11.glDepthFunc(GL_LEQUAL);
            GlStateManager.depthMask(true);
        }

        GL11.glEnable(GL_LIGHTING);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glEnable(GL_CULL_FACE);
        GL11.glColor4f(1, 1, 1, 1);
    }

    enum Page {
        Items,
        Hand
    }

    enum Lines {
        Wireframe,
        Outline,
        None
    }
}
