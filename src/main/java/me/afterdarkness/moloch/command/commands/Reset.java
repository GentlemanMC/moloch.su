package me.afterdarkness.moloch.command.commands;

import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.command.Command;
import net.spartanb312.base.common.annotations.CommandInfo;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.ChatUtil;

import java.util.Objects;

@CommandInfo(command = "reset", description = "reset a module to default config")
public class Reset extends Command {
    @Override
    public void onCall(String s, String[] args) {
        try {
            boolean flag = false;
            for (Module module : ModuleManager.getModules()) {
                if (Objects.equals(module.name, args[0])) {
                    flag = true;
                    module.getSettings().forEach(Setting::reset);
                    ChatUtil.sendNoSpamErrorMessage("Reset " + args[0] + "!");
                }
            }

            if (!flag) {
                ChatUtil.sendNoSpamErrorMessage(getSyntax());
            }

        } catch (Exception e) {
            ChatUtil.sendNoSpamErrorMessage(getSyntax());
        }
    }

    @Override
    public String getSyntax() {
        return "reset <module name>";
    }
}
