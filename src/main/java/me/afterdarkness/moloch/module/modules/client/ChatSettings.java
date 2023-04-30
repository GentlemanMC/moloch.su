package me.afterdarkness.moloch.module.modules.client;

import com.google.common.collect.Lists;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.mixinotherstuff.IChatLine;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.mixin.mixins.accessor.AccessorCPacketChatMessage;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "ChatSetting", category = Category.CLIENT, description = "Modify Other Chat")
public class ChatSettings extends Module {

    public static ChatSettings INSTANCE;
    public static List<ChatLine> drawnChatLines = Lists.newArrayList();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();

    Setting<Page> page = setting("Page", Page.ClientMessages);
    public Setting<Boolean> invisibleToggleMessages = setting("InvisibleToggleMessages", false).des("Modules that aren't visible will not send a chat notification on toggle").whenAtMode(page, Page.ClientMessages);
    public Setting<Boolean> autoDeleteToggleMessages = setting("AutoDeleteToggleMessages", true).des("Automatically deletes previous toggle messages (and maybe other client notifications) when another one is sent").whenAtMode(page, Page.ClientMessages);
    public Setting<Boolean> toggleMessagesMarked = setting("MarkedToggleMessages", true).des("Puts client name in front of module toggle messages").whenAtMode(page, Page.ClientMessages);
    //See NotificationManager
    public Setting<Brackets> brackets = setting("Brackets", Brackets.Chevron).des("Command Prefix Frame Brackets").whenAtMode(page, Page.ClientMessages);
    public Setting<Effects> effects = setting("Effects", Effects.None).des("Command Prefix Effect").whenAtMode(page, Page.ClientMessages);
    public Setting<Effects> moduleEffects = setting("ModuleEffects", Effects.Bold).des("Command Module Effect").whenAtMode(page, Page.ClientMessages);
    public Setting<StringColors> stringColor = setting("ChatColor", StringColors.DarkPurple).des("Color For Client Chat Stuff").whenAtMode(page, Page.ClientMessages);
    public Setting<StringColorsNoRainbow> bracketColor = setting("BracketColor", StringColorsNoRainbow.Gray).des("Color of client name brackets").whenAtMode(page, Page.ClientMessages);
    //check PopManager for popnotifications
    public Setting<Boolean> popNotifications = setting("PopNotifications", false).des("Puts client side notifications in chat whenever someone pops").whenAtMode(page, Page.PopNotification);
    public Setting<Boolean> popNotificationsMarked = setting("PopNotificationsMarked", true).des("Put client name in front of pop notification messages").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<Effects> popNotificationsEffect = setting("PopNotificationsEffect", Effects.None).des("Effects for pop notification message").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<StringColorsNoRainbow> popNotificationsColor = setting("PopNotificationsColor", StringColorsNoRainbow.DarkPurple).des("Color of pop notification message").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<StringColorsNoRainbow> popNotificationsPopNumColor = setting("PopNotifPopNumColor", StringColorsNoRainbow.White).des("Color of pop notification popped totems number").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<StringColorsNoRainbow> popNotificationNameColor = setting("PopNotifNameColors", StringColorsNoRainbow.White).des("Color of pop notification name").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<Effects> popNotificationNameEffect = setting("PopNotifNameEffect", Effects.None).des("Color of pop notification name").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<Effects> popNotificationsDeathEffect = setting("PopNotifDeathEffect", Effects.Bold).des("Effects for pop notification death message").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    public Setting<StringColorsNoRainbow> popNotificationsDeathColor = setting("PopNotifDeathColor", StringColorsNoRainbow.Red).des("Color of pop notification death message").whenTrue(popNotifications).whenAtMode(page, Page.PopNotification);
    Setting<Boolean> chatSuffix = setting("ChatSuffix", false).des("Appends Client Name To Message").whenAtMode(page, Page.Suffix);
    Setting<String> chatSuffixInput = setting("ChatSuffixInput", "moloch.su", false, null, true).des("Enter your suffix").whenTrue(chatSuffix).whenAtMode(page, Page.Suffix);
    Setting<String> chatSuffixSeparator = setting("ChatSuffixSeparator", " ", false, null, false).des("Enter your suffix separator").whenTrue(chatSuffix).whenAtMode(page, Page.Suffix);
    Setting<SuffixMode> chatSuffixMode = setting("ChatSuffixMode", SuffixMode.Superscript).des("Type of unicode text for use for suffix").whenTrue(chatSuffix).whenAtMode(page, Page.Suffix);
    public Setting<Boolean> chatTimeStamps = setting("ChatTimeStamps", false).des("Puts Time In Front Of Chat Messages").whenAtMode(page, Page.Timestamps);
    //See MixinGuiNewChat
    public Setting<Boolean> chatTimeStamps24hr = setting("ChatTimeStamps24hr", true).des("Chat TimeStamps In 24 Hours Format").whenTrue(chatTimeStamps).whenAtMode(page, Page.Timestamps);
    public Setting<StringColors> chatTimeStampsColor = setting("ChatTimeStampsColor", StringColors.Blue).des("Color For Chat TimeStamps").whenTrue(chatTimeStamps).whenAtMode(page, Page.Timestamps);
    public Setting<Brackets> chatTimeStampBrackets = setting("ChatTimeStampsBrackets", Brackets.Chevron).des("Brackets For Chat TimeStamps").whenTrue(chatTimeStamps).whenAtMode(page, Page.Timestamps);
    public Setting<Boolean> chatTimeStampSpace = setting("ChatTimStampsSpace", false).des("Space After Chat TimeStamps").whenTrue(chatTimeStamps).whenAtMode(page, Page.Timestamps);
    public Setting<Float> lgbtqSpeed = setting("Speed", 10.0f, 0.1f, 20.0f).when(() -> stringColor.getValue() == StringColors.Lgbtq || (chatTimeStampsColor.getValue() == StringColors.Lgbtq && chatTimeStamps.getValue())).des("Rainbow Speed").whenAtMode(page, Page.RollingColor);
    public Setting<Float> lgbtqSize = setting("ColorSize", 1.0f, 0.1f, 2.0f).when(() -> stringColor.getValue() == StringColors.Lgbtq || (chatTimeStampsColor.getValue() == StringColors.Lgbtq && chatTimeStamps.getValue())).des("Rainbow Size").whenAtMode(page, Page.RollingColor);
    public Setting<Float> lgbtqBright = setting("Brightness", 1.0f, 0.0f, 1.0f).when(() -> stringColor.getValue() == StringColors.Lgbtq || (chatTimeStampsColor.getValue() == StringColors.Lgbtq && chatTimeStamps.getValue())).des("Rainbow Brightness").whenAtMode(page, Page.RollingColor);
    public Setting<Float> lgbtqSaturation = setting("Saturation", 1.0f, 0.0f, 1.0f).when(() -> stringColor.getValue() == StringColors.Lgbtq || (chatTimeStampsColor.getValue() == StringColors.Lgbtq && chatTimeStamps.getValue())).des("Rainbow Saturation").whenAtMode(page, Page.RollingColor);
    public Setting<Float> rollSpeed = setting("RollSpeed", 10.0f, 0.1f, 20.0f).when(() -> stringColor.getValue() == StringColors.RollColors || (chatTimeStampsColor.getValue() == StringColors.RollColors && chatTimeStamps.getValue())).des("Roll colors speed").whenAtMode(page, Page.RollingColor);
    public Setting<Float> rollSize = setting("RollSize", 1.0f, 0.1f, 2.0f).when(() -> stringColor.getValue() == StringColors.RollColors || (chatTimeStampsColor.getValue() == StringColors.RollColors && chatTimeStamps.getValue())).des("Roll colors size").whenAtMode(page, Page.RollingColor);
    public Setting<Color> rollColor1 = setting("RollColor1", new Color(new java.awt.Color(100, 61, 255, 255).getRGB())).when(() -> stringColor.getValue() == StringColors.RollColors || (chatTimeStampsColor.getValue() == StringColors.RollColors && chatTimeStamps.getValue())).whenAtMode(page, Page.RollingColor);
    public Setting<Color> rollColor2 = setting("RollColor2", new Color(new java.awt.Color(173, 132, 255, 255).getRGB())).when(() -> stringColor.getValue() == StringColors.RollColors || (chatTimeStampsColor.getValue() == StringColors.RollColors && chatTimeStamps.getValue())).whenAtMode(page, Page.RollingColor);

