package me.afterdarkness.moloch.module.modules.other;

import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import net.minecraft.network.play.server.SPacketChat;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.ChatUtil;
import net.spartanb312.base.utils.Timer;

@Parallel(runnable = true)
@ModuleInfo(name = "AntiUnicodeSpam", category = Category.OTHER, description = "Stops large unicode messages from being seen in chat")
public class AntiUnicodeSpam extends Module {

    Setting<Integer> maxSymbolCount = setting("MaxSymbolCount", 100, 1, 250).des("Max amount of unicode in chat message to block");
    Setting<Boolean> notifications = setting("Notifications", true).des("Chat notification mode for blocked messages");
    Setting<ChatSettings.StringColorsNoRainbow> notificationColor = setting("NotificationColor", ChatSettings.StringColorsNoRainbow.Aqua).des("Color of chat notification").whenTrue(notifications);
    Setting<Boolean> notificationMarked = setting("NotificationMarked", true).des("Put client name in front of chat notifications").whenTrue(notifications);

    private final Timer delay = new Timer();

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChat) {
            String text = ((SPacketChat) event.getPacket()).chatComponent.getFormattedText();
            int symbolCount = 0;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                if (isSymbol(c)) symbolCount++;
                if (symbolCount > maxSymbolCount.getValue()) {
                    if (notifications.getValue() && delay.passed(10)) {
                        if (notificationMarked.getValue()) {
                            ChatUtil.printChatMessage("[AntiUniscodeSpam] " + ChatUtil.colored(ChatSettings.INSTANCE.colorString(notificationColor)) + "Message blocked!");
                        } else {
                            ChatUtil.printRawChatMessage("[AntiUnicodeSpam] " + ChatUtil.colored(ChatSettings.INSTANCE.colorString(notificationColor)) + "Message blocked!");
                        }
                        delay.reset();
                    }

                    event.cancel();
                    break;
                }
            }
        }
    }

    private boolean isSymbol(char charIn) {
        return !((charIn >= 65 && charIn <= 90) || (charIn >= 97 && charIn <= 122))
                && !(charIn >= 48 && charIn <= 57);
    }
}
