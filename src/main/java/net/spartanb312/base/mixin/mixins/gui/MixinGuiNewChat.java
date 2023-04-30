package net.spartanb312.base.mixin.mixins.gui;

import me.afterdarkness.moloch.mixinotherstuff.IChatLine;
import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import me.afterdarkness.moloch.module.modules.other.NameSpoof;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.spartanb312.base.client.FontManager;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.module.modules.visuals.NoRender;
import net.spartanb312.base.utils.ChatUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.spartanb312.base.BaseCenter.fontManager;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {

    @Shadow public abstract int getLineCount();
    @Shadow public abstract boolean getChatOpen();
    @Shadow public abstract float getChatScale();
    @Shadow public abstract int getChatWidth();
    @Shadow private int scrollPos;
    @Shadow private boolean isScrolled;
    @Shadow @Final public List<ChatLine> drawnChatLines;
    @Shadow @Final private Minecraft mc;
    private int intFlag = 0;
    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    public void drawChatHook(int updateCounter, CallbackInfo ci) {
        if (ChatSettings.INSTANCE.chatTimeStamps.getValue() || ModuleManager.getModule(NameSpoof.class).isEnabled() || (NoRender.INSTANCE.chat.getValue() && ModuleManager.getModule(NoRender.class).isEnabled())) {
            drawChat(updateCounter);
            ci.cancel();
        }
    }

    private void drawChat(int updateCounter) {
        intFlag = 0;
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            int j = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9f + 0.1f;

            if (j > 0) {
                boolean flag = this.getChatOpen();

                float f1 = this.getChatScale();
                int k = (int) MathUtilFuckYou.trollCeil((float) this.getChatWidth() / f1);
                GL11.glPushMatrix();
                GL11.glTranslatef(2.0f, 8.0f, 0.0f);
                GL11.glScalef(f1, f1, 1.0f);
                int l = 0;

                for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double)j1 / 200.0;
                            d0 = 1.0 - d0;
                            d0 = d0 * 10.0;
                            d0 = MathHelper.clamp(d0, 0.0D, 1.0);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0 * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++l;

                            if (l1 > 3) {
                                int j2 = -i1 * 9;
                                drawRect1(-2, j2 - 9, k + 4, j2, l1 / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                this.drawStringWithShadow(s, 0.0f, (float)(j2 - 8), 16777215 + (l1 << 24), chatline);
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag) {
                    int k2 = this.mc.fontRenderer.FONT_HEIGHT;
                    GL11.glTranslatef(-3.0f, 0.0f, 0.0f);
                    int l2 = j * k2 + j;
                    int i3 = l * k2 + l;
                    int j3 = this.scrollPos * i3 / j;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3)
                    {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect1(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect1(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GL11.glPopMatrix();
            }
        }
    }

    private void drawStringWithShadow(String text, float x, float y, int color, ChatLine currentLine) {
        if (ChatSettings.INSTANCE.chatTimeStamps.getValue()) {

            ChatSettings.drawnChatLines = new ArrayList<>(drawnChatLines);

            text = (ChatSettings.INSTANCE.chatTimeStampsColor.getValue() == ChatSettings.StringColors.Lgbtq ? "\u061c" : "") + ChatUtil.SECTIONSIGN + ChatUtil.colorString(ChatSettings.INSTANCE.chatTimeStampsColor) + ChatUtil.bracketLeft(ChatSettings.INSTANCE.chatTimeStampBrackets) + IChatLine.storedTime.get(currentLine) + ChatUtil.bracketRight(ChatSettings.INSTANCE.chatTimeStampBrackets) + (!text.contains("\u034f") ? "\u00a7r" : "") + "\u047e" + (ChatSettings.INSTANCE.chatTimeStampSpace.getValue() ? " " : "") + text;
        }

        if (ModuleManager.getModule(NameSpoof.class).isEnabled()) {
            text = text.replaceAll(mc.player.getName(), NameSpoof.INSTANCE.name.getValue());
        }

        if (text.contains("\u034f") || text.contains("\u25b0")) {
            fontManager.drawRolledString(text, x, y, ChatSettings.INSTANCE.lgbtqSpeed.getValue(), ChatSettings.INSTANCE.lgbtqSize.getValue(), ChatSettings.INSTANCE.lgbtqSaturation.getValue(), ChatSettings.INSTANCE.lgbtqBright.getValue(), color,
                    ChatSettings.INSTANCE.rollColor1.getValue().getColor(), ChatSettings.INSTANCE.rollColor2.getValue().getColor(), ChatSettings.INSTANCE.rollSpeed.getValue(), ChatSettings.INSTANCE.rollSize.getValue(), true);
        } else {
            mc.fontRenderer.drawStringWithShadow(text.replaceAll("\u047e", ""), x, y, color);
        }
    }

    private void drawRect1(int left, int top, int right, int bottom, int color) {
        intFlag += 1;
        if (!(NoRender.INSTANCE.chat.getValue() && NoRender.INSTANCE.isEnabled())) {
            Gui.drawRect(left, top, (ChatSettings.INSTANCE.chatTimeStamps.getValue() && intFlag < (getLineCount() + 1)) ? (right + FontManager.getWidth(ChatSettings.INSTANCE.chatTimeStamps24hr.getValue() ? "<88:88>      " : "<88:88 PM>      ")) : right, bottom, color);
        }
    }
}
