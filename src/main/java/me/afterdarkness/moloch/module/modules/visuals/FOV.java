package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.event.events.render.FOVItemModifyEvent;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

@Parallel(runnable = true)
@ModuleInfo(name = "FOV", category = Category.VISUALS, description = "Change FOV beyond what minecraft normally allows")
public class FOV extends Module {

    Setting<Boolean> modifyItemFOV = setting("ModifyItemFOV", false).des("Modifies your heldmodel with your FOV");
    Setting<Float> fov = setting("FOV", 120.0f, 0.0f, 180.0f);
    Setting<Float> heldFov = setting("HeldFOV", 200.0f, 0.0f, 180.0f).des("FOV for held model").whenTrue(modifyItemFOV);

    @Override
    public void onDisable() {
        mc.gameSettings.fovSetting = 100.0f;
    }

    @Override
    public void onTick() {
        if (mc.gameSettings.fovSetting != fov.getValue()) {
            mc.gameSettings.fovSetting = fov.getValue();
        }
    }

    @Listener
    public void onFOVModifyItems(FOVItemModifyEvent event) {
        if (modifyItemFOV.getValue()) {
            event.cancel();
            event.fov = heldFov.getValue();
        }
    }
}