    public ChatSettings() {
        INSTANCE = this;
        repeatUnits.add(cutMap);
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

    RepeatUnit cutMap = new RepeatUnit(() -> 10000, () -> {
        if (IChatLine.storedTime.size() > 200 || drawnChatLines.size() > 200) {
            HashMap<ChatLine, String> map;
            synchronized (IChatLine.storedTime) {
                map = new HashMap<>(IChatLine.storedTime);
            }
            for (Map.Entry<ChatLine, String> entry : map.entrySet()) {
                if (!drawnChatLines.contains(entry.getKey())) IChatLine.storedTime.remove(entry.getKey());
            }
        }
    });

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof CPacketChatMessage && chatSuffix.getValue()) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            if (s.startsWith("/") || s.startsWith("+") || s.startsWith(".") || s.startsWith("*") || s.startsWith("#") || s.startsWith(";") || s.endsWith(getSuffix())) return;
            s += chatSuffixSeparator.getValue() + getSuffix();
            if (s.length() >= 256) s = s.substring(0, 256);
            ((AccessorCPacketChatMessage) event.getPacket()).setMessage(s);
        }
    }


    @Override
    public void onDisable() {
        enable();
    }

    public enum Brackets {
        Chevron, Box, Curly, Round, None
    }

    public enum Effects {
        Bold, Underline, Italic, None
    }

    public enum StringColors {
        Black, Gold, Gray, Blue, Green, Aqua, Red, LightPurple, Yellow, White, DarkBlue, DarkGreen, DarkAqua, DarkRed, DarkPurple, DarkGray, Lgbtq, RollColors
    }

    public String colorString(Setting<StringColorsNoRainbow> setting) {
        switch (setting.getValue()) {
            case Black: return "0";

            case Gold: return "6";

            case Gray: return "7";

            case Blue: return "9";

            case Green: return "a";

            case Aqua: return "b";

            case Red: return "c";

            case LightPurple: return "d";

            case Yellow: return "e";

            case White: return "f";

            case DarkBlue: return "1";

            case DarkGreen: return "2";

            case DarkAqua: return "3";

            case DarkRed: return "4";

            case DarkPurple: return "5";

            case DarkGray: return "8";
        }
        return "";
    }

    private String getSuffix() {
        StringBuilder stringBuilder = new StringBuilder();
        switch (chatSuffixMode.getValue()) {
            case Superscript: {
                for (int i = 0; i < chatSuffixInput.getValue().length(); i++)
                    stringBuilder.append(superscriptStr(String.valueOf(chatSuffixInput.getValue().charAt(i))));
                break;
            }

            case Big: {
                for (int i = 0; i < chatSuffixInput.getValue().length(); i++)
                    stringBuilder.append(bigStr(String.valueOf(chatSuffixInput.getValue().charAt(i))));
                break;
            }

            case Circled: {
                for (int i = 0; i < chatSuffixInput.getValue().length(); i++)
                    stringBuilder.append(circledStr(String.valueOf(chatSuffixInput.getValue().charAt(i))));
                break;
            }

            case Normal: {
                stringBuilder.append(chatSuffixInput.getValue());
                break;
            }
        }
        return stringBuilder.toString();
    }

    private String superscriptStr(String str) {
        switch (str) {
            case "a": return "ᵃ";
            case "b": return "ᵇ";
            case "c":
            case "C":
                return "ᶜ";
            case "d": return "ᵈ";
            case "e": return "ᵉ";
            case "f":
            case "F":
                return "ᶠ";
            case "g": return "ᵍ";
            case "h": return "ʰ";
            case "i": return "ᶦ";
            case "j": return "ʲ";
            case "k": return "ᵏ";
            case "l": return "ˡ";
            case "m": return "ᵐ";
            case "n": return "ⁿ";
            case "o": return "ᵒ";
            case "p": return "ᵖ";
            case "q": return "ᑫ";
            case "r": return "ʳ";
            case "s":
            case "S":
                return "ˢ";
            case "t": return "ᵗ";
            case "u": return "ᵘ";
            case "v": return "ᵛ";
            case "w": return "ʷ";
            case "x": return "ˣ";
            case "y": return "ʸ";
            case "z": return "ᶻ";

            case "A": return "ᴬ";
            case "B": return "ᴮ";
            case "D": return "ᴰ";
            case "E": return "ᴱ";
            case "G": return "ᴳ";
            case "H": return "ᴴ";
            case "I": return "ᴵ";
            case "J": return "ᴶ";
            case "K": return "ᴷ";
            case "L": return "ᴸ";
            case "M": return "ᴹ";
            case "N": return "ᴺ";
            case "O": return "ᴼ";
            case "P": return "ᴾ";
            case "Q": return "Q";
            case "R": return "ᴿ";
            case "T": return "ᵀ";
            case "U": return "ᵁ";
            case "V": return "ⱽ";
            case "W": return "ᵂ";
            case "X": return "ˣ";
            case "Y": return "ʸ";
            case "Z": return "ᶻ";

            case " ": return " ";
        }
        return "";
    }

    private String bigStr(String str) {
        switch (str) {
            case "a": return "ａ";
            case "b": return "ｂ";
            case "c": return "ｃ";
            case "d": return "ｄ";
            case "e": return "ｅ";
            case "f": return "ｆ";
            case "g": return "ｇ";
            case "h": return "ｈ";
            case "i": return "ｉ";
            case "j": return "ｊ";
            case "k": return "ｋ";
            case "l": return "ｌ";
            case "m": return "ｍ";
            case "n": return "ｎ";
            case "o": return "ｏ";
            case "p": return "ｐ";
            case "q": return "ｑ";
            case "r": return "ｒ";
            case "s": return "ｓ";
            case "t": return "ｔ";
            case "u": return "ｕ";
            case "v": return "ｖ";
            case "w": return "ｗ";
            case "x": return "ｘ";
            case "y": return "ｙ";
            case "z": return "ｚ";

            case "A": return "Ａ";
            case "B": return "Ｂ";
            case "C": return "Ｃ";
            case "D": return "Ｄ";
            case "E": return "Ｅ";
            case "F": return "Ｆ";
            case "G": return "Ｇ";
            case "H": return "Ｈ";
            case "I": return "Ｉ";
            case "J": return "Ｊ";
            case "K": return "Ｋ";
            case "L": return "Ｌ";
            case "M": return "Ｍ";
            case "N": return "Ｎ";
            case "O": return "Ｏ";
            case "P": return "Ｐ";
            case "Q": return "Ｑ";
            case "R": return "Ｒ";
            case "S": return "Ｓ";
            case "T": return "Ｔ";
            case "U": return "Ｕ";
            case "V": return "Ｖ";
            case "W": return "Ｗ";
            case "X": return "Ｘ";
            case "Y": return "Ｙ";
            case "Z": return "Ｚ";

            case " ": return " ";
        }
        return "";
    }

    private String circledStr(String str) {
        switch (str) {
            case "a": return "ⓐ";
            case "b": return "ⓑ";
            case "c": return "ⓒ";
            case "d": return "ⓓ";
            case "e": return "ⓔ";
            case "f": return "ⓕ";
            case "g": return "ⓖ";
            case "h": return "ⓗ";
            case "i": return "ⓘ";
            case "j": return "ⓙ";
            case "k": return "ⓚ";
            case "l": return "ⓛ";
            case "m": return "ⓜ";
            case "n": return "ⓝ";
            case "o": return "ⓞ";
            case "p": return "ⓟ";
            case "q": return "ⓠ";
            case "r": return "ⓡ";
            case "s": return "ⓢ";
            case "t": return "ⓣ";
            case "u": return "ⓤ";
            case "v": return "ⓥ";
            case "w": return "ⓦ";
            case "x": return "ⓧ";
            case "y": return "ⓨ";
            case "z": return "ⓩ";

            case "A": return "Ⓐ";
            case "B": return "Ⓑ";
            case "C": return "Ⓒ";
            case "D": return "Ⓓ";
            case "E": return "Ⓔ";
            case "F": return "Ⓕ";
            case "G": return "Ⓖ";
            case "H": return "Ⓗ";
            case "I": return "Ⓘ";
            case "J": return "Ⓙ";
            case "K": return "Ⓚ";
            case "L": return "Ⓛ";
            case "M": return "Ⓜ";
            case "N": return "Ⓝ";
            case "O": return "Ⓞ";
            case "P": return "Ⓟ";
            case "Q": return "Ⓠ";
            case "R": return "Ⓡ";
            case "S": return "Ⓢ";
            case "T": return "Ⓣ";
            case "U": return "Ⓤ";
            case "V": return "Ⓥ";
            case "W": return "Ⓦ";
            case "X": return "Ⓧ";
            case "Y": return "Ⓨ";
            case "Z": return "Ⓩ";

            case " ": return " ";
        }
        return "";
    }

    public enum StringColorsNoRainbow {
        Black, Gold, Gray, Blue, Green, Aqua, Red, LightPurple, Yellow, White, DarkBlue, DarkGreen, DarkAqua, DarkRed, DarkPurple, DarkGray
    }

    enum Page {
        ClientMessages,
        PopNotification,
        Suffix,
        Timestamps,
        RollingColor
    }

    enum SuffixMode {
        Superscript,
        Big,
        Circled,
        Normal
    }
}
