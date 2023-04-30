package me.afterdarkness.moloch.command.commands;

import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.utils.ChatUtil;
import me.afterdarkness.moloch.client.EnemyManager;
import net.minecraft.entity.player.EntityPlayer;
import net.spartanb312.base.command.Command;
import net.spartanb312.base.common.annotations.CommandInfo;
import net.spartanb312.base.utils.EntityUtil;

/**
 * Created by killRED on 2020
 * Updated by B_312 on 04/30/21
 */
@CommandInfo(command = "enemy", description = "enemy command.")
public class Enemy extends Command {

    @Override
    public void onCall(String s, String[] args) {
        try {
            if (args[0].equalsIgnoreCase("all")) {
                for (EntityPlayer player : mc.world.playerEntities) {
                    if (EntityUtil.isFakeLocalPlayer(player)) {
                        continue;
                    }
                    if (!player.isInvisible()) {
                        EnemyManager.add(player);
                    }
                }
            } else if (args[0].equalsIgnoreCase("get")) {
                ChatUtil.sendNoSpamMessage(((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.toString());
            } else if (args[0].equalsIgnoreCase("add")) {
                EnemyManager.add(args[1]);
                ChatUtil.printChatMessage("Added enemy : " + args[1]);
            } else if (args[0].equalsIgnoreCase("remove")) {
                EnemyManager.remove(args[1]);
                ChatUtil.printChatMessage("Removed enemy : " + args[1]);
            } else {
                ChatUtil.sendNoSpamErrorMessage(getSyntax());
            }

        } catch (Exception e) {
            ChatUtil.sendNoSpamErrorMessage(getSyntax());
        }
    }

    @Override
    public String getSyntax() {
        return "enemy <add/all/get/remove>";
    }

}
