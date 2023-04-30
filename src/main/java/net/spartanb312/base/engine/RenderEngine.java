package net.spartanb312.base.engine;

import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.utils.ThreadUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static net.spartanb312.base.BaseCenter.mc;

public class RenderEngine {

    public static RenderEngine INSTANCE = new RenderEngine();
    private long lastUpdateTime = System.currentTimeMillis();
    public static boolean shouldRun = true;

    public RenderEngine() {
        new Thread(() -> {
            while (true) {
                if (shouldRun && mc.player != null) INSTANCE.onUpdate();
                ThreadUtil.delay();
            }
        }).start();
    }

    public static void startThread() {
        shouldRun = true;
    }

    public static void stopThread() {
        shouldRun = false;
    }

    private final List<AsyncRenderer> subscribedAsyncRenderers = new ArrayList<>();

    public static void subscribe(AsyncRenderer asyncRenderer) {
        synchronized (INSTANCE.subscribedAsyncRenderers) {
            if (!INSTANCE.subscribedAsyncRenderers.contains(asyncRenderer))
                INSTANCE.subscribedAsyncRenderers.add(asyncRenderer);
        }
    }

    public static void unsubscribe(AsyncRenderer asyncRenderer) {
        synchronized (INSTANCE.subscribedAsyncRenderers) {
            INSTANCE.subscribedAsyncRenderers.remove(asyncRenderer);
        }
    }

    private void onUpdate() {
        if (System.currentTimeMillis() - lastUpdateTime >= 15) {
            lastUpdateTime = System.currentTimeMillis();
            ScaledResolution resolution = new ScaledResolution(mc);
            int mouseX = Mouse.getX();
            int mouseY = Mouse.getY();
            synchronized (subscribedAsyncRenderers) {
                ConcurrentTaskManager.runBlocking(content -> subscribedAsyncRenderers.forEach(asyncRenderer -> content.launch(() -> asyncRenderer.onUpdate0(resolution, mouseX, mouseY))));
            }
        }
    }


}
