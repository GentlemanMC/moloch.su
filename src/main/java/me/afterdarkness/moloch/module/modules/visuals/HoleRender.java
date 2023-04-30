package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.client.HoleSettings;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.graphics.RenderHelper;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
//TODO: rolling color
@Parallel(runnable = true)
@ModuleInfo(name = "HoleRender", category = Category.VISUALS, description = "Highlights holes that are safe from crystal damage")
public class HoleRender extends Module {

    Setting<Page> page = setting("Page", Page.Render);

    Setting<Boolean> rollingHeight = setting("RollingHeight", false).des("Makes height of renders go up and down on an axis").whenAtMode(page, Page.Render);
    Setting<Float> rollingSpeed = setting("RollingSpeed", 1.0f, 0.1f, 10.0f).des("How fast the renders go up and down").whenTrue(rollingHeight).whenAtMode(page, Page.Render);
    Setting<Float> rollingWidth = setting("RollWidth", 0.4f, 0.0f, 2.0f).des("How wide should each 'wave' be").whenTrue(rollingHeight).whenAtMode(page, Page.Render);
    Setting<Float> rollingHeightMax = setting("RollHeightMax", 1.0f, -2.0f, 2.0f).des("Maximum height for rolling height").whenTrue(rollingHeight).whenAtMode(page, Page.Render);
    Setting<Float> rollingHeightMin = setting("RollHeightMin", 0.1f, -2.0f, 2.0f).des("Minimum height for rolling height").whenTrue(rollingHeight).whenAtMode(page, Page.Render);
    public Setting<Boolean> doubleHoles = setting("DoubleHoles", true).des("Render double holes").whenAtMode(page, Page.Render);
    public Setting<Boolean> mergeDoubleHoles = setting("MergeDoubleHoles", false).des("Render double holes as 1 rectangle instead of 2").whenTrue(doubleHoles).whenAtMode(page, Page.Render);
    Setting<SelfHighlightMode> selfHighlight = setting("SelfHighlight", SelfHighlightMode.None).des("Modifies the hole that you're currently in").whenAtMode(page, Page.Render);
    Setting<Float> selfHightlightAlphaFactor = setting("SelfHighlightAlphaFactor", 0.5f, 0.0f, 10.0f).des("Multiply hole alpha by this when you are in that hole").whenAtMode(selfHighlight, SelfHighlightMode.Alpha).whenAtMode(page, Page.Render);
    Setting<Float> selfHightlightHeight = setting("SelfHighlightHeight", 0.2f, -2.0f, 2.0f).des("Modify hole height when you are in that hole").whenAtMode(selfHighlight, SelfHighlightMode.Height).whenAtMode(page, Page.Render);
    Setting<Boolean> fadeIn = setting("FadeIn", false).des("Holes will have lower alpha if they are on the edge of your render range").whenAtMode(page, Page.Render);
    Setting<Float> fadeInRange = setting("FadeRange", 7.0f, 0.1f, 10.0f).des("Distance from you that holes will start fading").whenTrue(fadeIn).whenAtMode(page, Page.Render);
    Setting<Boolean> xCross = setting("BoxCross", false).des("Makes an x cross through the hole render").whenAtMode(page, Page.Render);
    Setting<Boolean> gradientXCross = setting("GradientBoxCross", false).des("Renders x cross with a gradient").whenTrue(xCross).whenAtMode(page, Page.Render);
    Setting<Boolean> flatXCross = setting("FlatBoxCross", false).des("Renders flat x cross").whenTrue(xCross).whenAtMode(page, Page.Render);
    Setting<Boolean> solidBox = setting("SolidBox", true).whenAtMode(page, Page.Render);
    Setting<Boolean> solidPyramid = setting("SolidPyramid", false).des("Render solid holes in a pyramid shape instead of box").whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> gradientSolidBox = setting("GradientSolid", false).des("Renders solid box with a gradient").whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> solidCull = setting("SolidCull", false).des("Don't render interior faces of solid render").whenTrue(gradientSolidBox).whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> wallSolid = setting("WallSolid", true).des("See solid render through wall").whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> sidesOnly = setting("SidesOnly", false).des("Removes the top and bottom planes from solid render when gradient to make cool effect or smt idk i thought it looked cool in xuanox's video").whenTrue(gradientSolidBox).whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> flatSolidBox = setting("FlatSolidBox", false).des("Renders flat solid box").whenTrue(solidBox).whenAtMode(page, Page.Render);
    Setting<Boolean> linesBox = setting("LinesBox", true).whenAtMode(page, Page.Render);
    Setting<Boolean> linesPyramid = setting("LinesPyramid", false).des("Render wireframe holes in a pyramid shape instead of box").whenTrue(linesBox).whenAtMode(page, Page.Render);
    Setting<Boolean> gradientLinesBox = setting("GradientLines", false).des("Renders lines box with a gradient").whenTrue(linesBox).whenAtMode(page, Page.Render);
    Setting<Boolean> wallLines = setting("WallLines", true).des("See lines render through wall").whenTrue(linesBox).whenAtMode(page, Page.Render);
    Setting<Boolean> flatLinesBox = setting("FlatLinesBox", false).des("Renders flat lines box").whenTrue(linesBox).whenAtMode(page, Page.Render);
    Setting<Boolean> boxOneBlockHeight = setting("OneBlockHeight", false).des("Makes hole render exactly one block tall (bc slider isn't percise enough)").whenFalse(rollingHeight).whenAtMode(page, Page.Render);
    Setting<Float> boxHeight = setting("Height", 1.0f, -2.0f, 2.0f).whenFalse(boxOneBlockHeight).whenFalse(rollingHeight).whenAtMode(page, Page.Render);
    Setting<Float> lineWidth = setting("LineWidth", 1.0f, 0.0f, 5.0f).when(() -> linesBox.getValue() || xCross.getValue()).whenAtMode(page, Page.Render);
    Setting<Boolean> flipPyramids = setting("FlipPyramids", false).des("Flip pyramid renders").when(() -> (solidBox.getValue() && solidPyramid.getValue()) || (linesBox.getValue() && linesPyramid.getValue())).whenAtMode(page, Page.Render);

