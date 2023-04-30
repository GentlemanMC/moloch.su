package net.spartanb312.base.utils;

import me.afterdarkness.moloch.module.modules.client.GlobalManagers;
import net.spartanb312.base.core.setting.Setting;
import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentString;

import static net.spartanb312.base.BaseCenter.mc;


public class ChatUtil {
    private static final int DeleteID = 114514;

    public static char SECTIONSIGN = '\u00A7';

    public static String colored(String code) {
        return SECTIONSIGN + code;
    }
    
    public static String bracketLeft (Setting<ChatSettings.Brackets> setting) {
        switch (setting.getValue()) {
            case Chevron: return "<";

            case Box: return "[";

            case Curly: return "{";

            case Round: return "(";

            case None: return " ";
        }
        return "";
    }

    public static String bracketRight (Setting<ChatSettings.Brackets> setting) {
        switch (setting.getValue()) {
            case Chevron: return ">";

            case Box: return "]";

            case Curly: return "}";

            case Round: return ")";

            case None: return " ";
        }
        return "";
    }

    public static String effectString(Setting<ChatSettings.Effects> setting) {
        switch (setting.getValue()) {
            case Bold: return SECTIONSIGN + "l";

            case Underline: return SECTIONSIGN + "n";

            case Italic: return SECTIONSIGN +"o";

            case None: return "";
        }
        return "";
    }

    public static String colorString(Setting<ChatSettings.StringColors> setting) {
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

            case RollColors: return "\u25b0";

            case Lgbtq: return "\u034f";
        }
        return "";
    }
    
    public static void sendNoSpamMessage(String message, int messageID) {
        sendNoSpamRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + colorString(ChatSettings.INSTANCE.stringColor) + effectString(ChatSettings.INSTANCE.effects) + GlobalManagers.INSTANCE.clientName.getValue() + "\u047e" + "\u00a7r" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message, messageID);
    }

    public static void sendNoSpamMessage(String message) {
        sendNoSpamRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + colorString(ChatSettings.INSTANCE.stringColor) + effectString(ChatSettings.INSTANCE.effects) + GlobalManagers.INSTANCE.clientName.getValue() + "\u047e" + "\u00a7r" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message);
    }

    public static void sendRawNoSpamMessage(String message) {
        sendNoSpamRawChatMessage("\u047e" + SECTIONSIGN + "r" + message);
    }

    public static void sendNoSpamMessage(String[] messages) {
        sendNoSpamMessage("");
        for (String s : messages) sendNoSpamRawChatMessage(s);
    }

    public static void sendNoSpamErrorMessage(String message) {
        sendNoSpamRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + "4" + SECTIONSIGN + "lERROR" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message);
    }

    public static void sendNoSpamErrorMessage(String message, int messageID) {
        sendNoSpamRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + "4" + SECTIONSIGN + "lERROR" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message, messageID);
    }

    public static void sendNoSpamRawChatMessage(String message) {
        sendSpamlessMessage(message);
    }

    public static void sendNoSpamRawChatMessage(String message, int messageID) {
        sendSpamlessMessage(messageID, message);
    }

    public static void printRawChatMessage(String message) {
        if (mc.player == null) return;
        ChatMessage(message);
    }

    public static void printChatMessage(String message) {
        printRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + colorString(ChatSettings.INSTANCE.stringColor) + effectString(ChatSettings.INSTANCE.effects) + GlobalManagers.INSTANCE.clientName.getValue() + "\u047e" + "\u00a7r" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message);
    }

    public static void printErrorChatMessage(String message) {
        printRawChatMessage("\u047e" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketLeft(ChatSettings.INSTANCE.brackets) + "\u047e" + SECTIONSIGN + "4" + SECTIONSIGN + "lERROR" + SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.bracketColor) + bracketRight(ChatSettings.INSTANCE.brackets) + " " + SECTIONSIGN + "r" + message);
    }

    public static void sendSpamlessMessage(String message) {
        if (mc.player == null) return;
        final GuiNewChat chat = mc.ingameGUI.getChatGUI();
        boolean hasRoll = message.contains("\u034f") || message.contains("\u25b0");
        chat.printChatMessageWithOptionalDeletion(new TextComponentString(!hasRoll ? message.replaceAll("\u047e", "") : message), DeleteID);
    }

    public static void sendSpamlessMessage(int messageID, String message) {
        if (mc.player == null) return;
        final GuiNewChat chat = mc.ingameGUI.getChatGUI();
        boolean hasRoll = message.contains("\u034f") || message.contains("\u25b0");
        chat.printChatMessageWithOptionalDeletion(new TextComponentString(!hasRoll ? message.replaceAll("\u047e", "") : message), messageID);
    }

    public static void ChatMessage(String message) {
        boolean hasRoll = message.contains("\u034f") || message.contains("\u25b0");
        mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(!hasRoll ? message.replaceAll("\u047e", "") : message));
    }
}
