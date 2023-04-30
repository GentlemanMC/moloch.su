package me.afterdarkness.moloch.hud.huds;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.RubberbandEvent;
import net.minecraft.client.gui.ScaledResolution;
import net.spartanb312.base.client.FontManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.engine.AsyncRenderer;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.hud.HUDModule;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.Timer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@ModuleInfo(name = "LagNotify", category = Category.HUD, description = "Alerts you when your internet is down or when the server is lagging")
public class LagNotify extends HUDModule {

    Setting<Integer> timeout = setting("Timeout", 500, 1, 1500).des("Amount of milliseconds passed from last packet sent before you are considered to be lagging");
    Setting<Integer> checkInternetDelay = setting("CheckInternetDelay", 250, 1, 5000).des("Amount of milliseconds delay between every attempt to check if internet is out");
    Setting<Boolean> rubberband = setting("Rubberband", false).des("Detects if you have recently rubberbanded");
    Setting<Integer> rubberbandTimeout = setting("RubberbandTimeout", 1500, 1, 10000).des("Amount of milliseconds passed from last rubberband to stop showing warning").whenTrue(rubberband);
    Setting<Boolean> textShadow = setting("TextShadow", true).des("Draws shadow under text");
    Setting<Boolean> fade = setting("Fade", true).des("Warnings fade in and out");
    Setting<Float> fadeInSpeed = setting("FadeInSpeed", 1.5f, 0.1f, 3.0f).des("Fade speed when rendering warning in").whenTrue(fade);
    Setting<Float> fadeOutSpeed = setting("FadeOutSpeed", 0.7f, 0.1f, 3.0f).des("Fade speed on stopping rendering waring").whenTrue(fade);
    Setting<Color> color = setting("Color", new Color(new java.awt.Color(255, 100, 100, 255).getRGB()));

    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final Timer packetTimer = new Timer();
    private final Timer rubberbandTimer = new Timer();
    private final Timer alphaTimer = new Timer();
    private float alphaFactor = 5.0f;
    private boolean isInternetDown = false;
    private String lagStr = "";
    public static LagNotify INSTANCE;
    RepeatUnit updateInternet = new RepeatUnit(() -> checkInternetDelay.getValue(), LagNotify::updateInternet);

    public LagNotify() {
        INSTANCE = this;
        repeatUnits.add(updateInternet);
        this.initRepeatUnits(false);
        asyncRenderer = new AsyncRenderer() {
            @Override
            public void onUpdate(ScaledResolution resolution, int mouseX, int mouseY) {
                width = FontManager.getWidthHUD("Server hasn't responded for 9999999 ms");
                height = FontManager.getHeight();
                float alphaFactor1 = MathUtilFuckYou.clamp(alphaFactor, 0.0f, 300.0f);

                if ((fade.getValue() && alphaFactor1 > 5.0f) || isInternetDown || packetTimer.passed(timeout.getValue()) || (rubberband.getValue() && !rubberbandTimer.passed(rubberbandTimeout.getValue()))) {
                    drawAsyncCenteredString(lagStr, x + (width * 0.5f), y, new java.awt.Color(color.getValue().getColorColor().getRed(), color.getValue().getColorColor().getGreen(), color.getValue().getColorColor().getBlue(), (int)(color.getValue().getAlpha() * alphaFactor1 / 300.0f)).getRGB(), textShadow.getValue());
                }

                if ((!fade.getValue() || alphaFactor1 > 5.0f) && isInternetDown) {
                    java.awt.Color color1 = new java.awt.Color(color.getValue().getColorColor().getRed(), color.getValue().getColorColor().getGreen(), color.getValue().getColorColor().getBlue(), (int)(color.getValue().getAlpha() * alphaFactor1 / 300.0f));
                    java.awt.Color color2 = ColorUtil.colorHSBChange(color1, 0.5f, ColorUtil.ColorHSBMode.Brightness);
                    int color3 = ColorUtil.colorShift(color1.getRGB(), color2.getRGB(), 150.0f + (float) (150.0f * Math.sin(((System.currentTimeMillis() / 2.0) % 300.0) * (Math.PI / 150.0))) / 300.0f);

                    drawAsyncIcon("*", x + (width * 0.5f) - (FontManager.getWidthHUD(lagStr) * 0.5f) - FontManager.iconFont.getStringWidth("*") - 5.0f, y, new java.awt.Color(ColorUtil.getRed(color3), ColorUtil.getGreen(color3), ColorUtil.getBlue(color3), (int)(color.getValue().getAlpha() * alphaFactor1 / 300.0f)).getRGB());
                }
            }
        };
    }

    @Override
    public void resetRepeatUnits() {
        repeatUnits.forEach(it -> {
            it.suspend();
            unregisterRepeatUnit(it);
        });
    }

    @Override
    public void initRepeatUnits(boolean resume) {
        repeatUnits.forEach(it -> {
            if (!(resume && isEnabled())) {
                it.suspend();
            }
            runRepeat(it);
            if (resume && isEnabled()) {
                it.resume();
            }
        });
    }

    @Override
    public void onHUDEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onHUDDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        packetTimer.reset();
    }

    @Override
    public void onTick() {
        if (fade.getValue()) {
            int passedms = (int) alphaTimer.hasPassed();
            alphaTimer.reset();
            if (passedms < 1000) {
                if (isInternetDown || packetTimer.passed(timeout.getValue() ) || (rubberband.getValue() && !rubberbandTimer.passed(rubberbandTimeout.getValue()))) {
                    alphaFactor += passedms * fadeInSpeed.getValue();
                    if (alphaFactor > 300.0f) {
                        alphaFactor = 300.0f;
                    }
                }
                else {
                    alphaFactor -= passedms * fadeOutSpeed.getValue();
                    if (alphaFactor < 5.0f) {
                        alphaFactor = 5.0f;
                    }
                }
            }
        }

        if (rubberband.getValue() && !rubberbandTimer.passed(rubberbandTimeout.getValue())) {
            lagStr = "Rubberbanded " + rubberbandTimer.hasPassed() + " ms ago";
        }

        if (isInternetDown) {
            lagStr = "Ur internet's fucked";
        }
        else if (packetTimer.passed(timeout.getValue())) {
            lagStr = "Server hasn't responded for " + packetTimer.hasPassed() + " ms";
        }
    }

    @Override
    public void onHUDRender(ScaledResolution resolution) {
        asyncRenderer.onRender();
    }

    @Listener
    public void onRubberband(RubberbandEvent event) {
        if (rubberband.getValue()) {
            if (event.distance > 0.5 || (Math.sqrt(event.rotationDif[0] * event.rotationDif[0] + event.rotationDif[1] * event.rotationDif[1]) > 1.0)) {
                rubberbandTimer.reset();
            }
        }
    }

    private static void updateInternet() {
        INSTANCE.isInternetDown = true;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("1.1.1.1", 80), 300);
            socket.close();
            INSTANCE.isInternetDown = false;
        } catch (Exception ignored) {}
    }
}