    Setting<Color> singleSafeColorSolid = setting("SingleSafeColorSolid", new Color(new java.awt.Color(50, 255, 50, 20).getRGB())).whenTrue(solidBox).whenAtMode(page, Page.Color);
    Setting<Color> singleSafeGradientColorSolid = setting("SingleSafeGradientColorSolid", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).whenTrue(gradientSolidBox).whenTrue(solidBox).whenAtMode(page, Page.Color);
    Setting<Color> singleSafeColorLines = setting("SingleSafeColorLines", new Color(new java.awt.Color(50, 255, 50, 175).getRGB())).when(() -> linesBox.getValue() || xCross.getValue()).whenAtMode(page, Page.Color);
    Setting<Color> singleSafeGradientColorLines = setting("SingleSafeGradientColorLines", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).when(() -> ((linesBox.getValue() && gradientLinesBox.getValue()) || (gradientXCross.getValue() && xCross.getValue()))).whenAtMode(page, Page.Color);
    Setting<Color> singleUnSafeColorSolid = setting("SingleUnSafeColorSolid", new Color(new java.awt.Color(255, 50, 50, 20).getRGB())).whenTrue(solidBox).whenAtMode(page, Page.Color);
    Setting<Color> singleUnSafeGradientColorSolid = setting("SingleUnSafeGradientColorSolid", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).whenTrue(gradientSolidBox).whenTrue(solidBox).whenAtMode(page, Page.Color);
    Setting<Color> singleUnSafeColorLines = setting("SingleUnSafeColorLines", new Color(new java.awt.Color(255, 50, 50, 175).getRGB())).when(() -> linesBox.getValue() || xCross.getValue()).whenAtMode(page, Page.Color);
    Setting<Color> singleUnSafeGradientColorLines = setting("SingleUnSafeGradientColorLines", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).when(() -> ((linesBox.getValue() && gradientLinesBox.getValue()) || (gradientXCross.getValue() && xCross.getValue()))).whenAtMode(page, Page.Color);
    Setting<Color> doubleSafeColorSolid = setting("DoubleSafeColorSolid", new Color(new java.awt.Color(255, 255, 50, 20).getRGB())).whenTrue(solidBox).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleSafeGradientColorSolid = setting("DoubleSafeGradientColorSolid", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).whenTrue(gradientSolidBox).whenTrue(solidBox).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleSafeColorLines = setting("DoubleSafeColorLines", new Color(new java.awt.Color(255, 255, 50, 175).getRGB())).when(() -> linesBox.getValue() || xCross.getValue()).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleSafeGradientColorLines = setting("DoubleSafeGradientColorLines", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).when(() -> ((linesBox.getValue() && gradientLinesBox.getValue()) || (gradientXCross.getValue() && xCross.getValue()))).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleUnSafeColorSolid = setting("DoubleUnSafeColorSolid", new Color(new java.awt.Color(255, 129, 50, 20).getRGB())).whenTrue(solidBox).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleUnSafeGradientColorSolid = setting("DoubleUnSafeGradientColorSolid", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).whenTrue(gradientSolidBox).whenTrue(solidBox).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleUnSafeColorLines = setting("DoubleUnSafeColorLines", new Color(new java.awt.Color(255, 129, 50, 175).getRGB())).when(() -> linesBox.getValue() || xCross.getValue()).whenTrue(doubleHoles).whenAtMode(page, Page.Color);
    Setting<Color> doubleUnSafeGradientColorLines = setting("DoubleUnSafeGradientColorLines", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).when(() -> ((linesBox.getValue() && gradientLinesBox.getValue()) || (gradientXCross.getValue() && xCross.getValue()))).whenTrue(doubleHoles).whenAtMode(page, Page.Color);

