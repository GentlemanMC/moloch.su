package net.spartanb312.base.utils.graphics;

import me.afterdarkness.moloch.client.EnemyManager;
import me.afterdarkness.moloch.module.modules.client.BackgroundThreadStuff;
import me.afterdarkness.moloch.module.modules.visuals.ESP;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.client.FriendManager;
import net.spartanb312.base.command.Command;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraftforge.client.ForgeHooksClient.renderLitItem;
import static net.spartanb312.base.utils.EntityUtil.mc;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class SpartanTessellator extends Tessellator {

    public static SpartanTessellator INSTANCE = new SpartanTessellator();
    public static final HashMap<EntityPlayer, double[][]> skeletonVerticesMap = new HashMap<>();
    public static final FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);

    public SpartanTessellator() {
        super(2097152);
    }

    public static void prepareGL() {
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glDisable(GL_TEXTURE_2D);
        GlStateManager.depthMask(false);
        GL11.glEnable(GL_BLEND);
        GlStateManager.disableDepth();
        GL11.glDisable(GL_ALPHA_TEST);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }

    public static void releaseGL() {
        GlStateManager.depthMask(true);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glEnable(GL_BLEND);
        GlStateManager.enableDepth();
        GL11.glEnable(GL_ALPHA_TEST);
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void begin(int mode) {
        INSTANCE.getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public static void render() {
        INSTANCE.draw();
    }

    public static void drawFlatFullBox(Vec3d pos, boolean useDepth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glDisable(GL_CULL_FACE);
        drawFlatFilledBox(INSTANCE.getBuffer(), useDepth, (float)pos.x, (float)pos.y, (float)pos.z, 1.0f, 1.0f, r, g, b, a);
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawFlatLineBox(Vec3d pos, boolean useDepth, float width, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(width);
        drawFlatLineBox(INSTANCE.getBuffer(), useDepth, (float)pos.x, (float)pos.y, (float)pos.z, 1.0f, 1.0f, r, g, b, a);
    }

    public static void drawBBFullBox(Entity entity, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = entity.getEntityBoundingBox();
        Vec3d entityPos = EntityUtil.getInterpolatedEntityPos(entity, mc.getRenderPartialTicks());
        drawFilledBox(INSTANCE.getBuffer(), false, (float)(entityPos.x - ((bb.maxX - bb.minX + 0.05) / 2.0f)), (float)(entityPos.y), (float)(entityPos.z - ((bb.maxZ - bb.minZ + 0.05) / 2.0f)), (float)(bb.maxX - bb.minX + 0.05), (float)(bb.maxY - bb.minY), (float)(bb.maxZ - bb.minZ + 0.05), r, g, b, a);
    }

    public static void drawBBLineBox(Entity entity, float width, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = entity.getEntityBoundingBox();
        Vec3d entityPos = EntityUtil.getInterpolatedEntityPos(entity, mc.getRenderPartialTicks());
        GL11.glLineWidth(width);
        drawLineBox(INSTANCE.getBuffer(), false, (float)(entityPos.x - ((bb.maxX - bb.minX + 0.05) / 2.0f)), (float)(entityPos.y), (float)(entityPos.z - ((bb.maxZ - bb.minZ + 0.05) / 2.0f)), (float)(bb.maxX - bb.minX + 0.05), (float)(bb.maxY - bb.minY), (float)(bb.maxZ - bb.minZ + 0.05), r, g, b, a);
    }

    public static void drawBlockBBFullBox(BlockPos blockPos, float scale, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingBoxFilled(INSTANCE.getBuffer(), bb, 1.0f, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawBlockBBLineBox(BlockPos blockPos, float scale, float width, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingBoxLines(INSTANCE.getBuffer(), bb, 1.0f, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawBlockBBFullBox(BlockPos blockPos, float scale, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingBoxFilled(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawBlockBBLineBox(BlockPos blockPos, float scale, float width, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingBoxLines(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawPyramidBBFullBox(BlockPos blockPos, boolean flipY, float scale, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingPyramidFilled(INSTANCE.getBuffer(), bb, false, flipY, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawCrownBBFullBox(BlockPos blockPos, boolean flipY, float scale, float height, float innerHeight, int color, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingCrownFilled(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r, g, b, a, ir, ig, ib, ia);
    }

    public static void drawPyramidBBLineBox(BlockPos blockPos, boolean flipY, float scale, float width, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingPyramidLines(INSTANCE.getBuffer(), bb, flipY, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawCrownBBLineBox(BlockPos blockPos, boolean flipY, float scale, float width, float height, float innerHeight, int color, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingCrownLines(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r, g, b, a, ir, ig, ib, ia);
    }

    public static void drawGradientCrownBBLineBox(BlockPos blockPos, boolean flipY, float scale, float width, float height, float innerHeight, int color, int color2, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingCrownLines(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r2, g2, b2, a2, ir, ig, ib, ia);
    }

    public static void drawBlockBBFullBox(AxisAlignedBB bb, float scale, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawBoundingBoxFilled(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawBlockBBLineBox(AxisAlignedBB bb, float scale, float width, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(width);
        drawBoundingBoxLines(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawPyramidBBFullBox(AxisAlignedBB bb, boolean flipY, float scale, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawBoundingPyramidFilled(INSTANCE.getBuffer(), bb, false, flipY, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawCrownBBFullBox(AxisAlignedBB bb, boolean flipY, float scale, float height, float innerHeight, int color, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        drawBoundingCrownFilled(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r, g, b, a, ir, ig, ib, ia);
    }

    public static void drawPyramidBBLineBox(AxisAlignedBB bb, boolean flipY, float scale, float width, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(width);
        drawBoundingPyramidLines(INSTANCE.getBuffer(), bb, flipY, height, scale, r, g, b, a, r, g, b, a);
    }

    public static void drawCrownBBLineBox(AxisAlignedBB bb, boolean flipY, float scale, float width, float height, float innerHeight, int color, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        GL11.glLineWidth(width);
        drawBoundingCrownLines(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r, g, b, a, ir, ig, ib, ia);
    }

    public static void drawGradientCrownBBLineBox(AxisAlignedBB bb, boolean flipY, float scale, float width, float height, float innerHeight, int color, int color2, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        GL11.glLineWidth(width);
        drawBoundingCrownLines(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r2, g2, b2, a2, ir, ig, ib, ia);
    }

    public static void drawGradientBlockBBFullBox(BlockPos blockPos, boolean stopCull, float scale, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingBoxFilled(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r2, g2, b2, a2);
        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientBlockBBLineBox(BlockPos blockPos, float scale, float width, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingBoxLines(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r2, g2, b2, a2);
    }

    public static void drawGradientPyramidBBFullBox(BlockPos blockPos, boolean stopCull, boolean flipY, float scale, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glDisable(GL_CULL_FACE);
        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingPyramidFilled(INSTANCE.getBuffer(), bb, stopCull, flipY, height, scale, r, g, b, a, r2, g2, b2, a2);
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientCrownBBFullBox(BlockPos blockPos, boolean flipY, float scale, float height, float innerHeight, int color, int color2, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        drawBoundingCrownFilled(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r2, g2, b2, a2, ir, ig, ib, ia);
    }

    public static void drawGradientPyramidBBLineBox(BlockPos blockPos, boolean flipY, float scale, float width, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        AxisAlignedBB bb = getBoundingFromPos(blockPos);
        GL11.glLineWidth(width);
        drawBoundingPyramidLines(INSTANCE.getBuffer(), bb, flipY, height, scale, r, g, b, a, r2, g2, b2, a2);
    }

    public static void drawGradientBlockBBFullBox(AxisAlignedBB bb, boolean stopCull, float scale, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        drawBoundingBoxFilled(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r2, g2, b2, a2);
        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientBlockBBLineBox(AxisAlignedBB bb, float scale, float width, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(width);
        drawBoundingBoxLines(INSTANCE.getBuffer(), bb, height, scale, r, g, b, a, r2, g2, b2, a2);
    }

    public static void drawGradientPyramidBBFullBox(AxisAlignedBB bb, boolean stopCull, boolean flipY, float scale, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glDisable(GL_CULL_FACE);
        drawBoundingPyramidFilled(INSTANCE.getBuffer(), bb, stopCull, flipY, height, scale, r, g, b, a, r2, g2, b2, a2);
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientCrownBBFullBox(AxisAlignedBB bb, boolean flipY, float scale, float height, float innerHeight, int color, int color2, int innerColor) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        int ir = ColorUtil.getRed(innerColor);
        int ig = ColorUtil.getGreen(innerColor);
        int ib = ColorUtil.getBlue(innerColor);
        int ia = ColorUtil.getAlpha(innerColor);

        drawBoundingCrownFilled(INSTANCE.getBuffer(), bb, flipY, height, innerHeight, scale, r, g, b, a, r2, g2, b2, a2, ir, ig, ib, ia);
    }

    public static void drawGradientPyramidBBLineBox(AxisAlignedBB bb, boolean flipY, float scale, float width, float height, int color, int color2) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(width);
        drawBoundingPyramidLines(INSTANCE.getBuffer(), bb, flipY, height, scale, r, g, b, a, r2, g2, b2, a2);
    }

    public static void drawBlockFullBox(Vec3d vec, boolean useDepth, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawFilledBox(INSTANCE.getBuffer(), useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r, g, b, a);
    }

    public static void drawGradientBlockFullBox(Vec3d vec, boolean stopCull, boolean useDepth, boolean sidesOnly, float height, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        drawGradientFilledBox(INSTANCE.getBuffer(), stopCull, useDepth, sidesOnly, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawBlockLineBox(Vec3d vec, boolean useDepth, float height, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawLineBox(INSTANCE.getBuffer(), useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r, g, b, a);
    }

    public static void drawGradientBlockLineBox(Vec3d vec, boolean useDepth, float height, float lineWidth, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawGradientLineBox(INSTANCE.getBuffer(), useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawDoubleBlockFullBox(Vec3d vec1, Vec3d vec2, boolean useDepth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawTwoPointFilledBox(INSTANCE.getBuffer(), useDepth, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r, g, b, a);
    }

    public static void drawDoubleBlockFullPyramid(Vec3d vec1, Vec3d vec2, boolean flipY, boolean useDepth, boolean flagx, boolean flagz, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawTwoPointFilledPyramid(INSTANCE.getBuffer(), false, flipY, useDepth, flagx, flagz, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r, g, b, a, r, g, b, a);
    }

    public static void drawGradientDoubleBlockFullPyramid(Vec3d vec1, Vec3d vec2, boolean stopCull, boolean flipY, boolean useDepth, boolean flagx, boolean flagz, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        drawTwoPointFilledPyramid(INSTANCE.getBuffer(), stopCull, flipY, useDepth, flagx, flagz, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawGradientDoubleBlockFullBox(Vec3d vec1, Vec3d vec2, boolean stopCull, boolean useDepth, boolean sidesOnly, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        drawGradientTwoPointFilledBox(INSTANCE.getBuffer(), stopCull, useDepth, sidesOnly, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawDoubleBlockLineBox(Vec3d vec1, Vec3d vec2, boolean useDepth, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawTwoPointLineBox(INSTANCE.getBuffer(), useDepth, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r, g, b, a);
    }

    public static void drawDoubleBlockLinePyramid(Vec3d vec1, Vec3d vec2, boolean flipY, boolean useDepth, float lineWidth, boolean flagx, boolean flagz, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawTwoPointLinePyramid(INSTANCE.getBuffer(), flipY, useDepth, flagx, flagz, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r, g, b, a, r, g, b, a);
    }

    public static void drawGradientDoubleBlockLinePyramid(Vec3d vec1, Vec3d vec2, boolean flipY, boolean useDepth, float lineWidth, boolean flagx, boolean flagz, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawTwoPointLinePyramid(INSTANCE.getBuffer(), flipY, useDepth, flagx, flagz, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawGradientDoubleBlockLineBox(Vec3d vec1, Vec3d vec2, boolean useDepth, float lineWidth, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawGradientTwoPointLineBox(INSTANCE.getBuffer(), useDepth, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawDoubleBlockFlatFullBox(Vec3d vec1, Vec3d vec2, boolean useDepth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glDisable(GL_CULL_FACE);
        drawDoublePointFlatFilledBox(INSTANCE.getBuffer(), useDepth, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.z + 0.5), r, g, b, a);
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawDoubleBlockFlatLineBox(Vec3d vec1, Vec3d vec2, boolean useDepth, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawDoublePointFlatLineBox(INSTANCE.getBuffer(), useDepth, (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.z + 0.5), r, g, b, a);
    }

    public static void drawXCross(Vec3d vec, float height, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawXCross(INSTANCE.getBuffer(), (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r, g, b, a);
    }

    public static void drawGradientXCross(Vec3d vec, float height, float lineWidth, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawGradientXCross(INSTANCE.getBuffer(), (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawFlatXCross(Vec3d vec, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawFlatXCross(INSTANCE.getBuffer(), (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, 1.0f, r, g, b, a);
    }

    public static void drawDoublePointXCross(Vec3d vec1, Vec3d vec2, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawDoublePointXCross(INSTANCE.getBuffer(), (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r, g, b, a);
    }

    public static void drawGradientDoublePointXCross(Vec3d vec1, Vec3d vec2, float lineWidth, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawGradientDoublePointXCross(INSTANCE.getBuffer(), (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.y), (float)(vec2.z + 0.5), r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawDoublePointFlatXCross(Vec3d vec1, Vec3d vec2, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawDoublePointFlatXCross(INSTANCE.getBuffer(), (float)(vec1.x + 0.5), (float)(vec1.y), (float)(vec1.z + 0.5), (float)(vec2.x + 0.5), (float)(vec2.z + 0.5), r, g, b, a);
    }

    public static void drawPyramidFullBox(Vec3d vec, boolean flipY, boolean useDepth, float height, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        drawFilledPyramid(INSTANCE.getBuffer(), false, flipY, useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r, g, b, a, r, g, b, a);
    }

    public static void drawGradientPyramidFullBox(Vec3d vec, boolean stopCull, boolean flipY, boolean useDepth, float height, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        drawFilledPyramid(INSTANCE.getBuffer(), stopCull, flipY, useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawPyramidLineBox(Vec3d vec, boolean flipY, boolean useDepth, float height, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawLinePyramid(INSTANCE.getBuffer(), flipY, useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r, g, b, a, r, g, b, a);
    }

    public static void drawGradientPyramidLineBox(Vec3d vec, boolean flipY, boolean useDepth, float height, float lineWidth, int color1, int color2) {
        int r1 = ColorUtil.getRed(color1);
        int g1 = ColorUtil.getGreen(color1);
        int b1 = ColorUtil.getBlue(color1);
        int a1 = ColorUtil.getAlpha(color1);

        int r2 = ColorUtil.getRed(color2);
        int g2 = ColorUtil.getGreen(color2);
        int b2 = ColorUtil.getBlue(color2);
        int a2 = ColorUtil.getAlpha(color2);

        GL11.glLineWidth(lineWidth);
        drawLinePyramid(INSTANCE.getBuffer(), flipY, useDepth, (float)(vec.x), (float)(vec.y), (float)(vec.z), 1.0f, height, 1.0f, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    //from earthhack
    public static float[] getRotations(ModelRenderer model) {
        return new float[]{model.rotateAngleX, model.rotateAngleY, model.rotateAngleZ};
    }

    public static float[][] getRotationsFromModel(ModelBiped modelBiped) {
        float[][] rotations = new float[5][3];
        rotations[0] = getRotations(modelBiped.bipedHead);
        rotations[1] = getRotations(modelBiped.bipedRightArm);
        rotations[2] = getRotations(modelBiped.bipedLeftArm);
        rotations[3] = getRotations(modelBiped.bipedRightLeg);
        rotations[4] = getRotations(modelBiped.bipedLeftLeg);
        return rotations;
    }

    private static void calcSkeletonVertices(EntityPlayer entity, float[][] rotations, float yOffset, float yOffset2, float xOffset) {
        //* legs area
        //top
        double[] ld1 = MathUtilFuckYou.rotationAroundAxis3d(-0.125f, yOffset, entity.isSneaking() ? -0.235 : 0.0, -xOffset * (Math.PI / 180.0f), "y");
        double[] ld2 = MathUtilFuckYou.rotationAroundAxis3d(0.125f, yOffset, entity.isSneaking() ? -0.235 : 0.0, -xOffset * (Math.PI / 180.0f), "y");

        //bottom
        double[] ld6 = MathUtilFuckYou.rotationAroundAxis3d(-0.125f, -yOffset, entity.isSneaking() ? -0.235 : 0.0, rotations[3][0], "x");
        double[] ld9 = MathUtilFuckYou.rotationAroundAxis3d(ld6[0], ld6[1], ld6[2], -xOffset * (Math.PI / 180.0f), "y");
        double[] ld7 = MathUtilFuckYou.rotationAroundAxis3d(0.125f, -yOffset, entity.isSneaking() ? -0.235 : 0.0, rotations[4][0], "x");
        double[] ld8 = MathUtilFuckYou.rotationAroundAxis3d(ld7[0], ld7[1], ld7[2], -xOffset * (Math.PI / 180.0f), "y");

        //* torso && head
        double[] td1 = MathUtilFuckYou.rotationAroundAxis3d(0.0, yOffset, entity.isSneaking() ? -0.235 : 0.0, -xOffset * (Math.PI / 180.0f), "y");
        double[] td2 = MathUtilFuckYou.rotationAroundAxis3d(0.0, yOffset2 + 0.55 + (entity.isSneaking() ? -0.05 : 0.0), entity.isSneaking() ? -0.0025 : 0.0, -xOffset * (Math.PI / 180.0f), "y");
        double[] td3 = MathUtilFuckYou.rotationAroundAxis3d(0.0,  0.3, entity.isSneaking() ? -0.0035 : 0.0, rotations[0][0], "x");
        double[] td4 = MathUtilFuckYou.rotationAroundAxis3d(td3[0], td3[1], td3[2], -(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * mc.getRenderPartialTicks()) * (Math.PI / 180.0f), "y");

        //* arms
        //mid
        double[] ad1 = MathUtilFuckYou.rotationAroundAxis3d(-0.375, yOffset2 + 0.55 + (entity.isSneaking() ? -0.05 : 0.0), entity.isSneaking() ? -0.0025 : 0.0, -xOffset * (Math.PI / 180.0f), "y");
        double[] ad2 = MathUtilFuckYou.rotationAroundAxis3d(0.375, yOffset2 + 0.55 + (entity.isSneaking() ? -0.05 : 0.0), entity.isSneaking() ? -0.0025 : 0.0, -xOffset * (Math.PI / 180.0f), "y");

        //actual arms
        //right
        double[] ad3 = MathUtilFuckYou.rotationAroundAxis3d( 0.0, -0.55, 0.0, rotations[1][0], "x");
        double[] ad31 = MathUtilFuckYou.rotationAroundAxis3d( ad3[0], ad3[1], ad3[2], -rotations[1][1], "y");
        double[] ad5 = MathUtilFuckYou.rotationAroundAxis3d(ad31[0], ad31[1], ad31[2], -rotations[1][2], "z");
        double[] ad6 = MathUtilFuckYou.rotationAroundAxis3d(ad5[0] - 0.375, ad5[1] + yOffset2 + 0.55f, ad5[2] + (entity.isSneaking() ? 0.02 : 0.0), -xOffset * (Math.PI / 180.0f), "y");
        //left
        double[] ad7 = MathUtilFuckYou.rotationAroundAxis3d( 0.0, -0.55, 0.0, rotations[2][0], "x");
        double[] ad71 = MathUtilFuckYou.rotationAroundAxis3d(ad7[0], ad7[1], ad7[2], -rotations[2][1], "y");
        double[] ad8 = MathUtilFuckYou.rotationAroundAxis3d(ad71[0], ad71[1], ad71[2], -rotations[2][2], "z");
        double[] ad9 = MathUtilFuckYou.rotationAroundAxis3d(ad8[0] + 0.375, ad8[1] + yOffset2    + 0.55f, ad8[2] + (entity.isSneaking() ? 0.02 : 0.0), -xOffset * (Math.PI / 180.0f), "y");

        skeletonVerticesMap.put(entity, new double[][] {
                ld1, ld2, ld9, ld8, td1, td2, td4, ad1, ad2, ad6, ad9
        });//    0    1    2    3    4    5    6    7    8    9   10
    }

    public static void drawSkeleton(EntityPlayer entity, float[][] rotations, float width, boolean fadeLimbs, boolean rollingColor, int rollColor1, int rollColor2, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);
        BufferBuilder buffer = INSTANCE.getBuffer();
        float xOffset = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * mc.getRenderPartialTicks();
        float yOffset = entity.isSneaking() ? 0.6f : 0.75f;
        float yOffset2 = entity.isSneaking() ? 0.45f : 0.75f;
        Vec3d entityPos = EntityUtil.getInterpolatedEntityPos(entity, mc.getRenderPartialTicks());

        BackgroundThreadStuff.INSTANCE.skeletonCalcsQueue.add(() -> calcSkeletonVertices(entity, rotations, yOffset, yOffset2, xOffset));

        if (!skeletonVerticesMap.containsKey(entity))
            return;

        double[][] vertices = skeletonVerticesMap.get(entity);
        double[] legs1 = vertices[2];
        double[] legs2 = vertices[0];
        double[] legs3 = vertices[1];
        double[] legs4 = vertices[3];
        double[] mid1 = vertices[4];
        double[] mid2 = vertices[5];
        double[] mid3 = vertices[6];
        double[] arms1 = vertices[9];
        double[] arms2 = vertices[7];
        double[] arms3 = vertices[8];
        double[] arms4 = vertices[10];

        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glLineWidth(width);
        begin(GL_LINE_STRIP);

        if (rollingColor) {
            int rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 0, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            if (fadeLimbs) buffer.pos(entityPos.x + legs1[0], entityPos.y + yOffset + legs1[1], entityPos.z + legs1[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), 0).endVertex();
            else buffer.pos(entityPos.x + legs1[0], entityPos.y + yOffset + legs1[1], entityPos.z + legs1[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 1000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + legs2[0], entityPos.y + legs2[1], entityPos.z + legs2[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 1000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + legs3[0], entityPos.y + legs3[1], entityPos.z + legs3[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 0, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            if (fadeLimbs) buffer.pos(entityPos.x + legs4[0], entityPos.y + yOffset + legs4[1], entityPos.z + legs4[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), 0).endVertex();
            else buffer.pos(entityPos.x + legs4[0], entityPos.y + yOffset + legs4[1], entityPos.z + legs4[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            render();
            begin(GL_LINE_STRIP);

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 1000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + mid1[0], entityPos.y + mid1[1], entityPos.z + mid1[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();
            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 2000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + mid2[0], entityPos.y + mid2[1], entityPos.z + mid2[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();
            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 3000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            if (fadeLimbs) buffer.pos(entityPos.x + mid3[0], entityPos.y + yOffset2 + 0.55f + (entity.isSneaking() ? -0.05 : 0.0) + mid3[1], entityPos.z + mid3[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), 0).endVertex();
            else buffer.pos(entityPos.x + mid3[0], entityPos.y + yOffset2 + 0.55f + (entity.isSneaking() ? -0.05 : 0.0) + mid3[1], entityPos.z + mid3[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            render();
            begin(GL_LINE_STRIP);

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 1000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            if (fadeLimbs) buffer.pos(entityPos.x + arms1[0], entityPos.y + arms1[1], entityPos.z + arms1[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), 0).endVertex();
            else buffer.pos(entityPos.x + arms1[0], entityPos.y + arms1[1], entityPos.z + arms1[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 2000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + arms2[0], entityPos.y + arms2[1], entityPos.z + arms2[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();
            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 2000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            buffer.pos(entityPos.x + arms3[0], entityPos.y + arms3[1], entityPos.z + arms3[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();

            rollColor = ColorUtil.rolledColor(rollColor1, rollColor2, 1000, ESP.INSTANCE.espSkeletonRollingColorSpeed.getValue(), 0.1f);
            if (fadeLimbs) buffer.pos(entityPos.x + arms4[0], entityPos.y + arms4[1], entityPos.z + arms4[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), 0).endVertex();
            else buffer.pos(entityPos.x + arms4[0], entityPos.y + arms4[1], entityPos.z + arms4[2]).color(ColorUtil.getRed(rollColor), ColorUtil.getGreen(rollColor), ColorUtil.getBlue(rollColor), ColorUtil.getAlpha(rollColor)).endVertex();
        }
        else {
            if (fadeLimbs) buffer.pos(entityPos.x + legs1[0], entityPos.y + yOffset + legs1[1], entityPos.z + legs1[2]).color(r, g, b, 0).endVertex();
            else buffer.pos(entityPos.x + legs1[0], entityPos.y + yOffset + legs1[1], entityPos.z + legs1[2]).color(r, g, b, a).endVertex();
            buffer.pos(entityPos.x + legs2[0], entityPos.y + legs2[1], entityPos.z + legs2[2]).color(r, g, b, a).endVertex();

            buffer.pos(entityPos.x + legs3[0], entityPos.y + legs3[1], entityPos.z + legs3[2]).color(r, g, b, a).endVertex();
            if (fadeLimbs) buffer.pos(entityPos.x + legs4[0], entityPos.y + yOffset + legs4[1], entityPos.z + legs4[2]).color(r, g, b, 0).endVertex();
            else buffer.pos(entityPos.x + legs4[0], entityPos.y + yOffset + legs4[1], entityPos.z + legs4[2]).color(r, g, b, a).endVertex();

            render();
            begin(GL_LINE_STRIP);

            buffer.pos(entityPos.x + mid1[0], entityPos.y + mid1[1], entityPos.z + mid1[2]).color(r, g, b, a).endVertex();
            buffer.pos(entityPos.x + mid2[0], entityPos.y + mid2[1], entityPos.z + mid2[2]).color(r, g, b, a).endVertex();
            if (fadeLimbs) buffer.pos(entityPos.x + mid3[0], entityPos.y + yOffset2 + 0.55f + (entity.isSneaking() ? -0.05 : 0.0) + mid3[1], entityPos.z + mid3[2]).color(r, g, b, 0).endVertex();
            else buffer.pos(entityPos.x + mid3[0], entityPos.y + yOffset2 + 0.55f + (entity.isSneaking() ? -0.05 : 0.0) + mid3[1], entityPos.z + mid3[2]).color(r, g, b, a).endVertex();

            render();
            begin(GL_LINE_STRIP);

            if (fadeLimbs) buffer.pos(entityPos.x + arms1[0], entityPos.y + arms1[1], entityPos.z + arms1[2]).color(r, g, b, 0).endVertex();
            else buffer.pos(entityPos.x + arms1[0], entityPos.y + arms1[1], entityPos.z + arms1[2]).color(r, g, b, a).endVertex();
            buffer.pos(entityPos.x + arms2[0], entityPos.y + arms2[1], entityPos.z + arms2[2]).color(r, g, b, a).endVertex();
            buffer.pos(entityPos.x + arms3[0], entityPos.y + arms3[1], entityPos.z + arms3[2]).color(r, g, b, a).endVertex();
            if (fadeLimbs) buffer.pos(entityPos.x + arms4[0], entityPos.y + arms4[1], entityPos.z + arms4[2]).color(r, g, b, 0).endVertex();
            else buffer.pos(entityPos.x + arms4[0], entityPos.y + arms4[1], entityPos.z + arms4[2]).color(r, g, b, a).endVertex();
        }
        render();

        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawXCross(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a) {
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINES);
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();

        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();

        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();

        buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawGradientXCross(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        GL11.glEnable(GL_LINE_SMOOTH);
        
        begin(GL_LINES);
        buffer.pos(x, y, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w, y + h, z + d).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x, y, z + d).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w, y + h, z).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + w, y, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x, y + h, z + d).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + w, y, z + d).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x, y + h, z).color(r2, g2, b2, a2).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawFlatXCross(BufferBuilder buffer, float x, float y, float z, float w, float d, int r, int g, int b, int a) {
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINES);
        buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();

        buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
        buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawDoublePointXCross(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINES);
        buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y2, z2).color(r, g, b, a).endVertex();

        buffer.pos(x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.pos(x2, y2, z1).color(r, g, b, a).endVertex();

        buffer.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y2, z2).color(r, g, b, a).endVertex();

        buffer.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y1, z2).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawGradientDoublePointXCross(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINES);
        buffer.pos(x1, y1, z1).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2, y2, z2).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x1, y1, z2).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2, y2, z1).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x2, y1, z1).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, z2).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x2, y1, z2).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, z1).color(r2, g2, b2, a2).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawDoublePointFlatXCross(BufferBuilder buffer, float x1, float y, float z1, float x2, float z2, int r, int g, int b, int a) {
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINES);
        buffer.pos(x1, y, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y, z2).color(r, g, b, a).endVertex();

        buffer.pos(x1, y, z2).color(r, g, b, a).endVertex();
        buffer.pos(x2, y, z1).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawFlatLineBox(BufferBuilder buffer, boolean useDepth, float x, float y, float z, float w, float d, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawFlatFilledBox(BufferBuilder buffer, boolean useDepth, float x, float y, float z, float w, float d, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        begin(GL_QUADS);
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawDoublePointFlatLineBox(BufferBuilder buffer, boolean useDepth, float x1, float y, float z1, float x2, float z2, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x1 + offset, y + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y + offset, z1 + offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawDoublePointFlatFilledBox(BufferBuilder buffer, boolean useDepth, float x1, float y, float z1, float x2, float z2, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        begin(GL_QUADS);
        buffer.pos(x1 + offset, y + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y + offset, z2 - offset).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawLineBox(BufferBuilder buffer, boolean useDepth, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_LINES);
        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawLinePyramid(BufferBuilder buffer, boolean flipY, boolean useDepth, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;

        double up = flipY ? y + offset : y + h;
        double down = flipY ? y + h : y + offset;

        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x + offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, down, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, down, z + offset).color(r, g, b, a).endVertex();

        buffer.pos(x + (w * 0.5f), up, z + (d * 0.5f)).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + w - offset, down, z + d - offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_LINE_STRIP);
        buffer.pos(x + w - offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + (w * 0.5f), up, z + (d * 0.5f)).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + offset, down, z + d - offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawGradientLineBox(BufferBuilder buffer, boolean useDepth, float x, float y, float z, float w, float h, float d, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x + offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_LINES);
        buffer.pos(x + w - offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawFilledBox(BufferBuilder buffer, boolean useDepth, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        begin(GL_QUAD_STRIP);
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r, g, b, a).endVertex();

        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r, g, b, a).endVertex();

        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r, g, b, a).endVertex();

        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r, g, b, a).endVertex();

        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_QUADS);
        buffer.pos(x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + offset, z + d - offset).color(r, g, b, a).endVertex();

        buffer.pos(x + offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawFilledPyramid(BufferBuilder buffer, boolean stopCull, boolean flipY, boolean useDepth, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;

        double up = flipY ? y + offset : y + h;
        double down = flipY ? y + h : y + offset;

        if (flipY) GL11.glFrontFace(GL_CCW);
        else GL11.glFrontFace(GL_CW);

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        else GL11.glEnable(GL_CULL_FACE);

        begin(GL_TRIANGLE_FAN);
        buffer.pos(x + (w * 0.5f), up, z + (d * 0.5f)).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, down, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, down, z + offset).color(r, g, b, a).endVertex();
        render();

        if (flipY) GL11.glFrontFace(GL_CW);
        else GL11.glFrontFace(GL_CCW);

        begin(GL_QUADS);
        buffer.pos(x + offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + offset).color(r, g, b, a).endVertex();
        buffer.pos(x + w - offset, down, z + d - offset).color(r, g, b, a).endVertex();
        buffer.pos(x + offset, down, z + d - offset).color(r, g, b, a).endVertex();
        render();

        GL11.glFrontFace(GL_CCW);
        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientFilledBox(BufferBuilder buffer, boolean stopCull, boolean useDepth, boolean sidesOnly, float x, float y, float z, float w, float h, float d, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        else GL11.glEnable(GL_CULL_FACE);

        begin(GL_QUAD_STRIP);
        buffer.pos(x + offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + w - offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + w - offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + w - offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x + offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x + offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
        render();

        if (!sidesOnly) {
            begin(GL_QUADS);
            buffer.pos(x + offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x + w - offset, y + offset, z + offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x + w - offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x + offset, y + offset, z + d - offset).color(r1, g1, b1, a1).endVertex();

            buffer.pos(x + offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x + w - offset, y + h, z + d - offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x + w - offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x + offset, y + h, z + offset).color(r2, g2, b2, a2).endVertex();
            render();
        }

        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawTwoPointLineBox(BufferBuilder buffer, boolean useDepth, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_LINES);
        buffer.pos(x1 + offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawTwoPointLinePyramid(BufferBuilder buffer, boolean flipY, boolean useDepth, boolean flagx, boolean flagz, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;
        float w = Math.abs(x1 - x2) * 0.5f;
        float d = Math.abs(z1 - z2) * 0.5f;

        if (flagx) {
            w *= -1.0f;
        }

        if (flagz) {
            d *= -1.0f;
        }

        double up = flipY ? y1 + offset : y2;
        double down = flipY ? y2 : y1 + offset;

        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x1 + offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, down, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, down, z1 + offset).color(r, g, b, a).endVertex();

        buffer.pos(x1 + w, up, z1 + d).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2 - offset, down, z2 - offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_LINE_STRIP);
        buffer.pos(x2 - offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + w, up, z1 + d).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1 + offset, down, z2 - offset).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawGradientTwoPointLineBox(BufferBuilder buffer, boolean useDepth, float x1, float y1, float z1, float x2, float y2, float z2, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;
        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1 + offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_LINES);
        buffer.pos(x1 + offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawTwoPointFilledBox(BufferBuilder buffer, boolean useDepth, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
        double offset = useDepth ? 0.003 : 0.0;
        begin(GL_QUAD_STRIP);
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r, g, b, a).endVertex();

        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r, g, b, a).endVertex();

        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r, g, b, a).endVertex();

        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z2 - offset).color(r, g, b, a).endVertex();

        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        render();

        begin(GL_QUADS);
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r, g, b, a).endVertex();

        buffer.pos(x1 + offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawTwoPointFilledPyramid(BufferBuilder buffer, boolean stopCull, boolean flipY, boolean useDepth, boolean flagx, boolean flagz, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;
        float w = Math.abs(x1 - x2) * 0.5f;
        float d = Math.abs(z1 - z2) * 0.5f;

        if (flagx) {
            w *= -1.0f;
        }

        if (flagz) {
            d *= -1.0f;
        }

        double up = flipY ? y1 + offset : y2;
        double down = flipY ? y2 : y1 + offset;

        if (flipY) GL11.glFrontFace(GL_CCW);
        else GL11.glFrontFace(GL_CW);

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        else GL11.glEnable(GL_CULL_FACE);

        begin(GL_TRIANGLE_FAN);
        buffer.pos(x1 + w, up, z1 + d).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x1 + offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, down, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, down, z1 + offset).color(r, g, b, a).endVertex();
        render();

        if (flipY) GL11.glFrontFace(GL_CW);
        else GL11.glFrontFace(GL_CCW);

        begin(GL_QUADS);
        buffer.pos(x1 + offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z1 + offset).color(r, g, b, a).endVertex();
        buffer.pos(x2 - offset, down, z2 - offset).color(r, g, b, a).endVertex();
        buffer.pos(x1 + offset, down, z2 - offset).color(r, g, b, a).endVertex();
        render();

        GL11.glFrontFace(GL_CCW);
        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawGradientTwoPointFilledBox(BufferBuilder buffer, boolean stopCull, boolean useDepth, boolean sidesOnly, float x1, float y1, float z1, float x2, float y2, float z2, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
        double offset = useDepth ? 0.003 : 0.0;

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        else GL11.glEnable(GL_CULL_FACE);

        begin(GL_QUAD_STRIP);
        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2 - offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();

        buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1 + offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
        render();

        if (!sidesOnly) {
            begin(GL_QUADS);
            buffer.pos(x1 + offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x2 - offset, y1 + offset, z1 + offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x2 - offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();
            buffer.pos(x1 + offset, y1 + offset, z2 - offset).color(r1, g1, b1, a1).endVertex();

            buffer.pos(x1 + offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x2 - offset, y2, z2 - offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x2 - offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
            buffer.pos(x1 + offset, y2, z1 + offset).color(r2, g2, b2, a2).endVertex();
            render();
        }

        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    /**
     * @param height range: 0.0f - 1.0f
     */
    public static void drawBoundingBoxLines(BufferBuilder buffer, AxisAlignedBB boundingBox, float height, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_LINES);
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    /**
     * @param height range: 0.0f - 1.0f
     */
    public static void drawBoundingBoxFilled(BufferBuilder buffer, AxisAlignedBB boundingBox, float height, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        begin(GL_QUAD_STRIP);
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();

        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();

        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();

        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();

        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_QUADS);
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(r, g, b, a).endVertex();

        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();

        render();
    }

    /**
     * @param height range: 0.0f - 1.0f
     */
    public static void drawBoundingPyramidLines(BufferBuilder buffer, AxisAlignedBB boundingBox, boolean flipY, float height, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        double up = flipY ? boundingBox.minY : boundingBox.maxY;
        double down = flipY ? boundingBox.maxY : boundingBox.minY;
        double w = boundingBox.maxX - boundingBox.minX;
        double d = boundingBox.maxZ - boundingBox.minZ;

        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_STRIP);
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();

        buffer.pos(boundingBox.minX + (w * 0.5f), up, boundingBox.minZ + (d * 0.5f)).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        render();

        begin(GL_LINE_STRIP);
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX + (w * 0.5f), up, boundingBox.minZ + (d * 0.5f)).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    /**
     * @param height range: 0.0f - 1.0f
     */
    public static void drawBoundingPyramidFilled(BufferBuilder buffer, AxisAlignedBB boundingBox, boolean stopCull, boolean flipY, float height, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        double up = flipY ? boundingBox.minY : boundingBox.maxY;
        double down = flipY ? boundingBox.maxY : boundingBox.minY;

        if (flipY) GL11.glFrontFace(GL_CCW);
        else GL11.glFrontFace(GL_CW);

        if (stopCull) GL11.glDisable(GL_CULL_FACE);
        else GL11.glEnable(GL_CULL_FACE);

        begin(GL_TRIANGLE_FAN);
        buffer.pos((boundingBox.maxX + boundingBox.minX) * 0.5f, up, (boundingBox.maxZ + boundingBox.minZ) * 0.5f).color(r2, g2, b2, a2).endVertex();

        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        render();

        if (flipY) GL11.glFrontFace(GL_CW);
        else GL11.glFrontFace(GL_CCW);

        begin(GL_QUADS);
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        render();

        GL11.glFrontFace(GL_CCW);
        if (stopCull) GL11.glEnable(GL_CULL_FACE);
    }

    /**
     * @param height range: 0.0f - 1.0f
     * @param innerHeight range: 0.0f - 1.0f
     */
    public static void drawBoundingCrownLines(BufferBuilder buffer, AxisAlignedBB boundingBox, boolean flipY, float height, float innerHeight, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2, int ir, int ig, int ib, int ia) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        double up = flipY ? boundingBox.minY : boundingBox.maxY;
        double down = flipY ? boundingBox.maxY : boundingBox.minY;
        double midX = (boundingBox.maxX + boundingBox.minX) * 0.5f;
        double midY = MathUtilFuckYou.linearInterp(down, up, innerHeight * 300.0f);
        double midZ = (boundingBox.maxZ + boundingBox.minZ) * 0.5f;

        GL11.glEnable(GL_LINE_SMOOTH);
        begin(GL_LINE_LOOP);
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, up, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.maxX, up, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, up, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.maxX, up, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_LINE_LOOP);
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    /**
     * @param height range: 0.0f - 1.0f
     * @param innerHeight range: 0.0f - 1.0f
     */
    public static void drawBoundingCrownFilled(BufferBuilder buffer, AxisAlignedBB boundingBox, boolean flipY, float height, float innerHeight, float scale, int r, int g, int b, int a, int r2, int g2, int b2, int a2, int ir, int ig, int ib, int ia) {
        boundingBox = EntityUtil.scaleBB(boundingBox, scale, height);

        double up = flipY ? boundingBox.minY : boundingBox.maxY;
        double down = flipY ? boundingBox.maxY : boundingBox.minY;
        double midX = (boundingBox.maxX + boundingBox.minX) * 0.5f;
        double midY = MathUtilFuckYou.linearInterp(down, up, innerHeight * 300.0f);
        double midZ = (boundingBox.maxZ + boundingBox.minZ) * 0.5f;

        if (flipY) GL11.glFrontFace(GL_CCW);
        else GL11.glFrontFace(GL_CW);

        begin(GL_TRIANGLE_FAN);
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();

        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        render();

        if (flipY) GL11.glFrontFace(GL_CW);
        else GL11.glFrontFace(GL_CCW);

        begin(GL_QUADS);
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        render();

        GL11.glFrontFace(GL_CCW);

        GL11.glDisable(GL_CULL_FACE);

        begin(GL_TRIANGLES);
        buffer.pos(boundingBox.maxX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.maxX, up, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_TRIANGLES);
        buffer.pos(boundingBox.maxX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.maxX, up, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_TRIANGLES);
        buffer.pos(boundingBox.minX, down, boundingBox.maxZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.minX, up, boundingBox.maxZ).color(r2, g2, b2, a2).endVertex();
        render();

        begin(GL_TRIANGLES);
        buffer.pos(boundingBox.minX, down, boundingBox.minZ).color(r, g, b, a).endVertex();
        buffer.pos(midX, midY, midZ).color(ir, ig, ib, ia).endVertex();
        buffer.pos(boundingBox.minX, up, boundingBox.minZ).color(r2, g2, b2, a2).endVertex();
        render();

        GL11.glEnable(GL_CULL_FACE);
    }

    public static AxisAlignedBB getBoundingFromPos(BlockPos pos) {
        IBlockState iBlockState = mc.world.getBlockState(pos);
        return iBlockState.getSelectedBoundingBox(mc.world, pos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
    }

    public static Vec3d[] verticesFromBlockFace(BlockPos pos, EnumFacing face) {
        AxisAlignedBB bb = getBoundingFromPos(pos);

        switch (face) {
            case UP: {
                return new Vec3d[] {
                        new Vec3d(bb.minX, bb.maxY, bb.minZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.minZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
                        new Vec3d(bb.minX, bb.maxY, bb.maxZ)
                };
            }

            case DOWN: {
                return new Vec3d[] {
                        new Vec3d(bb.minX, bb.minY, bb.minZ),
                        new Vec3d(bb.maxX, bb.minY, bb.minZ),
                        new Vec3d(bb.maxX, bb.minY, bb.maxZ),
                        new Vec3d(bb.minX, bb.minY, bb.maxZ)
                };
            }

            case NORTH: {
                return new Vec3d[] {
                        new Vec3d(bb.minX, bb.maxY, bb.minZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.minZ),
                        new Vec3d(bb.maxX, bb.minY, bb.minZ),
                        new Vec3d(bb.minX, bb.minY, bb.minZ)
                };
            }

            case SOUTH: {
                return new Vec3d[] {
                        new Vec3d(bb.minX, bb.minY, bb.maxZ),
                        new Vec3d(bb.maxX, bb.minY, bb.maxZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
                        new Vec3d(bb.minX, bb.maxY, bb.maxZ)
                };
            }

            case EAST: {
                return new Vec3d[] {
                        new Vec3d(bb.maxX, bb.minY, bb.minZ),
                        new Vec3d(bb.maxX, bb.minY, bb.maxZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
                        new Vec3d(bb.maxX, bb.maxY, bb.minZ)
                };
            }

            case WEST: {
                return new Vec3d[] {
                        new Vec3d(bb.minX, bb.minY, bb.minZ),
                        new Vec3d(bb.minX, bb.minY, bb.maxZ),
                        new Vec3d(bb.minX, bb.maxY, bb.maxZ),
                        new Vec3d(bb.minX, bb.maxY, bb.minZ)
                };
            }
        }

        return new Vec3d[] {new Vec3d(0.0, 0.0, 0.0),
                new Vec3d(0.0, 0.0, 0.0),
                new Vec3d(0.0, 0.0, 0.0),
                new Vec3d(0.0, 0.0, 0.0)};
    }

    public static void drawBlockFaceFilledBB(BlockPos pos, EnumFacing face, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glDisable(GL_CULL_FACE);
        drawBlockFaceFilledBB(INSTANCE.getBuffer(), pos, face, r, g, b, a);
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawBlockFaceFilledBB(BufferBuilder buffer, BlockPos pos, EnumFacing face, int r, int g, int b, int a) {
        Vec3d[] vertices = verticesFromBlockFace(pos, face);

        begin(GL_QUADS);
        buffer.pos(vertices[0].x, vertices[0].y, vertices[0].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[1].x, vertices[1].y, vertices[1].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[2].x, vertices[2].y, vertices[2].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[3].x, vertices[3].y, vertices[3].z).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawBlockFaceLinesBB(BlockPos pos, EnumFacing face, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glLineWidth(lineWidth);
        drawBlockFaceLinesBB(INSTANCE.getBuffer(), pos, face, r, g, b, a);
    }

    public static void drawBlockFaceLinesBB(BufferBuilder buffer, BlockPos pos, EnumFacing face, int r, int g, int b, int a) {
        Vec3d[] vertices = verticesFromBlockFace(pos, face);

        begin(GL_LINE_STRIP);
        buffer.pos(vertices[0].x, vertices[0].y, vertices[0].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[1].x, vertices[1].y, vertices[1].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[2].x, vertices[2].y, vertices[2].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[3].x, vertices[3].y, vertices[3].z).color(r, g, b, a).endVertex();
        buffer.pos(vertices[0].x, vertices[0].y, vertices[0].z).color(r, g, b, a).endVertex();
        render();
    }

    public static void drawLineToVec(Vec3d vec1, Vec3d vec2, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);
        BufferBuilder buffer = INSTANCE.getBuffer();

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL_LINE_SMOOTH);

        begin(GL_LINES);
        buffer.pos(vec1.x, vec1.y, vec1.z).color(r, g, b, a).endVertex();
        buffer.pos(vec2.x, vec2.y, vec2.z).color(r, g, b, a).endVertex();
        render();

        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawTracer(Entity entity, float lineWidth, boolean spine, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);
        Vec3d entityPos = EntityUtil.interpolateEntityRender(entity, mc.getRenderPartialTicks());
        assert mc.renderViewEntity != null;
        Vec3d selfPos = EntityUtil.interpolateEntityRender(mc.renderViewEntity, mc.getRenderPartialTicks());
        double[] rotations = MathUtilFuckYou.rotationAroundAxis3d(0.0f, 0.0f, 1.0f, mc.renderViewEntity.rotationPitch * (float)(Math.PI / 180.0f), "x");
        rotations = MathUtilFuckYou.rotationAroundAxis3d(rotations[0], rotations[1], rotations[2], -mc.renderViewEntity.rotationYaw * (float)(Math.PI / 180.0f), "y");
        selfPos = new Vec3d(selfPos.x + rotations[0], selfPos.y + mc.renderViewEntity.getEyeHeight() + rotations[1], selfPos.z + rotations[2]);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);

        if (mc.gameSettings.viewBobbing) {
            GL11.glLoadIdentity();
            mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());
        }

        //doesnt use bufferbuilder bc viewbobbing is gay and it fucks up the tracers centering on the crosshairs >:(
        GL11.glBegin(GL_LINES);
        if (spine) {
            GL11.glVertex3d(entityPos.x, entityPos.y, entityPos.z);
            GL11.glVertex3d(entityPos.x, entityPos.y + entity.height, entityPos.z);
        }

        GL11.glVertex3d(entityPos.x, entityPos.y, entityPos.z);
        GL11.glVertex3d(selfPos.x, selfPos.y, selfPos.z);

        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);

        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawTracer(Vec3d vec3d, float lineWidth, int color) {
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);
        assert mc.renderViewEntity != null;
        Vec3d selfPos = EntityUtil.interpolateEntityRender(mc.renderViewEntity, mc.getRenderPartialTicks());
        double[] rotations = MathUtilFuckYou.rotationAroundAxis3d(0.0f, 0.0f, 1.0f, mc.renderViewEntity.rotationPitch * (float)(Math.PI / 180.0f), "x");
        rotations = MathUtilFuckYou.rotationAroundAxis3d(rotations[0], rotations[1], rotations[2], -mc.renderViewEntity.rotationYaw * (float)(Math.PI / 180.0f), "y");
        selfPos = new Vec3d(selfPos.x + rotations[0], selfPos.y + mc.renderViewEntity.getEyeHeight() + rotations[1], selfPos.z + rotations[2]);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);

        if (mc.gameSettings.viewBobbing) {
            GL11.glLoadIdentity();
            mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());
        }

        //doesnt use bufferbuilder bc viewbobbing is gay and it fucks up the tracers centering on the crosshairs >:(
        GL11.glBegin(GL_LINES);
        GL11.glVertex3d(vec3d.x - mc.getRenderManager().renderPosX, vec3d.y - mc.getRenderManager().renderPosY, vec3d.z - mc.getRenderManager().renderPosZ);
        GL11.glVertex3d(selfPos.x, selfPos.y, selfPos.z);

        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);

        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void drawPlayer(EntityOtherPlayerMP entityPlayer, ModelPlayer model, float limbSwing, float limbSwingAmount, float headYaw, float headPitch, boolean solid, boolean lines, boolean points, float lineWidth, float pointSize, float alphaFactor, boolean texture, float swingProgress,
                                  int solidColorFriend, int lineColorFriend, int pointColorFriend,
                                  int solidColorEnemy, int lineColorEnemy, int pointColorEnemy,
                                  int solidColorSelf, int lineColorSelf, int pointColorSelf,
                                  int solidColor, int lineColor, int pointColor) {
        int sr, sg, sb, sa, lr, lg, lb, la, pr, pg, pb, pa;

        if (entityPlayer.getName().equals(mc.player.getName())) {
            sr = ColorUtil.getRed(solidColorSelf);
            sg = ColorUtil.getGreen(solidColorSelf);
            sb = ColorUtil.getBlue(solidColorSelf);
            sa = (int)(ColorUtil.getAlpha(solidColorSelf) * alphaFactor / 300.0f);

            lr = ColorUtil.getRed(lineColorSelf);
            lg = ColorUtil.getGreen(lineColorSelf);
            lb = ColorUtil.getBlue(lineColorSelf);
            la = (int)(ColorUtil.getAlpha(lineColorSelf) * alphaFactor / 300.0f);

            pr = ColorUtil.getRed(pointColorSelf);
            pg = ColorUtil.getGreen(pointColorSelf);
            pb = ColorUtil.getBlue(pointColorSelf);
            pa = (int)(ColorUtil.getAlpha(pointColorSelf) * alphaFactor / 300.0f);
        }
        else {
            if (FriendManager.isFriend(entityPlayer)) {
                sr = ColorUtil.getRed(solidColorFriend);
                sg = ColorUtil.getGreen(solidColorFriend);
                sb = ColorUtil.getBlue(solidColorFriend);
                sa = (int)(ColorUtil.getAlpha(solidColorFriend) * alphaFactor / 300.0f);

                lr = ColorUtil.getRed(lineColorFriend);
                lg = ColorUtil.getGreen(lineColorFriend);
                lb = ColorUtil.getBlue(lineColorFriend);
                la = (int)(ColorUtil.getAlpha(lineColorFriend) * alphaFactor / 300.0f);

                pr = ColorUtil.getRed(pointColorFriend);
                pg = ColorUtil.getGreen(pointColorFriend);
                pb = ColorUtil.getBlue(pointColorFriend);
                pa = (int)(ColorUtil.getAlpha(pointColorFriend) * alphaFactor / 300.0f);
            } else if (EnemyManager.isEnemy(entityPlayer)) {
                sr = ColorUtil.getRed(solidColorEnemy);
                sg = ColorUtil.getGreen(solidColorEnemy);
                sb = ColorUtil.getBlue(solidColorEnemy);
                sa = (int)(ColorUtil.getAlpha(solidColorEnemy) * alphaFactor / 300.0f);

                lr = ColorUtil.getRed(lineColorEnemy);
                lg = ColorUtil.getGreen(lineColorEnemy);
                lb = ColorUtil.getBlue(lineColorEnemy);
                la = (int)(ColorUtil.getAlpha(lineColorEnemy) * alphaFactor / 300.0f);

                pr = ColorUtil.getRed(pointColorEnemy);
                pg = ColorUtil.getGreen(pointColorEnemy);
                pb = ColorUtil.getBlue(pointColorEnemy);
                pa = (int)(ColorUtil.getAlpha(pointColorEnemy) * alphaFactor / 300.0f);
            } else {
                sr = ColorUtil.getRed(solidColor);
                sg = ColorUtil.getGreen(solidColor);
                sb = ColorUtil.getBlue(solidColor);
                sa = (int)(ColorUtil.getAlpha(solidColor) * alphaFactor / 300.0f);

                lr = ColorUtil.getRed(lineColor);
                lg = ColorUtil.getGreen(lineColor);
                lb = ColorUtil.getBlue(lineColor);
                la = (int)(ColorUtil.getAlpha(lineColor) * alphaFactor / 300.0f);

                pr = ColorUtil.getRed(pointColor);
                pg = ColorUtil.getGreen(pointColor);
                pb = ColorUtil.getBlue(pointColor);
                pa = (int)(ColorUtil.getAlpha(pointColor) * alphaFactor / 300.0f);
            }
        }

        GL11.glPushMatrix();
        Vec3d pos = EntityUtil.interpolateEntityRender(entityPlayer, mc.getRenderPartialTicks());
        GL11.glTranslated(pos.x, pos.y, pos.z);
        GlStateManager.enableRescaleNormal();
        GL11.glRotatef(180.0f - entityPlayer.rotationYaw, 0.0f, 1.0f, 0.0f);
        GL11.glScalef(-1.0f, -1.0f, 1.0f);
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
        GL11.glTranslatef(0.0f, -1.501f, 0.0f);

        if (solid) {
            if (texture) {
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GL11.glDepthRange(0.0, 0.01);
                GL11.glEnable(GL_TEXTURE_2D);
                Command.mc.getTextureManager().bindTexture(entityPlayer.getLocationSkin());
            }
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            GL11.glColor4f(sr / 255.0f, sg / 255.0f, sb / 255.0f, sa / 255.0f);
            model.render(entityPlayer, limbSwing, limbSwingAmount, entityPlayer.ticksExisted, headYaw, headPitch, 0.0625f);
            if (texture) {
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GL11.glDepthRange(0.0, 1.0);
                GL11.glDisable(GL_TEXTURE_2D);
            }
        }

        if (lines) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            GL11.glLineWidth(lineWidth);
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glColor4f(lr / 255.0f, lg / 255.0f, lb / 255.0f, la / 255.0f);
            model.render(entityPlayer, limbSwing, limbSwingAmount, entityPlayer.ticksExisted, headYaw, headPitch, 0.0625f);
            GL11.glDisable(GL_LINE_SMOOTH);
        }

        if (points) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
            GL11.glPointSize(pointSize);
            GL11.glEnable(GL_POINT_SMOOTH);
            GL11.glColor4f(pr / 255.0f, pg / 255.0f, pb / 255.0f, pa / 255.0f);
            model.render(entityPlayer, limbSwing, limbSwingAmount, entityPlayer.ticksExisted, headYaw, headPitch, 0.0625f);
            GL11.glDisable(GL_POINT_SMOOTH);
        }

        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void outline1() {
        Framebuffer fbo = mc.framebuffer;
        if (fbo != null) {
            if (fbo.depthBuffer > -1) {
                EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
                int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
                EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID);
                EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight);
                EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID);
                EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID);
                fbo.depthBuffer = -1;
            }
        }

        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glEnable(GL_STENCIL_TEST);
        GL11.glStencilFunc(GL_NEVER, GL_LINES, 0xF);
        GL11.glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public static void outline2() {
        GL11.glStencilFunc(GL_NEVER, GL_POINTS, 0xF);
        GL11.glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public static void outline3() {
        GL11.glStencilFunc(GL_EQUAL, GL_LINES, 0xF);
        GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        GL11.glDepthRange(0.0, 0.001);
    }

    public static void outlineRelease() {
        GL11.glDepthRange(0.0, 1.0);
        GL11.glDisable(GL_STENCIL_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public static void drawPolygonSolid(Vec3d centerPos, int vertices, float radius, boolean rotateAnimate, float rotateSpeed, float rotateOffset, int color) {
        BufferBuilder buffer = INSTANCE.getBuffer();
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glDisable(GL_CULL_FACE);
        begin(GL_TRIANGLE_FAN);

        buffer.pos(centerPos.x, centerPos.y, centerPos.z).color(r, g, b, a).endVertex();

        for (int i = 0; i < vertices + 1; i++) {
            double[] d = MathUtilFuckYou.rotationAroundAxis3d(radius * cos(i * (Math.PI / (vertices * 0.5f))), 0.0f, radius * sin(i * (Math.PI / (vertices * 0.5f))),
                    (((rotateAnimate ? (double) rotateSpeed : 1.0) * rotateOffset) % 360.0) * (Math.PI / 180.0f), "y");
            double x = centerPos.x + d[0];
            double z = centerPos.z + d[2];

            buffer.pos(x, centerPos.y, z).color(r, g, b, a).endVertex();
        }

        render();
        GL11.glEnable(GL_CULL_FACE);
    }

    public static void drawPolygonLines(Vec3d centerPos, int vertices, float linesWidth, float radius, boolean rotateAnimate, float rotateSpeed, float rotateOffset, int color) {
        BufferBuilder buffer = INSTANCE.getBuffer();
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glLineWidth(linesWidth);
        begin(GL_LINE_LOOP);

        for (int i = 0; i < vertices; i++) {
            double[] d = MathUtilFuckYou.rotationAroundAxis3d(radius * cos(i * (Math.PI / (vertices * 0.5f))), 0.0f, radius * sin(i * (Math.PI / (vertices * 0.5f))),
                    (((rotateAnimate ? (double) rotateSpeed : 1.0) * rotateOffset) % 360.0) * (Math.PI / 180.0f), "y");
            double x = centerPos.x + d[0];
            double z = centerPos.z + d[2];

            buffer.pos(x, centerPos.y, z).color(r, g, b, a).endVertex();
        }

        render();
        GL11.glDisable(GL_LINE_SMOOTH);
    }

    public static void renderItemModelPre(ItemStack stack) {
        if (stack.isEmpty) return;

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void renderItemModelPost(ItemStack stack) {
        if (stack.isEmpty) return;

        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    public static void renderItemModelVanilla(ItemStack stack, IBakedModel bakedmodel, float alpha, boolean useEmissive) {
        if (stack.isEmpty) return;

        if (bakedmodel.isBuiltInRenderer()) {
            GL11.glColor4f(1, 1, 1, alpha);
            stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
        } else {
            renderModel(bakedmodel, useEmissive, new Color(255, 255, 255, (int)(alpha * 255.0f)).getRGB(), stack);

            if (stack.hasEffect()) {
                int enchantColor = -8372020;
                int er = ColorUtil.getRed(enchantColor);
                int eg = ColorUtil.getGreen(enchantColor);
                int eb = ColorUtil.getBlue(enchantColor);
                int ea = ColorUtil.getAlpha(enchantColor);

                GlStateManager.depthMask(false);
                GlStateManager.depthFunc(GL_EQUAL);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
                GlStateManager.matrixMode(5890);
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                GlStateManager.translate(f, 0.0F, 0.0F);
                GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                renderModel(bakedmodel, useEmissive, new Color((int)(er * alpha), (int)(eg * alpha), (int)(eb * alpha), ea).getRGB(), ItemStack.EMPTY);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
                GlStateManager.translate(-f1, 0.0F, 0.0F);
                GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
                renderModel(bakedmodel, useEmissive, new Color((int)(er * alpha), (int)(eg * alpha), (int)(eb * alpha), ea).getRGB(), ItemStack.EMPTY);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(GL_LEQUAL);
                GlStateManager.depthMask(true);
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            }
        }
    }

    public static void renderItemModelColorOverlay(ItemStack stack, IBakedModel bakedmodel, int overlayColor, boolean useEmissive) {
        if (stack.isEmpty) return;

        int overlayR = ColorUtil.getRed(overlayColor);
        int overlayG = ColorUtil.getGreen(overlayColor);
        int overlayB = ColorUtil.getBlue(overlayColor);
        int overlayA = ColorUtil.getAlpha(overlayColor);

        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(GL_EQUAL);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        if (bakedmodel.isBuiltInRenderer()) {
            GL11.glColor4f(overlayR / 255.0f, overlayG / 255.0f, overlayB / 255.0f, overlayA / 255.0f);
            stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
        } else {
            renderModel(bakedmodel, useEmissive, new Color(overlayR, overlayG, overlayB, overlayA).getRGB(), ItemStack.EMPTY);
        }
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthFunc(GL_LEQUAL);
        GlStateManager.depthMask(true);
    }

    public static void renderItemModelLines(ItemStack stack, IBakedModel bakedmodel, boolean outlineLines, boolean forceSolid, int linesColor, boolean useEmissive) {
        if (stack.isEmpty) return;

        int lineR = ColorUtil.getRed(linesColor);
        int lineG = ColorUtil.getGreen(linesColor);
        int lineB = ColorUtil.getBlue(linesColor);
        int lineA = ColorUtil.getAlpha(linesColor);

        BUF_FLOAT_4.put(0, lineR / 255.0f);
        BUF_FLOAT_4.put(1, lineG / 255.0f);
        BUF_FLOAT_4.put(2, lineB / 255.0f);
        BUF_FLOAT_4.put(3, lineA / 255.0f);
        GL11.glTexEnv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4);
        GL11.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
        GL11.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
        GL11.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, GL_CONSTANT);
        GL11.glDisable(GL_CULL_FACE);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        if (forceSolid) GlStateManager.disableBlend();

        if (bakedmodel.isBuiltInRenderer()) {
            GL11.glColor4f(lineR / 255.0f, lineG / 255.0f, lineB / 255.0f, lineA / 255.0f);
            if (outlineLines) {
                outline1();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                outline2();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                outline3();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                outlineRelease();
            } else {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                GL11.glEnable(GL_LINE_SMOOTH);
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                GL11.glDisable(GL_LINE_SMOOTH);
            }
        }
        else {

            if (outlineLines) {
                outline1();
                renderModel(bakedmodel, useEmissive, new Color(lineR, lineG, lineB, lineA).getRGB(), ItemStack.EMPTY);
                outline2();
                renderModel(bakedmodel, useEmissive, new Color(lineR, lineG, lineB, lineA).getRGB(), ItemStack.EMPTY);
                outline3();
                renderModel(bakedmodel, useEmissive, new Color(lineR, lineG, lineB, lineA).getRGB(), ItemStack.EMPTY);
                outlineRelease();
            } else {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                GL11.glEnable(GL_LINE_SMOOTH);
                renderModel(bakedmodel, useEmissive, new Color(lineR, lineG, lineB, lineA).getRGB(), ItemStack.EMPTY);
                GL11.glDisable(GL_LINE_SMOOTH);
            }
        }

        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        GL11.glEnable(GL_CULL_FACE);
        if (forceSolid) GlStateManager.enableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableOutlineMode();
    }

    private static void renderModel(IBakedModel model, boolean useEmissive, int color, ItemStack stack) {
        if (net.minecraftforge.common.ForgeModContainer.allowEmissiveItems && useEmissive) {
            renderLitItem(mc.getRenderItem(), model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values()) {
            mc.getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), color, stack);
        }

        mc.getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, null, 0L), color, stack);
        tessellator.draw();
    }
}