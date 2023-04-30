package net.spartanb312.base.command.commands;

import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.command.Command;
import net.spartanb312.base.common.annotations.CommandInfo;
import net.spartanb312.base.utils.ChatUtil;

/**
 * Created by killRED on 2020
 * Updated by B_312 on 01/15/21
 */
@CommandInfo(command = "config", description = "Save, reload, or load new config.")
public class Config extends Command {

    @Override
    public void onCall(String s, String[] args) {
        if (!(args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("load"))) {
            ChatUtil.sendNoSpamErrorMessage("Invalid argument: Choose from save, reload, or load [config name] (default config is named 'config')");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save": {
                save();
                break;
            }

            case "reload": {
                reload();
                break;
            }

            case "load": {
                load(args[1]);
                break;
            }
        }
    }

    @Override
    public String getSyntax() {
        return "config <save/reload/load>";
    }

    public static void reload() {
        ConfigManager.loadAll(false);
        ChatUtil.sendNoSpamMessage("Configuration reloaded!");
    }

    public static void save() {
        ConfigManager.saveAll();
        ChatUtil.sendNoSpamMessage("Configuration saved!");
    }

    public static void load(String configName) {
        ConfigManager.saveAll();
        ConfigManager.updateConfigPath(configName);
        ConfigManager.syncFileConfigPaths(false);
        ChatUtil.sendNoSpamMessage("Loaded configuration " + configName + "!");
    }
}