    public static HoleRender INSTANCE;

    public HoleRender() {
        INSTANCE = this;
    }

    @Override
    public String getModuleInfo() {
        if (solidBox.getValue() && linesBox.getValue()) return "Full";
        else if (solidBox.getValue()) return "Solid";
        else if (linesBox.getValue()) return "Outline";
        else return "Ok";
    }

    @Override
    public void onRenderWorld(RenderEvent event) {
        HashMap<BlockPos, Integer> localHolePositions;
        synchronized (HoleSettings.INSTANCE.holePositions) {
            localHolePositions = new HashMap<>(HoleSettings.INSTANCE.holePositions);
        }
        if (doubleHoles.getValue() && !mergeDoubleHoles.getValue()) {
            synchronized (HoleSettings.INSTANCE.doubleHolePositions) {
                localHolePositions.putAll(HoleSettings.INSTANCE.doubleHolePositions);
            }
        }
        for (Map.Entry<BlockPos, Integer> entry : localHolePositions.entrySet()) {
            if (!RenderHelper.isInViewFrustrum(SpartanTessellator.getBoundingFromPos(entry.getKey()))) {
                continue;
            }

            java.awt.Color solidColor;
            java.awt.Color linesColor;
            java.awt.Color solidColor2;
            java.awt.Color linesColor2;
            BlockPos playerPos = EntityUtil.floorEntity(mc.player);
            boolean isInHole = BlockUtil.isSameBlockPos(playerPos, entry.getKey());
            float height = boxHeight.getValue();
            if (boxOneBlockHeight.getValue()) height = 1.0f;
            Vec3d holeVec = new Vec3d(entry.getKey());

            float alphaFactor = 1.0f;
            if (fadeIn.getValue()) {
                float f = HoleSettings.INSTANCE.range.getValue() - fadeInRange.getValue() * 0.5f;
                alphaFactor = (MathUtilFuckYou.clamp((HoleSettings.INSTANCE.range.getValue() * HoleSettings.INSTANCE.range.getValue() - (float) MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), holeVec)) / (f * f), 0.0f, 1.0f));
            }

