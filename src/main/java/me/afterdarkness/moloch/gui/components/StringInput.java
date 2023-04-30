package me.afterdarkness.moloch.gui.components;

import me.afterdarkness.moloch.core.Rect;
import me.afterdarkness.moloch.module.modules.client.CustomFont;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.spartanb312.base.client.FontManager;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.gui.Component;
import net.spartanb312.base.gui.Panel;
import net.spartanb312.base.gui.components.EnumButton;
import net.spartanb312.base.gui.renderers.ClickGUIRenderer;
import net.spartanb312.base.gui.renderers.HUDEditorRenderer;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.module.modules.client.ClickGUI;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.SoundUtil;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.graphics.RenderUtils2D;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.launch;
import static org.lwjgl.input.Keyboard.KEY_DOWN;
import static org.lwjgl.input.Keyboard.KEY_UP;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class StringInput extends Component {

    Setting<String> setting;
    String moduleName;
    boolean isOverflowing = false;
    Timer typeTimer = new Timer();
    Timer animationTimer = new Timer();
    Timer typingMarkerTimer = new Timer();
    Timer backspaceTimer = new Timer();
    Timer backspaceDelayTimer = new Timer();
    boolean backspaceFlag = false;
    Timer deleteTimer = new Timer();
    Timer deleteDelayTimer = new Timer();
    Timer pasteDelayTimer = new Timer();
    Timer holdTypeTimer = new Timer();
    Timer holdTypeDelayTimer = new Timer();
    boolean holdTypeFlag = false;
    int holdTypeKey;
    char holdTypeChar;
    boolean deleteFlag = false;
    boolean showTypingMarker = true;
    float animationAlpha = 0.0f;
    int typingMarkerOffset = 0;
    float prevTextWidth = 0.0f;
    float typingMarkerInterpDelta = 300.0f;
    boolean typingMarkerInterpFlag = false;
    boolean isTypingFlag = false;
    private boolean useMenuHover;
    private boolean reverseAnimationFlag = false;
    private boolean menuExtended = false;
    private final Timer menuTimer = new Timer();
    private boolean menuDelayFlag = false;
    private HashMap<String, Rect> menuRects = new HashMap<>();
    private final HashMap<String, Float> collectorXAnimateMap = new HashMap<>();
    private float animateDelta;
    private float scrollOffset;
    public float scrollDelta;
    private float prevScrollY;
    private float scrollUpOrDown = 1.0f;
    private Rect scissorRect;
    private Rect scrollRect;
    private boolean unlockScrollFlag;
    private boolean isSliding;
    private float reboundDelta;
    private boolean reboundMaxFlag;
    private float reboundDist;
    private float animateDelta1;

    public static StringInput INSTANCE;

    public StringInput(Setting<String> setting, int width, int height, Panel father, Module module) {
        this.width = width;
        this.height = height + 14;
        this.father = father;
        this.setting = setting;
        this.moduleName = module.name;
        INSTANCE = this;
    }

    @Override
    public void render(int mouseX, int mouseY, float translateDelta, float partialTicks) {
        if (((StringSetting)setting).listening) {
            isTyping = true;
            isTypingFlag = false;

            if (pasteDelayTimer.passed(200) && Keyboard.isKeyDown(Keyboard.KEY_V) && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                launch(() -> setting.setValue(new StringBuilder(setting.getValue()).insert(setting.getValue().length() - typingMarkerOffset, getStringFromClipboard()).toString()));
                pasteDelayTimer.reset();
            }

            if (backspaceFlag && Keyboard.isKeyDown(Keyboard.KEY_BACK) && backspaceTimer.passed(700) && backspaceDelayTimer.passed(2) && setting.getValue().length() >= 1) {
                setting.setValue(new StringBuilder(setting.getValue()).replace(setting.getValue().length() - typingMarkerOffset - 1, setting.getValue().length() - typingMarkerOffset, "").toString());
                backspaceDelayTimer.reset();
            }

            if (holdTypeFlag && Keyboard.isKeyDown(holdTypeKey) && holdTypeKey != 0 && holdTypeTimer.passed(700) && holdTypeDelayTimer.passed(2)) {
                setting.setValue(new StringBuilder(setting.getValue()).insert(setting.getValue().length() - typingMarkerOffset, holdTypeChar).toString());
                holdTypeDelayTimer.reset();
            }

            if (!Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
                backspaceFlag = false;
            }

            if (deleteFlag && Keyboard.isKeyDown(Keyboard.KEY_DELETE) && deleteTimer.passed(700) && deleteDelayTimer.passed(2) && setting.getValue().length() >= 1) {
                setting.setValue(new StringBuilder(setting.getValue()).replace(setting.getValue().length() - typingMarkerOffset, setting.getValue().length() - typingMarkerOffset + 1, "").toString());
                typingMarkerOffset--;
                if (typingMarkerOffset < 0) typingMarkerOffset = 0;
                deleteDelayTimer.reset();
            }

            if (!Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
                deleteFlag = false;
            }
        }
        else if (!isTypingFlag) {
            isTyping = false;
            isTypingFlag = true;
        }

        int passedms = (int) animationTimer.hasPassed();
        animationTimer.reset();

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN) || !setting.isVisible()) {
            ((StringSetting)setting).listening = false;
            isTyping = false;
            animateDelta = 0;
        }
        if (!((StringSetting)setting).listening && typingMarkerOffset != 0) {
            typingMarkerOffset = 0;
        }
        isOverflowing = (FontManager.getWidth(setting.getValue()) * ClickGUI.instance.stringInputValueScale.getValue()) + x + 5 > x + width - 7;

        RenderUtils2D.drawRect(x + 4, y + (height / 2.0f) + 2, x + width - 4, y + height - 2, ClickGUI.instance.stringInputBoxColor.getValue().getColor());

        float settingNameX = x + 5;
        float textY = y + (font.getHeight() / 2.0f) + ClickGUI.instance.stringInputNameOffset.getValue();

        if (CustomFont.instance.font.getValue() == CustomFont.FontMode.Minecraft) {
            GL11.glEnable(GL_TEXTURE_2D);
            GL11.glTranslatef((settingNameX) * (1.0f - CustomFont.instance.componentTextScale.getValue()), (textY) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
            GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());

            mc.fontRenderer.drawString(setting.getName(), settingNameX, textY, ClickGUI.instance.stringInputNameColor.getValue().getColor(), CustomFont.instance.textShadow.getValue());

            GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
            GL11.glTranslatef((settingNameX) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, (textY) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);
            GL11.glDisable(GL_TEXTURE_2D);
        }
        else {
            GL11.glTranslatef((settingNameX) * (1.0f - CustomFont.instance.componentTextScale.getValue()), (textY) * (1.0f - CustomFont.instance.componentTextScale.getValue()), 0.0f);
            GL11.glScalef(CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue(), CustomFont.instance.componentTextScale.getValue());
            if (CustomFont.instance.textShadow.getValue()) {
                FontManager.drawShadow(setting.getName(), settingNameX, textY, ClickGUI.instance.stringInputNameColor.getValue().getColor());
            }
            else {
                FontManager.draw(setting.getName(), settingNameX, textY, ClickGUI.instance.stringInputNameColor.getValue().getColor());
            }
            GL11.glScalef(1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()), 1.0f / (CustomFont.instance.componentTextScale.getValue()));
            GL11.glTranslatef((settingNameX) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, (textY) * (1.0f - CustomFont.instance.componentTextScale.getValue()) * -1.0f, 0.0f);
        }

        if (isOverflowing) {
            GL11.glTranslatef(-((FontManager.getWidth(setting.getValue()) * ClickGUI.instance.stringInputValueScale.getValue()) + x + 8 - (x + width - 4)), 0.0f, 0.0f);
        }

        RenderUtils2D.betterScissor(x + 4,
                (ClickGUI.instance.guiMove.getValue() ? mc.currentScreen.height - translateDelta : 0) + y + (height / 2.0f) + 2,
                width - 8,
                (height / 2.0f) - 4);
        GL11.glEnable(GL_SCISSOR_TEST);

        if (CustomFont.instance.font.getValue() == CustomFont.FontMode.Minecraft) {
            GL11.glEnable(GL_TEXTURE_2D);
            GL11.glTranslatef((x + 6) * (1.0f - ClickGUI.instance.stringInputValueScale.getValue()), (y + (height * 0.75f) - 2) * (1.0f - ClickGUI.instance.stringInputValueScale.getValue()), 0.0f);
            GL11.glScalef(ClickGUI.instance.stringInputValueScale.getValue(), ClickGUI.instance.stringInputValueScale.getValue(), 1.0f);

            mc.fontRenderer.drawString(setting.getValue(), x + 6, y + (height * 0.75f) - 2, ClickGUI.instance.stringInputValueColor.getValue().getColor(), CustomFont.instance.textShadow.getValue());

            GL11.glScalef(1.0f / ClickGUI.instance.stringInputValueScale.getValue(), 1.0f / ClickGUI.instance.stringInputValueScale.getValue(), 1.0f);
            GL11.glTranslatef((x + 6) * -(1.0f - ClickGUI.instance.stringInputValueScale.getValue()), (y + (height * 0.75f) - 2) * -(1.0f - ClickGUI.instance.stringInputValueScale.getValue()), 0.0f);
            GL11.glDisable(GL_TEXTURE_2D);
        }
        else {
            GL11.glTranslatef((x + 6) * (1.0f - ClickGUI.instance.stringInputValueScale.getValue()), (y + (height * 0.75f)) * (1.0f - ClickGUI.instance.stringInputValueScale.getValue()), 0.0f);
            GL11.glScalef(ClickGUI.instance.stringInputValueScale.getValue(), ClickGUI.instance.stringInputValueScale.getValue(), 1.0f);
            if (CustomFont.instance.textShadow.getValue()) {
                FontManager.drawShadow(setting.getValue(), x + 6, y + (height * 0.75f), ClickGUI.instance.stringInputValueColor.getValue().getColor());
            }
            else {
                FontManager.draw(setting.getValue(), x + 6, y + (height * 0.75f), ClickGUI.instance.stringInputValueColor.getValue().getColor());
            }
            GL11.glScalef(1.0f / ClickGUI.instance.stringInputValueScale.getValue(), 1.0f / ClickGUI.instance.stringInputValueScale.getValue(), 1.0f);
            GL11.glTranslatef((x + 6) * -(1.0f - ClickGUI.instance.stringInputValueScale.getValue()), (y + (height * 0.75f)) * -(1.0f - ClickGUI.instance.stringInputValueScale.getValue()), 0.0f);
        }
        GL11.glDisable(GL_SCISSOR_TEST);

        if (((StringSetting)setting).listening) {
            if (typingMarkerInterpFlag && passedms < 1000) {
                typingMarkerInterpDelta += passedms * 1.5f;
            }
            if (typingMarkerInterpDelta > 300) {
                typingMarkerInterpDelta = 300;
            }

            if (showTypingMarker) {
                RenderUtils2D.drawRect(((MathUtilFuckYou.linearInterp(prevTextWidth, FontManager.getWidth(setting.getValue().substring(0, setting.getValue().length() - typingMarkerOffset)), typingMarkerInterpDelta)) * ClickGUI.instance.stringInputValueScale.getValue()) + x + 6, y + (height / 2.0f) + 3, ((MathUtilFuckYou.linearInterp(prevTextWidth, FontManager.getWidth(setting.getValue().substring(0, setting.getValue().length() - typingMarkerOffset)), typingMarkerInterpDelta)) * ClickGUI.instance.stringInputValueScale.getValue()) + x + 6.5f, y + height - 3, ClickGUI.instance.stringInputTypingMarkColor.getValue().getColor());
            }
        }

        if (isOverflowing) {
            GL11.glTranslatef((FontManager.getWidth(setting.getValue()) * ClickGUI.instance.stringInputValueScale.getValue()) + x + 8 - (x + width - 4), 0.0f, 0.0f);

            if (passedms < 1000) {
                animationAlpha += passedms * 4.0f / 10.0f;
            }
        }
        else {
            if (passedms < 1000) {
                animationAlpha -= passedms * 4.0f / 10.0f;
            }
        }

        if (animationAlpha > 300.0f) {
            animationAlpha = 300.0f;
        }
        else if (animationAlpha < 0.0f) {
            animationAlpha = 0.0f;
        }

        if (typingMarkerTimer.passed(500)) {
            showTypingMarker = !showTypingMarker;
            typingMarkerTimer.reset();
        }

        GlStateManager.disableAlpha();
        RenderUtils2D.drawCustomRect(x + 4, y + (height / 2.0f) + 2, x + 19, y + height - 2, new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, (int)(170 * (animationAlpha / 300.0f))).getRGB(), new Color(0, 0, 0, (int)(170 * (animationAlpha / 300.0f))).getRGB(), new Color(0, 0, 0, 0).getRGB());
        if (ClickGUI.instance.stringInputBoxOutline.getValue()) {
            RenderUtils2D.drawRectOutline(x + 4, y + (height / 2.0f) + 2, x + width - 4, y + height - 2, ClickGUI.instance.stringInputBoxOutlineWidth.getValue(), ClickGUI.instance.stringInputBoxOutlineColor.getValue().getColor(), false, false);
        }
        GlStateManager.enableAlpha();


        //collector menu stuff
        if (((StringSetting) setting).isCollector && ((StringSetting) setting).feederList != null && !(anyExpanded && !menuExtended)) {
            useMenuHover = false;

            if (!((StringSetting) setting).feederList.isEmpty() && (isSliding || isHovered(mouseX, mouseY, x, y, x + width, y + height) || (menuExtended && ((scissorRect != null && scissorRect.isHovered(mouseX, mouseY)) || (ClickGUI.instance.stringInputScrollBar.getValue() && scrollRect != null && scrollRect.isHovered(mouseX, mouseY)))))) {
                reverseAnimationFlag = false;
                menuExtended = true;
                anyExpanded = true;
                menuDelayFlag = false;
            }
            else {
                if (!menuDelayFlag) {
                    menuTimer.reset();
                    menuDelayFlag = true;
                }

                if (menuTimer.passed(ClickGUI.instance.stringInputCollectorCloseDelay.getValue())) {
                    if (!ClickGUI.instance.stringInputCollectorAnimate.getValue()) {
                        menuExtended = false;
                    }
                    isSliding = false;
                    anyExpanded = false;
                    reverseAnimationFlag = true;
                }
            }

            if (ClickGUI.instance.stringInputCollectorAnimate.getValue() && reverseAnimationFlag && animateDelta <= 0.0f) {
                menuExtended = false;
            }

            if (menuExtended) {
                float startX = x + width + ClickGUI.instance.stringInputCollectorX.getValue();
                float endX = x + width + ClickGUI.instance.stringInputCollectorX.getValue() + Math.max(ClickGUI.instance.stringInputCollectorMinimumWidth.getValue(), ClickGUI.instance.stringInputCollectorExtraWidth.getValue() * 2 + getLongestLength((ArrayList<String>) ((StringSetting) setting).feederList));

                if (!reverseAnimationFlag) {
                    expandedX = startX;
                    expandedY = y;
                    expandedEndX = endX + (ClickGUI.instance.stringInputScrollBar.getValue() ? (ClickGUI.instance.stringInputScrollBarXOffset.getValue() + ClickGUI.instance.stringInputScrollBarBGWidth.getValue() + ClickGUI.instance.stringInputScrollBarExtraWidth.getValue() * 0.5f) : 0.0f);
                    expandedEndY = y + ClickGUI.instance.stringInputCollectorHeight.getValue();
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
                isSliding = false;
            }
        }
        else {
            shouldScroll = true;
            isSliding = false;
        }
    }

    @Override
    public void bottomRender(int mouseX, int mouseY, boolean lastSetting, boolean firstSetting, float partialTicks) {
        GlStateManager.disableAlpha();
        drawSettingRects(lastSetting, false);

        drawExtendedGradient(lastSetting, false);
        drawExtendedLine(lastSetting);

        useMenuHover = false;
        renderHoverRect(moduleName + setting.getName(), mouseX, mouseY, x, y, x + width, y + height, 2.0f, -15.0f, false);

        GlStateManager.enableAlpha();
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (menuExtended && ((StringSetting) setting).feederList != null && !((StringSetting) setting).feederList.isEmpty()) {
            String hoveredValue = getValueHovered(mouseX, mouseY);

            if (scissorRect != null && scissorRect.isHovered(mouseX, mouseY)) {
                if (father.category.isHUD) {
                    HUDEditorRenderer.panelToSwitch = father;
                    HUDEditorRenderer.switchPanelFlag = true;
                }
                else {
                    ClickGUIRenderer.panelToSwitch = father;
                    ClickGUIRenderer.switchPanelFlag = true;
                }
            }

            if (hoveredValue != null) {
                float xHeight = (FontManager.iconFont.getHeight() + 2) * ClickGUI.instance.stringInputCollectorXScale.getValue();
                float endX = menuRects.get(hoveredValue).endX;
                float startY = menuRects.get(hoveredValue).startY - 1;
                float elementHeight = height - 14;
                if (RenderUtils2D.isMouseInRect(mouseX, mouseY, endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue() + (FontManager.iconFont.getStringWidth("+") * ClickGUI.instance.stringInputCollectorXScale.getValue() * 0.5f) - (FontManager.iconFont.getStringWidth("+") * ClickGUI.instance.stringInputCollectorXScale.getValue() * 0.5f), startY + (1.5f * ClickGUI.instance.stringInputCollectorXScale.getValue()) + elementHeight * 0.5f - xHeight * 0.5f, endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue() + (FontManager.iconFont.getStringWidth("+") * ClickGUI.instance.stringInputCollectorXScale.getValue() * 0.5f) + (FontManager.iconFont.getStringWidth("+") * ClickGUI.instance.stringInputCollectorXScale.getValue() * 0.5f), startY + (1.5f * ClickGUI.instance.stringInputCollectorXScale.getValue()) + elementHeight * 0.5f + xHeight * 0.5f)) {
                    ((StringSetting) setting).feederList.remove(hoveredValue);
                }

                SoundUtil.playButtonClick();
            }

            if (scrollRect != null && scrollRect.isHovered(mouseX, mouseY)) {
                isSliding = true;
            }
        }

        if (!isHovered(mouseX, mouseY, x, y, x + width, y + height) || !setting.isVisible()) {
            ((StringSetting)setting).listening = false;
            return;
        }

        if (mouseButton == 0) {
            ((StringSetting)setting).listening = !((StringSetting)setting).listening;
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
    public Setting<?> getSetting() {
        return setting;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (((StringSetting)setting).listening) {
            if (isValidInput(keyCode) && typeTimer.passed(2) && !(Keyboard.isKeyDown(Keyboard.KEY_V) && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)))) {
                holdTypeTimer.reset();
                holdTypeFlag = true;
                holdTypeKey = keyCode;
                holdTypeChar = typedChar;
                setting.setValue(new StringBuilder(setting.getValue()).insert(setting.getValue().length() - typingMarkerOffset, typedChar).toString());
                typeTimer.reset();
            }

            if (keyCode == Keyboard.KEY_BACK && setting.getValue().length() >= 1) {
                backspaceTimer.reset();
                backspaceFlag = true;
                setting.setValue(new StringBuilder(setting.getValue()).replace(setting.getValue().length() - typingMarkerOffset - 1, setting.getValue().length() - typingMarkerOffset, "").toString());
            }

            if (keyCode == Keyboard.KEY_DELETE && setting.getValue().length() >= 1) {
                deleteTimer.reset();
                deleteFlag = true;
                setting.setValue(new StringBuilder(setting.getValue()).replace(setting.getValue().length() - typingMarkerOffset, setting.getValue().length() - typingMarkerOffset + 1, "").toString());
                typingMarkerOffset--;
                if (typingMarkerOffset < 0) typingMarkerOffset = 0;
            }

            if (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
                typingMarkerInterpDelta = 0.0f;
                typingMarkerInterpFlag = true;
                prevTextWidth = FontManager.getWidth(setting.getValue().substring(0, setting.getValue().length() - typingMarkerOffset));
            }

            if (keyCode == Keyboard.KEY_LEFT) {
                typingMarkerOffset++;
            }
            else if (keyCode == Keyboard.KEY_RIGHT) {
                typingMarkerOffset--;
            }

            typingMarkerOffset = (int)MathUtilFuckYou.clamp(typingMarkerOffset, 0, setting.getValue().length());
        }
    }

    private String getStringFromClipboard() {
        Clipboard clipboardData = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboardData.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String)contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private void drawListComponents(int mouseX, int mouseY, float startX, float endX, int passedms, float translateDelta) {
        animateDelta = MathUtilFuckYou.clamp(animateDelta + ((reverseAnimationFlag ? -1.0f : 1.0f) * ClickGUI.instance.stringInputCollectorAnimateSpeed.getValue() * passedms / 3.0f), 0.0f, 300.0f);
        animateDelta1 = MathUtilFuckYou.interpNonLinear(0.0f, 300.0f, animateDelta / 300.0f, ClickGUI.instance.stringInputCollectorAnimateFactor.getValue());
        Color rectBGColor = new Color(ClickGUI.instance.stringInputCollectorRectBGColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorRectBGColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorRectBGColor.getValue().getColorColor().getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((ClickGUI.instance.stringInputCollectorRectBGColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.stringInputCollectorRectBGColor.getValue().getAlpha());
        Color rectColor = new Color(ClickGUI.instance.stringInputCollectorRectColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorRectColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorRectColor.getValue().getColorColor().getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((ClickGUI.instance.stringInputCollectorRectColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.stringInputCollectorRectColor.getValue().getAlpha());
        Color outlineColor = new Color(ClickGUI.instance.stringInputCollectorOutlineColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorOutlineColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorOutlineColor.getValue().getColorColor().getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((ClickGUI.instance.stringInputCollectorOutlineColor.getValue().getAlpha()) / 300.0f) * animateDelta1) : ClickGUI.instance.stringInputCollectorOutlineColor.getValue().getAlpha());

        //change y of menu on mousewheel interaction
        scrollMenu(passedms);

        GL11.glPushMatrix();
        if (ClickGUI.instance.stringInputCollectorAnimateScale.getValue() && ClickGUI.instance.stringInputCollectorAnimate.getValue()) {
            GL11.glTranslatef((x + width + ClickGUI.instance.stringInputCollectorX.getValue()) * (1.0f - (animateDelta1 / 300.0f)), y * (1.0f - (animateDelta1 / 300.0f)), 0.0f);
            GL11.glScalef(animateDelta1 / 300.0f, animateDelta1 / 300.0f, 0.0f);
        }

        //top bottom extensions
        if (ClickGUI.instance.stringInputCollectorExtensions.getValue()) {
            RenderUtils2D.drawRect(startX, y - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue(), endX, y, rectBGColor.getRGB());

            RenderUtils2D.drawRect(startX, y + ClickGUI.instance.stringInputCollectorHeight.getValue(), endX, y + ClickGUI.instance.stringInputCollectorHeight.getValue() + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue(), rectBGColor.getRGB());
        }

        //base rect
        RenderUtils2D.drawRect(startX, y, endX, y + ClickGUI.instance.stringInputCollectorHeight.getValue(), rectBGColor.getRGB());

        //gradient shadow
        if (ClickGUI.instance.stringInputCollectorShadow.getValue()) {
            RenderUtils2D.drawBetterRoundRectFade(startX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y + ClickGUI.instance.stringInputCollectorHeight.getValue() + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y + ClickGUI.instance.stringInputCollectorHeight.getValue(),
                    ClickGUI.instance.stringInputCollectorShadowSize.getValue(),
                    40.0f,
                    false,
                    false,
                    false,
                    new Color(0, 0, 0, ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((ClickGUI.instance.stringInputCollectorShadowAlpha.getValue()) / 300.0f) * animateDelta1) : ClickGUI.instance.stringInputCollectorShadowAlpha.getValue()).getRGB());
        }

        //other side glow
        if (ClickGUI.instance.stringInputCollectorOtherSideGlow.getValue() != ClickGUI.EnumDropMenuOtherSideGlowMode.None) {
            me.afterdarkness.moloch.core.common.Color otherSideGlowColor1 = ClickGUI.instance.stringInputCollectorOtherSideGlowColor.getValue();
            Color otherSideGlowColor = new Color(otherSideGlowColor1.getRed(), otherSideGlowColor1.getGreen(), otherSideGlowColor1.getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((otherSideGlowColor1.getAlpha()) / 300.0f) * animateDelta1) : otherSideGlowColor1.getAlpha());

            GlStateManager.disableAlpha();
            if (ClickGUI.instance.stringInputCollectorOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Right || ClickGUI.instance.stringInputCollectorOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Both) {
                RenderUtils2D.drawCustomRect(endX - ClickGUI.instance.stringInputCollectorOtherSideGlowWidth.getValue(),
                        y,
                        endX,
                        y + ClickGUI.instance.stringInputCollectorHeight.getValue(),
                        otherSideGlowColor.getRGB(),
                        new Color(0, 0, 0, 0).getRGB(),
                        new Color(0, 0, 0, 0).getRGB(),
                        otherSideGlowColor.getRGB());
            }
            if (ClickGUI.instance.stringInputCollectorOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Left || ClickGUI.instance.stringInputCollectorOtherSideGlow.getValue() == ClickGUI.EnumDropMenuOtherSideGlowMode.Both) {
                RenderUtils2D.drawCustomRect(startX,
                        y,
                        startX + ClickGUI.instance.stringInputCollectorOtherSideGlowWidth.getValue(),
                        y + ClickGUI.instance.stringInputCollectorHeight.getValue(),
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
                (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y + ClickGUI.instance.stringInputCollectorHeight.getValue());
        RenderUtils2D.betterScissor(startX,
                (ClickGUI.instance.guiMove.getValue() ? scaledResolution.getScaledHeight() - translateDelta : 0) + y,
                (endX - startX) * (ClickGUI.instance.stringInputCollectorAnimateScale.getValue() && ClickGUI.instance.stringInputCollectorAnimate.getValue() ? animateDelta1 / 300.0f : 1.0f),
                ClickGUI.instance.stringInputCollectorHeight.getValue() * (ClickGUI.instance.stringInputCollectorAnimateScale.getValue() && ClickGUI.instance.stringInputCollectorAnimate.getValue() ? animateDelta1 / 300.0f : 1.0f));
        GL11.glEnable(GL_SCISSOR_TEST);

        drawElements(mouseX, mouseY, passedms, startX, endX, rectColor);

        GL11.glDisable(GL_SCISSOR_TEST);

        //top rects of menu extensions
        if (ClickGUI.instance.stringInputCollectorExtensions.getValue()) {
            RenderUtils2D.drawRect(startX + 1, y - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() + 1, endX - 1, y, rectColor.getRGB());

            RenderUtils2D.drawRect(startX + 1, y + ClickGUI.instance.stringInputCollectorHeight.getValue(), endX - 1, y + ClickGUI.instance.stringInputCollectorHeight.getValue() + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() - 1, rectColor.getRGB());
        }

        //interior border gradients
        if (ClickGUI.instance.stringInputCollectorBorderGradients2.getValue()) {
            me.afterdarkness.moloch.core.common.Color borderGradientColor = ClickGUI.instance.stringInputCollectorBorderGradient2Color.getValue();
            Color gradientColor = new Color(borderGradientColor.getRed(), borderGradientColor.getGreen(), borderGradientColor.getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((borderGradientColor.getAlpha()) / 300.0f) * animateDelta1) : borderGradientColor.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(startX,
                    y,
                    endX,
                    y + ClickGUI.instance.stringInputCollectorBorderGradient2Height.getValue(),
                    gradientColor.getRGB(),
                    gradientColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB());

            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.stringInputCollectorHeight.getValue() + y - ClickGUI.instance.stringInputCollectorBorderGradient2Height.getValue(),
                    endX,
                    ClickGUI.instance.stringInputCollectorHeight.getValue() + y + 1,
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    gradientColor.getRGB(),
                    gradientColor.getRGB());
            GlStateManager.enableAlpha();
        }

        //side glow
        if (ClickGUI.instance.stringInputCollectorSideGlow.getValue()) {
            me.afterdarkness.moloch.core.common.Color sideGlowColor1 = ClickGUI.instance.stringInputCollectorSideGlowColor.getValue();
            Color sideGlowColor = new Color(sideGlowColor1.getRed(), sideGlowColor1.getGreen(), sideGlowColor1.getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((sideGlowColor1.getAlpha()) / 300.0f) * animateDelta1) : sideGlowColor1.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(
                    startX,
                    y - (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f),
                    startX + ClickGUI.instance.stringInputCollectorSideGlowWidth.getValue(),
                    y + ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f),
                    new Color(0, 0, 0, 0).getRGB(),
                    sideGlowColor.getRGB(),
                    sideGlowColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB());
            GlStateManager.enableAlpha();
        }

        //sidebar
        if (ClickGUI.instance.stringInputCollectorSideBar.getValue()) {
            RenderUtils2D.drawCustomLine(startX + (ClickGUI.instance.stringInputCollectorSideBarWidth.getValue() / 4.0f),
                    y - (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f),
                    startX + (ClickGUI.instance.stringInputCollectorSideBarWidth.getValue() / 4.0f),
                    y + ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f),
                    ClickGUI.instance.stringInputCollectorSideBarWidth.getValue(),
                    outlineColor.getRGB(),
                    outlineColor.getRGB());
        }

        //border gradients
        if (ClickGUI.instance.stringInputCollectorBorderGradients.getValue()) {
            me.afterdarkness.moloch.core.common.Color borderGradientColor = ClickGUI.instance.stringInputCollectorBorderGradientColor.getValue();
            Color topBottomGradientColor = new Color(borderGradientColor.getRed(), borderGradientColor.getGreen(), borderGradientColor.getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (int)(((borderGradientColor.getAlpha()) / 300.0f) * animateDelta1) : borderGradientColor.getAlpha());

            GlStateManager.disableAlpha();
            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y + ClickGUI.instance.stringInputCollectorBorderGradientHeight.getValue() - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y + ClickGUI.instance.stringInputCollectorBorderGradientHeight.getValue(),
                    topBottomGradientColor.getRGB(),
                    topBottomGradientColor.getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB());

            RenderUtils2D.drawCustomRect(startX,
                    ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y - ClickGUI.instance.stringInputCollectorBorderGradientHeight.getValue() + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y - ClickGUI.instance.stringInputCollectorBorderGradientHeight.getValue()),
                    endX,
                    ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y),
                    new Color(0, 0, 0, 0).getRGB(),
                    new Color(0, 0, 0, 0).getRGB(),
                    topBottomGradientColor.getRGB(),
                    topBottomGradientColor.getRGB());
            GlStateManager.enableAlpha();
        }

        //outlines
        if (ClickGUI.instance.stringInputCollectorOutline.getValue()) {
            RenderUtils2D.drawRectOutline(startX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y - ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y,
                    endX,
                    ClickGUI.instance.stringInputCollectorExtensions.getValue() ? y + ClickGUI.instance.stringInputCollectorHeight.getValue() + ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : y + ClickGUI.instance.stringInputCollectorHeight.getValue(),
                    ClickGUI.instance.stringInputCollectorOutlineWidth.getValue(),
                    outlineColor.getRGB(),
                    false,
                    false);
        }

        //draw scrollbar
        if (ClickGUI.instance.stringInputScrollBar.getValue()) {
            clickScroll(mouseY);
            scrollRect = new Rect(endX + ClickGUI.instance.stringInputScrollBarXOffset.getValue() - ClickGUI.instance.stringInputScrollBarExtraWidth.getValue() * 0.5f,
                    y - (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f),
                    endX + ClickGUI.instance.stringInputScrollBarBGWidth.getValue() + ClickGUI.instance.stringInputScrollBarXOffset.getValue() + ClickGUI.instance.stringInputScrollBarExtraWidth.getValue() * 0.5f,
                    y + ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f));
            drawScrollBar(scrollOffset, ((StringSetting) setting).feederList.size(), height - 14, endX, y, animateDelta1, ClickGUI.instance.stringInputCollectorExtensions.getValue(), ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue(), ClickGUI.instance.stringInputCollectorHeight.getValue(), ClickGUI.instance.stringInputCollectorAnimate.getValue(), ClickGUI.instance.stringInputCollectorReboundSpace.getValue(),
                    ClickGUI.instance.stringInputScrollBarPattern.getValue(), ClickGUI.instance.stringInputScrollBarBGShadow.getValue(), ClickGUI.instance.stringInputScrollBarBGShadowSize.getValue(), ClickGUI.instance.stringInputScrollBarBGShadowAlpha.getValue(), ClickGUI.instance.stringInputScrollBarShadow.getValue(), ClickGUI.instance.stringInputScrollBarShadowSize.getValue(), ClickGUI.instance.stringInputScrollBarShadowAlpha.getValue(), ClickGUI.instance.stringInputScrollBarXOffset.getValue(), ClickGUI.instance.stringInputScrollBarBGWidth.getValue(), ClickGUI.instance.stringInputScrollBarExtraWidth.getValue(), ClickGUI.instance.stringInputScrollBarBGRounded.getValue(), ClickGUI.instance.stringInputScrollBarBGRoundedRadius.getValue(), ClickGUI.instance.stringInputScrollBarRounded.getValue(), ClickGUI.instance.stringInputScrollBarRoundedRadius.getValue(), ClickGUI.instance.stringInputScrollBarBGColor.getValue().getColorColor(), ClickGUI.instance.stringInputScrollBarColor.getValue().getColorColor(), ClickGUI.instance.stringInputScrollBarBGColor.getValue().getAlpha(), ClickGUI.instance.stringInputScrollBarColor.getValue().getAlpha(),
                    ClickGUI.instance.stringInputScrollBarPatternSmallDistance.getValue(), ClickGUI.instance.stringInputScrollBarPatternDist.getValue(), ClickGUI.instance.stringInputScrollBarPatternCount.getValue(), ClickGUI.instance.stringInputScrollBarPatternSmallBehavior.getValue(), ClickGUI.instance.stringInputScrollBarPatternRounded.getValue(), ClickGUI.instance.stringInputScrollBarPatternRoundedRadius.getValue(), ClickGUI.instance.stringInputScrollBarPatternWidth.getValue(), ClickGUI.instance.stringInputScrollBarPatternHeight.getValue(), ClickGUI.instance.stringInputScrollBarPatternRollColors.getValue(), ClickGUI.instance.stringInputScrollBarPatternColor.getValue(), ClickGUI.instance.stringInputScrollBarPatternRollColor.getValue(), ClickGUI.instance.stringInputScrollBarPatternRollSpeed.getValue(), ClickGUI.instance.stringInputScrollBarPatternRollSize.getValue());
        }

        GL11.glPopMatrix();
    }

    private int getLongestLength(ArrayList<String> list) {
        int longestLength = 0;
        String longestValue = "";
        for (String string : list) {
            if (string.length() > longestLength) {
                longestLength = string.length();
                longestValue = string;
            }
        }
        return FontManager.getWidth(longestValue);
    }

    private String getValueHovered(int mouseX, int mouseY) {
        if (menuRects.isEmpty() || !scissorRect.isHovered(mouseX, mouseY)) return null;
        for (Map.Entry<String, Rect> entry : new HashMap<>(menuRects).entrySet()) {
            if (entry.getValue().isHovered(mouseX, mouseY))
                return entry.getKey();
        }
        return null;
    }

    private static float getScrollProgress(float totalElementsHeight, float menuHeight, float scrollOffset) {
        if (totalElementsHeight > menuHeight) {
            return MathUtilFuckYou.clamp(scrollOffset / (totalElementsHeight - menuHeight), 0.0f, 1.0f);
        } else {
            return 0.0f;
        }
    }

    private void clickScroll(int mouseY) {
        if (isSliding) {
            double val = MathUtilFuckYou.clamp((mouseY - (y - (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() : 0.0f))) /
                    (ClickGUI.instance.stringInputCollectorHeight.getValue() + (ClickGUI.instance.stringInputCollectorExtensions.getValue() ? ClickGUI.instance.stringInputCollectorExtensionsHeight.getValue() * 2.0f : 0.0f)), 0.0f, 1.0f);
            scrollOffset = -1.0f * (float) (val * ((height - 14) * (((StringSetting) setting).feederList.size() + 2) - ClickGUI.instance.stringInputCollectorHeight.getValue()));
            prevScrollY = scrollOffset - ClickGUI.instance.scrollAmount.getValue() * scrollUpOrDown;
            scrollDelta = 300.0f;
        }
    }

    public static void drawScrollBar(float scrollOffset, int totalElements, float height, float endX, float y, float animateDelta, boolean extensions, float extensionHeight, float menuHeight, boolean expandAnimate, float reboundSpace,
                                     boolean pattern, boolean bgShadow, float bgShadowSize, int bgShadowAlpha, boolean barShadow, float barShadowSize, int barShadowAlpha, float xOffset, float bgWidth, float barExtraWidth, boolean bgRounded, float bgRoundRadius, boolean barRounded, float barRoundRadius, Color bgColor, Color barColor, int bgAlpha, int barAlpha,
                                     float patternSmallOffset, float patternDist, float patternCount, ClickGUI.PatternSmallMode patternSmallMode, boolean patternRounded, float patternRoundedRadius, float patternWidthFactor, float patternHeight, boolean patternRollingColor, me.afterdarkness.moloch.core.common.Color patternColor, me.afterdarkness.moloch.core.common.Color patternRollColor, float patternRollSpeed, float patternRollSize) {
        //background shadow
        if (bgShadow) {
            RenderUtils2D.drawBetterRoundRectFade(endX + xOffset,
                    extensions ? y - extensionHeight : y,
                    endX + xOffset + bgWidth,
                    extensions ? y + menuHeight + extensionHeight : y + menuHeight,
                    bgShadowSize,
                    40.0f,
                    false,
                    true,
                    false,
                    new Color(0, 0, 0, expandAnimate ? (int)((bgShadowAlpha / 300.0f) * animateDelta) : bgShadowAlpha).getRGB());
        }

        //background rect
        if (bgRounded) {
            RenderUtils2D.drawRoundedRect(endX + xOffset,
                    y - (extensions ? extensionHeight : 0.0f),
                    bgRoundRadius,
                    endX + bgWidth + xOffset,
                    y + menuHeight + (extensions ? extensionHeight : 0.0f),
                    false,
                    true,
                    true,
                    true,
                    true,
                    expandAnimate ? new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(bgAlpha * animateDelta / 300.0f)).getRGB() : bgColor.getRGB());
        } else {
            RenderUtils2D.drawRect(endX + xOffset,
                    y - (extensions ? extensionHeight : 0.0f),
                    endX + bgWidth + xOffset,
                    y + menuHeight + (extensions ? extensionHeight : 0.0f),
                    expandAnimate ? new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(bgAlpha * animateDelta / 300.0f)).getRGB() : bgColor.getRGB());
        }

        float scrollBarHeight = (menuHeight + extensionHeight * 2.0f)
                * MathUtilFuckYou.clamp(menuHeight / (height * totalElements + (height * reboundSpace * 2.0f)), 0.0f, 1.0f);

        float scrollBarY = y - (extensions ? extensionHeight : 0.0f)
                + getScrollProgress(height * totalElements + (height * reboundSpace * 2.0f), menuHeight, scrollOffset * -1.0f)
                * (menuHeight + (extensions ? extensionHeight * 2.0f : 0.0f) - scrollBarHeight);

        //bar shadow
        if (barShadow) {
            RenderUtils2D.drawBetterRoundRectFade(endX + xOffset - barExtraWidth * 0.5f,
                    scrollBarY,
                    endX + bgWidth + xOffset + barExtraWidth * 0.5f,
                    scrollBarY + scrollBarHeight,
                    barShadowSize,
                    40.0f,
                    false,
                    true,
                    false,
                    new Color(0, 0, 0, expandAnimate ? (int)((barShadowAlpha / 300.0f) * animateDelta) : barShadowAlpha).getRGB());
        }

        //bar rect
        if (barRounded) {
            RenderUtils2D.drawRoundedRect(endX + xOffset - barExtraWidth * 0.5f,
                    scrollBarY,
                    barRoundRadius,
                    endX + bgWidth + xOffset + barExtraWidth * 0.5f,
                    scrollBarY + scrollBarHeight,
                    false,
                    true,
                    true,
                    true,
                    true,
                    expandAnimate ? new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), (int)(barAlpha * animateDelta / 300.0f)).getRGB() : barColor.getRGB());
        } else {
            RenderUtils2D.drawRect(endX + xOffset - barExtraWidth * 0.5f,
                    scrollBarY,
                    endX + bgWidth + xOffset + barExtraWidth * 0.5f,
                    scrollBarY + scrollBarHeight,
                    expandAnimate ? new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), (int)(barAlpha * animateDelta / 300.0f)).getRGB() : barColor.getRGB());
        }

        //bar pattern
        if (pattern) {
            float patternStartY = scrollBarY + scrollBarHeight * 0.5f;
            boolean isSmall = patternSmallOffset > 0.5f * (scrollBarHeight - patternDist * patternCount);
            float gap = patternDist * (isSmall && patternSmallMode == ClickGUI.PatternSmallMode.Shrink ? (scrollBarHeight / (patternSmallOffset * 2.0f + patternDist * patternCount)) : 1.0f);

            if (isSmall && patternSmallMode == ClickGUI.PatternSmallMode.Disappear) {
                return;
            }

            for (int i = 0; i < patternCount; i++) {
                if (patternRounded) {
                    RenderUtils2D.drawRoundedRect(endX + xOffset + bgWidth * 0.5f - ((bgWidth + barExtraWidth) * patternWidthFactor * 0.5f),
                            patternStartY + i * gap - (gap * patternCount) * 0.5f,
                            patternRoundedRadius,
                            endX + xOffset + bgWidth * 0.5f + ((bgWidth + barExtraWidth) * patternWidthFactor * 0.5f),
                            patternStartY + i * gap + patternHeight - (gap * patternCount) * 0.5f,
                            false,
                            true,
                            true,
                            true,
                            true,
                            patternRollingColor ? ColorUtil.rolledColor(patternColor.getColor(), patternRollColor.getColor(), i * 100, patternRollSpeed, patternRollSize)
                                    : patternColor.getColor());
                } else {
                    RenderUtils2D.drawRect(endX + xOffset + bgWidth * 0.5f - ((bgWidth + barExtraWidth) * patternWidthFactor * 0.5f),
                            patternStartY + i * gap - (gap * patternCount) * 0.5f,
                            endX + xOffset + bgWidth * 0.5f + ((bgWidth + barExtraWidth) * patternWidthFactor * 0.5f),
                            patternStartY + i * gap + patternHeight - (gap * patternCount) * 0.5f,
                            patternRollingColor ? ColorUtil.rolledColor(patternColor.getColor(), patternRollColor.getColor(), i * 100, patternRollSpeed, patternRollSize)
                                    : patternColor.getColor());
                }
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
            scrollDelta = MathUtilFuckYou.clamp(scrollDelta + ClickGUI.instance.stringInputCollectorScrollSpeed.getValue() * passedms / 5.0f, 0.0f, 300.0f);
        }

        float limit1 = ((height - 14) * ((StringSetting) setting).feederList.size() - ClickGUI.instance.stringInputCollectorHeight.getValue() + ((height - 14) * ClickGUI.instance.stringInputCollectorReboundSpace.getValue() * 2.0f)) * -1.0f;


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
                reboundDelta += passedms / 20.0f * ClickGUI.instance.stringInputCollectorReboundFactor.getValue();
            }

            reboundDelta = MathUtilFuckYou.clamp(reboundDelta, 0.0f, 300.0f);
            prevScrollY -= (reboundDelta / 300.0f) * reboundDist;
        } else {
            reboundMaxFlag = false;
        }

        scrollOffset = MathUtilFuckYou.interpNonLinear(prevScrollY, prevScrollY + ClickGUI.instance.scrollAmount.getValue() * scrollUpOrDown, scrollDelta / 300.0f, ClickGUI.instance.scrollFactor.getValue() / 2.6667f);
    }

    private void drawElements(int mouseX, int mouseY, int passedms, float startX, float endX, Color rectColor) {
        int textAnimateAlpha = (int)(((ClickGUI.instance.stringInputCollectorTextColor.getValue().getAlpha()) / 300.0f) * animateDelta1);
        textAnimateAlpha = (int) MathUtilFuckYou.clamp(textAnimateAlpha, 4, 300);
        Color textColor = new Color(ClickGUI.instance.stringInputCollectorTextColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorTextColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorTextColor.getValue().getColorColor().getBlue(), ClickGUI.instance.stringInputCollectorAnimate.getValue() ? textAnimateAlpha : ClickGUI.instance.stringInputCollectorTextColor.getValue().getAlpha());
        ArrayList<String> values = new ArrayList<>(((StringSetting) setting).feederList);
        HashMap<String, Rect> menuRectsTemp = new HashMap<>();
        float elementHeight = height - 14;
        int index = 0;
        int menuY = y + (int)scrollOffset + (int)(elementHeight * ClickGUI.instance.stringInputCollectorReboundSpace.getValue());

        for (String value : values) {
            float startY = menuY + elementHeight * index;
            float endY = menuY + elementHeight + elementHeight * index;

            //top rect
            RenderUtils2D.drawRect(startX + 1, startY + 1, endX - 1, (index == values.size() - 1) ? (endY - 1 - ClickGUI.instance.stringInputCollectorRectGap.getValue()) : (endY - ClickGUI.instance.stringInputCollectorRectGap.getValue()), rectColor.getRGB());

            //hover rect
            if (ClickGUI.instance.stringInputCollectorHoverRect.getValue()) {
                useMenuHover = true;
                renderHoverRect(moduleName + setting.getName() + value, mouseX, mouseY, startX + 1, startY + 1, endX, (index == values.size() - 1) ? (endY - 1 - ClickGUI.instance.stringInputCollectorRectGap.getValue()) : (endY - ClickGUI.instance.stringInputCollectorRectGap.getValue()), 1.0f, 0.0f, false);
            }

            //text
            EnumButton.drawText(value, mc, getLongestLength(values), x, menuY, width, elementHeight, elementHeight * index, textColor.getRGB(),
                    ClickGUI.instance.stringInputCollectorTextScale.getValue(), ClickGUI.instance.stringInputCollectorX.getValue(), ClickGUI.instance.stringInputCollectorExtraWidth.getValue(), ClickGUI.instance.stringInputCollectorMinimumWidth.getValue());

            drawX(value, mouseX, mouseY, passedms, elementHeight, startY, endX);

            menuRectsTemp.put(value, new Rect(startX + 1, startY + 1, endX, (index == values.size() - 1) ? (endY - 1 - ClickGUI.instance.stringInputCollectorRectGap.getValue()) : (endY - ClickGUI.instance.stringInputCollectorRectGap.getValue())));
            index++;
        }
        menuRects = menuRectsTemp;
    }

    private void drawX(String value, int mouseX, int mouseY, int passedms, float elementHeight, float startY, float endX) {
        collectorXAnimateMap.putIfAbsent(value, 0.0f);
        float xAnimateDelta = MathUtilFuckYou.clamp(collectorXAnimateMap.get(value) + ((Objects.equals(getValueHovered(mouseX, mouseY), value) ? 1.0f : -1.0f) * ClickGUI.instance.stringInputCollectorXAnimateSpeed.getValue() * (passedms < 1000 ? passedms : 1.0f)), 0.0f, 300.0f);
        float xAnimateAlphaFactor = (ClickGUI.instance.stringInputCollectorXAnimate.getValue() ? (xAnimateDelta / 300.0f) : 1.0f) * (ClickGUI.instance.stringInputCollectorAnimate.getValue() ? (animateDelta1 / 300.0f) : 1.0f);
        Color xColor = new Color(ClickGUI.instance.stringInputCollectorXColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorXColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorXColor.getValue().getColorColor().getBlue(), (int) MathUtilFuckYou.clamp(ClickGUI.instance.stringInputCollectorXColor.getValue().getAlpha() * xAnimateAlphaFactor, 5.0f, 255.0f));

        if (Objects.equals(getValueHovered(mouseX, mouseY), value) || (ClickGUI.instance.stringInputCollectorXAnimate.getValue() && collectorXAnimateMap.get(value) > 0.0f)) {
            GL11.glPushMatrix();
            if (ClickGUI.instance.stringInputCollectorXAnimateScale.getValue()) {
                GL11.glTranslatef((endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue() + FontManager.iconFont.getStringWidth("+") * 0.5f) * (1.0f - (xAnimateDelta / 300.0f)),
                        (startY + elementHeight * 0.5f) * (1.0f - (xAnimateDelta / 300.0f)),
                        0.0f);
                GL11.glScalef(xAnimateDelta / 300.0f, xAnimateDelta / 300.0f, 1.0f);
            }

            GL11.glTranslatef((endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue()) * (1.0f - ClickGUI.instance.stringInputCollectorXScale.getValue()),
                    (startY + elementHeight * 0.5f) * (1.0f - ClickGUI.instance.stringInputCollectorXScale.getValue()),
                    0.0f);
            GL11.glScalef(ClickGUI.instance.stringInputCollectorXScale.getValue(), ClickGUI.instance.stringInputCollectorXScale.getValue(), 1.0f);

            if (ClickGUI.instance.stringInputCollectorXGlow.getValue()) {
                int glowColor = new Color(ClickGUI.instance.stringInputCollectorXGlowColor.getValue().getColorColor().getRed(), ClickGUI.instance.stringInputCollectorXGlowColor.getValue().getColorColor().getGreen(), ClickGUI.instance.stringInputCollectorXGlowColor.getValue().getColorColor().getBlue(), (int)(ClickGUI.instance.stringInputCollectorXGlowColor.getValue().getAlpha() * xAnimateAlphaFactor)).getRGB();

                GlStateManager.disableAlpha();
                RenderUtils2D.drawCustomCircle(endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue() + FontManager.iconFont.getStringWidth("+") * 0.5f, startY + elementHeight * 0.5f, ClickGUI.instance.stringInputCollectorXGlowSize.getValue(), glowColor, new Color(0, 0, 0, 0).getRGB());
                GlStateManager.disableAlpha();
            }


            FontManager.iconFont.drawString("+", endX - ClickGUI.instance.stringInputCollectorXXOffset.getValue(), startY + elementHeight * 0.5f, xColor.getRGB());

            GL11.glPopMatrix();
        }
        collectorXAnimateMap.put(value, xAnimateDelta);
    }

    private boolean isValidInput(int keyCode) {
        if (((StringSetting) setting).noSymbols) {
            switch (keyCode) {
                case Keyboard.KEY_MINUS:
                case Keyboard.KEY_EQUALS:
                case Keyboard.KEY_LBRACKET:
                case Keyboard.KEY_RBRACKET:
                case Keyboard.KEY_SLASH:
                case Keyboard.KEY_BACKSLASH:
                case Keyboard.KEY_GRAVE:
                case Keyboard.KEY_SEMICOLON:
                case Keyboard.KEY_APOSTROPHE:
                case Keyboard.KEY_COMMA:
                case Keyboard.KEY_AT:
                case Keyboard.KEY_PERIOD: return false;
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                switch (keyCode) {
                    case Keyboard.KEY_0:
                    case Keyboard.KEY_1:
                    case Keyboard.KEY_2:
                    case Keyboard.KEY_3:
                    case Keyboard.KEY_4:
                    case Keyboard.KEY_5:
                    case Keyboard.KEY_6:
                    case Keyboard.KEY_7:
                    case Keyboard.KEY_8:
                    case Keyboard.KEY_9: return false;
                }
            }
        }

        switch (keyCode) {
            case Keyboard.KEY_A:
            case Keyboard.KEY_B:
            case Keyboard.KEY_C:
            case Keyboard.KEY_D:
            case Keyboard.KEY_E:
            case Keyboard.KEY_F:
            case Keyboard.KEY_G:
            case Keyboard.KEY_H:
            case Keyboard.KEY_I:
            case Keyboard.KEY_J:
            case Keyboard.KEY_K:
            case Keyboard.KEY_L:
            case Keyboard.KEY_M:
            case Keyboard.KEY_N:
            case Keyboard.KEY_O:
            case Keyboard.KEY_P:
            case Keyboard.KEY_Q:
            case Keyboard.KEY_R:
            case Keyboard.KEY_S:
            case Keyboard.KEY_T:
            case Keyboard.KEY_U:
            case Keyboard.KEY_V:
            case Keyboard.KEY_W:
            case Keyboard.KEY_X:
            case Keyboard.KEY_Y:
            case Keyboard.KEY_Z:
            case Keyboard.KEY_0:
            case Keyboard.KEY_1:
            case Keyboard.KEY_2:
            case Keyboard.KEY_3:
            case Keyboard.KEY_4:
            case Keyboard.KEY_5:
            case Keyboard.KEY_6:
            case Keyboard.KEY_7:
            case Keyboard.KEY_8:
            case Keyboard.KEY_9:
            case Keyboard.KEY_MINUS:
            case Keyboard.KEY_EQUALS:
            case Keyboard.KEY_LBRACKET:
            case Keyboard.KEY_RBRACKET:
            case Keyboard.KEY_SLASH:
            case Keyboard.KEY_BACKSLASH:
            case Keyboard.KEY_GRAVE:
            case Keyboard.KEY_SEMICOLON:
            case Keyboard.KEY_APOSTROPHE:
            case Keyboard.KEY_COMMA:
            case Keyboard.KEY_PERIOD:
            case Keyboard.KEY_SPACE: return true;
        }
        return false;
    }
}
