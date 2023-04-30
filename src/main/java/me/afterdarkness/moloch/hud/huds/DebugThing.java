package me.afterdarkness.moloch.hud.huds;

import net.minecraft.client.gui.ScaledResolution;
import net.spartanb312.base.client.FontManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.engine.AsyncRenderer;
import net.spartanb312.base.hud.HUDModule;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.modules.client.ClickGUI;

@ModuleInfo(name = "DebugThing", category = Category.HUD, description = "For developer debug stuff")
public class DebugThing extends HUDModule {
    public static float debugInt;
    public static String debugString;

    public DebugThing() {
        asyncRenderer = new AsyncRenderer() {
            @Override
            public void onUpdate(ScaledResolution resolution, int mouseX, int mouseY) {
                drawAsyncString(debugInt + "", x, y, ClickGUI.instance.globalColor.getValue().getColor(), true);
                drawAsyncString(debugString, x, y + FontManager.getHeight(), ClickGUI.instance.globalColor.getValue().getColor(), true);
                width = Math.max(FontManager.getWidthHUD(debugInt + ""), FontManager.getWidthHUD(debugString));
                height = FontManager.getHeight() * 2;
            }
        };
    }

    @Override
    public void onHUDRender(ScaledResolution resolution) {
        asyncRenderer.onRender();
    }
}
