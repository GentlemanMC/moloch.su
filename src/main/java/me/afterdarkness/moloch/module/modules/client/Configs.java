package me.afterdarkness.moloch.module.modules.client;

import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.command.commands.Config;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

@Parallel(runnable = true)
@ModuleInfo(name = "Configs", category = Category.CLIENT, description = "Manages different configurations in moloch.su folder (default config is the folder named 'config')")
public class Configs extends Module {

    Setting<Boolean> reloadConfig = setting("ReloadConfig", false).des("Force syncs loaded config with the current config folder");
    Setting<Boolean> saveConfig = setting("SaveConfig", false).des("Force saves current config to the current config folder");
    public Setting<String> loadConfigPath = setting("LoadConfigPath", ConfigManager.getConfigPathFromFile(), false, null, true).des("Current name of config folder (new config will be created if name isn't in moloch.su folder)");

    private boolean prevTypingState = false;
    public static Configs INSTANCE;

    public Configs() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (reloadConfig.getValue()) {
            reloadConfig.setValue(false);
            Config.reload();
        }

        if (saveConfig.getValue()) {
            saveConfig.setValue(false);
            Config.save();
        }

        if (prevTypingState && !((StringSetting) loadConfigPath).listening) {
            Config.load(loadConfigPath.getValue());
        }
        prevTypingState = ((StringSetting) loadConfigPath).listening;
    }

    @Override
    public void onDisable() {
        enable();
    }
}