            if (rollingHeight.getValue()) {
                height = getRolledHeight(entry.getKey().x);
            }

            if (selfHighlight.getValue() == SelfHighlightMode.Alpha && isInHole) {
                alphaFactor *= selfHightlightAlphaFactor.getValue();
            }

            if (selfHighlight.getValue() == SelfHighlightMode.Height && isInHole) {
                height = selfHightlightHeight.getValue();
            }

            switch (entry.getValue()) {
                case 1: {
                    solidColor = new java.awt.Color(singleSafeColorSolid.getValue().getColorColor().getRed(), singleSafeColorSolid.getValue().getColorColor().getGreen(), singleSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor = new java.awt.Color(singleSafeColorLines.getValue().getColorColor().getRed(), singleSafeColorLines.getValue().getColorColor().getGreen(), singleSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    solidColor2 = new java.awt.Color(singleSafeGradientColorSolid.getValue().getColorColor().getRed(), singleSafeGradientColorSolid.getValue().getColorColor().getGreen(), singleSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor2 = new java.awt.Color(singleSafeGradientColorLines.getValue().getColorColor().getRed(), singleSafeGradientColorLines.getValue().getColorColor().getGreen(), singleSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    break;
                }

                case 2: {
                    solidColor = new java.awt.Color(singleUnSafeColorSolid.getValue().getColorColor().getRed(), singleUnSafeColorSolid.getValue().getColorColor().getGreen(), singleUnSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor = new java.awt.Color(singleUnSafeColorLines.getValue().getColorColor().getRed(), singleUnSafeColorLines.getValue().getColorColor().getGreen(), singleUnSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    solidColor2 = new java.awt.Color(singleUnSafeGradientColorSolid.getValue().getColorColor().getRed(), singleUnSafeGradientColorSolid.getValue().getColorColor().getGreen(), singleUnSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor2 = new java.awt.Color(singleUnSafeGradientColorLines.getValue().getColorColor().getRed(), singleUnSafeGradientColorLines.getValue().getColorColor().getGreen(), singleUnSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    break;
                }

                case 3: {
                    solidColor = new java.awt.Color(doubleSafeColorSolid.getValue().getColorColor().getRed(), doubleSafeColorSolid.getValue().getColorColor().getGreen(), doubleSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor = new java.awt.Color(doubleSafeColorLines.getValue().getColorColor().getRed(), doubleSafeColorLines.getValue().getColorColor().getGreen(), doubleSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    solidColor2 = new java.awt.Color(doubleSafeGradientColorSolid.getValue().getColorColor().getRed(), doubleSafeGradientColorSolid.getValue().getColorColor().getGreen(), doubleSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0 , 255)));
                    linesColor2 = new java.awt.Color(doubleSafeGradientColorLines.getValue().getColorColor().getRed(), doubleSafeGradientColorLines.getValue().getColorColor().getGreen(), doubleSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    break;
                }

                default: {
                    solidColor = new java.awt.Color(doubleUnSafeColorSolid.getValue().getColorColor().getRed(), doubleUnSafeColorSolid.getValue().getColorColor().getGreen(), doubleUnSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor = new java.awt.Color(doubleUnSafeColorLines.getValue().getColorColor().getRed(), doubleUnSafeColorLines.getValue().getColorColor().getGreen(), doubleUnSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    solidColor2 = new java.awt.Color(doubleUnSafeGradientColorSolid.getValue().getColorColor().getRed(), doubleUnSafeGradientColorSolid.getValue().getColorColor().getGreen(), doubleUnSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                    linesColor2 = new java.awt.Color(doubleUnSafeGradientColorLines.getValue().getColorColor().getRed(), doubleUnSafeGradientColorLines.getValue().getColorColor().getGreen(), doubleUnSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                    break;
                }
            }

            if (solidBox.getValue()) {
                if (!wallSolid.getValue()) {
                    GL11.glEnable(GL_DEPTH_TEST);
                }

                if (flatSolidBox.getValue()) {
                    SpartanTessellator.drawFlatFullBox(holeVec, !wallSolid.getValue(), solidColor.getRGB());
                }
                else {
                    if (gradientSolidBox.getValue()) {
                        if (solidPyramid.getValue()) {
                            SpartanTessellator.drawGradientPyramidFullBox(holeVec, !solidCull.getValue(), flipPyramids.getValue(), !wallSolid.getValue(), height, solidColor.getRGB(), solidColor2.getRGB());
                        }
                        else {
                            SpartanTessellator.drawGradientBlockFullBox(holeVec, !solidCull.getValue(), !wallSolid.getValue(), sidesOnly.getValue(), height, solidColor.getRGB(), solidColor2.getRGB());
                        }
                    }
                    else {
                        if (solidPyramid.getValue()) {
                            SpartanTessellator.drawPyramidFullBox(holeVec, flipPyramids.getValue(), !wallSolid.getValue(), height, solidColor.getRGB());
                        }
                        else {
                            SpartanTessellator.drawBlockFullBox(holeVec, !wallSolid.getValue(), height, solidColor.getRGB());
                        }
                    }
                }

                if (!wallSolid.getValue()) {
                    GL11.glDisable(GL_DEPTH_TEST);
                }
            }

            if (linesBox.getValue()) {
                if (!wallLines.getValue()) {
                    GL11.glEnable(GL_DEPTH_TEST);
                }

                if (flatLinesBox.getValue()) {
                    SpartanTessellator.drawFlatLineBox(holeVec, !wallLines.getValue(), lineWidth.getValue(), linesColor.getRGB());
                }
                else {
                    if (gradientLinesBox.getValue()) {
                        if (linesPyramid.getValue()) {
                            SpartanTessellator.drawGradientPyramidLineBox(holeVec, flipPyramids.getValue(), !wallLines.getValue(), height, lineWidth.getValue(), linesColor.getRGB(), linesColor2.getRGB());
                        }
                        else {
                            SpartanTessellator.drawGradientBlockLineBox(holeVec, !wallLines.getValue(), height, lineWidth.getValue(), linesColor.getRGB(), linesColor2.getRGB());
                        }
                    }
                    else {
                        if (linesPyramid.getValue()) {
                            SpartanTessellator.drawPyramidLineBox(holeVec, flipPyramids.getValue(), !wallLines.getValue(), height, lineWidth.getValue(), linesColor.getRGB());
                        }
                        else {
                            SpartanTessellator.drawBlockLineBox(holeVec, !wallLines.getValue(), height, lineWidth.getValue(), linesColor.getRGB());
                        }
                    }
                }

                if (!wallLines.getValue()) {
                    GL11.glDisable(GL_DEPTH_TEST);
                }
            }

            if (xCross.getValue()) {
                if (!wallLines.getValue()) {
                    GL11.glEnable(GL_DEPTH_TEST);
                }

                if (flatXCross.getValue() || (linesBox.getValue() && linesPyramid.getValue()) || (solidBox.getValue() && solidPyramid.getValue())) {
                    SpartanTessellator.drawFlatXCross(holeVec, lineWidth.getValue(), linesColor.getRGB());
                }
                else {
                    if (gradientXCross.getValue()) {
                        SpartanTessellator.drawGradientXCross(holeVec, height, lineWidth.getValue(), linesColor.getRGB(), linesColor2.getRGB());
                    }
                    else {
                        SpartanTessellator.drawXCross(holeVec, height, lineWidth.getValue(), linesColor.getRGB());
                    }
                }

                if (!wallLines.getValue()) {
                    GL11.glDisable(GL_DEPTH_TEST);
                }
            }
        }

        if (mergeDoubleHoles.getValue()) {
            HashMap<Pair<BlockPos, BlockPos>, Integer> localMergedHolePositions;
            synchronized (HoleSettings.INSTANCE.mergedHolePositions) {
                localMergedHolePositions = new HashMap<>(HoleSettings.INSTANCE.mergedHolePositions);
            }
            for (Map.Entry<Pair<BlockPos, BlockPos>, Integer> entry : localMergedHolePositions.entrySet()) {
                if (!RenderHelper.isInViewFrustrum(SpartanTessellator.getBoundingFromPos(entry.getKey().a)) && !RenderHelper.isInViewFrustrum(SpartanTessellator.getBoundingFromPos(entry.getKey().b))) {
                    continue;
                }

                java.awt.Color solidColor;
                java.awt.Color linesColor;
                java.awt.Color solidColor2;
                java.awt.Color linesColor2;
                BlockPos playerPos = EntityUtil.floorEntity(mc.player);
                boolean isInHole = BlockUtil.isSameBlockPos(playerPos, entry.getKey().a) || BlockUtil.isSameBlockPos(playerPos, entry.getKey().b);
                float height = boxHeight.getValue();
                if (boxOneBlockHeight.getValue()) height = 1.0f;
                boolean flagx = false;
                boolean flagz = false;

                if (rollingHeight.getValue()) {
                    height = getRolledHeight(entry.getKey().a.x);
                }

                if (selfHighlight.getValue() == SelfHighlightMode.Height && isInHole) {
                    height = selfHightlightHeight.getValue();
                }

                Vec3d holeVec1 = new Vec3d(entry.getKey().a.x - 0.5, entry.getKey().a.y, entry.getKey().a.z - 0.5);
                Vec3d holeVec2 = new Vec3d(entry.getKey().b.x + 0.5, entry.getKey().b.y + height, entry.getKey().b.z + 0.5);

                if (entry.getKey().a.x == entry.getKey().b.x + 1.0) {
                    flagx = true;
                    holeVec1 = new Vec3d(entry.getKey().a.x + 0.5, entry.getKey().a.y, entry.getKey().a.z - 0.5);
                    holeVec2 = new Vec3d(entry.getKey().b.x - 0.5, entry.getKey().b.y + height, entry.getKey().b.z + 0.5);
                }

                if (entry.getKey().a.z == entry.getKey().b.z + 1.0) {
                    flagz = true;
                    holeVec1 = new Vec3d(entry.getKey().a.x - 0.5, entry.getKey().a.y, entry.getKey().a.z + 0.5);
                    holeVec2 = new Vec3d(entry.getKey().b.x + 0.5, entry.getKey().b.y + height, entry.getKey().b.z - 0.5);
                }

                float alphaFactor = 1.0f;
                if (fadeIn.getValue()) {
                    float f = HoleSettings.INSTANCE.range.getValue() - fadeInRange.getValue() * 0.5f;
                    Vec3d centerVec = new Vec3d((holeVec1.x + holeVec2.x) / 2.0, (holeVec1.y + holeVec2.y) / 2.0, (holeVec1.z + holeVec2.z) / 2.0);
                    alphaFactor = (MathUtilFuckYou.clamp((HoleSettings.INSTANCE.range.getValue() * HoleSettings.INSTANCE.range.getValue() - (float) MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), centerVec)) / (f * f), 0.0f, 1.0f));
                }

                if (selfHighlight.getValue() == SelfHighlightMode.Alpha && isInHole) {
                    alphaFactor *= selfHightlightAlphaFactor.getValue();
                }

                switch (entry.getValue()) {
                    case 1: {
                        solidColor = new java.awt.Color(singleSafeColorSolid.getValue().getColorColor().getRed(), singleSafeColorSolid.getValue().getColorColor().getGreen(), singleSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor = new java.awt.Color(singleSafeColorLines.getValue().getColorColor().getRed(), singleSafeColorLines.getValue().getColorColor().getGreen(), singleSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        solidColor2 = new java.awt.Color(singleSafeGradientColorSolid.getValue().getColorColor().getRed(), singleSafeGradientColorSolid.getValue().getColorColor().getGreen(), singleSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor2 = new java.awt.Color(singleSafeGradientColorLines.getValue().getColorColor().getRed(), singleSafeGradientColorLines.getValue().getColorColor().getGreen(), singleSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        break;
                    }

                    case 2: {
                        solidColor = new java.awt.Color(singleUnSafeColorSolid.getValue().getColorColor().getRed(), singleUnSafeColorSolid.getValue().getColorColor().getGreen(), singleUnSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor = new java.awt.Color(singleUnSafeColorLines.getValue().getColorColor().getRed(), singleUnSafeColorLines.getValue().getColorColor().getGreen(), singleUnSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        solidColor2 = new java.awt.Color(singleUnSafeGradientColorSolid.getValue().getColorColor().getRed(), singleUnSafeGradientColorSolid.getValue().getColorColor().getGreen(), singleUnSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor2 = new java.awt.Color(singleUnSafeGradientColorLines.getValue().getColorColor().getRed(), singleUnSafeGradientColorLines.getValue().getColorColor().getGreen(), singleUnSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(singleUnSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        break;
                    }

                    case 3: {
                        solidColor = new java.awt.Color(doubleSafeColorSolid.getValue().getColorColor().getRed(), doubleSafeColorSolid.getValue().getColorColor().getGreen(), doubleSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor = new java.awt.Color(doubleSafeColorLines.getValue().getColorColor().getRed(), doubleSafeColorLines.getValue().getColorColor().getGreen(), doubleSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        solidColor2 = new java.awt.Color(doubleSafeGradientColorSolid.getValue().getColorColor().getRed(), doubleSafeGradientColorSolid.getValue().getColorColor().getGreen(), doubleSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor2 = new java.awt.Color(doubleSafeGradientColorLines.getValue().getColorColor().getRed(), doubleSafeGradientColorLines.getValue().getColorColor().getGreen(), doubleSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        break;
                    }

                    default: {
                        solidColor = new java.awt.Color(doubleUnSafeColorSolid.getValue().getColorColor().getRed(), doubleUnSafeColorSolid.getValue().getColorColor().getGreen(), doubleUnSafeColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor = new java.awt.Color(doubleUnSafeColorLines.getValue().getColorColor().getRed(), doubleUnSafeColorLines.getValue().getColorColor().getGreen(), doubleUnSafeColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        solidColor2 = new java.awt.Color(doubleUnSafeGradientColorSolid.getValue().getColorColor().getRed(), doubleUnSafeGradientColorSolid.getValue().getColorColor().getGreen(), doubleUnSafeGradientColorSolid.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeGradientColorSolid.getValue().getAlpha() * alphaFactor, 0, 255)));
                        linesColor2 = new java.awt.Color(doubleUnSafeGradientColorLines.getValue().getColorColor().getRed(), doubleUnSafeGradientColorLines.getValue().getColorColor().getGreen(), doubleUnSafeGradientColorLines.getValue().getColorColor().getBlue(), (int)(MathUtilFuckYou.clamp(doubleUnSafeGradientColorLines.getValue().getAlpha() * alphaFactor, 0, 255)));
                        break;
                    }
                }

                if (solidBox.getValue()) {
                    if (!wallSolid.getValue()) {
                        GL11.glEnable(GL_DEPTH_TEST);
                    }

                    if (flatSolidBox.getValue()) {
                        SpartanTessellator.drawDoubleBlockFlatFullBox(holeVec1, holeVec2, !wallSolid.getValue(), solidColor.getRGB());
                    }
                    else {
                        if (gradientSolidBox.getValue()) {
                            if (solidPyramid.getValue()) {
                                SpartanTessellator.drawGradientDoubleBlockFullPyramid(holeVec1, holeVec2, !solidCull.getValue(), flipPyramids.getValue(), !wallSolid.getValue(), flagx, flagz, solidColor.getRGB(), solidColor2.getRGB());
                            }
                            else {
                                SpartanTessellator.drawGradientDoubleBlockFullBox(holeVec1, holeVec2, !solidCull.getValue(), !wallSolid.getValue(), sidesOnly.getValue(), solidColor.getRGB(), solidColor2.getRGB());
                            }
                        }
                        else {
                            if (solidPyramid.getValue()) {
                                SpartanTessellator.drawDoubleBlockFullPyramid(holeVec1, holeVec2, flipPyramids.getValue(), !wallSolid.getValue(), flagx, flagz, solidColor.getRGB());
                            }
                            else {
                                SpartanTessellator.drawDoubleBlockFullBox(holeVec1, holeVec2, !wallSolid.getValue(), solidColor.getRGB());
                            }
                        }
                    }

                    if (!wallSolid.getValue()) {
                        GL11.glDisable(GL_DEPTH_TEST);
                    }
                }

                if (linesBox.getValue()) {
                    if (!wallLines.getValue()) {
                        GL11.glEnable(GL_DEPTH_TEST);
                    }

                    if (flatLinesBox.getValue()) {
                        SpartanTessellator.drawDoubleBlockFlatLineBox(holeVec1, holeVec2, !wallLines.getValue(), lineWidth.getValue(), linesColor.getRGB());
                    }
                    else {
                        if (gradientLinesBox.getValue()) {
                            if (linesPyramid.getValue()) {
                                SpartanTessellator.drawGradientDoubleBlockLinePyramid(holeVec1, holeVec2, flipPyramids.getValue(), !wallLines.getValue(), lineWidth.getValue(), flagx, flagz, linesColor.getRGB(), linesColor2.getRGB());
                            }
                            else {
                                SpartanTessellator.drawGradientDoubleBlockLineBox(holeVec1, holeVec2, !wallLines.getValue(), lineWidth.getValue(), linesColor.getRGB(), linesColor2.getRGB());
                            }
                        }
                        else {
                            if (linesPyramid.getValue()) {
                                SpartanTessellator.drawDoubleBlockLinePyramid(holeVec1, holeVec2, flipPyramids.getValue(), !wallLines.getValue(), lineWidth.getValue(), flagx, flagz, linesColor.getRGB());
                            }
                            else {
                                SpartanTessellator.drawDoubleBlockLineBox(holeVec1, holeVec2, !wallLines.getValue(), lineWidth.getValue(), linesColor.getRGB());
                            }
                        }
                    }

                    if (!wallLines.getValue()) {
                        GL11.glDisable(GL_DEPTH_TEST);
                    }
                }

                if (xCross.getValue()) {
                    if (!wallLines.getValue()) {
                        GL11.glEnable(GL_DEPTH_TEST);
                    }

                    if (flatXCross.getValue() || (linesBox.getValue() && linesPyramid.getValue()) || (solidBox.getValue() && solidPyramid.getValue())) {
                        SpartanTessellator.drawDoublePointFlatXCross(holeVec1, holeVec2, lineWidth.getValue(), linesColor.getRGB());
                    }
                    else {
                        if (gradientXCross.getValue()) {
                            SpartanTessellator.drawGradientDoublePointXCross(holeVec1, holeVec2, lineWidth.getValue(), linesColor.getRGB(), linesColor2.getRGB());
                        }
                        else {
                            SpartanTessellator.drawDoublePointXCross(holeVec1, holeVec2, lineWidth.getValue(), linesColor.getRGB());
                        }
                    }

                    if (!wallLines.getValue()) {
                        GL11.glDisable(GL_DEPTH_TEST);
                    }
                }
            }
        }
    }

    private float getRolledHeight(float offset) {
        double s = (System.currentTimeMillis() * (double)rollingSpeed.getValue()) + (offset * rollingWidth.getValue() * 100.0f);
        s %= 300.0;
        s = (150.0f * Math.sin(((s - 75.0f) * Math.PI) / 150.0f)) + 150.0f;
        return rollingHeightMax.getValue() + ((float)s * ((rollingHeightMin.getValue() - rollingHeightMax.getValue()) / 300.0f));
    }

    enum Page {
        Render,
        Color
    }

    enum SelfHighlightMode {
        Alpha,
        Height,
        None
    }
}
