package net.spartanb312.base.module.modules.client;

import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.gui.ClickGUIFinal;
import net.spartanb312.base.gui.Panel;
import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.client.CustomFont;
import me.afterdarkness.moloch.module.modules.client.Particles;
import me.afterdarkness.moloch.utils.graphics.ParticleUtil;
import org.lwjgl.input.Keyboard;

@Parallel
@ModuleInfo(name = "ClickGUI", category = Category.CLIENT, keyCode = Keyboard.KEY_RSHIFT, description = "place to click stuff")
public class ClickGUI extends Module {

    public static ClickGUI instance;
    public ClickGUI() {
        instance = this;
    }

    public int flag = 0;

    Setting<Page> page = setting("Page", Page.General);

    //public Setting<Integer> panelWidth = setting("PanelWidth", 120, 0, 250).des("Panel Width").whenAtMode(page, Page.General);
    //public Setting<Integer> panelHeight = setting("PanelHeight", 16, 0, 25).des("Panel Rect Height").whenAtMode(page, Page.General);
    public Setting<Boolean> arrowScroll = setting("ArrowScroll", true).des("Use arrows to scroll in addition to using mouse wheel").whenAtMode(page, Page.General);
    public Setting<Integer> scrollAmount = setting("GUIScrollAmount", 13, 1, 20).des("Distance to scroll to").whenAtMode(page, Page.General);
    public Setting<Float> scrollSpeed = setting("GUIScrollSpeed", 5.0f, 0.1f, 10.0f).des("GUI Scroll Speed").whenAtMode(page, Page.General);
    public Setting<Float> scrollFactor = setting("GUIScrollFactor", 0.5f, 0.1f, 1.0f).des("Steepness of GUI scrolling").whenAtMode(page, Page.General);
    public Setting<Boolean> guiMove = setting("GUIAnimation", true).des("GUI Toggle Animation").whenAtMode(page, Page.General);
    public Setting<Float> guiMoveSpeed = setting("GUIAnimateSpeed", 1.0f, 0.1f, 2.0f).whenTrue(guiMove).des("GUI Animation Speed Factor").whenAtMode(page, Page.General);
    public Setting<Float> moduleOpenSpeed = setting("ModuleSpeed", 1.0f, 0.1f, 2.0f).des("Module Opening Speed Factor").whenAtMode(page, Page.General);
    public Setting<Float> panelOpenSpeed = setting("PanelSpeed", 0.7f, 0.1f, 2.0f).des("Panel Opening Speed Factor").whenAtMode(page, Page.General);

    public Setting<Color> globalColor = setting("GlobalColor", new Color(new java.awt.Color(100, 61, 255, 200).getRGB())).des("Global Colors").whenAtMode(page, Page.General);

