package net.spartanb312.base.gui.components;

import me.afterdarkness.moloch.core.Rect;
import me.afterdarkness.moloch.gui.components.StringInput;
import me.afterdarkness.moloch.module.modules.client.CustomFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.spartanb312.base.client.FontManager;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.EnumSetting;
import net.spartanb312.base.gui.Panel;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.module.modules.client.ClickGUI;
import net.spartanb312.base.module.modules.client.HUDEditor;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.SoundUtil;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.graphics.RenderUtils2D;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.input.Keyboard.KEY_DOWN;
import static org.lwjgl.input.Keyboard.KEY_UP;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class EnumButton extends net.spartanb312.base.gui.Component {

    EnumSetting<?> setting;

    public static EnumButton instance;
    public Enum<?> lastElement;
    private HashMap<Enum<?>, Rect> menuRects = new HashMap<>();
    boolean flag = false;
    String moduleName;

    public EnumButton(Setting<? extends Enum<?>> setting, int width, int height, Panel father, Module module) {
        this.moduleName = module.name;
        this.width = width;
        this.height = height;
        this.father = father;
        this.setting = (EnumSetting<?>) setting;
        instance = this;
    }


    public static HashMap<String, Integer> storedArrowAnimationLoopsRight = new HashMap<>();
    public static HashMap<String, Integer> storedArrowAnimationLoopsLeft = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuSelectLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuSelectSideRectLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuSelectSideGlowLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuSelectTextLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuExpandRectLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumIconExpandedLoops = new HashMap<>();
    public static HashMap<String, Integer> storedEnumDropMenuOpenCloseAlphaLayerLoops = new HashMap<>();
    public static float lastSelectedRectStartY = -999;
    public static float lastSelectedRectEndY = -999;
    private final Timer animateTimer = new Timer();
    private boolean useMenuHover;
    boolean flag1 = true;
    boolean flag2 = true;
    boolean flag4 = true;
    boolean reverseAnimateFlag = false;
    private boolean isSliding;
    private float animateDelta1;
    private float animateDelta;
    private Rect scissorRect;
    private float scrollOffset;
    private float scrollUpOrDown = 1.0f;
    public float scrollDelta;
    private float prevScrollY;
    private boolean unlockScrollFlag;
    private Rect scrollRect;
    private float reboundDelta;
    private boolean reboundMaxFlag;
    private float reboundDist;

    @Override
    public void render(int mouseX, int mouseY, float translateDelta, float partialTicks) {
        GlStateManager.disableAlpha();

        int passedms = (int) animateTimer.hasPassed();
        animateTimer.reset();

        if (!ClickGUI.instance.enumDropMenu.getValue() || !expanded) {
            menuRects.clear();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || !setting.isVisible()) {
            shouldScroll = true;
            unlockScrollFlag = true;
            animateDelta = 0;
        }

        Color displayTextColor = new Color(ClickGUI.instance.enumDisplayTextColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDisplayTextColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDisplayTextColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDisplayTextColor.getValue().getAlpha());
        Color nameTextColor = new Color(ClickGUI.instance.enumNameColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumNameColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumNameColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumNameColor.getValue().getAlpha());

        float currentTextWidth = font.getStringWidth(setting.displayValue());
        if (CustomFont.instance.font.getValue() == CustomFont.FontMode.Minecraft) {
            GL11.glEnable(GL_TEXTURE_2D);
            GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()), ((int) (y + height / 2 - font.getHeight() / 2f)) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
            GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

            mc.fontRenderer.drawString(setting.getName(), x + 5, (int) (y + height / 2 - font.getHeight() / 2f), nameTextColor.getRGB(), CustomFont.instance.textShadow.getValue());

            GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
            GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, ((int) (y + height / 2 - font.getHeight() / 2f)) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);


            GL11.glTranslatef(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue())) + ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f)) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
            GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

            mc.fontRenderer.drawString(setting.displayValue(),
                    x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), (int) (y + height / 2 - font.getHeight() / 2f),
                    displayTextColor.getRGB(), CustomFont.instance.textShadow.getValue());

            GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
            GL11.glTranslatef(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f) - ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f)) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);
            GL11.glDisable(GL_TEXTURE_2D);
        }
        else {
            if (CustomFont.instance.textShadow.getValue()) {
                GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
                GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

                fontManager.drawShadow(setting.getName(), x + 5, (int) (y + height / 2 - font.getHeight() / 2f) + 3, nameTextColor.getRGB());

                GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
                GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);


                GL11.glTranslatef(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue())) + ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
                GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

                fontManager.drawShadow(setting.displayValue(),
                        x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), (int) (y + height / 2 - font.getHeight() / 2f) + 3,
                        displayTextColor.getRGB());


                GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
                GL11.glTranslatef(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f) - ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);

            }
            else {
                GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
                GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

                fontManager.draw(setting.getName(), x + 5, (int) (y + height / 2 - font.getHeight() / 2f) + 3, nameTextColor.getRGB());

                GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
                GL11.glTranslatef((x + 5) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);


                GL11.glTranslated(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue())) + ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
                GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

                fontManager.draw(setting.displayValue(),
                        x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), (int) (y + height / 2 - font.getHeight() / 2f) + 3,
                        displayTextColor.getRGB());

                GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
                GL11.glTranslated(((x + width - 3 - font.getStringWidth(setting.displayValue()) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f) - ((1.0f - CustomFont.instance.componentTextScale.getValue()) * currentTextWidth), ((int) (y + height / 2 - font.getHeight() / 2f) + 3) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);

            }
        }


        if (ClickGUI.instance.enumDropMenu.getValue()) {

            if (ClickGUI.instance.enumDropMenuIcon.getValue()) {
                Color enumIconColor = new Color(ClickGUI.instance.enumDropMenuIconColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuIconColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuIconColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuIconColor.getValue().getAlpha());

                if (ClickGUI.instance.enumDropMenuIconExpandedChange.getValue()) {
                    Color enumIconExpandedColor = new Color(ClickGUI.instance.enumDropMenuIconExpandedChangedColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuIconExpandedChangedColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuIconExpandedChangedColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuIconExpandedChangedColor.getValue().getAlpha());

                    if (ClickGUI.instance.enumDropMenuIconExpandedChangeAnimation.getValue()) {

                        storedEnumIconExpandedLoops.putIfAbsent(setting.getName(), 0);
                        int animateLoops = storedEnumIconExpandedLoops.get(setting.getName());

                        int red = (int)(MathUtilFuckYou.linearInterp(enumIconColor.getRed(), enumIconExpandedColor.getRed(), animateLoops));
                        int green = (int)(MathUtilFuckYou.linearInterp(enumIconColor.getGreen(), enumIconExpandedColor.getGreen(), animateLoops));
                        int blue = (int)(MathUtilFuckYou.linearInterp(enumIconColor.getBlue(), enumIconExpandedColor.getBlue(), animateLoops));
                        int alpha = (int)(MathUtilFuckYou.linearInterp(ClickGUI.instance.enumDropMenuIconColor.getValue().getAlpha(), ClickGUI.instance.enumDropMenuIconExpandedChangedColor.getValue().getAlpha(), animateLoops));


                        enumIconColor = new Color(red, green, blue, alpha);

                        if (passedms < 1000) {
                            if (expanded) {
                                animateLoops += ClickGUI.instance.enumDropMenuIconExpandedChangeAnimationSpeed.getValue() * passedms / 3.0f;
                            }
                            else {
                                animateLoops -= ClickGUI.instance.enumDropMenuIconExpandedChangeAnimationSpeed.getValue() * passedms / 3.0f;
                            }
                        }


                        if (animateLoops >= 300) {
                            animateLoops = 300;
                        }
                        if (animateLoops <= 0) {
                            animateLoops = 0;
                        }

                        storedEnumIconExpandedLoops.put(setting.getName(), animateLoops);
                    }
                    else {
                        if (expanded) {
                            enumIconColor = enumIconExpandedColor;
                        }
                    }

                    if (ClickGUI.instance.enumDropMenuIconExpandedGlow.getValue()) {
                        GlStateManager.disableAlpha();
                        RenderUtils2D.drawCustomCircle(x + width - 3 - (FontManager.getEnumIconWidth() / 2.0f) - ClickGUI.instance.enumDropMenuIconXOffset.getValue(), y + (height / 2.0f), ClickGUI.instance.enumDropMenuIconExpandedGlowSize.getValue(), new Color(enumIconExpandedColor.getRed(), enumIconExpandedColor.getGreen(), enumIconExpandedColor.getBlue(), ClickGUI.instance.enumDropMenuIconExpandedChangeAnimation.getValue() ? (int)((ClickGUI.instance.enumDropMenuIconExpandedGlowAlpha.getValue() / 300.0f) * storedEnumIconExpandedLoops.get(setting.getName())) : ClickGUI.instance.enumDropMenuIconExpandedGlowAlpha.getValue()).getRGB(), new Color(0, 0, 0, 0).getRGB());
                        GlStateManager.enableAlpha();
                    }
                }


                GL11.glTranslatef((x + width - 3 - (FontManager.getEnumIconWidth() / 2.0f) - ClickGUI.instance.enumDropMenuIconXOffset.getValue()) * (1.0f - ClickGUI.instance.enumDropMenuIconScale.getValue()), (y + (height / 2.0f)) * (1.0f - ClickGUI.instance.enumDropMenuIconScale.getValue()), 0.0f);
                GL11.glScalef(ClickGUI.instance.enumDropMenuIconScale.getValue(), ClickGUI.instance.enumDropMenuIconScale.getValue(), 0.0f);

                FontManager.drawEnumIcon(x + width - 3 - FontManager.getEnumIconWidth() - ClickGUI.instance.enumDropMenuIconXOffset.getValue(), (int)(y + (height / 2.0f) - (FontManager.getIconHeight() / 4.0f)), enumIconColor.getRGB());

                GL11.glScalef(1.0f / ClickGUI.instance.enumDropMenuIconScale.getValue(), 1.0f / ClickGUI.instance.enumDropMenuIconScale.getValue(), 0.0f);
                GL11.glTranslatef((x + width - 3 - (FontManager.getEnumIconWidth() / 2.0f) - ClickGUI.instance.enumDropMenuIconXOffset.getValue()) * (1.0f - ClickGUI.instance.enumDropMenuIconScale.getValue()) * -1.0f, (y + (height / 2.0f)) * (1.0f - ClickGUI.instance.enumDropMenuIconScale.getValue()) * -1.0f, 0.0f);
            }

            if (ClickGUI.instance.isDisabled() && HUDEditor.instance.isDisabled()) {
                if (ClickGUI.instance.enumDropMenuExpandAnimate.getValue()) {
                    storedEnumDropMenuOpenCloseAlphaLayerLoops.put(setting.getName(), 0);
                }

                expanded = false;
                anyExpanded = false;
            }

            if (expanded) {
                float startX = x + width + ClickGUI.instance.enumDropMenuXOffset.getValue();
                float endX = x + width + ClickGUI.instance.enumDropMenuXOffset.getValue() + Math.max(ClickGUI.instance.enumDropMenuMinimumWidth.getValue(), ClickGUI.instance.enumDropMenuWidthFactor.getValue() * 2 + setting.getLongestElementLength());

                if (!reverseAnimateFlag) {
                    expandedX = startX;
                    expandedY = y;
                    expandedEndX = endX + (ClickGUI.instance.enumDropMenuScrollBar.getValue() ? (ClickGUI.instance.enumDropMenuScrollBarXOffset.getValue() + ClickGUI.instance.enumDropMenuScrollBarBGWidth.getValue() + ClickGUI.instance.enumDropMenuScrollBarExtraWidth.getValue() * 0.5f) : 0.0f);
                    expandedEndY = y + ClickGUI.instance.enumDropMenuHeight.getValue();
                }

                if (flag1) {
                    lastElement = setting.getValue();
                    flag1 = false;
                }

                storedEnumDropMenuSelectLoops.putIfAbsent(setting.getName(), 0);
                if (ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() != ClickGUI.EnumDropMenuSelectedRectAnimation.None) {
                    int animateLoops = storedEnumDropMenuSelectLoops.get(setting.getName());

                    if (passedms < 1000) {
                        animateLoops += ClickGUI.instance.enumDropMenuSelectedRectAnimationSpeed.getValue() * passedms / 3.0f;
                    }

                    if (animateLoops >= 300) {
                        animateLoops = 300;
                    }
                    storedEnumDropMenuSelectLoops.put(setting.getName(), animateLoops);
                }

                if (scissorRect != null && scissorRect.isHovered(mouseX, mouseY)) {
                    shouldScroll = false;
                    unlockScrollFlag = false;
                }
                else if (!unlockScrollFlag) {
                    shouldScroll = true;
                    unlockScrollFlag = true;
                }

                drawListComponents(mouseX, mouseY, startX, endX, passedms, translateDelta);
            }
            else {
                menuRects.clear();
                shouldScroll = true;
                isSliding = false;
            }

            if (ClickGUI.instance.enumDropMenuExpandAnimate.getValue() && reverseAnimateFlag && animateDelta <= 0.0f) {
                expanded = false;
            }
        }
        else {

            //arrows
            if (ClickGUI.instance.enumLoopModeArrows.getValue()) {
                Color arrowsColor = new Color(ClickGUI.instance.enumArrowColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumArrowColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumArrowColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumArrowColor.getValue().getAlpha());

                //left arrow
                RenderUtils2D.drawTriangle(x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) - (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f), x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) + (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), arrowsColor.getRGB());
                //right arrow
                RenderUtils2D.drawTriangle(x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() + ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f), x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) - (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) + (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), arrowsColor.getRGB());

                if (ClickGUI.instance.enumArrowClickAnimationMode.getValue() != ClickGUI.EnumArrowClickAnimationMode.None) {

                    storedArrowAnimationLoopsRight.putIfAbsent(setting.getName(), 0);
                    int animateLoops = storedArrowAnimationLoopsRight.get(setting.getName());

                    if (isHovered(mouseX, mouseY, x, y, x + width, y + height) && Mouse.getEventButton() == 0 && Mouse.isButtonDown(0)) {
                        animateLoops = 0;
                    }

                    if (passedms < 1000) {
                        animateLoops += ClickGUI.instance.enumArrowClickAnimationFactor.getValue() * passedms / 3.0f;
                    }

                    if (animateLoops >= 300) {
                        animateLoops = 300;
                    }
                    if (animateLoops <= 0) {
                        animateLoops = 0;
                    }
                    storedArrowAnimationLoopsRight.put(setting.getName(), animateLoops);


                    storedArrowAnimationLoopsLeft.putIfAbsent(setting.getName(), 0);
                    int animateLoops2 = storedArrowAnimationLoopsLeft.get(setting.getName());

                    if (isHovered(mouseX, mouseY, x, y, x + width, y + height) && Mouse.getEventButton() == 1 && Mouse.isButtonDown(1)) {
                        animateLoops2 = 0;
                    }

                    if (passedms < 1000) {
                        animateLoops2 += ClickGUI.instance.enumArrowClickAnimationFactor.getValue() * passedms / 3.0f;
                    }

                    if (animateLoops2 >= 300) {
                        animateLoops2 = 300;
                    }
                    if (animateLoops2 <= 0) {
                        animateLoops2 = 0;
                    }
                    storedArrowAnimationLoopsLeft.put(setting.getName(), animateLoops2);

                    Color arrowsColorLeft = new Color(arrowsColor.getRed(), arrowsColor.getGreen(), arrowsColor.getBlue(), ClickGUI.instance.enumArrowClickAnimationMaxAlpha.getValue() - (int)((ClickGUI.instance.enumArrowClickAnimationMaxAlpha.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName())));
                    Color arrowsColorRight = new Color(arrowsColor.getRed(), arrowsColor.getGreen(), arrowsColor.getBlue(), ClickGUI.instance.enumArrowClickAnimationMaxAlpha.getValue() - (int)((ClickGUI.instance.enumArrowClickAnimationMaxAlpha.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName())));


                    if (ClickGUI.instance.enumArrowClickAnimationMode.getValue() == ClickGUI.EnumArrowClickAnimationMode.Scale) {
                        GL11.glTranslatef((x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() + (ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() / 2.0f) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName()))), (y + (height / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName()))), 0.0f);
                        GL11.glScalef(((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName())), ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName())), 0.0f);
                    }

                    RenderUtils2D.drawTriangle(x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() + ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f), x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) - (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) + (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), arrowsColorRight.getRGB());

                    if (ClickGUI.instance.enumArrowClickAnimationMode.getValue() == ClickGUI.EnumArrowClickAnimationMode.Scale) {
                        GL11.glScalef(1.0f / ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName())), 1.0f / ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName())), 0.0f);
                        GL11.glTranslatef((x + width + ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() + (ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() / 2.0f) - ClickGUI.instance.enumLoopModeTextXOffset.getValue()) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName()))) * -1.0f, (y + (height / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsRight.get(setting.getName()))) * -1.0f, 0.0f);
                    }


                    if (ClickGUI.instance.enumArrowClickAnimationMode.getValue() == ClickGUI.EnumArrowClickAnimationMode.Scale) {
                        GL11.glTranslatef((x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue() - (ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName()))), (y + (height / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName()))), 0.0f);
                        GL11.glScalef(((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName())), ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName())), 0.0f);
                    }

                    RenderUtils2D.drawTriangle(x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) - (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f), x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue(), y + (height / 2.0f) + (ClickGUI.instance.enumLoopModeArrowsScaleY.getValue() / 2.0f), arrowsColorLeft.getRGB());

                    if (ClickGUI.instance.enumArrowClickAnimationMode.getValue() == ClickGUI.EnumArrowClickAnimationMode.Scale) {
                        GL11.glScalef(1.0f / ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName())), 1.0f / ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName())), 0.0f);
                        GL11.glTranslatef((x + width - 7 - (font.getStringWidth(setting.displayValue()) * CustomFont.instance.componentTextScale.getValue()) - ClickGUI.instance.enumLoopModeArrowsXOffset.getValue() - ClickGUI.instance.enumLoopModeTextXOffset.getValue() - (ClickGUI.instance.enumLoopModeArrowsScaleX.getValue() / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName()))) * -1.0f, (y + (height / 2.0f)) * (1.0f - ((ClickGUI.instance.enumArrowClickAnimationMaxScale.getValue() / 300.0f) * storedArrowAnimationLoopsLeft.get(setting.getName()))) * -1.0f, 0.0f);
                    }

                }

            }
        }


        GlStateManager.enableAlpha();
    }


    @Override
    public void bottomRender(int mouseX, int mouseY, boolean lastSetting, boolean firstSetting, float partialTicks) {
        GlStateManager.disableAlpha();
        drawSettingRects(lastSetting, false);

        drawExtendedGradient(lastSetting, false);
        drawExtendedLine(lastSetting);

        useMenuHover = false;
        renderHoverRect(moduleName + setting.getName(), mouseX, mouseY, x, y, x + width, y + height, 2.0f, -1.0f, false);

        GlStateManager.enableAlpha();
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY, float startX, float startY, float endX, float endY) {
        if (useMenuHover) {
            return RenderUtils2D.isMouseInRect(mouseX, mouseY, startX, startY, endX, endY);
        }
        else {
            return RenderUtils2D.isMouseInRect(mouseX, mouseY, startX, startY, endX, endY - 1)
                    && (!anyExpanded || !RenderUtils2D.isMouseInRect(mouseX, mouseY, expandedX, expandedY, expandedEndX, expandedEndY))
                    && Panel.isHighestHovered(mouseX, mouseY, father);
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (ClickGUI.instance.enumDropMenu.getValue() && expanded) {
            Enum<?> hoveredEnum = getEnumHovered(mouseX, mouseY);
            if (hoveredEnum == null) {
                if (!(ClickGUI.instance.enumDropMenuScrollBar.getValue() && scrollRect != null && scrollRect.isHovered(mouseX, mouseY)) && !scissorRect.isHovered(mouseX, mouseY)) {
                    isSliding = false;
                    anyExpanded = false;
                    if (ClickGUI.instance.enumDropMenuExpandAnimate.getValue()) {
                        reverseAnimateFlag = true;
                    }
                    else {
                        expanded = false;
                    }
                }
            } else {
                setting.setByName(hoveredEnum.name());
                SoundUtil.playButtonClick();
            }

            if (scrollRect != null && scrollRect.isHovered(mouseX, mouseY)) {
                isSliding = true;
            }
        }

        if (!isHovered(mouseX, mouseY, x, y, x + width, y + height) || !setting.isVisible()) return;

        if (ClickGUI.instance.enumDropMenu.getValue()) {
            if (!anyExpanded && !expanded) {
                reverseAnimateFlag = false;
                anyExpanded = true;
                expanded = true;
            }
        }
        else {
            if (mouseButton == 0) {
                setting.forwardLoop();
                SoundUtil.playButtonClick();
            }
            else if (mouseButton == 1) {
                setting.backwardLoop();
                SoundUtil.playButtonClick();
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        isSliding = false;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    @Override
    public String getDescription() {
        return setting.getDescription();
    }

    @Override
    public Setting<?> getSetting() {
        return setting;
    }

    private void drawListComponents(int mouseX, int mouseY, float startX, float endX, int passedms, float translateDelta) {
        animateDelta = MathUtilFuckYou.clamp(animateDelta + ((reverseAnimateFlag ? -1.0f : 1.0f) * ClickGUI.instance.enumDropMenuExpandAnimateSpeed.getValue() * (passedms < 1000 ? passedms : 1.0f) / 3.0f), 0.0f, 300.0f);
        animateDelta1 = MathUtilFuckYou.interpNonLinear(0.0f, 300.0f, animateDelta / 300.0f, ClickGUI.instance.enumDropMenuExpandAnimateFactor.getValue());
        Color rectBGColor = new Color(ClickGUI.instance.enumDropMenuRectBGColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuRectBGColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuRectBGColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuRectBGColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuRectBGColor.getValue().getAlpha());
        Color rectColor = new Color(ClickGUI.instance.enumDropMenuRectColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuRectColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuRectColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuRectColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuRectColor.getValue().getAlpha());
        Color outlineColor = new Color(ClickGUI.instance.enumDropMenuOutlineColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuOutlineColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuOutlineColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuOutlineColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuOutlineColor.getValue().getAlpha());

        //change y of menu on mousewheel interaction
        scrollMenu(passedms);

        GL11.glPushMatrix();
        if (ClickGUI.instance.enumDropMenuExpandAnimateScale.getValue() && ClickGUI.instance.enumDropMenuExpandAnimate.getValue()) {
            GL11.glTranslatef((x + width + ClickGUI.instance.enumDropMenuXOffset.getValue()) * (1.0f - (animateDelta1 / 300.0f)), y * (1.0f - (animateDelta1 / 300.0f)), 0.0f);
            GL11.glScalef(animateDelta1 / 300.0f, animateDelta1 / 300.0f, 0.0f);
        }

        //top bottom extensions
        if (ClickGUI.instance.enumDropMenuExtensions.getValue()) {
            RenderUtils2D.drawRect(startX, y - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue(), endX, y, rectBGColor.getRGB());

            RenderUtils2D.drawRect(startX, y + ClickGUI.instance.enumDropMenuHeight.getValue(), endX, y + ClickGUI.instance.enumDropMenuHeight.getValue() + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue(), rectBGColor.getRGB());
        }

        //base rect
        RenderUtils2D.drawRect(startX, y, endX, y + ClickGUI.instance.enumDropMenuHeight.getValue(), rectBGColor.getRGB());

        //gradient shadow
        if (ClickGUI.instance.enumDropMenuShadow.getValue()) {
            RenderUtils2D.drawBetterRoundRectFade(startX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y + ClickGUI.instance.enumDropMenuHeight.getValue() + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y + ClickGUI.instance.enumDropMenuHeight.getValue(),
                    ClickGUI.instance.enumDropMenuShadowSize.getValue(),
                    40.0f,
                    false,
                    false,
                    false,
                    new Color(0, 0, 0, ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuShadowAlpha.getValue()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuShadowAlpha.getValue()).getRGB());
        }

        //other side glow
        if (ClickGUI.instance.enumDropMenuOtherSideGlow.getValue() != ClickGUI.EnumDropMenuOtherSideGlowMode.None) {
            me.afterdarkness.moloch.core.common.Color otherSideGlowColor1 = ClickGUI.instance.enumDropMenuOtherSideGlowColor.getValue();
            Color otherSideGlowColor = new Color(otherSideGlowColor1.getRed(), otherSideGlowColor1.getGreen(), otherSideGlowColor1.getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((otherSideGlowColor1.getAlpha()) / 300.0f) * animateDelta1) : otherSideGlowColor1.getAlpha());

            GlStateManager.disableAlpha();
            if (ClickGUI.instance.enumDropMenuOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Right || ClickGUI.instance.enumDropMenuOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Both) {
                RenderUtils2D.drawCustomRect(endX - ClickGUI.instance.enumDropMenuOtherSideGlowWidth.getValue(),
                        y,
                        endX,
                        y + ClickGUI.instance.enumDropMenuHeight.getValue(),
                        otherSideGlowColor.getRGB(),
                        new Color(0, 0, 0, 0).getRGB(),
                        new Color(0, 0, 0, 0).getRGB(),
                        otherSideGlowColor.getRGB());
            }
            if (ClickGUI.instance.enumDropMenuOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Left || ClickGUI.instance.enumDropMenuOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Both) {
                RenderUtils2D.drawCustomRect(startX,
                        y,
                        startX + ClickGUI.instance.enumDropMenuOtherSideGlowWidth.getValue(),
                        y + ClickGUI.instance.enumDropMenuHeight.getValue(),
                        new Color(0, 0, 0, 0).getRGB(),
                        otherSideGlowColor.getRGB(),
                        otherSideGlowColor.getRGB(),
                        new Color(0, 0, 0, 0).getRGB());
            }
            GlStateManager.enableAlpha();
        }

        //individual rect stuff
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        scissorRect = new Rect(startX,
                (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y,
                endX,
                (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y + ClickGUI.instance.enumDropMenuHeight.getValue());
        RenderUtils2D.betterScissor(startX,
                (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y,
                (endX - startX) * (ClickGUI.instance.enumDropMenuExpandAnimateScale.getValue() && ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? animateDelta1 / 300.0f : 1.0f),
                ClickGUI.instance.enumDropMenuHeight.getValue() * (ClickGUI.instance.enumDropMenuExpandAnimateScale.getValue() && ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? animateDelta1 / 300.0f : 1.0f));
        GL11.glEnable(GL_SCISSOR_TEST);

        drawElements(mouseX, mouseY, passedms, startX, endX, rectColor);

        GL11.glDisable(GL_SCISSOR_TEST);

        //selected rect expand
        if (ClickGUI.instance.enumDropMenuSelectedRectScaleOut.getValue()) {
            Color expandedRectColor = new Color(ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getAlpha());
            int menuY = y + (int)scrollOffset + height;
            int index = 0;

            RenderUtils2D.betterScissor(0,
                    (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y,
                    mc.displayWidth,
                    ClickGUI.instance.enumDropMenuHeight.getValue() * (ClickGUI.instance.enumDropMenuExpandAnimateScale.getValue() && ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? animateDelta1 / 300.0f : 1.0f));
            GL11.glEnable(GL_SCISSOR_TEST);

            for (Enum<?> element : setting.getValue().getDeclaringClass().getEnumConstants()) {
                float startY = menuY + height * index;
                float endY = menuY + height + height * index;

                storedEnumDropMenuExpandRectLoops.putIfAbsent(setting.getName() + element.name(), 300);

                expandedRectColor = new Color(expandedRectColor.getRed(), expandedRectColor.getGreen(), expandedRectColor.getBlue(), (int)((ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getAlpha() / 300.0f) * storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name()) * -1.0f) + ClickGUI.instance.enumDropMenuSelectedRectScaleOutColor.getValue().getAlpha());

                int animateLoops = storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name());

                if (passedms < 1000) {
                    animateLoops += ClickGUI.instance.enumDropMenuSelectedRectScaleOutFactor.getValue() * passedms / 3.0f;
                }

                if (animateLoops >= 300) {
                    animateLoops = 300;
                }

                storedEnumDropMenuExpandRectLoops.put(setting.getName() + element.name(), animateLoops);

                GlStateManager.disableAlpha();
                RenderUtils2D.drawRect(startX + 1 - ((ClickGUI.instance.enumDropMenuSelectedRectScaleMaxScale.getValue() / 300.0f) * storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name())), startY + 1 - ((ClickGUI.instance.enumDropMenuSelectedRectScaleMaxScale.getValue() / 300.0f) * storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name())), endX - 1 + ((ClickGUI.instance.enumDropMenuSelectedRectScaleMaxScale.getValue() / 300.0f) * storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name())), endY + ((ClickGUI.instance.enumDropMenuSelectedRectScaleMaxScale.getValue() / 300.0f) * storedEnumDropMenuExpandRectLoops.get(setting.getName() + element.name())), expandedRectColor.getRGB());
                GlStateManager.enableAlpha();

                index++;
            }

            GL11.glDisable(GL_SCISSOR_TEST);
        }

        //top rects of menu extensions
        if (ClickGUI.instance.enumDropMenuExtensions.getValue()) {
            RenderUtils2D.drawRect(startX + 1, y - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() + 1, endX - 1, y, rectColor.getRGB());

            RenderUtils2D.drawRect(startX + 1, y + ClickGUI.instance.enumDropMenuHeight.getValue(), endX - 1, y + ClickGUI.instance.enumDropMenuHeight.getValue() + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() - 1, rectColor.getRGB());
        }

        //interior border gradients
        if (ClickGUI.instance.enumDropMenuInteriorGradients.getValue()) {
            me.afterdarkness.moloch.core.common.Color borderGradientColor = ClickGUI.instance.enumDropMenuInteriorGradientColor.getValue();
            Color gradientColor = new Color(borderGradientColor.getRed(), borderGradientColor.getGreen(), borderGradientColor.getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((borderGradientColor.getAlpha()) / 300.0f) * animateDelta1) : borderGradientColor.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(startX,
                    y,
                    endX,
                    y + ClickGUI.instance.enumDropMenuInteriorGradientHeight.getValue(),
                    gradientColor.getRGB(),
                    gradientColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB());

            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.enumDropMenuHeight.getValue() + y - ClickGUI.instance.enumDropMenuInteriorGradientHeight.getValue(),
                    endX,
                    ClickGUI.instance.enumDropMenuHeight.getValue() + y + 1,
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    gradientColor.getRGB(),
                    gradientColor.getRGB());
            GlStateManager.enableAlpha();
        }

        //side glow
        if (ClickGUI.instance.enumDropMenuSideGlow.getValue()) {
            me.afterdarkness.moloch.core.common.Color sideGlowColor1 = ClickGUI.instance.enumDropMenuSideGlowColor.getValue();
            Color sideGlowColor = new Color(sideGlowColor1.getRed(), sideGlowColor1.getGreen(), sideGlowColor1.getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((sideGlowColor1.getAlpha()) / 300.0f) * animateDelta1) : sideGlowColor1.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(
                    startX,
                    y - (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f),
                    startX + ClickGUI.instance.enumDropMenuSideGlowWidth.getValue(),
                    y + ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f),
                    new Color(0, 0, 0, 0).getRGB(),
                    sideGlowColor.getRGB(),
                    sideGlowColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB());
            GlStateManager.enableAlpha();
        }

        //sidebar
        if (ClickGUI.instance.enumDropMenuSideBar.getValue()) {
            RenderUtils2D.drawCustomLine(startX + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 4.0f),
                    y - (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f),
                    startX + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 4.0f),
                    y + ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f),
                    ClickGUI.instance.enumDropMenuSideBarWidth.getValue(),
                    outlineColor.getRGB(),
                    outlineColor.getRGB());
        }

        //border gradients
        if (ClickGUI.instance.enumDropMenuTopBottomGradients.getValue()) {
            me.afterdarkness.moloch.core.common.Color borderGradientColor = ClickGUI.instance.enumDropMenuTopBottomGradientsColor.getValue();
            Color topBottomGradientColor = new Color(borderGradientColor.getRed(), borderGradientColor.getGreen(), borderGradientColor.getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((borderGradientColor.getAlpha()) / 300.0f) * animateDelta1) : borderGradientColor.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y + ClickGUI.instance.enumDropMenuTopBottomGradientsHeight.getValue() - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y + ClickGUI.instance.enumDropMenuTopBottomGradientsHeight.getValue(),
                    topBottomGradientColor.getRGB(),
                    topBottomGradientColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB());

            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? y - ClickGUI.instance.enumDropMenuTopBottomGradientsHeight.getValue() + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y - ClickGUI.instance.enumDropMenuTopBottomGradientsHeight.getValue()),
                    endX,
                    ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? y + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    topBottomGradientColor.getRGB(),
                    topBottomGradientColor.getRGB());
            GlStateManager.enableAlpha();
        }

        //outlines
        if (ClickGUI.instance.enumDropMenuOutline.getValue()) {
            RenderUtils2D.drawRectOutline(startX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y - ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.enumDropMenuExtensions.getValue() ? y + ClickGUI.instance.enumDropMenuHeight.getValue() + ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : y + ClickGUI.instance.enumDropMenuHeight.getValue(),
                    ClickGUI.instance.enumDropMenuOutlineWidth.getValue(),
                    outlineColor.getRGB(),
                    false,
                    false);
        }

        //draw scrollbar
        if (ClickGUI.instance.enumDropMenuScrollBar.getValue()) {
            clickScroll(mouseY);
            scrollRect = new Rect(endX + ClickGUI.instance.enumDropMenuScrollBarXOffset.getValue() - ClickGUI.instance.enumDropMenuScrollBarExtraWidth.getValue() * 0.5f,
                    y - (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f),
                    endX + ClickGUI.instance.enumDropMenuScrollBarBGWidth.getValue() + ClickGUI.instance.enumDropMenuScrollBarXOffset.getValue() + ClickGUI.instance.enumDropMenuScrollBarExtraWidth.getValue() * 0.5f,
                    y + ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f));
            StringInput.drawScrollBar(scrollOffset, setting.getValue().getDeclaringClass().getEnumConstants().length, height, endX, y, animateDelta1, ClickGUI.instance.enumDropMenuExtensions.getValue(), ClickGUI.instance.enumDropMenuExtensionsHeight.getValue(), ClickGUI.instance.enumDropMenuHeight.getValue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue(), ClickGUI.instance.enumDropMenuReboundSpace.getValue(),
                    ClickGUI.instance.enumDropMenuScrollBarPattern.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGShadow.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGShadowSize.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGShadowAlpha.getValue(), ClickGUI.instance.enumDropMenuScrollBarShadow.getValue(), ClickGUI.instance.enumDropMenuScrollBarShadowSize.getValue(), ClickGUI.instance.enumDropMenuScrollBarShadowAlpha.getValue(), ClickGUI.instance.enumDropMenuScrollBarXOffset.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGWidth.getValue(), ClickGUI.instance.enumDropMenuScrollBarExtraWidth.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGRounded.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGRoundedRadius.getValue(), ClickGUI.instance.enumDropMenuScrollBarRounded.getValue(), ClickGUI.instance.enumDropMenuScrollBarRoundedRadius.getValue(), ClickGUI.instance.enumDropMenuScrollBarBGColor.getValue().getColorColor(), ClickGUI.instance.enumDropMenuScrollBarColor.getValue().getColorColor(), ClickGUI.instance.enumDropMenuScrollBarBGColor.getValue().getAlpha(), ClickGUI.instance.enumDropMenuScrollBarColor.getValue().getAlpha(),
                    ClickGUI.instance.enumDropMenuScrollBarPatternSmallDistance.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternDist.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternCount.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternSmallBehavior.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRounded.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRoundedRadius.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternWidth.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternHeight.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRollColors.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternColor.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRollColor.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRollSpeed.getValue(), ClickGUI.instance.enumDropMenuScrollBarPatternRollSize.getValue());
        }

        GL11.glPopMatrix();
    }

    private void drawElements(int mouseX, int mouseY, int passedms, float startX, float endX, Color rectColor) {
        Color selectedSideSideGlowColor = new Color(ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getRed(), ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getGreen(), ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getAlpha());
        Color selectedSideRectColor = new Color(ClickGUI.instance.enumDropMenuSelectedSideRectColor.getValue().getRed(), ClickGUI.instance.enumDropMenuSelectedSideRectColor.getValue().getGreen(), ClickGUI.instance.enumDropMenuSelectedSideRectColor.getValue().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuSelectedSideRectColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuSelectedSideRectColor.getValue().getAlpha());
        Color selectedRectColor = new Color(ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getRed(), ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getGreen(), ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getBlue(), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getAlpha());
        float textAnimateDelta = MathUtilFuckYou.clamp(animateDelta1, 4.0f, 300.0f);
        Enum<?>[] elements = setting.getValue().getDeclaringClass().getEnumConstants();
        HashMap<Enum<?>, Rect> menuRectsTemp = new HashMap<>();
        int index = 0;
        int menuY = y + (int)scrollOffset + (int)(height * ClickGUI.instance.enumDropMenuReboundSpace.getValue());
        float selectedRectStartY = 0;
        float selectedRectEndY = 0;

        for (Enum<?> element : elements) {
            float startY = menuY + height * index;
            float endY = menuY + height + height * index;
            if (element == setting.getValue()) {
                selectedRectStartY = startY + 1;
                selectedRectEndY = (index == elements.length - 1) ? (endY - 1 - ClickGUI.instance.enumDropMenuRectGap.getValue()) : (endY - ClickGUI.instance.enumDropMenuRectGap.getValue());
            }

            //top rect
            RenderUtils2D.drawRect(startX + 1, startY + 1, endX - 1, (index == elements.length - 1) ? (endY - 1 - ClickGUI.instance.enumDropMenuRectGap.getValue()) : (endY - ClickGUI.instance.enumDropMenuRectGap.getValue()), rectColor.getRGB());

            //hover rect
            if (ClickGUI.instance.enumDropMenuRectHover.getValue()) {
                useMenuHover = true;
                renderHoverRect(moduleName + setting.getName() + element.name(), mouseX, mouseY, startX + 1, startY + 1, endX, (index == elements.length - 1) ? (endY - 1 - ClickGUI.instance.enumDropMenuRectGap.getValue()) : (endY - ClickGUI.instance.enumDropMenuRectGap.getValue()), 1.0f, 0.0f, false);
            }

            //detect change select shit
            if (lastElement != setting.getValue()) {
                if (!flag) {
                    storedEnumDropMenuSelectLoops.put(setting.getName(), 0);
                    storedEnumDropMenuExpandRectLoops.put(setting.getName() + setting.getValue().name(), 1);
                    flag = true;
                }


                if (element == lastElement) {
                    lastSelectedRectStartY = startY - 1;
                    lastSelectedRectEndY = (index == elements.length - 1) ? (endY - 1 - ClickGUI.instance.enumDropMenuRectGap.getValue()) : (endY - ClickGUI.instance.enumDropMenuRectGap.getValue());
                    lastElement = setting.getValue();

                    flag = false;
                }
                else {
                    lastSelectedRectStartY = selectedRectStartY;
                    lastSelectedRectEndY = selectedRectEndY;
                }
            }

            menuRectsTemp.put(element, new Rect(startX + 1, startY + 1, endX, (index == elements.length - 1) ? (endY - 1 - ClickGUI.instance.enumDropMenuRectGap.getValue()) : (endY - ClickGUI.instance.enumDropMenuRectGap.getValue())));
            index++;
        }
        menuRects = menuRectsTemp;


        //slide shit
        if (ClickGUI.instance.enumDropMenuSelectedRect.getValue() && ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.Slide) {
            if (lastSelectedRectStartY == -999 || (lastSelectedRectStartY != selectedRectEndY && flag4)) {
                lastSelectedRectStartY = selectedRectStartY;
                flag4 = false;
            }
            if (lastSelectedRectEndY == -999 || (lastSelectedRectEndY != selectedRectEndY && flag2)) {
                lastSelectedRectEndY = selectedRectEndY;
                flag2 = false;
            }


            float tempStartY = (lastSelectedRectStartY + (storedEnumDropMenuSelectLoops.get(setting.getName()) * ((selectedRectStartY - lastSelectedRectStartY) / 300.0f)));
            float tempEndY = (lastSelectedRectEndY + (storedEnumDropMenuSelectLoops.get(setting.getName()) * ((selectedRectEndY - lastSelectedRectEndY) / 300.0f)));

            if (ClickGUI.instance.enumDropMenuSelectedRectRounded.getValue()) {
                RenderUtils2D.drawRoundedRect(startX + (ClickGUI.instance.enumDropMenuSideBar.getValue() ? (1 + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 2.0f) + 0.5f) : (ClickGUI.instance.enumDropMenuOutline.getValue() ? (1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f) : 1)), tempStartY, ClickGUI.instance.enumDropMenuSelectedRectRoundedRadius.getValue(), endX - (ClickGUI.instance.enumDropMenuOutline.getValue() ? 1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f : 1), tempEndY, false, true, true, true, true, selectedRectColor.getRGB());
            }
            else {
                RenderUtils2D.drawRect(startX + (ClickGUI.instance.enumDropMenuSideBar.getValue() ? (1 + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 2.0f) + 0.5f) : (ClickGUI.instance.enumDropMenuOutline.getValue() ? (1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f) : 1)), tempStartY, endX - (ClickGUI.instance.enumDropMenuOutline.getValue() ? 1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f : 1), tempEndY, selectedRectColor.getRGB());
            }
        }

        index = 0;
        for (Enum<?> element : elements) {
            int textColor = getTextColor(element, passedms);
            textColor = new Color(ColorUtil.getRed(textColor), ColorUtil.getGreen(textColor), ColorUtil.getBlue(textColor), ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)((textAnimateDelta / 300.0f) * ColorUtil.getAlpha(textColor)) : ColorUtil.getAlpha(textColor)).getRGB();
            float startY = menuY + height * index;
            float endY = menuY + height + height * index;

            selectedElementShit(element, startX, startY, endX, endY, selectedRectStartY, selectedRectEndY, passedms,
                    selectedRectColor, selectedSideRectColor, selectedSideSideGlowColor);

            //text
            drawText(element.name(), mc, setting.getLongestElementLength(), x, menuY, width, height, height * index, textColor,
                    ClickGUI.instance.enumDropMenuTextScale.getValue(), ClickGUI.instance.enumDropMenuXOffset.getValue(), ClickGUI.instance.enumDropMenuWidthFactor.getValue(), ClickGUI.instance.enumDropMenuMinimumWidth.getValue());

            index++;
        }
    }

    private void selectedElementShit(Enum<?> element, float startX, float startY, float endX, float endY, float selectedRectStartY, float selectedRectEndY, int passedms,
                                     Color selectedRectColor, Color selectedSideRectColor, Color selectedSideSideGlowColor) {
        //other full rect shit
        if (ClickGUI.instance.enumDropMenuSelectedRect.getValue() && ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() != ClickGUI.EnumDropMenuSelectedRectAnimation.Slide) {
            if (ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.None) {
                if (element == setting.getValue()) {
                    if (ClickGUI.instance.enumDropMenuSelectedRectRounded.getValue()) {
                        RenderUtils2D.drawRoundedRect(startX + (ClickGUI.instance.enumDropMenuSideBar.getValue() ? (1 + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 2.0f) + 0.5f) : (ClickGUI.instance.enumDropMenuOutline.getValue() ? (1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f) : 1)), selectedRectStartY, ClickGUI.instance.enumDropMenuSelectedRectRoundedRadius.getValue(), endX - (ClickGUI.instance.enumDropMenuOutline.getValue() ? 1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f : 1), selectedRectEndY, false, true, true, true, true, selectedRectColor.getRGB());
                    }
                    else {
                        RenderUtils2D.drawRect(startX + (ClickGUI.instance.enumDropMenuSideBar.getValue() ? (1 + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 2.0f) + 0.5f) : (ClickGUI.instance.enumDropMenuOutline.getValue() ? (1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f) : 1)), selectedRectStartY, endX - (ClickGUI.instance.enumDropMenuOutline.getValue() ? 1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f : 1), selectedRectEndY, selectedRectColor.getRGB());
                    }
                }
            }
            else {
                storedEnumDropMenuSelectLoops.putIfAbsent(setting.getName() + element.name(), 0);

                int animateLoops = storedEnumDropMenuSelectLoops.get(setting.getName() + element.name());

                if (passedms < 1000) {
                    animateLoops += element == setting.getValue() ? (ClickGUI.instance.enumDropMenuSelectedRectAnimationSpeed.getValue() * passedms / 3.0f) : (ClickGUI.instance.enumDropMenuSelectedRectAnimationSpeed.getValue() * passedms / 3.0f * -1.0f);
                }

                animateLoops = (int) MathUtilFuckYou.clamp(animateLoops, 0.0f, 300.0f);

                storedEnumDropMenuSelectLoops.put(setting.getName() + element.name(), animateLoops);

                if (ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.Alpha || ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.AlpScale) {
                    selectedRectColor = new Color(selectedRectColor.getRed(), selectedRectColor.getGreen(), selectedRectColor.getBlue(), (int)(((ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuSelectedRectColor.getValue().getAlpha()) / 300.0f) * storedEnumDropMenuSelectLoops.get(setting.getName() + element.name())));
                }

                float animatedSelectedRectStartX = startX + (ClickGUI.instance.enumDropMenuSideBar.getValue() ? (1 + (ClickGUI.instance.enumDropMenuSideBarWidth.getValue() / 2.0f) + 0.5f) : (ClickGUI.instance.enumDropMenuOutline.getValue() ? (1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f) : 1));
                float animatedSelectedRectEndX = endX - (ClickGUI.instance.enumDropMenuOutline.getValue() ? 1 + (ClickGUI.instance.enumDropMenuOutlineWidth.getValue() / 2.0f) + 0.5f : 1);

                float animatedSelectedRectStartY = startY + 1;
                float animatedSelectedRectEndY = endY - ClickGUI.instance.enumDropMenuRectGap.getValue();

                if (ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.Scale || ClickGUI.instance.enumDropMenuSelectedRectAnimation.getValue() == ClickGUI.EnumDropMenuSelectedRectAnimation.AlpScale) {
                    animatedSelectedRectStartX = animatedSelectedRectStartX + ((endX - startX) / 2.0f) - ((((endX - startX) / 2.0f) / 300.0f) * storedEnumDropMenuSelectLoops.get(setting.getName() + element.name()));
                    animatedSelectedRectEndX = animatedSelectedRectEndX - ((endX - startX) / 2.0f) + ((((endX - startX) / 2.0f) / 300.0f) * storedEnumDropMenuSelectLoops.get(setting.getName() + element.name()));
                    animatedSelectedRectStartY = animatedSelectedRectStartY + ((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f) - ((((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f) / 300.0f) * storedEnumDropMenuSelectLoops.get(setting.getName() + element.name()));
                    animatedSelectedRectEndY = animatedSelectedRectEndY - ((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f) + ((((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f) / 300.0f) * storedEnumDropMenuSelectLoops.get(setting.getName() + element.name()));

                    if (storedEnumDropMenuSelectLoops.get(setting.getName() + element.name()) == 0) {
                        animatedSelectedRectStartX = animatedSelectedRectStartX + ((animatedSelectedRectEndX - animatedSelectedRectStartX) / 2.0f);
                        animatedSelectedRectEndX = animatedSelectedRectEndX - ((animatedSelectedRectEndX - animatedSelectedRectStartX) / 2.0f);
                        animatedSelectedRectStartY = animatedSelectedRectStartY + ((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f);
                        animatedSelectedRectEndY = animatedSelectedRectEndY - ((animatedSelectedRectEndY - animatedSelectedRectStartY) / 2.0f);
                    }
                }

                GlStateManager.disableAlpha();
                if (ClickGUI.instance.enumDropMenuSelectedRectRounded.getValue()) {
                    RenderUtils2D.drawRoundedRect(animatedSelectedRectStartX, animatedSelectedRectStartY, ClickGUI.instance.enumDropMenuSelectedRectRoundedRadius.getValue(), animatedSelectedRectEndX, animatedSelectedRectEndY, false, true, true, true, true, selectedRectColor.getRGB());
                }
                else {
                    RenderUtils2D.drawRect(animatedSelectedRectStartX, animatedSelectedRectStartY, animatedSelectedRectEndX, animatedSelectedRectEndY, selectedRectColor.getRGB());
                }
                GlStateManager.enableAlpha();
            }
        }

        //side rect shit
        if (ClickGUI.instance.enumDropMenuSelectedSideRect.getValue()) {
            storedEnumDropMenuSelectSideRectLoops.putIfAbsent(setting.getName() + element.name(), 0);

            int animateLoops = storedEnumDropMenuSelectSideRectLoops.get(setting.getName() + element.name());

            if (passedms < 1000) {
                animateLoops += element == setting.getValue() ? (ClickGUI.instance.enumDropMenuSelectedSideRectAnimationSpeed.getValue() * passedms / 3.0f) : (ClickGUI.instance.enumDropMenuSelectedSideRectAnimationSpeed.getValue() * passedms / 3.0f * -1.0f);
            }

            animateLoops = (int) MathUtilFuckYou.clamp(animateLoops, 0.0f, 300.0f);

            storedEnumDropMenuSelectSideRectLoops.put(setting.getName() + element.name(), animateLoops);

            float selectedSideRectStartX;
            float selectedSideRectEndX;

            float selectedSideRectStartY = startY + ((endY - startY) / 2.0f) - (ClickGUI.instance.enumDropMenuSelectedSideRectHeight.getValue()/ 2.0f);
            float selectedSideRectEndY = endY - ((endY - startY) / 2.0f) + (ClickGUI.instance.enumDropMenuSelectedSideRectHeight.getValue()/ 2.0f);

            if (ClickGUI.instance.enumDropMenuSelectedSideRectSide.getValue() == ClickGUI.EnumDropMenuSelectedSideRectSide.Right) {
                selectedSideRectStartX = endX - ClickGUI.instance.enumDropMenuSelectedSideRectWidth.getValue() - ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue();
                selectedSideRectEndX = endX - ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue();
            }
            else {
                selectedSideRectStartX = startX + ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue();
                selectedSideRectEndX = startX + ClickGUI.instance.enumDropMenuSelectedSideRectWidth.getValue() + ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue();
            }

            if (ClickGUI.instance.enumDropMenuSelectedSideRectAnimation.getValue()) {
                selectedSideRectStartY = startY + ((endY - startY) / 2.0f) - (((ClickGUI.instance.enumDropMenuSelectedSideRectHeight.getValue()/ 2.0f) / 300.0f) * storedEnumDropMenuSelectSideRectLoops.get(setting.getName() + element.name()));
                selectedSideRectEndY = endY - ((endY - startY) / 2.0f) + (((ClickGUI.instance.enumDropMenuSelectedSideRectHeight.getValue()/ 2.0f) / 300.0f) * storedEnumDropMenuSelectSideRectLoops.get(setting.getName() + element.name()));
                if (ClickGUI.instance.enumDropMenuSelectedSideRectSide.getValue() == ClickGUI.EnumDropMenuSelectedSideRectSide.Right) {
                    selectedSideRectStartX = endX - ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue() - ((ClickGUI.instance.enumDropMenuSelectedSideRectWidth.getValue() / 300.0f) * storedEnumDropMenuSelectSideRectLoops.get(setting.getName() + element.name()));
                }
                else {
                    selectedSideRectEndX = startX + ClickGUI.instance.enumDropMenuSelectedSideRectXOffset.getValue() + ((ClickGUI.instance.enumDropMenuSelectedSideRectWidth.getValue() / 300.0f) * storedEnumDropMenuSelectSideRectLoops.get(setting.getName() + element.name()));
                }

                if (ClickGUI.instance.enumDropMenuSelectedSideRectRounded.getValue()) {
                    if (ClickGUI.instance.enumDropMenuSelectSideRectFull.getValue()) {
                        RenderUtils2D.drawRoundedRect(selectedSideRectStartX, selectedSideRectStartY, ClickGUI.instance.enumDropMenuSelectedSideRectRoundedRadius.getValue(), selectedSideRectEndX, selectedSideRectEndY, false, true, true, true, true, selectedSideRectColor.getRGB());
                    }
                    else {
                        RenderUtils2D.drawCustomGradientRoundedRectModuleEnableMode(selectedSideRectStartX, selectedSideRectStartY, selectedSideRectEndX, selectedSideRectEndY, ClickGUI.instance.enumDropMenuSelectedSideRectRoundedRadius.getValue(), ClickGUI.instance.enumDropMenuSelectedSideRectSide.getValue() == ClickGUI.EnumDropMenuSelectedSideRectSide.Right, selectedSideRectColor.getRGB(), selectedSideRectColor.getRGB());
                    }
                }
                else {
                    RenderUtils2D.drawRect(selectedSideRectStartX, selectedSideRectStartY, selectedSideRectEndX, selectedSideRectEndY, selectedSideRectColor.getRGB());
                }

            }
            else {
                if (element == setting.getValue()) {
                    if (ClickGUI.instance.enumDropMenuSelectedSideRectRounded.getValue()) {
                        if (ClickGUI.instance.enumDropMenuSelectSideRectFull.getValue()) {
                            RenderUtils2D.drawRoundedRect(selectedSideRectStartX, selectedSideRectStartY, ClickGUI.instance.enumDropMenuSelectedSideRectRoundedRadius.getValue(), selectedSideRectEndX, selectedSideRectEndY, false, true, true, true, true, selectedSideRectColor.getRGB());
                        }
                        else {
                            RenderUtils2D.drawCustomGradientRoundedRectModuleEnableMode(selectedSideRectStartX, selectedSideRectStartY, selectedSideRectEndX, selectedSideRectEndY, ClickGUI.instance.enumDropMenuSelectedSideRectRoundedRadius.getValue(), ClickGUI.instance.enumDropMenuSelectedSideRectSide.getValue() == ClickGUI.EnumDropMenuSelectedSideRectSide.Right, selectedSideRectColor.getRGB(), selectedSideRectColor.getRGB());
                        }
                    }
                    else {
                        RenderUtils2D.drawRect(selectedSideRectStartX, selectedSideRectStartY, selectedSideRectEndX, selectedSideRectEndY, selectedSideRectColor.getRGB());
                    }
                }
            }
        }

        //selected side glow
        if (ClickGUI.instance.enumDropMenuSelectedSideGlow.getValue()) {
            if (ClickGUI.instance.enumDropMenuSelectedSideGlowAnimate.getValue()) {
                storedEnumDropMenuSelectSideGlowLoops.putIfAbsent(setting.getName() + element.name(), 0);

                int animateLoops = storedEnumDropMenuSelectSideGlowLoops.get(setting.getName() + element.name());

                if (passedms < 1000) {
                    animateLoops += element == setting.getValue() ? (ClickGUI.instance.enumDropMenuSelectedSideGlowAnimateSpeed.getValue() * passedms / 3.0f) : (ClickGUI.instance.enumDropMenuSelectedSideGlowAnimateSpeed.getValue() * passedms / 3.0f * -1.0f);
                }

                animateLoops = (int) MathUtilFuckYou.clamp(animateLoops, 0.0f, 300.0f);

                storedEnumDropMenuSelectSideGlowLoops.put(setting.getName() + element.name(), animateLoops);

                selectedSideSideGlowColor = new Color(selectedSideSideGlowColor.getRed(), selectedSideSideGlowColor.getGreen(), selectedSideSideGlowColor.getBlue(), (int)(((ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? (int)(((ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.enumDropMenuSelectedSideGlowColor.getValue().getAlpha()) / 300.0f) * storedEnumDropMenuSelectSideGlowLoops.get(setting.getName() + element.name())));

                GlStateManager.disableAlpha();
                if (ClickGUI.instance.enumDropMenuSelectedSideGlowSide.getValue() == ClickGUI.EnumDropMenuSelectedSideGlowSide.Right) {
                    RenderUtils2D.drawCustomRect(endX - ClickGUI.instance.enumDropMenuSelectedSideGlowWidth.getValue(), startY + 1, endX, endY - ClickGUI.instance.enumDropMenuRectGap.getValue(), selectedSideSideGlowColor.getRGB(), new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 0).getRGB(), selectedSideSideGlowColor.getRGB());
                }
                else {
                    RenderUtils2D.drawCustomRect(startX, startY + 1, startX + ClickGUI.instance.enumDropMenuSelectedSideGlowWidth.getValue(), endY - ClickGUI.instance.enumDropMenuRectGap.getValue(), new Color(0, 0, 0, 0).getRGB(), selectedSideSideGlowColor.getRGB(), selectedSideSideGlowColor.getRGB(), new Color(0, 0, 0, 0).getRGB());
                }
                GlStateManager.enableAlpha();
            }
            else if (element == setting.getValue()) {
                GlStateManager.disableAlpha();
                if (ClickGUI.instance.enumDropMenuSelectedSideGlowSide.getValue() == ClickGUI.EnumDropMenuSelectedSideGlowSide.Right) {
                    RenderUtils2D.drawCustomRect(endX - ClickGUI.instance.enumDropMenuSelectedSideGlowWidth.getValue(), selectedRectStartY, endX, selectedRectEndY, selectedSideSideGlowColor.getRGB(), new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 0).getRGB(), selectedSideSideGlowColor.getRGB());
                }
                else {
                    RenderUtils2D.drawCustomRect(startX, selectedRectStartY, startX + ClickGUI.instance.enumDropMenuSelectedSideGlowWidth.getValue(), selectedRectEndY, new Color(0, 0, 0, 0).getRGB(), selectedSideSideGlowColor.getRGB(), selectedSideSideGlowColor.getRGB(), new Color(0, 0, 0, 0).getRGB());
                }
                GlStateManager.enableAlpha();
            }
        }
    }

    private void scrollMenu(int passedms) {
        if (isSliding || shouldScroll) return;

        int dWheel = Mouse.getDWheel();

        if (dWheel < 0 || (ClickGUI.instance.arrowScroll.getValue() && Keyboard.isKeyDown(KEY_DOWN))) {
            scrollUpOrDown = -1.0f;
        } else if (dWheel > 0 || (ClickGUI.instance.arrowScroll.getValue() && Keyboard.isKeyDown(KEY_UP))) {
            scrollUpOrDown = 1.0f;
        }

        if ((dWheel < 0 || (ClickGUI.instance.arrowScroll.getValue() && Keyboard.isKeyDown(KEY_UP))) || (dWheel > 0 || (ClickGUI.instance.arrowScroll.getValue() && Keyboard.isKeyDown(KEY_DOWN)))) {
            prevScrollY = scrollOffset;
            scrollDelta = 0.0f;
        }

        if (passedms < 1000) {
            scrollDelta = MathUtilFuckYou.clamp(scrollDelta + ClickGUI.instance.enumDropMenuScrollSpeed.getValue() * passedms / 5.0f, 0.0f, 300.0f);
        }

        float limit1 = (height * setting.getValue().getDeclaringClass().getEnumConstants().length - ClickGUI.instance.enumDropMenuHeight.getValue() + (height * ClickGUI.instance.enumDropMenuReboundSpace.getValue() * 2.0f)) * -1.0f;

        if ((scrollOffset < limit1 && limit1 < 0.0f) || (scrollOffset < 0.0f && limit1 >= 0.0f)) {
            if ((scrollOffset - limit1) * -1.0f > reboundDist) reboundDist = scrollOffset - limit1;
        }
        else if (scrollOffset > limit1 && scrollOffset > 0.0f) {
            if (scrollOffset > reboundDist) reboundDist = scrollOffset;
        }

        if (((scrollOffset < limit1 && limit1 < 0.0f) || (scrollOffset < 0.0f && limit1 >= 0.0f)) || (scrollOffset > limit1 && scrollOffset > 0.0f)) {
            if (!reboundMaxFlag) {
                reboundDelta = 0.0f;
                reboundMaxFlag = true;
            }

            if (passedms < 1000) {
                reboundDelta += passedms / 20.0f * ClickGUI.instance.enumDropMenuReboundFactor.getValue();
            }

            reboundDelta = MathUtilFuckYou.clamp(reboundDelta, 0.0f, 300.0f);
            prevScrollY -= (reboundDelta / 300.0f) * reboundDist;
        } else {
            reboundMaxFlag = false;
        }

        scrollOffset = MathUtilFuckYou.interpNonLinear(prevScrollY, prevScrollY + ClickGUI.instance.scrollAmount.getValue() * scrollUpOrDown, scrollDelta / 300.0f, ClickGUI.instance.scrollFactor.getValue() / 2.6667f);
    }

    private Enum<?> getEnumHovered(int mouseX, int mouseY) {
        if (menuRects.isEmpty() || !scissorRect.isHovered(mouseX, mouseY)) return null;
        for (Map.Entry<Enum<?>, Rect> entry : new HashMap<>(menuRects).entrySet()) {
            if (entry.getValue().isHovered(mouseX, mouseY))
                return entry.getKey();
        }
        return null;
    }

    public static void drawText(String text, Minecraft mc, int longestWordLength, float x, float y, float width, float height, float heightOffset, int textColor,
                                float scale, float xOffset, int extraWidth, float minWidth) {
        float theX = x + width + xOffset + (Math.max(minWidth, ((extraWidth * 2) + longestWordLength)) / 2.0f);
        float theY = y + (height / 2.0f) + heightOffset;

        GL11.glTranslatef(theX * (1.0f - scale), theY * (1.0f - scale), 0.0f);
        GL11.glScalef(scale, scale, 0.0f);

        GlStateManager.disableAlpha();
        if (CustomFont.instance.font.getValue() == CustomFont.FontMode.Minecraft) {
            GL11.glEnable(GL_TEXTURE_2D);
            mc.fontRenderer.drawString(text, (int)(theX - (FontManager.getWidth(text) / 2.0f)), (int)theY, textColor);
            GL11.glDisable(GL_TEXTURE_2D);
        }
        else {
            FontManager.draw(text, theX - (FontManager.getWidth(text) / 2.0f), theY, textColor);
        }
        GlStateManager.enableAlpha();

        GL11.glScalef(1.0f / scale, 1.0f / scale, 0.0f);
        GL11.glTranslatef(theX * (1.0f - scale) * -1.0f,theY * (1.0f - scale) * -1.0f, 0.0f);
    }

    private int getTextColor(Enum<?> element, int passedms) {
        Color selectedTextColor = new Color(ClickGUI.instance.enumDropMenuSelectedTextColor.getValue().getRed(), ClickGUI.instance.enumDropMenuSelectedTextColor.getValue().getGreen(), ClickGUI.instance.enumDropMenuSelectedTextColor.getValue().getBlue(), ClickGUI.instance.enumDropMenuSelectedTextColor.getValue().getAlpha());
        Color textColor = new Color(ClickGUI.instance.enumDropMenuTextColor.getValue().getColorColor().getRed(), ClickGUI.instance.enumDropMenuTextColor.getValue().getColorColor().getGreen(), ClickGUI.instance.enumDropMenuTextColor.getValue().getColorColor().getBlue(), ClickGUI.instance.enumDropMenuTextColor.getValue().getAlpha());
        Color newTextColor = new Color(255, 255, 255, ClickGUI.instance.enumDropMenuExpandAnimate.getValue() ? 0 : 255);

        if (ClickGUI.instance.enumDropMenuSelectedTextColorAnimation.getValue() && ClickGUI.instance.enumDropMenuSelectedTextDifColor.getValue()) {
            storedEnumDropMenuSelectTextLoops.putIfAbsent(setting.getName() + element.name(), 0);
            int animateLoops = storedEnumDropMenuSelectTextLoops.get(setting.getName() + element.name());

            int red = (int)(MathUtilFuckYou.linearInterp(textColor.getRed(), selectedTextColor.getRed(), animateLoops));
            int green = (int)(MathUtilFuckYou.linearInterp(textColor.getGreen(), selectedTextColor.getGreen(), animateLoops));
            int blue = (int)(MathUtilFuckYou.linearInterp(textColor.getBlue(), selectedTextColor.getBlue(), animateLoops));
            int alpha = (int)(MathUtilFuckYou.linearInterp(ClickGUI.instance.enumDropMenuTextColor.getValue().getAlpha(), ClickGUI.instance.enumDropMenuSelectedTextColor.getValue().getAlpha(), animateLoops));


            newTextColor = new Color(red, green, blue, alpha);

            if (passedms < 1000) {
                animateLoops += element == setting.getValue() ? (ClickGUI.instance.enumDropMenuSelectedTextColorAnimationSpeed.getValue() * passedms / 3.0f) : (ClickGUI.instance.enumDropMenuSelectedTextColorAnimationSpeed.getValue() * passedms / 3.0f * -1.0f);
            }

            animateLoops = (int) MathUtilFuckYou.clamp(animateLoops, 0.0f, 300.0f);
            
            storedEnumDropMenuSelectTextLoops.put(setting.getName() + element.name(), animateLoops);
        }

        return ClickGUI.instance.enumDropMenuSelectedTextDifColor.getValue() ? (ClickGUI.instance.enumDropMenuSelectedTextColorAnimation.getValue() ? (newTextColor.getRGB()) : (element == setting.getValue() ? (selectedTextColor.getRGB()) : textColor.getRGB())) : textColor.getRGB();
    }

    private void clickScroll(int mouseY) {
        if (isSliding) {
            double val = MathUtilFuckYou.clamp((mouseY - (y - (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() : 0.0f))) /
                    (ClickGUI.instance.enumDropMenuHeight.getValue() + (ClickGUI.instance.enumDropMenuExtensions.getValue() ? ClickGUI.instance.enumDropMenuExtensionsHeight.getValue() * 2.0f : 0.0f)), 0.0f, 1.0f);
            scrollOffset = -1.0f * (float) (val * (height * (setting.getValue().getDeclaringClass().getEnumConstants().length + 2) - ClickGUI.instance.enumDropMenuHeight.getValue()));
            prevScrollY = scrollOffset - ClickGUI.instance.scrollAmount.getValue() * scrollUpOrDown;
            scrollDelta = 300.0f;
        }
    }
}