    public Setting<Boolean> rectHorns = setting("RectHorns", true).des("Category Rect Draw Horns").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsGap = setting("RectHornsGap", 1.0f, 0.0f, 2.0f).des("Category Rect Space Between Horns").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Integer> rectHornsX = setting("RectHornsX", 10, -30, 30).des("Category Rect Horns X").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Integer> rectHornsY = setting("RectHornsY", 11, -30, 30).des("Category Rect Horns Y").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Integer> rectHornsScale = setting("RectHornsScale", 19, 0, 50).des("Category Rect Horns Scale").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> rectHornsColor = setting("RectHornsColor", new Color(new java.awt.Color(69, 0, 178, 255).getRGB())).des("Category Rect Horn Color").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> rectHornsShadow = setting("RectHornsShadows", true).des("Category Rect Shadows").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsShadowXOffsetRight = setting("RHShadowsXOffsetRight", 0.7f, -20.0f, 20.0f).des("Category Rect Horns Right Shadows X Offset").whenTrue(rectHornsShadow).whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsShadowXOffsetLeft = setting("RHShadowsXOffsetLeft", 0.0f, -20.0f, 20.0f).des("Category Rect Horns Left Shadows X Offset").whenTrue(rectHornsShadow).whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsSHadowsYOffset = setting("RHornsShadowYOffset", -3.0f, -20.0f, 20.0f).des("Category Rect Horns Shadows Y Offset").whenTrue(rectHornsShadow).whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsShadowSize = setting("RHornsShadowsSize", 4.8f, 0.0f, 10.0f).des("Category Rect Shadows Size").whenTrue(rectHornsShadow).whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Integer> rectHornsShadowAlpha = setting("RHornsShadowsAlpha", 132, 0, 255).des("Category Rect Shadows Alpha").whenTrue(rectHornsShadow).whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> rectHornsHoverDifColor = setting("RectHornsHoverDifColor", true).des("Category Rect Horns Hover Color Change").whenTrue(rectHorns).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> rectHornsHoverColorSmooth = setting("RHHoverColorSmooth", true).des("Category Rect Horns Hover Color Change Animation").whenTrue(rectHorns).whenTrue(rectHornsHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsHoverColorSmoothFactorIn = setting("RHHoverColorSmoothIn", 3.6f, 0.4f, 10.0f).des("Category Rect Horns Hover Color Change Animation Speed In").whenTrue(rectHorns).whenTrue(rectHornsHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHornsHoverColorSmoothFactorOut = setting("RHHoverColorSmoothOut", 1.1f, 0.4f, 10.0f).des("Category Rect Horns Hover Color Change Animation Speed Out").whenTrue(rectHorns).whenTrue(rectHornsHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> rectHornsHoverColor = setting("RectHornsHoverColor", new Color(new java.awt.Color(122, 64, 255, 255).getRGB())).des("Category Rect Horns Hover Color").whenTrue(rectHorns).whenTrue(rectHornsHoverDifColor).whenAtMode(page, Page.CategoryBars);

    public Setting<Float> rectWidth = setting("RectWidth", -2.6f, -10.0f, 10.0f).des("Category Rect Width").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectHeight = setting("RectHeight", 7.1f, -10.0f, 10.0f).des("Category Rect Height").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectX = setting("RectX", -2.0f, -30.0f, 30.0f).des("Category Rect X").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> rectY = setting("RectY", -3.9f, -30.0f, 30.0f).des("Category Rect Y").whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> guiRoundRect = setting("GUIRoundedRects", true).des("Draw Category Rectangles As Rounded Rectangles").whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> arcTopRight = setting("TopRightRounded", true).des("Round Top Right Corner").whenTrue(guiRoundRect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> arcTopLeft = setting("TopLeftRounded", false).des("Round Top Left Corner").whenTrue(guiRoundRect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> arcDownRight = setting("DownRightRounded", false).des("Round Bottom Right Corner").whenTrue(guiRoundRect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> arcDownLeft = setting("DownLeftRounded", true).des("Round Bottom Left Corner").whenTrue(guiRoundRect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> radius = setting("CornerRadius", 1.0f, 0.0f, 1.0f).des("Radius Of Rounded Category Rect Corners").whenTrue(guiRoundRect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectOutline = setting("RectOutline", false).des("Category Rect Outline").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectOutlineWidth = setting("RectOutlineWidth", 1.0f, 0.0f, 4.0f).des("Category Rect Outline Width").whenTrue(categoryRectOutline).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectOutlineBottomLineToggle = setting("RectOutlineBottomToggle", false).des("Category Rect Outline Bottom Toggle").whenTrue(categoryRectOutline).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryRectOutlineColor = setting("RectOutlineColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).des("Category Rect Outline Color").whenTrue(categoryRectOutline).whenAtMode(page, Page.CategoryBars);

    public Setting<Color> categoryRectColor = setting("RectColor", new Color(new java.awt.Color(68, 0, 164, 255).getRGB())).des("Category Rect Color").whenAtMode(page, Page.CategoryBars);

    public Setting<Boolean> categoryGlow = setting("RectGlow", true).des("Category Rect Glow").whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryGlowWidth = setting("RectGlowWidth", 40.0f, -150.0f, 150.0f).des("Category Rect Glow Width").whenTrue(categoryGlow).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryGlowHeight = setting("RectGlowHeight", 38.6f, -50.0f, 50.0f).des("Category Rect Glow Height").whenTrue(categoryGlow).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryGlowColor = setting("RectGlowColor", new Color(new java.awt.Color(100, 45, 255, 176).getRGB())).des("Category Rect Glow Color").whenTrue(categoryGlow).whenAtMode(page, Page.CategoryBars);

    public Setting<Boolean> categoryRectHoverEffect = setting("RectHoverEffect", true).des("Category Rect Hover Effect").whenAtMode(page, Page.CategoryBars);
    public Setting<CategoryRectHoverParticlesMode> categoryRectHoverParticlesMode = setting("RectHoverParticles", CategoryRectHoverParticlesMode.Triangles).des("Category Rect Hover Particles").whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesStartXOffset = setting("RHoverParticlesStartX", 10.8f, 0.0f, 50.0f).des("Category Rect Hover Start X Offset").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesEndXOffset = setting("RHoverParticlesEndX", 10.8f, 0.0f, 50.0f).des("Category Rect Hover End X Offset").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<CategoryRectHoverParticlesScaleFadeMode> categoryRectHoverParticlesScaleFadeMode = setting("RectHoverParticlesFade", CategoryRectHoverParticlesScaleFadeMode.Both).des("Category Rect Hover Particles Fade").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverParticlesTrianglesSpin = setting("RHoverParticlesTriangleSpin", true).des("Category Rect Hover Particles Triangle Spin").when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverParticlesRandomTriangleSpinSpeed = setting("RHPRandomTriSpinSpeed", true).des("Category Rect Hover Particles Random Triangle Spin Speed").whenTrue(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRandomTriangleSpinSpeedMax = setting("RHPRandomTriSpinSpeedMax", 2.6f, 0.0f, 10).des("Category Rect Hover Particles Random Triangle Spin Speed Max").whenTrue(categoryRectHoverParticlesRandomTriangleSpinSpeed).whenTrue(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRandomTriangleSpinSpeedMin = setting("RHPRandomTriSpinSpeedMin", 0.1f, 0.0f, 10).des("Category Rect Hover Particles Random Triangle Spin Speed Min").whenTrue(categoryRectHoverParticlesRandomTriangleSpinSpeed).whenTrue(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesTrianglesSpinSpeed = setting("RHoverParticlesTriSpinSpeed", 1.0f, 0.0f, 5.0f).des("Category Rect Hover Particles Triangle Spin Speed").whenFalse(categoryRectHoverParticlesRandomTriangleSpinSpeed).whenTrue(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverParticlesTriangleRandomAngle = setting("RHParticlesRandomTriAngle", false).des("Category Rect Hover Particles Random Triangle Tilt").whenFalse(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesTrianglesAngle = setting("RHoverParticlesTriAngle", 0.0f, 0.0f, 360.0f).des("Category Rect Hover Particles Triangle Tilt").whenFalse(categoryRectHoverParticlesTriangleRandomAngle).whenFalse(categoryRectHoverParticlesTrianglesSpin).when(() -> categoryRectHoverParticlesMode.getValue() == CategoryRectHoverParticlesMode.Triangles).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesGenerateRate = setting("RHParticlesGenerateRate", 0.3f, 0.1f, 3.0f).des("Category Rect Hover Particles Generate Rate").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverParticlesRiseSpeedRandom = setting("RHParticlesRandomSpeed", true).des("Category Rect Hover Particles Random Rise Speed").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRiseSpeed = setting("RHoverParticlesUpSpeed", 0.5f, 0.0f, 2.0f).des("Category Rect Hover Particles Rise Speed").whenFalse(categoryRectHoverParticlesRiseSpeedRandom).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRiseSpeedRandomMax = setting("RHPRandomMaxRiseSpeed", 1.0f, 0.0f, 2.0f).des("Category Rect Hover Particles Random Max Speed").whenTrue(categoryRectHoverParticlesRiseSpeedRandom).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRiseSpeedRandomMin = setting("RHPRandomMinRiseSpeed", 0.1f, 0.0f, 2.0f).des("Category Rect Hover Particles Random Min Speed").whenTrue(categoryRectHoverParticlesRiseSpeedRandom).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverParticlesRandomSize = setting("RHoverParticlesRandomSize", true).des("Category Rect Hover Particles Random Size").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRandomSizeMax = setting("RHParticlesRandomSizeMax", 6.0f, 0.0f, 7.0f).des("Category Rect Hover Particles Random Size Max").whenTrue(categoryRectHoverParticlesRandomSize).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesRandomSizeMin = setting("RHParticlesRandomSizeMin", 0.9f, 0.0f, 7.0f).des("Category Rect Hover Particles Random Size Min").whenTrue(categoryRectHoverParticlesRandomSize).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesSize = setting("RHoverParticlesSize", 5.0f, 0.0f, 7.0f).des("Category Rect Hover Particles Size").whenFalse(categoryRectHoverParticlesRandomSize).when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesHeightCap = setting("RHoverParticlesYCap", 88.7f, 0.1f, 100.0f).des("Category Rect Hover Particles Fade Height").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesAlphaFadeFactor = setting("RHParticlesAlphaFadeFactor", 3.2f, 0.0f, 5.0f).des("Category Rect Hover Particles Alpha Fade Speed").when(() -> (categoryRectHoverParticlesScaleFadeMode.getValue() == CategoryRectHoverParticlesScaleFadeMode.Alpha || categoryRectHoverParticlesScaleFadeMode.getValue() == CategoryRectHoverParticlesScaleFadeMode.Both) && categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverParticlesScaleFadeFactor = setting("RHParticlesScaleFadeFactor", 5.3f, 0.0f, 10.0f).des("Category Rect Hover Particles Scale Fade Speed").when(() -> (categoryRectHoverParticlesScaleFadeMode.getValue() == CategoryRectHoverParticlesScaleFadeMode.Scale || categoryRectHoverParticlesScaleFadeMode.getValue() == CategoryRectHoverParticlesScaleFadeMode.Both) && categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryRectHoverParticlesColor = setting("RHoverParticlesColor", new Color(new java.awt.Color(120, 60, 255, 221).getRGB())).des("Category Rect Hover Particles Color").when(() -> categoryRectHoverParticlesMode.getValue() != CategoryRectHoverParticlesMode.None).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverDifColor = setting("RectHoverDifColor", true).des("Category Rect Hover Color Change").whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverColorSmooth = setting("RectHoverColorSmooth", true).des("Category Rect Hover Color Change Animation").whenTrue(categoryRectHoverEffect).whenTrue(categoryRectHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverColorSmoothFactorIn = setting("RHoverColorSmoothFactorIn", 3.6f, 0.4f, 10.0f).des("Category Rect Hover Color Change Animation Speed In").whenTrue(categoryRectHoverEffect).whenTrue(categoryRectHoverColorSmooth).whenTrue(categoryRectHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverColorSmoothFactorOut = setting("RHoverColorSmoothFactorOut", 1.2f, 0.4f, 10.0f).des("Category Rect Hover Color Change Animation Speed Out").whenTrue(categoryRectHoverEffect).whenTrue(categoryRectHoverColorSmooth).whenTrue(categoryRectHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryRectHoverColor = setting("RectHoverColor", new Color(new java.awt.Color(122, 64, 255, 221).getRGB())).des("Category Rect Hover Color").whenTrue(categoryRectHoverEffect).whenTrue(categoryRectHoverDifColor).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverTextScale = setting("RectHoverTextScale", true).des("Category Rect Text Scale On Hover").whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverTextScaleNewScale = setting("RHoverTextNewScale", 1.0f, 0.1f, 2.0f).des("Category Rect Hover Text New Scale").whenTrue(categoryRectHoverTextScale).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverTextScaleFactorIn = setting("RHTScaleAnimateFactorIn", 1.0f, 0.1f, 3.0f).des("Category Rect Hover Text Scale Animation Speed In").whenTrue(categoryRectHoverTextScale).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverTextScaleFactorOut = setting("RHTScaleAnimateFactorOut", 0.5f, 0.1f, 3.0f).des("Category Rect Hover Text Scale Animation Speed Out").whenTrue(categoryRectHoverTextScale).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverShadowGradientAlpha = setting("RHoverShadowGradientAlpha", false).des("Category Rect Hover Shadow Gradient Alpha Change").whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryRectHoverShadowGradientAlphaSmooth = setting("RHoverShadowGAnimate", true).des("Category Rect Hover Shadow Gradient Alpha Change Animation").whenTrue(categoryRectHoverShadowGradientAlpha).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Integer> categoryRectHoverShadowGradientNewAlpha = setting("RHoverShadowGNewAlpha", 100, 0, 255).des("Category Rect Hover Shadow Gradient New Alpha").whenTrue(categoryRectHoverShadowGradientAlphaSmooth).whenTrue(categoryRectHoverShadowGradientAlpha).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverShadowGradientFactorIn = setting("RHShadowGAnimateFactorIn", 1.0f, 0.1f, 10.0f).des("Category Rect Hover Shadow Gradient Scale Animation Speed In").whenTrue(categoryRectHoverShadowGradientAlphaSmooth).whenTrue(categoryRectHoverShadowGradientAlpha).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryRectHoverShadowGradientFactorOut = setting("RHShadowGAnimateFactorOut", 1.0f, 0.1f, 10.0f).des("Category Rect Hover Shadow Gradient Scale Animation Speed Out").whenTrue(categoryRectHoverShadowGradientAlphaSmooth).whenTrue(categoryRectHoverShadowGradientAlpha).whenTrue(categoryRectHoverEffect).whenAtMode(page, Page.CategoryBars);

    public Setting<Boolean> categoryIcons = setting("CategoryIcons", true).des("Draw Category Icons").whenAtMode(page, Page.CategoryBars);
    public Setting<CategoryIconsSides> categoryIconsSide = setting("CategoryIconsSide", CategoryIconsSides.Left).des("Category Icons Side").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsX = setting("CategoryIconsX", 3.4f, -30.0f, 30.0f).des("Category Icons X Offset").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsY = setting("CategoryIconsY", -3.2f, -10.0f, 10.0f).des("Category Icons Y Offset").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsScale = setting("CategoryIconsSize", 1.1f, 0.0f, 2.0f).des("Category Icons Size").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryIconsColor = setting("CategoryIconsColor", new Color(new java.awt.Color(255, 255, 255, 165).getRGB())).des("Category Icons Color").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryIconsBG = setting("CategoryIconsBG", true).des("Draw Category Icons BG").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsBGScaleOutside = setting("CIconsBGScale", 0.8f, 0.0f, 1.0f).des("Category Icons BG Scale").whenTrue(categoryIconsBG).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsBGSideX = setting("CIconsBGSideX", 15.8f, -30.0f, 30.0f).des("Category Icons BG Side X Offset").whenTrue(categoryIconsBG).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryIconsBGSideFade = setting("CIconsBGSideFade", true).des("Category Icons BG Side Fade").whenTrue(categoryIconsBG).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsBGSideFadeSize = setting("CIconsBGSideFadeSize", 5.8f, -30.0f, 70.0f).des("Category Icons BG Side Fade Size").whenTrue(categoryIconsBGSideFade).whenTrue(categoryIconsBG).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryIconsBGColor = setting("CIconsBGColor", new Color(new java.awt.Color(0, 0, 0, 93).getRGB())).des("Category Icons BG Color").whenTrue(categoryIconsBG).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Boolean> categoryIconsGlow = setting("CIconsGlow", false).des("Category Icons Glow").whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Float> categoryIconsGlowSize = setting("CIconsGlowSize", 12.0f, 0.0f, 15.0f).des("Category Icons Glow Size").whenTrue(categoryIconsGlow).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);
    public Setting<Color> categoryIconsGlowColor = setting("CIconsGlowColor", new Color(new java.awt.Color(255, 255, 255, 70).getRGB())).des("Category Icons Glow Color").whenTrue(categoryIconsGlow).whenTrue(categoryIcons).whenAtMode(page, Page.CategoryBars);

    public Setting<Boolean> categoryBar = setting("CategoryBar", true).des("Bar At The Bottom of Category Rect").whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryBarX = setting("CategoryBarX", -0.2f, -25.0f, 25.0f).des("X Of Category Bottom Bar").whenTrue(categoryBar).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryBarY = setting("CategoryBarY", 0.0f, -25.0f, 25.0f).des("Y Of Category Bottom Bar").whenTrue(categoryBar).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryBarXScale = setting("CategoryBarXScale", 0.0f, -50.0f, 50.0f).des("Length Factor Of Category Bottom Bar").whenTrue(categoryBar).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryBarYScale = setting("CategoryBarYScale", 0.0f, -4.0f, 4.0f).des("Height Factor Of Category Bottom Bar").whenTrue(categoryBar).whenAtMode(page, Page.BorderBars);
    public Setting<Color> barColor = setting("BarColor", new Color(new java.awt.Color(0, 0, 0, 255).getRGB())).des("Category Bottom Bar Color").whenTrue(categoryBar).whenAtMode(page, Page.BorderBars);
    public Setting<Boolean> categoryGradient = setting("CategoryFade", true).des("Gradient Bar At The Bottom of Category Rect").whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryGradientX = setting("CategoryFadeX", 0.0f, -25.0f, 25.0f).des("X Of Category Bottom Gradient Bar").whenTrue(categoryGradient).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryGradientY = setting("CategoryFadeY", 2.8f, -25.0f, 25.0f).des("Y Of Category Bottom Gradient Bar").whenTrue(categoryGradient).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryGradientXScale = setting("CategoryFadeXScale", 0.0f, -100.0f, 100.0f).des("Length Factor Of Category Bottom Gradient Bar").whenTrue(categoryGradient).whenAtMode(page, Page.BorderBars);
    public Setting<Float> categoryGradientYScale = setting("CategoryFadeYScale", 22.7f, 0.0f, 100.0f).des("Height Factor Of Category Bottom Gradient Bar").whenTrue(categoryGradient).whenAtMode(page, Page.BorderBars);
    public Setting<Color> gradientBarColor = setting("CategoryBGradientColor", new Color(new java.awt.Color(0, 0, 0, 115).getRGB())).des("Category Bottom Gradient Color").whenTrue(categoryGradient).whenAtMode(page, Page.BorderBars);

    public Setting<Boolean> bottomGradient = setting("BottomGradient", true).des("Panel Bottom Gradient").whenAtMode(page, Page.BorderBars);
    public Setting<Float> bottomGradientWidth = setting("PBottomHeight", 17.8f, 0.0f, 100.0f).des("Panel Bottom Gradient Width").whenTrue(bottomGradient).whenAtMode(page, Page.BorderBars);
    public Setting<Color> panelBottomGradientColor = setting("PBottomColor", new Color(new java.awt.Color(0, 0, 0, 60).getRGB())).des("Panel Bottom Gradient Color").whenTrue(bottomGradient).whenAtMode(page, Page.BorderBars);

    public Setting<Boolean> guiCategoryShadow = setting("TopShadow", true).des("GUI Category Rectangle Shadow").whenAtMode(page, Page.Shadow);
    public Setting<Float> shadowRadiusCategory = setting("TopRadius", 0.7f, 0.0f, 1.0f).des("Category Rectangle Shadow Radius").whenTrue(guiCategoryShadow).whenAtMode(page, Page.Shadow);
    public Setting<Integer> shadowAlpha = setting("TopShadowAlpha", 108, 0, 255).des("Category Rectangle Shadow Alpha").whenTrue(guiCategoryShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> shadowSizeFactorX = setting("TopShadowSizeX", 1.2f, 0.0f, 4.0f).des("Category Rectangle Size Of Shadow Horizontal").whenTrue(guiCategoryShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> shadowSizeFactorY = setting("TopShadowSizeY", 2.7f, 0.0f, 4.0f).des("Category Rectangle Size Of Shadow Vertical").whenTrue(guiCategoryShadow).whenAtMode(page, Page.Shadow);
    public Setting<Boolean> guiModuleShadow = setting("ModuleShadow", true).des("GUI Module Rectangle Shadow").whenAtMode(page, Page.Shadow);
    public Setting<Boolean> guiModuleShadowFilled = setting("MShadowFilled", false).des("Fill In Center Of GUI Modules Shadow").whenTrue(guiModuleShadow).whenAtMode(page, Page.Shadow);
    public Setting<Integer> shadowAlphaModules = setting("MShadowAlpha", 122, 0, 255).des("Modules Panel Shadow Alpha").whenTrue(guiModuleShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> moduleShadowSizeFactor = setting("MShadowSize", 0.2f, 0.0f, 1.0f).des("Modules Panel Size Of Shadow").whenTrue(guiModuleShadow).whenAtMode(page, Page.Shadow);

    public Setting<Boolean> moduleRectRounded = setting("ModuleRectRound", false).des("Module Rect Rounded Corners").whenAtMode(page, Page.Panel);
    public Setting<Float> moduleRectRoundedRadius = setting("ModuleCornerRadius", 1.0f, 0.0f, 1.0f).des("Rounded Module Corner Radius").whenTrue(moduleRectRounded).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleRoundedTopRight = setting("MRoundedTopRight", true).des("Rounded Module Arc Top Right").whenTrue(moduleRectRounded).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleRoundedTopLeft = setting("MRoundedTopLeft", true).des("Rounded Module Arc Top Left").whenTrue(moduleRectRounded).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleRoundedBottomRight = setting("MRoundedDownRight", true).des("Rounded Module Arc Bottom Right").whenTrue(moduleRectRounded).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleRoundedBottomLeft = setting("MRoundedDownLeft", true).des("Rounded Module Arc Bottom Left").whenTrue(moduleRectRounded).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleRectOutline = setting("ModuleRectOutline", false).des("Module Rect Outlines").whenAtMode(page, Page.Panel);
    public Setting<Float> moduleRectOutlineLineWidth = setting("MRectOutlineThick", 0.9f, 0.0f, 4.0f).des("Module Rect Outlines Line Width").whenTrue(moduleRectOutline).whenAtMode(page, Page.Panel);
    public Setting<Color> moduleRectOutlineColor = setting("MRectOutlineColor", new Color(new java.awt.Color(120, 61, 255, 170).getRGB())).des("Module Rect Outlines Color").whenTrue(moduleRectOutline).whenAtMode(page, Page.Panel);

    public Setting<Boolean> moduleImageDescrip = setting("ModuleMiniIcon", true).des("Display Small Image Of Module Next To Name").whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleImageDescripGlow = setting("ModuleMiniIconGlow", true).des("Module Mini Image Glow").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleImageDescripGlowRadius = setting("MMiniIconGlowRadius", 12.0f, 0.0f, 15.0f).des("Module Mini Image Glow Radius").whenTrue(moduleImageDescripGlow).whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleImageDescripGlowAlpha = setting("MMiniIconGlowAlpha", 60, 0, 255).des("Module Mini Image Glow Alpha").whenTrue(moduleImageDescripGlow).whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Color> moduleImageDescripColor = setting("MMiniIconColor", new Color(new java.awt.Color(81, 81, 81, 76).getRGB())).des("Module Mini Image Color").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleImageDescripDisableOnModuleEnable = setting("MMiniIconEnabledDisable", false).des("Don't Draw Module Mini Icon When Module Is Enabled").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<ModuleMiniIconSide> moduleMiniIconSide = setting("MMiniIconHardSide", ModuleMiniIconSide.HardLeft).des("Module Mini Image Side").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleMiniIconXOffset = setting("MMiniIconX", 0, -50, 50).des("Module Mini Image X Offset").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleTextExtendedMove = setting("MTextExtendedMove", true).des("Module Text Extended Move").when(() -> CustomFont.instance.moduleTextPos.getValue() != CustomFont.TextPos.Center).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleNameHardMoveRightSide = setting("RightESModuleShift", false).des("Make All Module Names Move To Make Room For Right Rect").whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleNameHardMoveLeftSide = setting("LeftESModuleShift", false).des("Make All Module Names Move To Make Room For Left Rect").whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleGap = setting("ModuleGap", 0, -1, 13).des("Module Rect Height").whenAtMode(page, Page.Panel);

    public Setting<Boolean> moduleHoverStuff = setting("HoverRender", true).des("Do Stuff When Module Is Hovered").whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleHoverFadeFactor = setting("HoverFade", 20, 1, 100).des("Module Hover Alpha Fade Factor").whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleHoverStartAlpha = setting("HoverStartAlpha", 33, 0, 255).des("Module Hover Start Alpha").whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<HoverScaleFadeMode> moduleHoverScaleFade = setting("HoverScaleFade", HoverScaleFadeMode.None).des("Module Hover Scale Fade").whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleHoverScaleFadeFactor = setting("HoverScaleFadeFactor", 8.9f, 0.0f, 20.0f).des("Module Hover Scale Fade Speed").whenTrue(moduleHoverStuff).when(() -> moduleHoverScaleFade.getValue() != ClickGUI.HoverScaleFadeMode.None).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleHoverTextScale = setting("HoverTextScale", true).des("Module Hover Text Scale").whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleHoverTextScaleNewScale = setting("HoverTextNewScale", 0.2f, 0.1f, 2.0f).des("Module Hover Text New Scale").whenTrue(moduleHoverTextScale).whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleHoverTextScaleFactorIn = setting("HTScaleAnimateFactorIn", 1.6f, 0.1f, 3.0f).des("Module Hover Text Scale Animation Speed In").whenTrue(moduleHoverTextScale).whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleHoverTextScaleFactorOut = setting("HTScaleAnimateFactorOut", 0.6f, 0.1f, 3.0f).des("Module Hover Text Scale Animation Speed Out").whenTrue(moduleHoverTextScale).whenTrue(moduleHoverStuff).whenAtMode(page, Page.Panel);

    public Setting<PanelExtensions> panelExtensions = setting("PanelExtensions", PanelExtensions.Both).des("Modules Panel Top Bottom Extra Space").whenAtMode(page, Page.Panel);
    public Setting<Integer> panelExtensionsHeight = setting("PanelExtensionsHeight", 8, 0, 12).des("Panel Extensions Height").when(() -> panelExtensions.getValue() != ClickGUI.PanelExtensions.None).whenAtMode(page, Page.Panel);

    public Setting<Boolean> moduleSeparators = setting("ModuleSeparators", true).des("Module Separator Bars").whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleSeparatorsOnTop = setting("MSeparatorsOnTop", false).des("Draw Module Separators On Top Of Module Rects").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSeparatorX = setting("MSeparatorsX", 0, -50, 50).des("Module Separator X").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSeparatorY = setting("MSeparatorsY", 0, -50, 50).des("Module Separator Y").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSeparatorWidth = setting("MSeparatorsWidth", 79, 0, 100).des("Module Separator Width").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSeparatorHeight = setting("MSeparatorsHeight", 3, 0, 10).des("Module Separator Height").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleSeparatorGlow = setting("MSeparatorGlow", true).des("Module Separator Glow").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleSeparatorGlowHeight = setting("MSGlowHeight", 10.0f, 0.0f, 10.0f).des("Module Separator Glow Height").whenTrue(moduleSeparatorGlow).whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSeparatorGlowAlpha = setting("MSGlowAlpha", 86, 0, 255).des("Module Separator Glow Alpha").whenTrue(moduleSeparatorGlow).whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<ModuleSeparatorFadeMode> moduleSeparatorFadeMode = setting("MSFadeMode", ModuleSeparatorFadeMode.Right).des("Module Separator Fade Mode").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleSeparatorFadeLength = setting("MSFadeLength", 0.0f, 0.0f, 1.0f).des("Length Of Module Separator Fade").when(() -> moduleSeparatorFadeMode.getValue() != ClickGUI.ModuleSeparatorFadeMode.None).whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);
    public Setting<Color> moduleSeparatorColor = setting("MSeparatorColor", new Color(new java.awt.Color(141, 141, 141, 40).getRGB())).des("Module Separator Color").whenTrue(moduleSeparators).whenAtMode(page, Page.Panel);

    public Setting<ModuleSideGlow> moduleSideGlow = setting("ModuleSideGlow", ModuleSideGlow.Both).des("Module Gradients On Inner Panel").whenAtMode(page, Page.Panel);
    public Setting<Boolean> moduleSideGlowLayer = setting("MSideGlowOnTop", false).des("Draw Module Gradients On Top Of Module Rects").when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);
    public Setting<ModuleSideGlowDouble> moduleSideGlowDouble = setting("MSideGlowDouble", ModuleSideGlowDouble.Right).des("Draw Module Gradients Twice").when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleSideGlowDoubleWidth = setting("MSideGlowDoubleWidth", 23.7f, 0.0f, 100.0f).des("Double Module Gradient Width").when(() -> moduleSideGlowDouble.getValue() != ClickGUI.ModuleSideGlowDouble.None).when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);
    public Setting<Integer> moduleSideGlowDoubleAlpha = setting("MSideDoubleAlpha", 20, 0, 255).des("Double Module Gradient Alpha").when(() -> moduleSideGlowDouble.getValue() != ClickGUI.ModuleSideGlowDouble.None).when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);
    public Setting<Float> moduleSideGlowWidth = setting("SideGlowWidth", 32.6f, 0.0f, 100.0f).des("Side Glow Width").when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);
    public Setting<Color> moduleSideGlowColor = setting("SideGlowColor", new Color(new java.awt.Color(186, 186, 186, 80).getRGB())).des("Side Glow Color").when(() -> moduleSideGlow.getValue() != ClickGUI.ModuleSideGlow.None).whenAtMode(page, Page.Panel);

    public Setting<Color> moduleColor = setting("ModuleColor", new Color(new java.awt.Color(0, 0, 0, 69).getRGB())).des("Module Color").whenAtMode(page, Page.Panel);
    public Setting<Color> moduleBGColor = setting("ModuleBGColor", new Color(new java.awt.Color(0,  0, 0, 178).getRGB())).des("Module BG Color").whenAtMode(page, Page.Panel);

    public Setting<Boolean> enabledRect = setting("EnabledRect", true).des("Module Enabled Full Rectangle").whenAtMode(page, Page.Enabled);
    public Setting<EnabledRectAnimation> enabledRectAnimation = setting("EnabledRectAnimate", EnabledRectAnimation.Alpha).des("Module Enabled Rect Animation").whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<EnabledRectMoveMode> enabledRectMove = setting("EnabledRectScale", EnabledRectMoveMode.All).des("Enabled Rect Scale Animation").when(() -> enabledRectAnimation.getValue() == EnabledRectAnimation.Scale || enabledRectAnimation.getValue() == EnabledRectAnimation.Both).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectAnimationFactor = setting("ERectAnimateFactor", 1.7f, 0.4f, 3.0f).des("Enabled Rect Animation Speed").when(() -> enabledRectAnimation.getValue() != EnabledRectAnimation.None).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledRectBrightnessRoll = setting("ERectBrightnessRoll", true).des("Module Enabled Rect Rolling Brightness").whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<EnabledRectBrightRollAxis> enabledRectBrightRollAxis = setting("ERectBrightRollAxis", EnabledRectBrightRollAxis.Y).des("Module Enabled Rect Rolling Brightness Axis").whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<EnabledRectBrightRollDirectionX> enabledRectBrightRollDirectionX = setting("ERBrightRollDirectionX", EnabledRectBrightRollDirectionX.Left).des("Module Enabled Rect Rolling Brightness Direction").when(() -> enabledRectBrightRollAxis.getValue() == EnabledRectBrightRollAxis.X).whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<EnabledRectBrightRollDirectionY> enabledRectBrightRollDirectionY = setting("ERBrightRollDirectionY", EnabledRectBrightRollDirectionY.Down).des("Module Enabled Rect Rolling Brightness Direction").when(() -> enabledRectBrightRollAxis.getValue() == EnabledRectBrightRollAxis.Y).whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectBrightRollSpeed = setting("ERectBrightRollSpeed", 3.6f, 0.0f, 15.0f).des("Module Enabled Rect Rolling Brightness Speed").whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectBrightRollMax = setting("ERectBrightRollMax", 1.0f, 0.0f, 1.0f).des("Module Enabled Rect Rolling Brightness Max").whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectBrightRollMin = setting("ERectBrightRollMin", 0.2f, 0.0f, 1.0f).des("Module Enabled Rect Rolling Brightness Min").whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectBrightRollLength = setting("ERectBrightRollLength", 0.6f, 0.0f, 1.0f).des("Module Enabled Rect Rolling Brightness Length").whenTrue(enabledRectBrightnessRoll).whenTrue(enabledRect).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledRectColor = setting("EnabledRectColor", new Color(new java.awt.Color(69, 31, 255, 69).getRGB())).des("Enabled Rect Color").whenTrue(enabledRect).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledSide = setting("EnabledSideRect", true).des("Module Enabled Side Rectangle").whenAtMode(page, Page.Enabled);
    public Setting<EnabledSideSide> enabledSideSide = setting("EnabledSideSide", EnabledSideSide.Right).des("Side Of Enabled Side").whenAtMode(page, Page.Enabled).whenTrue(enabledSide);
    public Setting<Float> enabledSideHeight = setting("EnabledSideHeight", 0.6f, 0.1f, 1.0f).des("Height Of Enabled Side").whenAtMode(page, Page.Enabled).whenTrue(enabledSide);
    public Setting<Float> enabledSideX = setting("EnabledSideX", 2.4f, -50.0f, 50.0f).des("X Of Enabled Side").whenAtMode(page, Page.Enabled).whenTrue(enabledSide);
    public Setting<Float> enabledSideY = setting("EnabledSideY", 1.4f, -13.0f, 13.0f).des("Y Of Enabled Side").whenAtMode(page, Page.Enabled).whenTrue(enabledSide);
    public Setting<Float> enabledSideSize = setting("EnabledSideSize", 4.9f, 0.0f, 20.0f).des("Enabled Shape Side Width").whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideRound = setting("ESideRounded", true).des("Module Enabled Rounded Side Rect").whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideRoundedFull = setting("ESideFullRounded", true).des("Module Enabled All Sides Rounded Side Rect").whenTrue(enabledSideRound).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledSideRadius = setting("ESideCornerRad", 1.0f, 0.0f, 1.0f).des("Radius Of Rounded Enabled Side Rect Corners").whenTrue(enabledSide).whenTrue(enabledSideRound).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideMove = setting("EnabledSideMove", true).des("Enabled Side Move").whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideMoveX = setting("EnabledSideMoveX", true).des("Enabled Side Move X").whenTrue(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledSideMoveFactor = setting("ESideMoveFactor", 1.0f, 0.4f, 3.0f).des("Enabled Shape Side Move Speed").whenTrue(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideColorMode = setting("ESideColorEnable", false).des("Module Enabled Side Color Change Enabled Marker").whenFalse(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideColorModeAnimation = setting("ESideColorAnimate", true).des("Module Enabled Side Color Change Animation").whenTrue(enabledSideColorMode).whenFalse(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledSideColorModeAnimationFactor = setting("ESideColorAnimateFactor", 1.0f, 0.4f, 3.0f).des("Module Enabled SIde Color Change Animation Speed").whenTrue(enabledSideColorModeAnimation).whenTrue(enabledSideColorMode).whenFalse(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledSideColor = setting("EnabledSideColor", new Color(new java.awt.Color(100, 27, 255, 255).getRGB())).des("Enabled Side Color").whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Color> disabledSideColorModeColor = setting("DisabledSideColor", new Color(new java.awt.Color(47, 47, 47, 255).getRGB())).des("Side Disabled Color Mode Color").whenTrue(enabledSideColorMode).whenFalse(enabledSideMove).whenTrue(enabledSide).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledRectScaleFade = setting("ERectScaleFade", true).des("Enabled Shape Rect Scale Out And Fade").whenAtMode(page, Page.Enabled);
    public Setting<EnableDisableScaleRect> enabledRectScaleOnWhat = setting("ERectScaleWhen", EnableDisableScaleRect.Enable).des("Enabled Shape Rect Scale On Enable Or Disable").whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectScaleX = setting("ERScaleX", 33.0f, 0.0f, 100.0f).des("Enabled Shape Rect Scale X").whenTrue(enabledRectScaleFade).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectScaleY = setting("ERScaleY", 34.0f, 0.0f, 100.0f).des("Enabled Shape Rect Scale Y").whenTrue(enabledRectScaleFade).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledRectScaleFactor = setting("ERScaleFactor", 0.6f, 0.3f, 3.0f).des("Enabled Shape Rect Move Speed").whenTrue(enabledRectScaleFade).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledRectStartAlpha = setting("ERStartAlpha", 162, 0, 255).des("Enabled Shape Rect Start Alpha").whenTrue(enabledRectScaleFade).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> moduleTextNoMove = setting("ModuleTextNoMove", false).des("Module Text No Move On Enable").whenTrue(enabledSide).whenAtMode(page, Page.Panel);

    public Setting<Float> enabledSideIconXOffset = setting("EnabledSideIconX", 0.0f, -20.0f, 20.0f).des("Module Enabled Side Icon X Offset").whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideIconDifColor = setting("EnabledSideIcon", true).des("Module Enabled Side Icon Color").whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledSideIconColorSmooth = setting("EnabledSideIconSmooth", true).des("Enabled Side Icon Color Animation").whenTrue(enabledSideIconDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledSideIconColorSmoothFactor = setting("ESideIconSmoothFactor", 1.0f, 0.4f, 3.0f).des("Enabled Side Icon Color Animation Speed").whenTrue(enabledSideIconColorSmooth).whenTrue(enabledSideIconDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledSideIconColor = setting("ESideIconColor", new Color(new java.awt.Color(255, 255, 255, 144).getRGB())).des("Enabled Side Icon Color").whenTrue(enabledSideIconDifColor).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledTextDifColor = setting("EnabledText", true).des("Module Enabled Text Color").whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledTextSmooth = setting("ETextAnimate", true).des("Module Enabled Text Animation").whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledTextSmoothFactor = setting("ETextAnimateFactor", 0.8f, 0.4f, 3.0f).des("Module Enabled Text Animation Speed").whenTrue(enabledTextDifColor).whenTrue(enabledTextSmooth).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledTextBrightnessRoll = setting("ETextBrightnessRoll", false).des("Module Enabled Text Rolling Brightness").whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<EnabledTextBrightRollAxis> enabledTextBrightRollAxis = setting("ETextBrightRollAxis", EnabledTextBrightRollAxis.Y).des("Module Enabled Text Rolling Brightness Axis").whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<EnabledTextBrightRollDirectionX> enabledTextBrightRollDirectionX = setting("ETBrightRollDirectionX", EnabledTextBrightRollDirectionX.Left).des("Module Enabled Text Rolling Brightness Direction").when(() -> enabledTextBrightRollAxis.getValue() == EnabledTextBrightRollAxis.X).whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<EnabledTextBrightRollDirectionY> enabledTextBrightRollDirectionY = setting("ETBrightRollDirectionY", EnabledTextBrightRollDirectionY.Down).des("Module Enabled Text Rolling Brightness Direction").when(() -> enabledTextBrightRollAxis.getValue() == EnabledTextBrightRollAxis.Y).whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledTextBrightRollSpeed = setting("ETextBrightRollSpeed", 1.0f, 0.0f, 15.0f).des("Module Enabled Text Rolling Brightness Speed").whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledTextBrightRollMax = setting("ETextBrightRollMax", 1.0f, 0.0f, 1.0f).des("Module Enabled Text Rolling Brightness Max").whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledTextBrightRollMin = setting("ETextBrightRollMin", 0.0f, 0.0f, 1.0f).des("Module Enabled Text Rolling Brightness Min").whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledTextBrightRollLength = setting("ETextBrightRollSize", 20.0f, 0.1f, 50.0f).des("Module Enabled Text Rolling Brightness Length").whenTrue(enabledTextBrightnessRoll).whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledTextColor = setting("ETextColor", new Color(new java.awt.Color(185, 185, 185, 204).getRGB())).des("Enabled Text Color").whenTrue(enabledTextDifColor).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledGlow = setting("EnabledGlow", true).des("Module Enabled Glow").whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowRadius = setting("EnabledGlowRadius", 1.0f, 0.0f, 1.0f).des("Module Enabled Glow Corner Radius").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledGlowX = setting("EnabledGlowX", 52, -100, 100).des("Module Enabled Glow X").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledGlowY = setting("EnabledGlowY", 1, -15, 15).des("Module Enabled Glow Y").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledGlowXScale = setting("EGlowXScale", -66, -100, 100).des("Module Enabled Glow X Scale").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledGlowYScale = setting("EGlowYScale", 28, -100, 100).des("Module Enabled Glow Y Scale").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledGlowMoveSideXOffset = setting("EGMoveWithSideX", true).des("Module Enabled Glow Move With Side Rect X Offset").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledGlowColor = setting("EnabledGlowColor", new Color(new java.awt.Color(106, 47, 255, 115).getRGB())).des("Enabled Glow Color").whenTrue(enabledGlow).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledTextGlow = setting("EnabledTextGlow", false).des("Module Text Glow Gradient").whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledTextGlowX = setting("EGTextXScale", 8, -50, 50).des("Enabled Glow Text X Scale").whenTrue(enabledTextGlow).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledTextGlowY = setting("EGTextYScale", 15, -50, 50).des("Enabled Glow Text Y Scale").whenTrue(enabledTextGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowTextRadius = setting("EGlowTextRadius", 0.7f, 0.0f, 1.0f).des("Enabled Glow Text Radius").whenTrue(enabledTextGlow).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledTextGlowColor = setting("ETextGlowColor", new Color(new java.awt.Color(100, 61, 255, 145).getRGB())).des("Enabled Text Glow Color").whenTrue(enabledTextGlow).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledSideGlow = setting("EnabledSideGlow", true).des("Module Enabled Side Glow").whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledGlowSideTop = setting("EGlowSideTop", false).des("Enabled Glow Draw Side On Top").whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowSideTopFactor = setting("EGSideTopFactor", 0.0f, -15.0f, 15.0f).des("Enabled Glow Side Top Size").whenTrue(enabledGlowSideTop).whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledGlowSideBottom = setting("EGlowSideDown", false).des("Enabled Glow Draw Side On Bottom").whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowSideBottomFactor = setting("EGSideDownFactor", 0.0f, -15.0f, 15.0f).des("Enabled Glow Side Bottom Size").whenTrue(enabledGlowSideBottom).whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledGlowSideRight = setting("EGlowSideRight", true).des("Enabled Glow Draw Side On Right").whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowSideRightFactor = setting("EGSideRightFactor", 50.5f, -100.0f, 100.0f).des("Enabled Glow Side Right Size").whenTrue(enabledGlowSideRight).whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledGlowSideLeft = setting("EGlowSideLeft", false).des("Enabled Glow Draw Side On Left").whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledGlowSideLeftFactor = setting("EGSideLeftFactor", 0.0f, -100.0f, 100.0f).des("Enabled Glow Side Left Size").whenTrue(enabledGlowSideLeft).whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledSideGlowColor = setting("ESideGlowColor", new Color(new java.awt.Color(68, 0, 164, 81).getRGB())).des("Enabled Side Glow Color").whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledAllGlowFade = setting("EnabledAllGlowFade", true).des("Module Enabled All Glow Fade Animation").when(() -> enabledSideGlow.getValue() || enabledGlow.getValue() || enabledTextGlow.getValue()).whenTrue(enabledSideGlow).whenAtMode(page, Page.Enabled);
    public Setting<Float> enabledAllGlowAlphaFactor = setting("EAllGlowAlphaFactor", 1.4f, 0.4f, 3.0f).des("Module Enabled All Glow Fade Factor").when(() -> enabledSideGlow.getValue() || enabledGlow.getValue() || enabledTextGlow.getValue() && enabledAllGlowFade.getValue()).whenAtMode(page, Page.Enabled);

    public Setting<Boolean> enabledMiniIconDifColor = setting("EMiniIcon", false).des("Change Color Of Module Mini Icon On Enable").whenTrue(moduleImageDescrip).whenAtMode(page, Page.Enabled);
    public Setting<Color> enabledMiniIconColor = setting("EMiniIconColor", new Color(new java.awt.Color(121, 121, 121, 91).getRGB())).des("Module Mini Icon Enabled Color").whenTrue(enabledMiniIconDifColor).whenTrue(moduleImageDescrip).whenAtMode(page, Page.Enabled);
    public Setting<Integer> enabledMiniIconGlowAlpha = setting("EMIconGlowAlpha", 93, 0, 255).des("Module Mini Icon Glow Enabled Alpha").whenTrue(enabledMiniIconDifColor).whenTrue(moduleImageDescrip).whenAtMode(page, Page.Enabled);
    public Setting<Boolean> enabledMiniIconColorSmooth = setting("EMiniIconSmooth", true).des("Mini Icon Enable Color Animation").whenTrue(enabledMiniIconDifColor).whenTrue(moduleImageDescrip).whenAtMode(page, Page.Enabled);

    public Setting<Color> enabledColor = setting("EnabledColor", new Color(new java.awt.Color(100, 27, 255, 255).getRGB())).des("Enabled Color").whenAtMode(page, Page.Enabled);

    public Setting<Float> moduleExtendedIconScale = setting("SideIconScale", 0.8f, 0.1f, 1.5f).des("Size Of Icon On Side Of Module").whenAtMode(page, Page.SideIcon);
    public Setting<ClickGUI.SideIconMode> sideIconMode = setting("SideIcon", ClickGUI.SideIconMode.Dots).des("Icon On Side Of Module").whenAtMode(page, Page.SideIcon);
    public Setting<ClickGUI.SideIconSide> sideIconSide = setting("SideIconSide", ClickGUI.SideIconSide.Right).des("Icon On Side's Side").when(() -> sideIconMode.getValue() != ClickGUI.SideIconMode.None).whenAtMode(page, Page.SideIcon);
    public Setting<Integer> sideIconXOffset = setting("SideIconX", -1, -20, 50).des("Icon On Side X Offset").when(() -> sideIconMode.getValue() != ClickGUI.SideIconMode.None).whenAtMode(page, Page.SideIcon);
    public Setting<Boolean> sideIconNoMove = setting("SideIconNoMove", false).des("Icon On Side No Move").whenTrue(enabledSide).when(() -> sideIconMode.getValue() != ClickGUI.SideIconMode.None).whenAtMode(page, Page.SideIcon);
    public Setting<Boolean> sideIconNoSideRectDraw = setting("NoIconOnSideRect", false).des("Don't Render Icon When Side Rect Enable Is On").whenTrue(enabledSide).when(() -> sideIconMode.getValue() != ClickGUI.SideIconMode.None).whenAtMode(page, Page.SideIcon);
    public Setting<Color> sideIconColor = setting("SIconColor", new Color(new java.awt.Color(228, 228, 228, 40).getRGB())).des("Icon On Side Color").when(() -> sideIconMode.getValue() != ClickGUI.SideIconMode.None).whenAtMode(page, Page.SideIcon);

    public Setting<Float> extendedWidth = setting("ExtendedWidth", 1.6f, 0.0f, 4.0f).des("Extended Line Width").whenAtMode(page, Page.Extended);
    public Setting<Color> extendedColor = setting("ExtendedColor", new Color(new java.awt.Color(95, 27, 255, 166).getRGB())).des("Extended Line Color").whenAtMode(page, Page.Extended);
    public Setting<Integer> extendedRectGap = setting("ExtendedRectGap", 0, -1, 15).des("Extended Rect Gap").whenAtMode(page, Page.Extended);
    public Setting<Color> extendedRectColor = setting("ExtendedRectColor", new Color(new java.awt.Color(0, 0, 0, 40).getRGB())).des("Extended Rect Color").whenAtMode(page, Page.Extended);

    public Setting<Boolean> extendedVerticalGradient = setting("ExtendedGradient", true).des("Vertical Extended Module Gradient").whenAtMode(page, Page.Extended);
    public Setting<Float> extendedGradientWidth = setting("EGradientWidth", 41.6f, 0.0f, 100.0f).des("Extended Gradient Width").whenTrue(extendedVerticalGradient).whenAtMode(page, Page.Extended);
    public Setting<Color> extendedGradientColor = setting("EGradientColor", new Color(new java.awt.Color(108, 20, 255, 69).getRGB())).des("Extended Gradient Color").whenTrue(extendedVerticalGradient).whenAtMode(page, Page.Extended);

    public Setting<Boolean> extendedBottomExtensions = setting("ExtendedDownExtensions", true).des("Extended Bottom Extra Space").whenAtMode(page, Page.Extended);
    public Setting<Float> extendedBottomExtensionsHeight = setting("EDowExtensionsHeight", 6.0f, 0.0f, 16.0f).des("Extended Bottom Extra Space Height").whenTrue(extendedBottomExtensions).whenAtMode(page, Page.Extended);

    public Setting<Boolean> extendedTopBars = setting("ExtendedTopBars", true).des("Draw Bars At Top Of Extended Module").whenAtMode(page, Page.Extended);
    public Setting<Boolean> extendedCategoryBar = setting("EBar", true).des("Bar At The Top of Extended Module").whenTrue(extendedTopBars).whenAtMode(page, Page.Extended);
    public Setting<Float> extendedCategoryBarX = setting("EBarX", 0.0f, -25.0f, 25.0f).des("X Of Extended Module Bar").whenTrue(extendedTopBars).whenTrue(extendedCategoryBar).whenAtMode(page, Page.Extended);
    public Setting<Float> extendedCategoryBarY = setting("EBarY", 0.0f, -25.0f, 25.0f).des("Y Of Extended Module Bar").whenTrue(extendedTopBars).whenTrue(extendedCategoryBar).whenAtMode(page, Page.Extended);
    public Setting<Float> extendedCategoryBarXScale = setting("EBarXScale", 0.0f, -50.0f, 50.0f).des("Length Factor Of Extended Module Bar").whenTrue(extendedTopBars).whenTrue(extendedCategoryBar).whenAtMode(page, Page.Extended);
    public Setting<Float> extendedCategoryBarYScale = setting("EBarYScale", 0.9f, -4.0f, 4.0f).des("Height Factor Of Extended Module Bar").whenTrue(extendedTopBars).whenTrue(extendedCategoryBar).whenAtMode(page, Page.Extended);
    public Setting<Color> extendedBarColor = setting("ExtendedBarColor", new Color(new java.awt.Color(0, 0, 0, 165).getRGB())).des("Extended Module Bar Color").whenTrue(extendedTopBars).whenTrue(extendedCategoryBar).whenAtMode(page, Page.Extended);
    public Setting<Boolean> extendedCategoryGradient = setting("ETopFade", true).des("Gradient Bar At The Top Of Extended Module").whenTrue(extendedTopBars).whenAtMode(page, Page.Extended);
    public Setting<Float> extendedCategoryGradientHeight = setting("ETopFadeHeight", 40.0f, 0.0f, 100.0f).des("Height Factor Of Extended Module Gradient Bar").whenTrue(extendedTopBars).whenTrue(extendedCategoryGradient).whenAtMode(page, Page.Extended);
    public Setting<Boolean> extendedGradientBottom = setting("EDownFade", true).des("Gradient Bar At The Bottom Of Extended Module").whenAtMode(page, Page.Extended);
    public Setting<Float> extendedGradientBottomHeight = setting("EDownFadeHeight", 40.0f, 0.0f, 100.0f).des("Height Factor Of Extended Module Bottom Gradient").whenTrue(extendedGradientBottom).whenAtMode(page, Page.Extended);
    public Setting<Color> extendedTopDownGradientColor = setting("ETopDownGradientColor", new Color(new java.awt.Color(0, 0, 0, 141).getRGB())).des("Extended Top Down Gradient Color").whenTrue(extendedTopBars).whenTrue(extendedCategoryGradient).whenAtMode(page, Page.Extended);

    public Setting<Boolean> guiCategoryBase = setting("BaseRect", true).des("Draw Rectangle At The Bottom Of Categories").whenAtMode(page, Page.Outline);

    public Setting<Boolean> panelBaseShadow = setting("BaseRectShadow", true).des("Panel Base Rect Shadow").whenTrue(guiCategoryBase).whenAtMode(page, Page.Shadow);
    public Setting<Boolean> panelBaseShadowFilled = setting("BRectShadowFilled", true).des("Fill In Center Of Base Rect Shadow").whenTrue(guiCategoryBase).whenTrue(panelBaseShadow).whenAtMode(page, Page.Shadow);
    public Setting<Integer> panelBaseShadowAlpha = setting("BRectShadowAlpha", 149, 0, 255).des("Base Rect Shadow Alpha").whenTrue(guiCategoryBase).whenTrue(panelBaseShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> panelBaseShadowHeight = setting("BRectShadowHeight", 26.6f, 0.0f, 30.0f).des("Base Rect Shadow Height").whenTrue(guiCategoryBase).whenTrue(panelBaseShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> panelBaseShadowWidth = setting("BRectShadowWidth", 18.1f, 0.0f, 30.0f).des("Base Rect Shadow Width").whenTrue(guiCategoryBase).whenTrue(panelBaseShadow).whenAtMode(page, Page.Shadow);
    public Setting<Float> panelBaseShadowRadius = setting("BRectShadowRadius", 0.8f, 0.0f, 1.0f).des("Base Rect Shadow Radius").whenTrue(guiCategoryBase).whenTrue(panelBaseShadow).whenAtMode(page, Page.Shadow);


    public Setting<Boolean> baseGlow = setting("BaseRectGlow", true).des("Base Rect Glow").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseGlowWidth = setting("BRectGlowWidth", 40.0f, -150.0f, 150.0f).des("Base Rect Glow Width").whenTrue(guiCategoryBase).whenTrue(baseGlow).whenAtMode(page, Page.Outline);
    public Setting<Float> baseGlowHeight = setting("BRectGlowHeight", 36.7f, -50.0f, 50.0f).des("Base Rect Glow Height").whenTrue(guiCategoryBase).whenTrue(baseGlow).whenAtMode(page, Page.Outline);
    public Setting<Color> baseGlowColor = setting("BaseRectGlowColor", new Color(new java.awt.Color(100, 45, 255, 100).getRGB())).des("Base Rect Glow Color").whenTrue(guiCategoryBase).whenTrue(baseGlow).whenAtMode(page, Page.Outline);

    public Setting<Float> heightBase = setting("BaseHeight", 13.9f, 0.0f, 40.0f).des("Height Of Base").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> widthBase = setting("BaseWidth", 5.5f, 0.0f, 40.0f).des("Extra Width Of Base").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Integer> baseAlpha = setting("BaseAlpha", 227, 0, 255).des("Base Alpha").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseOutline = setting("BaseOutline", false).des("Base Outline").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseOutlineTopToggle = setting("BOutlineTopToggle", false).des("Base Outline Top Line Toggle").whenTrue(baseOutline).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseOutlineWidth = setting("BaseOutlineWidth", 0.8f, 0.0f, 4.0f).des("Base Outline Width").whenTrue(baseOutline).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Color> baseOutlineColor = setting("BaseOutlineColor", new Color(new java.awt.Color(230, 230, 230, 230).getRGB())).des("Base Outline Color").whenTrue(baseOutline).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);

    public Setting<ClickGUI.BaseRectPattern> baseRectPattern = setting("BasePattern", ClickGUI.BaseRectPattern.Circles).des("Base Geometric Patterns").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<ClickGUI.BaseRectPatternExtra> baseRectPatternExtra = setting("BasePatternType", ClickGUI.BaseRectPatternExtra.Single).des("Base Geometric Patterns Type").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<ClickGUI.BaseRectPatternTrianglesSingleExtra> baseRectPatternSingleTrianglesExtra = setting("BPatternTrianglesType", ClickGUI.BaseRectPatternTrianglesSingleExtra.Up).des("Base Triangle Pattern Type").when(() -> baseRectPattern.getValue() == ClickGUI.BaseRectPattern.Triangles && baseRectPatternExtra.getValue() == ClickGUI.BaseRectPatternExtra.Single).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternYOffset = setting("BPatternY", 1.9f, -15.0f, 15.0f).des("Base Pattern Y Offset").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternSize = setting("BPatternSize", 1.9f, 0.0f, 20.0f).des("Base Pattern Size Of Shapes").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternGap = setting("BPatternGap", 2.1f, 0.0f, 30.0f).des("Base Pattern Gap Between Shapes").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternDoubleYGap = setting("BPatternDoubleGap", 2.2f, -15.0f, 15.0f).des("Base Pattern Y Gap Between Shapes").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None && baseRectPatternExtra.getValue() == ClickGUI.BaseRectPatternExtra.Double).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Integer> baseRectPatternAmount = setting("BPatternAmount", 3, 1, 15).des("Base Pattern Amount Of Shapes").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseRectPatternReflect = setting("BPatternReflect", true).des("Flip Base Pattern Upside Down").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None && baseRectPatternExtra.getValue() == ClickGUI.BaseRectPatternExtra.Double).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseRectPatternShadow = setting("BPatternShadow", true).des("Base Pattern Shadow Gradient").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternShadowRadius = setting("BPatternShadowRadius", 1.0f, 0.0f, 1.0f).des("Base Pattern Shadow Radius").whenTrue(baseRectPatternShadow).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternShadowY = setting("BPatternShadowY", 1.2f, -5.0f, 5.0f).des("Base Pattern Shadow Y Adjust").whenTrue(baseRectPatternShadow).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Integer> baseRectPatternShadowAlpha = setting("BPatternShadowAlpha", 75, 0, 255).des("Base Pattern Shadow Alpha").whenTrue(baseRectPatternShadow).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternShadowWidth = setting("BPatternShadowWidth", 36.6f, 0.0f, 100.0f).des("Base Pattern Shadow Width").whenTrue(baseRectPatternShadow).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternShadowHeight = setting("BPatternShadowHeight", 9.7f, 0.0f, 20.0f).des("Base Pattern Shadow Height").whenTrue(baseRectPatternShadow).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseRectPatternOutline = setting("BPatternOutline", false).des("Base Pattern Shapes Outline").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternOutlineWidth = setting("BPatternOutlineWidth", 0.4f, 0.0f, 1.0f).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(baseRectPatternOutline).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseRectPatternOutlineBrightnessRoll = setting("BRPOutlineBrightRoll", false).des("Base Pattern Outline Rolling Brightness").whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<BaseRectPatternOutlineBrightnessRollDirection> baseRectPatternOutlineBrightnessRollDirection = setting("BRPOBrightRollDirection", BaseRectPatternOutlineBrightnessRollDirection.Right).des("Base Pattern Outline Rolling Brightness Direction").whenTrue(baseRectPatternOutlineBrightnessRoll).whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternOutlineBrightnessRollSpeed = setting("BRPOBrightRollSpeed", 1.6f, 0.0f, 3.0f).des("Base Pattern Outline Rolling Brightness Speed").whenTrue(baseRectPatternOutlineBrightnessRoll).whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternOutlineBrightnessRollMaxBright = setting("BRPOBrightRollMax", 0.0f, 0.0f, 1.0f).des("Base Pattern Outline Rolling Brightness Max Brightness").whenTrue(baseRectPatternOutlineBrightnessRoll).whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternOutlineBrightnessRollMinBright = setting("BRPOBrightRollMin", 0.5f, 0.0f, 1.0f).des("Base Pattern Outline Rolling Brightness Min Brightness").whenTrue(baseRectPatternOutlineBrightnessRoll).whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternOutlineBrightnessRollLength = setting("BRPOBrightRollLength", 21.3f, 0.1f, 50.0f).des("Base Pattern Outline Rolling Brightness Length").whenTrue(baseRectPatternOutlineBrightnessRoll).whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Color> baseRectPatternOutlineColor = setting("BRPOutlineColor", new Color(new java.awt.Color(0, 0, 0, 78).getRGB())).des("Base Pattern Outline Color").whenTrue(baseRectPatternOutline).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> baseRectPatternBrightnessRoll = setting("BRPatternBrightRoll", true).des("Base Pattern Rolling Brightness").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<BaseRectPatternBrightnessRollDirection> baseRectPatternBrightnessRollDirection = setting("BRPBrightRollDirection", BaseRectPatternBrightnessRollDirection.Left).des("Base Pattern Rolling Brightness Direction").whenTrue(baseRectPatternBrightnessRoll).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternBrightnessRollSpeed = setting("BRPBrightRollFactor", 1.5f, 0.0f, 15.0f).des("Base Pattern Rolling Brightness Speed").whenTrue(baseRectPatternBrightnessRoll).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternBrightnessRollMaxBright = setting("BRPBrightRollMax", 1.0f, 0.0f, 1.0f).des("Base Pattern Rolling Brightness Max Brightness").whenTrue(baseRectPatternBrightnessRoll).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternBrightnessRollMinBright = setting("BRPBrightRollMin", 0.2f, 0.0f, 1.0f).des("Base Pattern Rolling Brightness Min Brightness").whenTrue(baseRectPatternBrightnessRoll).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> baseRectPatternBrightnessRollLength = setting("BRPBrightRollSize", 0.4f, 0.0f, 1.0f).des("Base Pattern Rolling Brightness Length").whenTrue(baseRectPatternBrightnessRoll).when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Color> baseRectPatternColor = setting("BRPatternColor", new Color(new java.awt.Color(154, 154, 154, 116).getRGB())).des("Base Pattern Shape Color").when(() -> baseRectPattern.getValue() != ClickGUI.BaseRectPattern.None).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);

    public Setting<Boolean> guiCategoryBaseRound = setting("BaseRoundedRect", true).des("Draw Rounded Rectangle At The Bottom Of Categories").whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> radiusBase = setting("BaseCornerRad", 0.6f, 0.0f, 1.0f).des("Radius Of Rounded Base Rect Corners").whenTrue(guiCategoryBaseRound).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> arcTopRightBase = setting("TRightRoundBase", true).des("Base Rect Round Top Right Corner").whenTrue(guiCategoryBaseRound).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> arcTopLeftBase = setting("TLeftRoundBase", true).des("Base Rect Round Top Left Corner").whenTrue(guiCategoryBaseRound).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> arcDownRightBase = setting("DRightRoundBase", true).des("Base Rect Round Bottom Right Corner").whenTrue(guiCategoryBaseRound).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> arcDownLeftBase = setting("DLeftRoundBase", true).des("Base Rect Round Bottom Left Corner").whenTrue(guiCategoryBaseRound).whenTrue(guiCategoryBase).whenAtMode(page, Page.Outline);

    public Setting<Boolean> guiCategoryPanelFadeDownExtend = setting("PanelExtendFade", false).des("Draw Fade At Bottom Of Panels").whenFalse(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Boolean> panelFadeDownExtendOutline = setting("PExtendFadeOutline", true).des("Panel Bottom Extend Outline").whenTrue(guiCategoryPanelFadeDownExtend).whenFalse(guiCategoryBase).whenAtMode(page, Page.Outline);
    public Setting<Float> panelFadeDownExtendHeight = setting("PExtendFadeHeight", 17.8f, 0.0f, 50.0f).des("Panel Bottom Extend Fade Height").whenTrue(guiCategoryPanelFadeDownExtend).whenFalse(guiCategoryBase).whenAtMode(page, Page.Outline);

    public Setting<Boolean> outline = setting("Outline", true).des("Outline Around Panels").whenAtMode(page, Page.Outline);
    public Setting<Boolean> outlineDownToggle = setting("OutlineDownToggle", false).des("Don't Draw Bottom Outline Line").whenTrue(outline).whenAtMode(page, Page.Outline);
    public Setting<Float> outlineWidth = setting("OutlineWidth", 1.1f, 0.0f, 4.0f).des("Width Of GUI Outline").whenTrue(outline).whenAtMode(page, Page.Outline);
    public Setting<Boolean> outlineColorGradient = setting("OutlineGradient", true).des("Outline Gradient Color").whenTrue(outline).whenAtMode(page, Page.Outline);
    public Setting<Color> outlineColor = setting("OutlineColor", new Color(new java.awt.Color(100, 61, 255, 255).getRGB())).des("Outline Color").whenTrue(outline).whenFalse(outlineColorGradient).whenAtMode(page, Page.Outline);

    public Setting<Color> outlineTopColor = setting("OutlineTopColor", new Color(new java.awt.Color(68, 0, 164, 126).getRGB())).des("Outline Top Color").whenTrue(outline).whenTrue(outlineColorGradient).whenAtMode(page, Page.Outline);
    public Setting<Color> outlineDownColor = setting("OutlineDownColor", new Color(new java.awt.Color(68, 0, 179, 255).getRGB())).des("Outline Down Color").whenTrue(outline).whenTrue(outlineColorGradient).whenAtMode(page, Page.Outline);

    public Setting<Boolean> backgroundColor = setting("BGColorTint", true).des("Background color tint effect").whenAtMode(page, Page.Background);

    public Setting<Boolean> gradient = setting("ColorGradient", true).des("Background Color Gradient").whenTrue(backgroundColor).whenAtMode(page, Page.Background);
    public Setting<Color> trColor = setting("TopRightColor", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).des("Top Right Color").whenTrue(backgroundColor).whenTrue(gradient).whenAtMode(page, Page.Background);
    public Setting<Color> tlColor = setting("TopLeftColor", new Color(new java.awt.Color(0, 0, 0, 0).getRGB())).des("Top Left Color").whenTrue(backgroundColor).whenTrue(gradient).whenAtMode(page, Page.Background);
    public Setting<Color> brColor = setting("BottomRightColor", new Color(new java.awt.Color(0, 0, 0, 255).getRGB())).des("Bottom Right Color").whenTrue(backgroundColor).whenTrue(gradient).whenAtMode(page, Page.Background);
    public Setting<Color> blColor = setting("BottomLeftColor", new Color(new java.awt.Color(0, 0, 0, 255).getRGB())).des("Bottom Left Color").whenTrue(backgroundColor).whenTrue(gradient).whenAtMode(page, Page.Background);
    public Setting<Color> bgColor = setting("BGColor", new Color(new java.awt.Color(0, 0, 0, 100).getRGB())).des("BG Color").whenTrue(backgroundColor).whenFalse(gradient).whenAtMode(page, Page.Background);

    Setting<SettingsPage> settingsPage = setting("SettingsPage", SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Boolean> booleanFullRect = setting("BooleanFullRect", false).des("Boolean Full Rect").whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Boolean> booleanFullRectSmooth = setting("BFullRectSmooth", true).des("Boolean Full Rect Animation").whenTrue(booleanFullRect).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanFullRectSmoothFactor = setting("BFullRectSpeed", 1.0f, 0.4f, 10.0f).des("Boolean Full Rect Animation Speed").whenTrue(booleanFullRectSmooth).whenTrue(booleanFullRect).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Boolean> booleanFullRectSmoothAlpha = setting("BFullRectSmoothAlpha", true).des("Boolean Full Rect Alpha Animation").whenTrue(booleanFullRectSmooth).whenTrue(booleanFullRect).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<BooleanFullRectScaleType> booleanFullRectScaleType = setting("BFullRectSmoothScale", BooleanFullRectScaleType.All).des("Boolean Full Rect Scale Animation Type").whenTrue(booleanFullRectSmooth).whenTrue(booleanFullRect).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Color> booleanFullRectColor = setting("BFullRectColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Boolean Full Rect Color").whenTrue(booleanFullRect).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);

    public Setting<BooleanSwitchTypes> booleanSwitchType = setting("BooleanSwitchType", BooleanSwitchTypes.SliderRound).des("Boolean Switch Types").whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanSwitchLineWidth = setting("BSwitchLineWidth", 1.0f, 1.0f, 2.0f).des("Boolean Switch Outline Width").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanSwitchX = setting("BooleanSwitchX", 6.0f, -20.0f, 20.0f).des("Boolean Switch X Offset").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanSwitchScale = setting("BSwitchScale", 0.7f, 0.1f, 2.0f).des("Boolean Switch Scale").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanDotFillAmount = setting("BDotFillAmount", 0.5f, 0.0f, 1.0f).des("Boolean Dot Fill Amount").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Boolean> booleanSmooth = setting("BooleanSmooth", true).des("Boolean Animation").when(() -> (booleanSwitchType.getValue() == BooleanSwitchTypes.SliderRound || booleanSwitchType.getValue() == BooleanSwitchTypes.SliderNonRound) && booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanSmoothFactor = setting("BooleanSmoothSpeed", 6.6f, 0.4f, 10.0f).des("Boolean Animation Speed").whenTrue(booleanSmooth).when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<BooleanDotMode> booleanDotMode = setting("BDotSmoothMode", BooleanDotMode.Both).des("Boolean Dot Animation Mode").when(() -> booleanSwitchType.getValue() == BooleanSwitchTypes.Square || booleanSwitchType.getValue() == BooleanSwitchTypes.Circle).whenTrue(booleanSmooth).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Boolean> booleanSwitchColorChange = setting("BooleanColorChange", true).des("Boolean Color Change On Enable").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);

    public Setting<Color> booleanDisabledColor = setting("BDisabledColor", new Color(new java.awt.Color(31, 0, 98, 255).getRGB())).des("Boolean Disabled Color").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenTrue(booleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Color> booleanEnabledColor = setting("BEnabledColor", new Color(new java.awt.Color(105, 45, 255, 255).getRGB())).des("Boolean Enabled Color").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenTrue(booleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Color> booleanColor = setting("BooleanColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des(" Boolean Color").when(() -> booleanSwitchType.getValue() != BooleanSwitchTypes.None).whenFalse(booleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);

    public Setting<Boolean> booleanTextColorChange = setting("BooleanTextColorChange", true).des("Boolean Text Color Change").whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Boolean> booleanTextColorSmooth = setting("BooleanTextColorSmooth", true).des("Boolean Text Color Change Animation").whenTrue(booleanTextColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Float> booleanTextColorSmoothFactor = setting("BTextColorSmoothFactor", 3.4f, 0.4f, 10.0f).des("Boolean Text Color Change Animation Speed").whenTrue(booleanTextColorSmooth).whenTrue(booleanTextColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Color> booleanTextColorEnabledColor = setting("BEnabledTextColor", new Color(new java.awt.Color(113, 79, 255, 255).getRGB())).des("Boolean Enabled Text Color").whenTrue(booleanTextColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);
    public Setting<Color> booleanTextColorDisabledColor = setting("BDisabledTextColor", new Color(new java.awt.Color(86, 86, 86, 255).getRGB())).des("Boolean Disabled Text Color").whenTrue(booleanTextColorChange).whenAtMode(settingsPage, SettingsPage.Boolean).whenAtMode(page, Page.Settings);

    public Setting<Boolean> numSliderThinMode = setting("NumSliderThinMode", true).des("Number Slider Thin Mode").whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeTextOffset = setting("NumSliderThinTextOffset", 3.5f, 0.0f, 8.0f).des("Number Slider Thin Text Y Offset").whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeBarThickness = setting("NSliderThinBarThick", 1.2f, 0.0f, 5.0f).des("Num Slider Thin Thickness").whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderThinModeRounded = setting("NSliderThinRounded", true).des("Number Slider Thin Mode Rounded").whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeRoundedRadius = setting("NSliderThinRadius", 1.0f, 0.0f, 1.0f).des("Number Slider Thin Mode Rounded Corners Radius").whenTrue(numSliderThinModeRounded).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderThinModeSliderButton = setting("NSliderButton", true).des("Number Slider Thin Mode Header Button").whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderThinModeSliderButtonRounded = setting("NSliderButtonRounded", true).des("Number Slider Thin Mode Header Button Rounded Corner").whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeSliderButtonRoundedRadius = setting("NSliderButtonRadius", 0.7f, 0.0f, 1.0f).des("Number Slider Thin Mode Header Button Rounded Corner Size").whenTrue(numSliderThinModeSliderButtonRounded).whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeSliderButtonWidth = setting("NSliderButtonWidth", 6.0f, 0.0f, 15.0f).des("Number Slider Thin Mode Header Button Width").whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeSliderButtonHeight = setting("NSliderButtonHeight", 4.0f, 0.0f, 10.0f).des("Number Slider Thin Mode Header Button Height").whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderThinModeSliderButtonShadow = setting("NSliderButtonShadow", true).des("Number Slider Thin Mode Header Button Shadow").whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeSliderButtonShadowSize = setting("NSliderButtonShadowSize", 0.5f, 0.0f, 1.0f).des("Number Slider Thin Mode Header Button Shadow Size").whenTrue(numSliderThinModeSliderButtonShadow).whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Integer> numSliderThinModeSliderButtonShadowAlpha = setting("NSliderButtonShadowAlpha", 139, 0, 255).des("Number Slider Thin Mode Header Button Shadow Alpha").whenTrue(numSliderThinModeSliderButtonShadow).whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderThinModeSliderButtonShadowAlphaFadeOut = setting("NSliderButtonShadowFade", true).des("Number Slider Thin Mode Header Button Fade Shadow At Start Of Bar").whenTrue(numSliderThinModeSliderButtonShadow).whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Float> numSliderThinModeSliderButtonShadowAlphaFadeOutThreshold = setting("NSButtonShadowFadePoint", 0.2f, 0.0f, 0.5f).des("Number Slider Thin Mode Header Button Fade Shadow Threshold").whenTrue(numSliderThinModeSliderButtonShadowAlphaFadeOut).whenTrue(numSliderThinModeSliderButtonShadow).whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Color> numSliderThinModeSliderButtonColor = setting("NSliderButtonColor", new Color(new java.awt.Color(113, 113, 113, 255).getRGB())).des("Number Slider Thin Mode Header Button Color").whenTrue(numSliderThinModeSliderButton).whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Color> numSliderThinModeUnSlidedColor = setting("NumThinUnSlidedColor", new Color(new java.awt.Color(40, 40, 40, 255).getRGB())).des("Number Slider Thin Mode UnSlided Area Color").whenTrue(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Color> numSliderTextColor = setting("NSliderTextColor", new Color(new java.awt.Color(204, 204, 204, 255).getRGB())).des("Number Slider Text Color").whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Color> numSliderDisplayValueTextColor = setting("NSValueTextColor", new Color(new java.awt.Color(122, 122, 122, 255).getRGB())).des("Number Slider Display Value Text Color").whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Boolean> numSliderGradient = setting("NumSliderFade", true).des("Number Slider Gradient").whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Boolean> numSliderValueLock = setting("NSliderValueLock", false).des("Number Slider Display Value Pos Lock").whenFalse(numSliderThinMode).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Color> numSliderLeftColor = setting("NSliderLeftColor", new Color(new java.awt.Color(93, 26, 255, 255).getRGB())).des("Number Slider Left Color").whenTrue(numSliderGradient).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);
    public Setting<Color> numSliderRightColor = setting("NSliderRightColor", new Color(new java.awt.Color(79, 0, 199, 255).getRGB())).des("Number Slider Right Color").whenTrue(numSliderGradient).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Color> numSliderColor = setting("NumSliderColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Number Slider Color").whenFalse(numSliderGradient).whenAtMode(settingsPage, SettingsPage.Slider).whenAtMode(page, Page.Settings);

    public Setting<Boolean> bindButtonFancy = setting("BindButtonFancy", true).des("Bind Button Fancy Mode").whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Boolean> bindButtonFancyRounded = setting("BindButtonRounded", true).des("Bind Button Fancy Mode Rounded").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyRoundedRadius = setting("BButtonRoundedRadius", 0.6f, 0.0f, 1.0f).des("Bind Button Fancy Mode Rounded Corners Size").whenTrue(bindButtonFancyRounded).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Boolean> bindButtonFancyOutline = setting("BindButtonOutline", true).des("Bind Button Fancy Mode Offsetted Outline").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyOutlineOffset = setting("BButtonOutlineOffset", 2.0f, 0.0f, 5.0f).des("Bind Button Fancy Mode Outline Offset").whenTrue(bindButtonFancyOutline).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyOutlineWidth = setting("BButtonOutlineWidth", 1.2f, 1.0f, 2.0f).des("Bind Button Fancy Mode Outline Width").whenTrue(bindButtonFancyOutline).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Boolean> bindButtonFancyWaitingDots = setting("BButtonWaitDots", true).des("Bind Button Fancy Mode Waiting For Input Dots").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingDotsRadius = setting("BButtonWaitDotsSize", 0.6f, 0.0f, 3.0f).des("Bind Button Fancy Mode Waiting For Input Dots Size").whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingDotsGap = setting("BButtonWaitDotsGap", 1.0f, 0.0f, 3.0f).des("Bind Button Fancy Mode Waiting For Input Dots Gap").whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Color> bindButtonFancyWaitingDotsColor = setting("BButtonWaitDotsColor", new Color(new java.awt.Color(129, 129, 129, 255).getRGB())).des("Bind Button Fancy Mode Waiting For Input Dots Color").whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);

    public Setting<Boolean> bindButtonFancyWaitingRect = setting("BButtonWaitRect", true).des("Bind Button Fancy Mode Waiting For Input Colored Rect").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Color> bindButtonFancyWaitingRectColor = setting("BButtonWaitRectColor", new Color(new java.awt.Color(98, 98, 98, 255).getRGB())).des("Bind Button Fancy Mode Waiting For Input Colored Rect Color").whenTrue(bindButtonFancyWaitingRect).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);

    public Setting<Boolean> bindButtonFancyWaitingAnimate = setting("BButtonWaitAnimate", true).des("Bind Button Fancy Mode Waiting For Input Animation").when(() -> bindButtonFancyWaitingDots.getValue() || bindButtonFancyWaitingRect.getValue()).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingAnimateFactor = setting("BButtonWaitAnimateSpeed", 5.6f, 0.4f, 10.0f).des("Bind Button Fancy Mode Waiting For Input Animation Speed").whenTrue(bindButtonFancyWaitingAnimate).when(() -> bindButtonFancyWaitingDots.getValue() || bindButtonFancyWaitingRect.getValue()).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Boolean> bindButtonFancyWaitingDotsRollingBrightnessAnimate = setting("BBWaitDotsRollBright", true).des("Bind Button Fancy Mode Waiting For Input Dots Rolling Brightness Animation").whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<BindButtonWaitingDotsRolledBrightnessDirection> bindButtonFancyWaitingDotsRollingBrightnessRollDirection = setting("BBWaitDotsRollDirection", BindButtonWaitingDotsRolledBrightnessDirection.Right).des("Bind Button Fancy Mode Waiting For Input Dots Animate Direction").whenTrue(bindButtonFancyWaitingDotsRollingBrightnessAnimate).whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingAnimateDotsRollingBrightnessAnimateFactor = setting("BButtonWaitDotsFactor", 1.6f, 0.0f, 15.0f).des("Bind Button Fancy Mode Waiting For Input Dots Animate Speed").whenTrue(bindButtonFancyWaitingDotsRollingBrightnessAnimate).whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingAnimateDotsRollingBrightnessAnimateMaxBright = setting("BButtonWaitDotsMax", 0.8f, 0.0f, 1.0f).des("Bind Button Fancy Mode Waiting For Input Dots Animate Max Brightness").whenTrue(bindButtonFancyWaitingDotsRollingBrightnessAnimate).whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingAnimateDotsRollingBrightnessAnimateMinBright = setting("BButtonWaitDotsMin", 0.4f, 0.0f, 1.0f).des("Bind Button Fancy Mode Waiting For Input Dots Animate Min Brightness").whenTrue(bindButtonFancyWaitingDotsRollingBrightnessAnimate).whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonFancyWaitingAnimateDotsRollingBrightnessAnimateRollLength = setting("BButtonWaitDotsRollSize", 0.3f, 0.0f, 1.0f).des("Bind Button Fancy Mode Waiting For Input Dots Animate Rolling Brightness Length").whenTrue(bindButtonFancyWaitingDotsRollingBrightnessAnimate).whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingDots).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<BindButtonColoredRectAnimateMode> bindButtonColoredRectAnimateMode = setting("BBWaitRectAnimateMode", BindButtonColoredRectAnimateMode.Both).des("Bind Button Fancy Mode Waiting For Input Colored Rect Animate Mode").whenTrue(bindButtonFancyWaitingAnimate).whenTrue(bindButtonFancyWaitingRect).whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);

    public Setting<Color> bindButtonKeyColor = setting("BButtonKeyColor", new Color(new java.awt.Color(199, 199, 199, 218).getRGB())).des("Bind Button Key Color").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);

    public Setting<KeyBindFancyFont> bindButtonKeyStrFont = setting("BButtonKeyStrFont", KeyBindFancyFont.Objectivity).des("Bind Button Key String Font").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonKeyStrX = setting("BButtonKeyStrX", 0.7f, -20.0f, 20.0f).des("Bind Button Key String X").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonKeyStrY = setting("BButtonKeyStrY", -0.3f, -20.0f, 20.0f).des("Bind Button Key String Y").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Float> bindButtonKeyStrScale = setting("BButtonKeyStrScale", 0.6f, 0.1f, 1.5f).des("Bind Button Key String Scale").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Color> bindButtonKeyStringColor = setting("BButtonKeyStrColor", new Color(new java.awt.Color(55, 55, 55, 255).getRGB())).des("Bind Button Key String Color").whenTrue(bindButtonFancy).whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);
    public Setting<Color> bindButtonTextColor = setting("BButtonTextColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Bind Button Text Color").whenAtMode(settingsPage, SettingsPage.Bind).whenAtMode(page, Page.Settings);

    public Setting<VisibilitySettingMode> visibilitySettingMode = setting("VisibilitySettingMode", VisibilitySettingMode.Symbol).des("Visibility Setting Mode").whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Color> visibilityVisibleTextColor = setting("VVisibleTextColor", new Color(new java.awt.Color(115, 115, 115, 255).getRGB())).des("Visibility 'Visible' Text Color").whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Color> visibilityTextColor = setting("VisibilityTextColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Visibility Text Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Text).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Boolean> visibilityBooleanFullRect = setting("VBooleanFullRect", false).des("Visibility Boolean Full Rect").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Boolean> visibilityBooleanFullRectSmooth = setting("VBFullRectSmooth", true).des("Visibility Boolean Full Rect Animation").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanFullRect).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanFullRectSmoothFactor = setting("VBFullRectSpeed", 1.0f, 0.4f, 10.0f).des("Visibility Boolean Full Rect Animation Speed").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanFullRectSmooth).whenTrue(visibilityBooleanFullRect).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Boolean> visibilityBooleanFullRectSmoothAlpha = setting("VBFullRectSmoothAlpha", true).des("Visibility Boolean Full Rect Alpha Animation").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanFullRectSmooth).whenTrue(visibilityBooleanFullRect).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<BooleanFullRectScaleType> visibilityBooleanFullRectScaleType = setting("VBFullRectSmoothScale", BooleanFullRectScaleType.All).des("Visibility Boolean Full Rect Scale Animation Type").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanFullRectSmooth).whenTrue(visibilityBooleanFullRect).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Color> visibilityBooleanFullRectColor = setting("VBFullRectColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Visibility Boolean Full Rect Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanFullRect).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<BooleanSwitchTypes> visibilityBooleanSwitchType = setting("VBooleanSwitchType", BooleanSwitchTypes.SliderRound).des("Visibility Boolean Switch Types").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanSwitchLineWidth = setting("VBSwitchLineWidth", 1.0f, 1.0f, 2.0f).des("Visibility Boolean Switch Outline Width").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanSwitchX = setting("VBooleanSwitchX", 0.0f, -20.0f, 20.0f).des("Visibility Boolean Switch X Offset").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanSwitchScale = setting("VBSwitchScale", 1.0f, 0.1f, 2.0f).des("Visibility Boolean Switch Scale").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanDotFillAmount = setting("VBDotFillAmount", 0.5f, 0.0f, 1.0f).des("Visibility Boolean Dot Fill Amount").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Boolean> visibilityBooleanSmooth = setting("VBooleanSmooth", true).des("Visibility Boolean Animation").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> (visibilityBooleanSwitchType.getValue() == BooleanSwitchTypes.SliderRound || visibilityBooleanSwitchType.getValue() == BooleanSwitchTypes.SliderNonRound) && visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityBooleanSmoothFactor = setting("VBooleanSmoothSpeed", 1.0f, 0.4f, 10.0f).des("Visibility Boolean Animation Speed").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).whenTrue(visibilityBooleanSmooth).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<BooleanDotMode> visibilityBooleanDotMode = setting("VBDotSmoothMode", BooleanDotMode.Both).des("Visibility Boolean Dot Animation Mode").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() == BooleanSwitchTypes.Square || visibilityBooleanSwitchType.getValue() == BooleanSwitchTypes.Circle).whenTrue(visibilityBooleanSmooth).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Boolean> visibilityBooleanSwitchColorChange = setting("VBooleanColorChange", true).des("Visibility Boolean Color Change On Enable").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Color> visibilityBooleanDisabledColor = setting("VBDisabledColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Visibility Boolean Disabled Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenTrue(visibilityBooleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Color> visibilityBooleanEnabledColor = setting("VBEnabledColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Visibility Boolean Enabled Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenTrue(visibilityBooleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Color> visibilityBooleanColor = setting("VBooleanColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Visibility Boolean Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Boolean).when(() -> visibilityBooleanSwitchType.getValue() != BooleanSwitchTypes.None).whenFalse(visibilityBooleanSwitchColorChange).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Integer> visibilityIconYOffset = setting("VisibilityIconY", 0, -15, 15).des("Visibility Icon Y Offset").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityIconScale = setting("VisibilityIconScale", 1.0f, 0.1f, 2.0f).des("Visibility Icon Scale").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Color> visibilityIconColor = setting("VisibilityIconColor", new Color(new java.awt.Color(255, 255, 255, 81).getRGB())).des("Visibility Icon Color").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Boolean> visibilityIconGlow = setting("VisibilityIconGlow", true).des("Visibility Icon Glow").whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Boolean> visibilityIconGlowToggle = setting("VIconGlowToggle", true).des("Visibility Icon Glow Toggle When Visibility Is Off").whenTrue(visibilityIconGlow).whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityIconGlowX = setting("VisibilityIconGlowX", 0.0f, -10.0f, 10.0f).des("Visibility Icon Glow X Offset").whenTrue(visibilityIconGlow).whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityIconGlowY = setting("VisibilityIconGlowY", -0.9f, -10.0f, 10.0f).des("Visibility Icon Glow Y Offset").whenTrue(visibilityIconGlow).whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Float> visibilityIconGlowSize = setting("VisibilityIconGlowSize", 12.0f, 0.0f, 15.0f).des("Visibility Icon Glow Size").whenTrue(visibilityIconGlow).whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);
    public Setting<Color> visibilityIconGlowColor = setting("VIconGlowColor", new Color(new java.awt.Color(255, 255, 255, 76).getRGB())).des("Visibility Icon Glow Color").whenTrue(visibilityIconGlow).whenAtMode(visibilitySettingMode, VisibilitySettingMode.Symbol).whenAtMode(settingsPage, SettingsPage.Visibility).whenAtMode(page, Page.Settings);

    public Setting<Boolean> enumDropMenu = setting("EnumDropMenu", true).des("Enum Dropdown Menu").whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumNameColor = setting("EnumNameColor", new Color(new java.awt.Color(192, 192, 192, 255).getRGB())).des("Enum Name Color").whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumLoopModeTextXOffset = setting("EnumTextX", 12, -10, 50).des("Enum Display Text X Offset").whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDisplayTextColor = setting("EDisplayTextColor", new Color(new java.awt.Color(115, 115, 115, 255).getRGB())).des("Enum Display Text Color").whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<EnumDropMenuPage> enumDropMenuPage = setting("EnumDropMenuPage", EnumDropMenuPage.Base).whenTrue(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Boolean> enumDropMenuIcon = setting("EnumIcon", true).des("Enum Dropdown Menu Icon").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuIconXOffset = setting("EnumIconX", 0, -10, 30).des("Enum Dropdown Menu Icon X Offset").whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuIconScale = setting("EnumIconScale", 0.8f, 0.1f, 2.0f).des("Enum Dropdown Menu Icon Scale").whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuIconColor = setting("EnumIconColor", new Color(new java.awt.Color(255, 255, 255, 36).getRGB())).des("Enum Dropdown Menu Icon Color").whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuIconExpandedChange = setting("EIconExpandedChange", true).des("Enum Dropdown Menu Icon Change When Expanded").whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuIconExpandedChangeAnimation = setting("EIconExpandAnimate", true).des("Enum Dropdown Menu Icon Expanded Change Animation").whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuIconExpandedChangeAnimationSpeed = setting("EIconExpandAnimateSpeed", 3.5f, 0.4f, 10.0f).des("Enum Dropdown Menu Icon Expanded Change Animation Speed").whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIconExpandedChangeAnimation).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuIconExpandedGlow = setting("EIconExpandGlow", true).des("Enum Dropdown Menu Icon Expanded Glow").whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuIconExpandedGlowSize = setting("EIconExpandGlowSize", 10.0f, 0.0f, 15.0f).des("Enum Dropdown Menu Icon Expanded Glow Size").whenTrue(enumDropMenuIconExpandedGlow).whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuIconExpandedGlowAlpha = setting("EIconExpandGlowAlpha", 52, 0, 255).des("Enum Dropdown Menu Icon Expanded Glow Alpha").whenTrue(enumDropMenuIconExpandedGlow).whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuIconExpandedChangedColor = setting("EIconExpandedColor", new Color(new java.awt.Color(255, 255, 255, 64).getRGB())).whenTrue(enumDropMenuIconExpandedChange).whenTrue(enumDropMenuIcon).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Icon).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Float> enumDropMenuHeight = setting("EnumMenuHeight", 80.0f, 1.0f, 500.0f).des("Height of enum dropdown menu").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuMinimumWidth = setting("EnumMenuMinWidth", 70.0f, 1.0f, 100.0f).des("Minimum width of enum dropdown menu").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollSpeed = setting("EnumMenuScrollSpeed", 5.0f, 0.1f, 10.0f).des("Speed of scrolling enum dropdown menu").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuReboundFactor = setting("EnumMenuReboundFactor", 3.0f, 0.1f, 15.0f).des("How much you can scroll out of bounds").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuReboundSpace = setting("EnumMenuReboundSpace", 0.5f, 0.0f, 1.0f).des("Amount of space to leave for menu when you've scrolled to either end").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuExpandAnimate = setting("EMenuExpandAnimate", true).des("Enum Dropdown Menu Expand Toggle Animate").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuExpandAnimateScale = setting("EMExpandAnimateScale", true).des("Enum Dropdown Menu Expand Toggle Animate Scale").whenTrue(enumDropMenuExpandAnimate).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuExpandAnimateSpeed = setting("EMExpandAnimateSpeed", 2.0f, 0.4f, 10.0f).des("Enum Dropdown Menu Expand Toggle Animate Speed").whenTrue(enumDropMenuExpandAnimate).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuExpandAnimateFactor = setting("EMExpandAnimateFactor", 0.5f, 0.1f, 1.0f).des("Steepness of enum dropdown menu expand toggle animation").whenTrue(enumDropMenuExpandAnimate).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuXOffset = setting("EnumMenuX", 5, 0, 50).des("Enum Dropdown Menu X Offset").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuWidthFactor = setting("EMenuWidthFactor", 10, 0, 40).des("Enum Dropdown Menu Width From Text").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuTextScale = setting("EnumMenuTextScale", 0.8f, 0.1f, 2.0f).des("Enum Dropdown Menu Text Scale").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuTextColor = setting("EMenuTextColor", new Color(new java.awt.Color(62, 62, 62, 209).getRGB())).des("Enum Dropdown Menu Text Color").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSideBar = setting("EnumMenuSideBar", true).des("Enum Dropdown Menu Side Bar").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSideBarWidth = setting("EMenuSideBarWidth", 1.9f, 1.0f, 5.0f).des("Enum Dropdown Menu Side Bar Width").whenTrue(enumDropMenuSideBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuOutline = setting("EnumMenuOutline", false).des("Enum Dropdown Menu Outline").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuOutlineWidth = setting("EMenuOutlineWidth", 1.0f, 1.0f, 3.0f).des("Enum Dropdown Menu Outline Width").whenTrue(enumDropMenuOutline).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuOutlineColor = setting("EMenuOutlineColor", new Color(new java.awt.Color(98, 26, 255, 255).getRGB())).des("Enum Dropdown Menu Outline Color").when(() -> enumDropMenuOutline.getValue() || enumDropMenuSideBar.getValue()).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuExtensions = setting("EnumMenuExtend", true).des("Enum Dropdown Menu Top Bottom Extensions").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuExtensionsHeight = setting("EMenuExtendHeight", 6.0f, 0.0f, 15.0f).des("Enum Dropdown Menu Top Bottom Extensions Height").whenTrue(enumDropMenuExtensions).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuRectGap = setting("EnumMenuRectGap", 0, -1, 15).des("Enum Dropdown Menu Rect Gaps").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuRectColor = setting("EMenuRectColor", new Color(new java.awt.Color(0, 0, 0, 64).getRGB())).des("Enum Dropdown Menu Rect Color").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuRectBGColor = setting("EMenuRectBGColor", new Color(new java.awt.Color(0, 0, 0, 206).getRGB())).des("Enum Dropdown Menu Rect BG Color").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuRectHover = setting("EMenuRectHover", true).des("Draws a rect on enum element when that element is hovered").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSideGlow = setting("EnumMenuSideGlow", true).des("Enum Dropdown Menu Side Glow").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSideGlowWidth = setting("EMSideGlowWidth", 20.0f, 0.0f, 50.0f).des("Enum Dropdown Menu Side Glow").whenTrue(enumDropMenuSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSideGlowColor = setting("EMSideGlowColor", new Color(new java.awt.Color(122, 43, 255, 101).getRGB())).des("Enum Dropdown Menu Side Glow Color").whenTrue(enumDropMenuSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuTopBottomGradients = setting("EMenuEndFade", true).des("Enum Dropdown Menu Gradients At Top And Bottom").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuTopBottomGradientsHeight = setting("EMEndFadeHeight", 15.5f, 0.0f, 30.0f).des("Enum Dropdown Menu Gradients At Top And Bottom Height").whenTrue(enumDropMenuTopBottomGradients).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuTopBottomGradientsColor = setting("EMEndFadeColor", new Color(new java.awt.Color(0, 0, 0, 178).getRGB())).des("Enum Dropdown Menu Gradients At Top And Bottom Color").whenTrue(enumDropMenuTopBottomGradients).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuInteriorGradients = setting("EMInteriorEndFades", true).des("Draw gradients on top and bottom string input collector menu").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuInteriorGradientHeight = setting("EMInteriorEndFadeHeight", 8.0f, 0.0f, 30.0f).des("Height of gradients on top and bottom string input collector menu").whenTrue(enumDropMenuInteriorGradients).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuInteriorGradientColor = setting("EMInteriorEndFadeColor", new Color(new java.awt.Color(0, 0, 0, 178).getRGB())).whenTrue(enumDropMenuInteriorGradients).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<EnumDropMenuOtherSideGlowMode> enumDropMenuOtherSideGlow = setting("EMSideGlow2", EnumDropMenuOtherSideGlowMode.Right).des("Enum Dropdown Menu Another Glow").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuOtherSideGlowWidth = setting("EMSideGlow2Width", 15.0f, 0.0f, 50.0f).des("Enum Dropdown Menu Another Glow Width").when(() -> enumDropMenuOtherSideGlow.getValue() != EnumDropMenuOtherSideGlowMode.None).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuOtherSideGlowColor = setting("EMSideGlow2Color", new Color(new java.awt.Color(255, 255, 255, 31).getRGB())).des("Enum Dropdown Menu Another Glow Color").when(() -> enumDropMenuOtherSideGlow.getValue() != EnumDropMenuOtherSideGlowMode.None).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuShadow = setting("EnumMenuShadow", true).des("Enum Dropdown Menu Gradient Shadow").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuShadowSize = setting("EMenuShadowSize", 0.2f, 0.0f, 1.0f).des("Enum Dropdown Menu Gradient Shadow Size").whenTrue(enumDropMenuShadow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuShadowAlpha = setting("EMenuShadowAlpha", 151, 0, 255).des("Enum Dropdown Menu Gradient Shadow Alpha").whenTrue(enumDropMenuShadow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Base).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Boolean> enumDropMenuSelectedRectScaleOut = setting("EMSelectExpandRect", false).des("Enum Dropdown Menu Expand Out Rect On Select").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedRectScaleOutFactor = setting("EMSelectExpandRectFactor", 1.0f, 0.4f, 10.0f).des("Enum Dropdown Menu Expand Out Rect On Select Speed").whenTrue(enumDropMenuSelectedRectScaleOut).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedRectScaleMaxScale = setting("EMSelectExpandRectMax", 20.0f, 0.0f, 50.0f).des("Enum Dropdown Menu Expand Out Rect On Select Max Scale").whenTrue(enumDropMenuSelectedRectScaleOut).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSelectedRectScaleOutColor = setting("EMSelectExpandRColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Enum Dropdown Menu Expand Out Rect On Select Color").whenTrue(enumDropMenuSelectedRectScaleOut).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedRect = setting("EMenuSelectRect", false).des("Enum Dropdown Menu Selected Rect").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedRectRounded = setting("EMenuSelectRectRound", false).des("Enum Dropdown Menu Selected Rect Rounded Corners").whenTrue(enumDropMenuSelectedRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedRectRoundedRadius = setting("EMenuSelectRectRadius", 0.5f, 0.0f, 1.0f).des("Enum Dropdown Menu Selected Rect Rounded Corners Radius").whenTrue(enumDropMenuSelectedRectRounded).whenTrue(enumDropMenuSelectedRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<EnumDropMenuSelectedRectAnimation> enumDropMenuSelectedRectAnimation = setting("EMenuSelectRectAnimate", EnumDropMenuSelectedRectAnimation.Slide).des("Enum Dropdown Menu Selected Rect Animation").whenTrue(enumDropMenuSelectedRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedRectAnimationSpeed = setting("EMSelectRectAnimateSpeed", 1.0f, 0.4f, 10.0f).des("Enum Dropdown Selected Rect Animation Speed").when(() -> enumDropMenuSelectedRectAnimation.getValue() != EnumDropMenuSelectedRectAnimation.None).whenTrue(enumDropMenuSelectedRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSelectedRectColor = setting("EMSelectRectColor", new Color(new java.awt.Color(120, 40, 255, 255).getRGB())).des("Enum Menu Selected Rect Color").whenTrue(enumDropMenuSelectedRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedSideRect = setting("EMSelectSideRect", true).des("Enum Menu Selected Side Rect").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedSideRectRounded = setting("EMSelectSideRectRound", true).des("Enum Menu Selected Side Rect Rounded Corners").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideRectRoundedRadius = setting("EMSelectSideRectRadius", 1.0f, 0.0f, 1.0f).des("Enum Menu Selected Side Rect Rounded Corners Radius").whenTrue(enumDropMenuSelectedSideRectRounded).whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<EnumDropMenuSelectedSideRectSide> enumDropMenuSelectedSideRectSide = setting("EMSelectSideRectSide", EnumDropMenuSelectedSideRectSide.Right).des("Enum Menu Selected Side Rect Side").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectSideRectFull = setting("EMSelectSideRAllRound", true).des("Enum Menu Selected Side Rect All Sides Rounded").whenTrue(enumDropMenuSelectedSideRectRounded).whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideRectXOffset = setting("EMSelectSideRectX", 1.8f, -10.0f, 40.0f).des("Enum Menu Selected Side Rect X Offset").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideRectWidth = setting("EMSelectSideRectWidth", 3.5f, 0.0f, 15.0f).des("Enum Menu Selected Side Rect Width").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideRectHeight = setting("EMSelectSideRectHeight", 10.4f, 0.0f, 16.0f).des("Enum Menu Selected Side Rect Height").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedSideRectAnimation = setting("EMSelectSideRAnimate", true).des("Enum Menu Select Side Rect Scale Animation").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideRectAnimationSpeed = setting("EMSelectSideRAnimateSpeed", 3.4f, 0.4f, 10.0f).des("Enum Menu Select Side Rect Scale Animation Speed").whenTrue(enumDropMenuSelectedSideRectAnimation).whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSelectedSideRectColor = setting("EMSelectSideColor", new Color(new java.awt.Color(93, 26, 255, 255).getRGB())).des("Enum Menu Selected Side Rect Color").whenTrue(enumDropMenuSelectedSideRect).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedSideGlow = setting("EMSelectSideGlow", true).des("Enum Menu Selected Side Glow").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<EnumDropMenuSelectedSideGlowSide> enumDropMenuSelectedSideGlowSide = setting("EMSelectSideGlowSide", EnumDropMenuSelectedSideGlowSide.Right).des("Enum Menu Select Side Glow Side").whenTrue(enumDropMenuSelectedSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideGlowWidth = setting("EMSelectSideGlowWidth", 18.4f, 0.0f, 50.0f).des("Enum Menu Select Side Glow Width").whenTrue(enumDropMenuSelectedSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedSideGlowAnimate = setting("EMSelectSideGlowAnimate", true).des("Enum Menu Select Side Glow Animate").whenTrue(enumDropMenuSelectedSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedSideGlowAnimateSpeed = setting("EMSelectSideGAnimateFactor", 1.0f, 0.4f, 10.0f).des("Enum Menu Select Side Glow Animate Speed").whenTrue(enumDropMenuSelectedSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSelectedSideGlowColor = setting("EMSelectSideFadeColor", new Color(new java.awt.Color(96, 26, 255, 129).getRGB())).des("Enum Menu Selected Side Glow Color").whenTrue(enumDropMenuSelectedSideGlowAnimate).whenTrue(enumDropMenuSelectedSideGlow).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedTextDifColor = setting("EMSelectTextDifColor", true).des("Enum Menu Selected Text Color Change").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuSelectedTextColorAnimation = setting("EMSelectTextColorAnimate", true).des("Enum Menu Selected Text Color Change Animation").whenTrue(enumDropMenuSelectedTextDifColor).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuSelectedTextColorAnimationSpeed = setting("EMSelectTextColorAnimateSpeed", 2.7f, 0.4f, 10.0f).des("Enum Menu Selected Text Color Change Animation Speed").whenTrue(enumDropMenuSelectedTextDifColor).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuSelectedTextColor = setting("EMSelectTextColor", new Color(new java.awt.Color(255, 255, 255, 206).getRGB())).des("Enum Menu Selected Text Color").whenTrue(enumDropMenuSelectedTextDifColor).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.Selected).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Boolean> enumDropMenuScrollBar = setting("EnumMenuScrollBar", true).des("Draw an interactable scroll bar next to enum dropdown menu").whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarXOffset = setting("EnumMenuScrollBarX", 3.0f, 0.0f, 10.0f).des("X offset of enum dropdown menu scroll bar from the menu").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarBGWidth = setting("EnumMenuScrollBarBGWidth", 3.0f, 1.0f, 15.0f).des("Width of enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarExtraWidth = setting("EnumMenuScrollBarExtraWidth", 1.0f, 0.0f, 10.0f).des("Extra of enum dropdown menu scroll bar from scroll bar background").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarBGRounded = setting("EnumMenuScrollBarBGRounded", true).des("Use rounded rect for enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarBGRoundedRadius = setting("EnumMenuScrollBarBGRoundedRadius", 0.8f, 0.0f, 1.0f).des("Radius of rounded rect for enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBarBGRounded).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuScrollBarBGColor = setting("EnumMenuScrollBarBGColor", new Color(new java.awt.Color(45, 45, 45, 255).getRGB())).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarBGShadow = setting("EnumMenuScrollBarBGShadow", true).des("Draw gradient shadow under enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarBGShadowSize = setting("EnumMenuScrollBarBGShadowSize", 0.2f, 0.0f, 1.0f).des("Size of gradient shadow under enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBarBGShadow).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuScrollBarBGShadowAlpha = setting("EnumMenuScrollBarBGShadowAlpha", 76, 0, 255).des("Alpha of gradient shadow under enum dropdown menu scroll bar background").whenTrue(enumDropMenuScrollBarBGShadow).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarRounded = setting("EnumMenuScrollBarRounded", true).des("Use rounded rect for enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarRoundedRadius = setting("EnumMenuScrollBarRoundedRadius", 0.5f, 0.0f, 1.0f).des("Radius of rounded rect for enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBarRounded).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuScrollBarColor = setting("EnumMenuScrollBarColor", new Color(new java.awt.Color(110, 110, 110, 255).getRGB())).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarShadow = setting("EnumMenuScrollBarShadow", true).des("Draw gradient shadow under enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarShadowSize = setting("EnumMenuScrollBarShadowSize", 0.3f, 0.0f, 1.0f).des("Size of gradient shadow under enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBarShadow).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuScrollBarShadowAlpha = setting("EnumMenuScrollBarShadowAlpha", 89, 0, 255).des("Alpha of gradient shadow under enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBarShadow).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarPattern = setting("EnumMenuScrollBarPattern", true).des("Draw a pattern in the middle of enum dropdown menu scroll bar").whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternSmallDistance = setting("EnumMenuPatternSmallDist", 5.0f, 0.0f, 20.0f).des("Minimum distance that the edge of the scroll bar can be away from the scroll bar pattern to have the the pattern change").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<PatternSmallMode> enumDropMenuScrollBarPatternSmallBehavior = setting("EnumMenuPatternSmallBehavior", PatternSmallMode.Shrink).des("What happens to scroll bar pattern when the bar is smaller than the pattern + specified distance").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumDropMenuScrollBarPatternCount = setting("EnumMenuPatternCount", 3, 1, 6).des("Amount of shapes in enum dropdown menu scroll bar pattern").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternDist = setting("EnumMenuPatternDist", 2.55f, 0.1f, 5.0f).des("Distance between shapes in enum dropdown menu scroll bar pattern").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarPatternRounded = setting("EnumMenuPatternRounded", false).des("Rounded shapes in enum dropdown menu scroll bar pattern").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternRoundedRadius = setting("EnumMenuPatternRadius", 0.8f, 0.0f, 1.0f).des("Radius of rounded shapes in enum dropdown menu scroll bar pattern").whenTrue(enumDropMenuScrollBarPatternRounded).whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternWidth = setting("EnumMenuPatternWidth", 0.7f, 0.0f, 1.0f).des("Fraction of scroll bar width to make patten width").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternHeight = setting("EnumMenuPatternHeight", 1.5f, 0.0f, 5.0f).des("Scroll bar pattern height").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Boolean> enumDropMenuScrollBarPatternRollColors = setting("EnumMenuPatternRollColors", true).des("Scroll bar pattern rollers between 2 colors").whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternRollSpeed = setting("EnumMenuPatternRollSpeed", 3.0f, 0.1f, 20.0f).des("Scroll bar pattern roll color speed").whenTrue(enumDropMenuScrollBarPatternRollColors).whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumDropMenuScrollBarPatternRollSize = setting("EnumMenuPatternRollSize", 0.3f, 0.1f, 2.0f).des("Scroll bar pattern roll color roll size").whenTrue(enumDropMenuScrollBarPatternRollColors).whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuScrollBarPatternRollColor = setting("EnumMenuPatternRollColor", new Color(new java.awt.Color(60, 38, 110, 255).getRGB())).whenTrue(enumDropMenuScrollBarPatternRollColors).whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumDropMenuScrollBarPatternColor = setting("EnumMenuPatternColor", new Color(new java.awt.Color(100, 61, 255, 255).getRGB())).whenTrue(enumDropMenuScrollBarPattern).whenTrue(enumDropMenuScrollBar).whenTrue(enumDropMenu).whenAtMode(enumDropMenuPage, EnumDropMenuPage.ScrollBar).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Boolean> enumLoopModeArrows = setting("EnumLooperArrows", false).des("Enum Looper Arrows").whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumLoopModeArrowsXOffset = setting("EnumLooperArrowsX", 0.0f, -30.0f, 30.0f).des("Enum Looper Arrows X Offset").whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumLoopModeArrowsScaleX = setting("ELooperArrowsWidth", 15.0f, 0.0f, 30.0f).des("Enum Looper Arrows Width").whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumLoopModeArrowsScaleY = setting("ELooperArrowsHeight", 7.0f, 0.0f, 20.0f).des("Enum Looper Arrows Height").whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<EnumArrowClickAnimationMode> enumArrowClickAnimationMode = setting("ELoopClickAnimate", EnumArrowClickAnimationMode.Scale).des("Enum Looper Arrows Animation").whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumArrowClickAnimationFactor = setting("ELooperClickAnimateSpeed", 1.0f, 0.4f, 10.0f).des("Enum Looper Arrows Animation Speed").when(() -> enumArrowClickAnimationMode.getValue() != EnumArrowClickAnimationMode.None).whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Float> enumArrowClickAnimationMaxScale = setting("ELCAnimateMaxScale", 1.5f, 0.1f, 5.0f).des("Enum Looper Arrows Animation Max Scale").when(() -> enumArrowClickAnimationMode.getValue() == EnumArrowClickAnimationMode.Scale).whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Integer> enumArrowClickAnimationMaxAlpha = setting("ELCAnimateMaxAlpha", 200, 0, 255).des("Enum Looper Arrows Animation Max Alpha").when(() -> enumArrowClickAnimationMode.getValue() != EnumArrowClickAnimationMode.None).whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);
    public Setting<Color> enumArrowColor = setting("ELooperArrowColor", new Color(new java.awt.Color(255, 255, 255, 150).getRGB())).des("Enum Looper Arrows Color").whenTrue(enumLoopModeArrows).whenFalse(enumDropMenu).whenAtMode(settingsPage, SettingsPage.Enum).whenAtMode(page, Page.Settings);

    public Setting<Color> colorNameTextColor = setting("ColorNameRainbow", new Color(new java.awt.Color(146, 146, 146, 255).getRGB())).des("Color Name Text Color").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<ColorDisplayShape> colorDisplayShape = setting("ColorDisplayShape", ColorDisplayShape.Square).des("Color Display Shape").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplaySize = setting("ColorDisplaySize", 9.9f, 0.0f, 16.0f).des("Color Display Size").when(() -> colorDisplayShape.getValue() != ColorDisplayShape.Rect).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayRectWidth = setting("CDisplayRectWidth", 30.0f, 0.0f, 50.0f).des("Color Display Rect Shape Width").when(() -> colorDisplayShape.getValue() == ColorDisplayShape.Rect).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayRectHeight = setting("CDisplayRectHeight", 12.0f, 0.0f, 16.0f).des("Color Display Rect Shape Height").when(() -> colorDisplayShape.getValue() == ColorDisplayShape.Rect).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayXOffset = setting("CDisplayXOffset", 3.5f, -10.0f, 50.0f).des("Color Display X Offset").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDisplayRounded = setting("ColorDisplayRounded", false).des("Color Display Rounded Corners").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayRoundedRadius = setting("CDisplayRoundedSize", 0.5f, 0.0f, 1.0f).des("Color Display Rounded Corners Size").whenTrue(colorDisplayRounded).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDisplayOutline = setting("ColorDisplayOutline", true).des("Color Display Outline").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayOutlineOffset = setting("CDisplayOutlineOffset", 1.3f, 0.0f, 5.0f).des("Color Display Outline Offset").whenTrue(colorDisplayOutline).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDisplayOutlineWidth = setting("CDisplayOutlineWidth", 1.0f, 1.0f, 2.0f).des("Color Display Outline Width").whenTrue(colorDisplayOutline).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);

    public Setting<Boolean> colorDropMenuAnimate = setting("ColorMenuAnimate", true).des("Color DropDown Menu Toggle Animation").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuAnimateScale = setting("ColorMenuAnimateScale", true).des("Color DropDown Menu Toggle Scale Animation").whenTrue(colorDropMenuAnimate).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuAnimateSpeed = setting("ColorMenuAnimateSpeed", 2.0f, 0.4f, 10.0f).des("Color DropDown Menu Toggle Animation Speed").whenTrue(colorDropMenuAnimate).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuAnimateFactor = setting("ColorMenuAnimateFactor", 0.5f, 0.1f, 1.0f).des("Colro dropdown menu toggle animation steepness").whenTrue(colorDropMenuAnimate).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Integer> colorDropMenuXOffset = setting("ColorMenuXOffset", 5, 0, 50).des("Color DropDown Menu X Offset").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuSideBar = setting("ColorMenuSideBar", true).des("Color DropDown Menu Side Bar").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuSideBarWidth = setting("CMenuSideBarWidth", 1.7f, 0.0f, 4.0f).des("Color DropDown Menu Side Bar Width").whenTrue(colorDropMenuSideBar).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Color> colorDropMenuSideBarColor = setting("CMenuSideBarColor", new Color(new java.awt.Color(95, 27, 255, 166).getRGB())).des("Color DropDown Menu Side Bar Color").whenTrue(colorDropMenuSideBar).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuOutline = setting("ColorMenuOutline", false).des("Color DropDown Menu Outline").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuOutlineWidth = setting("CMenuOutlineWidth", 1.0f, 1.0f, 2.0f).des("Color DropDown Menu Outline Width").whenTrue(colorDropMenuOutline).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Color> colorDropMenuOutlineColor = setting("CMenuOutlineColor", new Color(new java.awt.Color(95, 27, 255, 166).getRGB())).des("Color DropDown Menu Outline Color").whenTrue(colorDropMenuOutline).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuExtensions = setting("ColorMenuExtensions", true).des("Color DropDown Menu Top Bottom Extensions").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Integer> colorDropMenuExtensionsHeight = setting("CMenuExtensionsHeight", 6, 0, 12).des("Color DropDown Menu Top Bottom Extensions Height").whenTrue(colorDropMenuExtensions).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuShadow = setting("ColorMenuShadow", true).des("Color DropDown Menu Gradient Shadows").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuShadowSize = setting("CMenuShadowSize", 0.2f, 0.0f, 1.0f).des("Color DropDown Menu Gradient Shadow Size").whenTrue(colorDropMenuShadow).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Integer> colorDropMenuShadowAlpha = setting("CMenuShadowAlpha", 100, 0, 255).des("Color DropDown Menu Gradient Shadow Alpha").whenTrue(colorDropMenuShadow).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Boolean> colorDropMenuTopBottomGradients = setting("CMenuTopBottomGradients", true).des("Color DropDown Menu Top Bottom Gradients").whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Float> colorDropMenuTopBottomGradientsHeight = setting("CMenuTopBottomGHeight", 40.0f, 0.0f, 50.0f).des("Color DropDown Menu Top Bottom Gradients Height").whenTrue(colorDropMenuTopBottomGradients).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);
    public Setting<Color> colorDropMenuTopBottomGradientsColor = setting("CMenuTopBottomGColor", new Color(new java.awt.Color(0, 0, 0, 185).getRGB())).des("Color DropDown Menu Top Bottom Gradients Color").whenTrue(colorDropMenuTopBottomGradients).whenAtMode(settingsPage, SettingsPage.Color).whenAtMode(page, Page.Settings);

    public Setting<StringInputPage> stringInputPage = setting("StringInputPage", StringInputPage.Normal).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputNameOffset = setting("StringInputNameOffsetY", 4.6f, 0.0f, 8.0f).des("Y offset of string input setting name").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputBoxOutline = setting("StringInputOutline", false).des("Draw an outline on the string input text box").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputBoxOutlineWidth = setting("StringInputOutlineWidth", 1.0f, 1.0f, 3.0f).des("Width of outline on string input text box").whenTrue(stringInputBoxOutline).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputBoxOutlineColor = setting("StringInputOutlineColor", new Color(new java.awt.Color(225, 225, 225, 255).getRGB())).whenTrue(stringInputBoxOutline).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputBoxColor = setting("StringInputBoxColor", new Color(new java.awt.Color(20, 20, 20, 120).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputNameColor = setting("StringInputNameColor", new Color(new java.awt.Color(190, 190, 190, 255).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputValueColor = setting("StringInputValueColor", new Color(new java.awt.Color(120, 120, 120, 255).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputTypingMarkColor = setting("StringInputTypingMarkColor", new Color(new java.awt.Color(170, 170, 170, 255).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputValueScale = setting("StringInputValueScale", 0.8f, 0.1f, 1.0f).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.Normal).whenAtMode(page, Page.Settings);

    public Setting<Float> stringInputCollectorHeight = setting("StrInputCollectorHeight", 80.0f, 1.0f, 500.0f).des("Height of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorMinimumWidth = setting("StrInputCollectorMinWidth", 70.0f, 1.0f, 100.0f).des("Minimum width of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorScrollSpeed = setting("StrInputCollectorScrollSpeed", 5.0f, 0.1f, 10.0f).des("Speed of scrolling string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorReboundFactor = setting("StrInputCollectorReboundFactor", 3.0f, 0.1f, 15.0f).des("How much you can scroll out of bounds").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorReboundSpace = setting("StrInputCollectorReboundSpace", 0.5f, 0.0f, 1.0f).des("Amount of empty space when you've scrolled to either end").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorCloseDelay = setting("StrInputCollectorCloseDelay", 300, 0, 2000).des("Milliseconds to wait for string input collector menu to close after you aren't hovering on the setting").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorTextScale = setting("StrInputCollectorTextScale", 0.8f, 0.1f, 2.0f).des("Scale of text in string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorExtensions = setting("StrInputCollectorExtend", true).des("Adds extra rects to the top and bottom of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorExtensionsHeight = setting("StrInputCollectorExtendHeight", 6.0f, 0.0f, 15.0f).des("Height of extra string input collector menu rects").whenTrue(stringInputCollectorExtensions).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorShadow = setting("StrInputCollectorShadow", true).des("Draw gradient shadow behind string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorShadowAlpha = setting("StrInputCollectorShadowAlpha", 151, 0, 255).des("Alpha of string input collector menu gradient shadow").whenTrue(stringInputCollectorShadow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorShadowSize = setting("StrInputCollectorShadowSize", 0.2f, 0.0f, 1.0f).des("Size factor of string input collector menu gradient shadow").whenTrue(stringInputCollectorShadow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorX = setting("StrInputCollectorX", 5, 0, 50).des("X offset of string input collector menu from setting").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorExtraWidth = setting("StrInputCollectorExtraWidth", 10, 0, 40).des("String input collector menu extra width from text").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorHoverRect = setting("StrInputCollectorHoverRect", true).des("Draw a rect on top of hovered string input collector element").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorAnimate = setting("StrInputCollectorAnimate", true).des("Animate alpha of string input collector menu on toggle").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorAnimateScale = setting("StrInputCollectorAnimateScale", true).des("Animate scale of string input collector menu on toggle").whenTrue(stringInputCollectorAnimate).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorAnimateSpeed = setting("StrInputCollectorAnimateSpeed", 2.0f, 0.4f, 10.0f).des("Speed of string input collector menu toggle animation").whenTrue(stringInputCollectorAnimate).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorAnimateFactor = setting("StrInputCollectorAnimateFactor", 0.5f, 0.1f, 1.0f).des("Steepness of string input collector menu toggle animation").whenTrue(stringInputCollectorAnimate).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorRectGap = setting("StrInputCollectorRectGap", 0, -1, 15).des("Gap between element rects in string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorSideBar = setting("StrInputCollectorSideBar", true).des("Bar on side of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorSideBarWidth = setting("StrInputCollectorSideBarWidth", 1.9f, 1.0f, 5.0f).des("Width of bar on side of string input collector menu").whenTrue(stringInputCollectorSideBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorSideGlow = setting("StrInputCollectorSideGlow", true).des("Glow on side of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorSideGlowWidth = setting("StrInputCollectorSideGlowWidth", 20.0f, 0.0f, 50.0f).des("Width of glow on side of string input collector menu").whenTrue(stringInputCollectorSideGlow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorSideGlowColor = setting("StrInputCollectorSideGlowColor", new Color(new java.awt.Color(122, 43, 255, 101).getRGB())).whenTrue(stringInputCollectorSideGlow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<EnumDropMenuOtherSideGlowMode> stringInputCollectorOtherSideGlow = setting("StrInputCollectorOtherSideGlow", EnumDropMenuOtherSideGlowMode.Right).des("Glow on other side of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorOtherSideGlowWidth = setting("StrInputCollectorOtherSideGlowWidth", 15.0f, 0.0f, 50.0f).des("Width of glow on other side of string input collector menu").when(() -> stringInputCollectorOtherSideGlow.getValue() != EnumDropMenuOtherSideGlowMode.None).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorOtherSideGlowColor = setting("StrInputCollectorOtherSideGlowColor", new Color(new java.awt.Color(255, 255, 255, 31).getRGB())).when(() -> stringInputCollectorOtherSideGlow.getValue() != EnumDropMenuOtherSideGlowMode.None).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorBorderGradients = setting("StrInputCollectorBorderGradients", true).des("Draw gradients on top and bottom string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorBorderGradientHeight = setting("StrInputCollectorBorderGradientHeight", 15.5f, 0.0f, 30.0f).des("Height of gradients on top and bottom string input collector menu").whenTrue(stringInputCollectorBorderGradients).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorBorderGradientColor = setting("StrInputCollectorBorderGradientColor", new Color(new java.awt.Color(0, 0, 0, 178).getRGB())).whenTrue(stringInputCollectorBorderGradients).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorBorderGradients2 = setting("StrInputCollectorBorderGradients2", true).des("Draw gradients on top and bottom string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorBorderGradient2Height = setting("StrInputCollectorBorderGradient2Height", 8.0f, 0.0f, 30.0f).des("Height of gradients on top and bottom string input collector menu").whenTrue(stringInputCollectorBorderGradients2).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorBorderGradient2Color = setting("StrInputCollectorBorderGradient2Color", new Color(new java.awt.Color(0, 0, 0, 178).getRGB())).whenTrue(stringInputCollectorBorderGradients2).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorOutline = setting("StrInputCollectorOutline", false).des("Draw outline on edge of string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorOutlineWidth = setting("StrInputCollectorOutlineWidth", 1.0f, 1.0f, 3.0f).des("Width of outline on edge of string input collector menu").whenTrue(stringInputCollectorOutline).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorOutlineColor = setting("StrInputCollectorOutlineColor", new Color(new java.awt.Color(98, 26, 255, 255).getRGB())).whenTrue(stringInputCollectorOutline).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorRectColor = setting("StrInputCollectorRectColor", new Color(new java.awt.Color(0, 0, 0, 64).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorRectBGColor = setting("StrInputCollectorRectBGColor", new Color(new java.awt.Color(0, 0, 0, 206).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorTextColor = setting("StrInputCollectorTextColor", new Color(new java.awt.Color(182, 182, 182, 209).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputCollectorXXOffset = setting("StrInputCollectorXX", 11, 0, 50).des("X offset of string input collector X button").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorXScale = setting("StrInputCollectorXScale", 0.7f, 0.1f, 2.0f).des("Scale of string input collector X button").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorXColor = setting("StrInputCollectorXColor", new Color(new java.awt.Color(255, 100, 100, 209).getRGB())).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorXGlow = setting("StrInputCollectorXGlow", true).des("Draw a circle glow behind string input collector X button").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorXGlowSize = setting("StrInputCollectorXGlowSize", 12.0f, 0.0f, 15.0f).des("Radius of circle glow behind string input collector X button").whenTrue(stringInputCollectorXGlow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputCollectorXGlowColor = setting("StrInputCollectorXGlowColor", new Color(new java.awt.Color(255, 100, 100, 76).getRGB())).whenTrue(stringInputCollectorXGlow).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorXAnimate = setting("StrInputCollectorXAnimate", true).des("Alpha animation when rendering in string input collector X button on hover").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputCollectorXAnimateScale = setting("StrInputCollectorXAnimateScale", true).des("Scale animation when rendering in string input collector X button on hover").whenTrue(stringInputCollectorXAnimate).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputCollectorXAnimateSpeed = setting("StrInputCollectorXAnimateSpeed", 1.0f, 0.1f, 3.0f).des("String input collector X button animation speed factor").whenTrue(stringInputCollectorXAnimate).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.CollectorMenu).whenAtMode(page, Page.Settings);

    public Setting<Boolean> stringInputScrollBar = setting("StrInputScrollBar", true).des("Draw an interactable scroll bar next to string input collector menu").whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarXOffset = setting("StrInputScrollBarX", 3.0f, 0.0f, 10.0f).des("X offset of string input collector menu scroll bar from the menu").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarBGWidth = setting("StrInputScrollBarBGWidth", 3.0f, 1.0f, 15.0f).des("Width of string input collector menu scroll bar background").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarExtraWidth = setting("StrInputScrollBarExtraWidth", 1.0f, 0.0f, 10.0f).des("Extra of string input collector menu scroll bar from scroll bar background").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarBGRounded = setting("StrInputScrollBarBGRounded", true).des("Use rounded rect for string input collector menu scroll bar background").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarBGRoundedRadius = setting("StrInputScrollBarBGRoundedRadius", 0.8f, 0.0f, 1.0f).des("Radius of rounded rect for string input collector menu scroll bar background").whenTrue(stringInputScrollBarBGRounded).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputScrollBarBGColor = setting("StrInputScrollBarBGColor", new Color(new java.awt.Color(45, 45, 45, 255).getRGB())).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarBGShadow = setting("StrInputScrollBarBGShadow", true).des("Draw gradient shadow under string input collector menu scroll bar background").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarBGShadowSize = setting("StrInputScrollBarBGShadowSize", 0.2f, 0.0f, 1.0f).des("Size of gradient shadow under string input collector menu scroll bar background").whenTrue(stringInputScrollBarBGShadow).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputScrollBarBGShadowAlpha = setting("StrInputScrollBarBGShadowAlpha", 76, 0, 255).des("Alpha of gradient shadow under string input collector menu scroll bar background").whenTrue(stringInputScrollBarBGShadow).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarRounded = setting("StrInputScrollBarRounded", true).des("Use rounded rect for string input collector menu scroll bar").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarRoundedRadius = setting("StrInputScrollBarRoundedRadius", 0.5f, 0.0f, 1.0f).des("Radius of rounded rect for string input collector menu scroll bar").whenTrue(stringInputScrollBarRounded).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputScrollBarColor = setting("StrInputScrollBarColor", new Color(new java.awt.Color(110, 110, 110, 255).getRGB())).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarShadow = setting("StrInputScrollBarShadow", true).des("Draw gradient shadow under string input collector menu scroll bar").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarShadowSize = setting("StrInputScrollBarShadowSize", 0.3f, 0.0f, 1.0f).des("Size of gradient shadow under string input collector menu scroll bar").whenTrue(stringInputScrollBarShadow).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputScrollBarShadowAlpha = setting("StrInputScrollBarShadowAlpha", 89, 0, 255).des("Alpha of gradient shadow under string input collector menu scroll bar").whenTrue(stringInputScrollBarShadow).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarPattern = setting("StrInputScrollBarPattern", true).des("Draw a pattern in the middle of string input collector menu scroll bar").whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternSmallDistance = setting("StrInputPatternSmallDist", 5.0f, 0.0f, 20.0f).des("Minimum distance that the edge of the scroll bar can be away from the scroll bar pattern to have the the pattern change").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<PatternSmallMode> stringInputScrollBarPatternSmallBehavior = setting("StrInputPatternSmallBehavior", PatternSmallMode.Shrink).des("What happens to scroll bar pattern when the bar is smaller than the pattern + specified distance").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Integer> stringInputScrollBarPatternCount = setting("StrInputPatternCount", 3, 1, 6).des("Amount of shapes in string input collector menu scroll bar pattern").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternDist = setting("StrInputPatternDist", 2.55f, 0.1f, 5.0f).des("Distance between shapes in string input collector menu scroll bar pattern").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarPatternRounded = setting("StrInputPatternRounded", false).des("Rounded shapes in string input collector menu scroll bar pattern").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternRoundedRadius = setting("StrInputPatternRadius", 0.8f, 0.0f, 1.0f).des("Radius of rounded shapes in string input collector menu scroll bar pattern").whenTrue(stringInputScrollBarPatternRounded).whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternWidth = setting("StrInputPatternWidth", 0.7f, 0.0f, 1.0f).des("Fraction of scroll bar width to make patten width").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternHeight = setting("StrInputPatternHeight", 1.5f, 0.0f, 5.0f).des("Scroll bar pattern height").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Boolean> stringInputScrollBarPatternRollColors = setting("StrInputPatternRollColors", true).des("Scroll bar pattern rollers between 2 colors").whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternRollSpeed = setting("StrInputPatternRollSpeed", 3.0f, 0.1f, 20.0f).des("Scroll bar pattern roll color speed").whenTrue(stringInputScrollBarPatternRollColors).whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Float> stringInputScrollBarPatternRollSize = setting("StrInputPatternRollSize", 0.3f, 0.1f, 2.0f).des("Scroll bar pattern roll color roll size").whenTrue(stringInputScrollBarPatternRollColors).whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputScrollBarPatternRollColor = setting("StrInputPatternRollColor", new Color(new java.awt.Color(60, 38, 110, 255).getRGB())).whenTrue(stringInputScrollBarPatternRollColors).whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);
    public Setting<Color> stringInputScrollBarPatternColor = setting("StrInputPatternColor", new Color(new java.awt.Color(100, 61, 255, 255).getRGB())).whenTrue(stringInputScrollBarPattern).whenTrue(stringInputScrollBar).whenAtMode(settingsPage, SettingsPage.StringInput).whenAtMode(stringInputPage, StringInputPage.MenuScrollBar).whenAtMode(page, Page.Settings);

    enum Page {
        General, CategoryBars, BorderBars, Shadow, Panel, SideIcon, Extended, Enabled, Outline, Background, Settings
    }

    enum SettingsPage {
        Boolean, Slider, Enum, Visibility, Color, Bind, StringInput
    }

    public enum EnabledSideSide {
        Left, Right
    }

    public enum SideIconMode {
        Dots, Plus, Arrow, Future, None
    }

    public enum EnabledRectMoveMode {
        Left, Right, All
    }

    public enum ModuleSideGlow {
        Left, Right, Both, None
    }

    public enum ModuleSeparatorFadeMode {
        Left, Right, Both, None
    }

    public enum EnableDisableScaleRect {
        Enable, Disable, Both, None
    }

    public enum HoverScaleFadeMode {
        Left, Right, Both, All, None
    }

    public enum SideIconSide {
        Left, Right
    }

    public enum ModuleMiniIconSide {
        Left, Right, HardRight, HardLeft
    }

    public enum ModuleSideGlowDouble {
        Left, Right, None
    }

    public enum PanelExtensions {
        Top, Bottom, Both, None
    }

    public enum BaseRectPattern {
        Triangles, Circles, Diamonds, None
    }

    public enum BaseRectPatternExtra {
        Double, Single
    }

    public enum BaseRectPatternTrianglesSingleExtra {
        Down, Up, Left, Right
    }

    public enum BaseRectPatternOutlineBrightnessRollDirection {
        Left, Right
    }

    public enum BaseRectPatternBrightnessRollDirection {
        Left, Right
    }

    public enum CategoryIconsSides {
        Left, Right
    }

    public enum EnabledTextBrightRollAxis {
        X, Y
    }

    public enum EnabledTextBrightRollDirectionX {
        Left, Right
    }

    public enum EnabledTextBrightRollDirectionY {
        Up, Down
    }

    public enum CategoryRectHoverParticlesMode {
        Circles, Diamonds, Triangles, None
    }

    public enum CategoryRectHoverParticlesScaleFadeMode {
        Alpha, Scale, Both
    }

    public enum EnabledRectAnimation {
        Alpha, Scale, Both, None
    }

    public enum EnabledRectBrightRollAxis {
        X, Y
    }

    public enum EnabledRectBrightRollDirectionX {
        Left, Right
    }

    public enum EnabledRectBrightRollDirectionY {
        Up, Down
    }

    public enum BooleanDotMode {
        Alpha, Scale, Both
    }

    public enum BooleanSwitchTypes {
        SliderRound, SliderNonRound, Circle, Square, None
    }

    public enum BooleanFullRectScaleType {
        Left, Right, All, None
    }

    public enum BindButtonColoredRectAnimateMode {
        Scale, Alpha, Both, None
    }

    public enum KeyBindFancyFont {
        Comfortaa, Arial, Objectivity, Minecraft
    }

    public enum BindButtonWaitingDotsRolledBrightnessDirection {
        Left, Right
    }

    public enum VisibilitySettingMode {
        Text, Symbol, Boolean
    }

    public enum EnumArrowClickAnimationMode {
        Scale, Alpha, None
    }

    public enum EnumDropMenuOtherSideGlowMode {
        Right, Left, Both, None
    }

    public enum EnumDropMenuPage {
        Icon, Selected, Base, ScrollBar
    }

    public enum EnumDropMenuSelectedRectAnimation {
        Alpha, Scale, AlpScale, Slide, None
    }

    public enum EnumDropMenuSelectedSideRectSide {
        Left, Right
    }

    public enum EnumDropMenuSelectedSideGlowSide {
        Left, Right
    }

    public enum ColorDisplayShape {
        Square, Rect, Diamond
    }

    enum StringInputPage {
        Normal,
        CollectorMenu,
        MenuScrollBar
    }

    public enum PatternSmallMode {
        Shrink,
        Disappear,
        None
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            if (!(mc.currentScreen instanceof ClickGUIFinal)) {
                mc.displayGuiScreen(new ClickGUIFinal());
                if (ClickGUI.instance.guiMove.getValue()) {
                    flag = 1;
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (HUDEditor.instance.isEnabled()) {
            ModuleManager.getModule(HUDEditor.class).disable();
        }
    }

    @Override
    public void onDisable() {
        ClickGUIFinal.previousIndex = 0;
        ClickGUIFinal.previousText = "";
        Panel.categoryRectHoverParticlesList.clear();
        Panel.categoryRectHoverParticlesOriginalYs.clear();
        Panel.categoryRectHoverParticlesSpeed.clear();
        Panel.categoryRectHoverParticlesTriAngle.clear();
        Panel.categoryRectHoverParticlesTriSpinSpeed.clear();
        Panel.categoryRectHoverParticlesSize.clear();

        if (!ClickGUI.instance.guiMove.getValue()) {
            if (Particles.INSTANCE.isEnabled())
                ParticleUtil.clearParticles();

            if (mc.currentScreen instanceof ClickGUIFinal)
                mc.displayGuiScreen(null);
        }

        ConfigManager.saveAll();
    }
}
