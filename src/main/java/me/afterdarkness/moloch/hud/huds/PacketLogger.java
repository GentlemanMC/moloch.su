package me.afterdarkness.moloch.hud.huds;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.afterdarkness.moloch.core.common.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.engine.AsyncRenderer;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.hud.HUDModule;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.utils.FileUtil;
import net.spartanb312.base.utils.graphics.RenderUtils2D;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.launch;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

//TODO:add extra packet info other than just the name   &&   make panel look less ugly
@ModuleInfo(name = "PacketLogger", category = Category.HUD, description = "Logs packets sent or received", hasCollector = true)
public class PacketLogger extends HUDModule {

    Setting<Boolean> send = setting("Send", true).des("Log client sent packets");
    Setting<Boolean> receive = setting("Receive", false).des("Log packets received from server");
    Setting<Boolean> writeToFile = setting("WriteToFile", false).des("Writes logged packets in txt file");
    Setting<PacketFilterMode> packetFilterMode = setting("PacketFilterMode", PacketFilterMode.Whitelist);
    Setting<String> packets = setting("Packets", "", true, new ArrayList<>(), false).des("Input packets (i.e. CPacketPlayer.Position)");
    Setting<Float> panelHeight = setting("PanelHeight", 200.0f, 1.0f, 500.0f).des("Height of packet logger panel");
    Setting<Float> panelWidth = setting("PanelWidth", 230.0f, 1.0f, 500.0f).des("Width of packet logger panel");
    Setting<Boolean> panelUpperFade = setting("PanelUpperFade", true).des("Draw a gradient extending from the top of the panel");
    Setting<Float> panelUpperFadeSize = setting("PanelUpperFadeSize", 30.0f, 0.0f, 50.0f).des("Size of gradient extending from top of panel").whenTrue(panelUpperFade);
    Setting<Float> textSeparationOffset = setting("TextSeparationOffset", 9.0f, 1.0f, 40.0f).des("Y distance between each line");
    Setting<Boolean> timestamps24hr = setting("TimeStamps24hrs", true).des("Writes time stamps in 24 hour format");
    Setting<Boolean> textShadow = setting("TextShadow", false).des("Draws shadow under text");
    Setting<Color> textColor = setting("TextColor", new Color(new java.awt.Color(185, 185, 185, 255).getRGB()));
    Setting<Color> panelColor = setting("PanelColor", new Color(new java.awt.Color(0, 0, 0, 200).getRGB()));
    Setting<Color> panelUpperFadeColor = setting("PanelUpperFadeColor", new Color(new java.awt.Color(0, 0, 0, 200).getRGB())).whenTrue(panelUpperFade);

    public static PacketLogger INSTANCE;
    public File PACKETS_FILE = new File("moloch.su/config/moloch_PacketLogger_Packets.json");
    public String PACKETLOG_FILE = "moloch.su/config/moloch_PacketLogger_LogDump.txt";
    private int prevCacheSize = ((StringSetting) packets).feederList.size();
    private final List<String> loggedPackets = new ArrayList<>();

    public PacketLogger() {
        INSTANCE = this;
        asyncRenderer = new AsyncRenderer() {
            @Override
            public void onUpdate(ScaledResolution resolution, int mouseX, int mouseY) {
                drawAsyncRect(x, y, x + panelWidth.getValue(), y + panelHeight.getValue(), panelColor.getValue().getColor());

                for (int i = 0; i < new ArrayList<>(loggedPackets).size(); i++) {
                    drawAsyncString(loggedPackets.get(i), x + 3, y + panelHeight.getValue() - ((i + 1) * textSeparationOffset.getValue()), textColor.getValue().getColor(), textShadow.getValue());
                }

                drawAsyncRect(x, y, x + panelWidth.getValue(), y + panelUpperFadeSize.getValue(), panelUpperFadeColor.getValue().getColor(), panelUpperFadeColor.getValue().getColor(), new java.awt.Color(0, 0, 0, 0).getRGB(), new java.awt.Color(0, 0, 0, 0).getRGB());
            }
        };
        syncToPacketsFile();
    }

    @Override
    public void onHUDRender(ScaledResolution resolution) {
        RenderUtils2D.betterScissor(x, y, panelWidth.getValue(), panelHeight.getValue());
        GL11.glEnable(GL_SCISSOR_TEST);
        asyncRenderer.onRender();
        GL11.glDisable(GL_SCISSOR_TEST);
    }

    @Override
    public void onTick() {
        height = panelHeight.getValue().intValue();
        width = panelWidth.getValue().intValue();

        if (loggedPackets.size() * textSeparationOffset.getValue() > panelHeight.getValue() + textSeparationOffset.getValue()) {
            launch(this::trimLoggedPackets);
        }
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (send.getValue() && isInPacketsFile(getPacketName(event.getPacket()))) {
            String str = new SimpleDateFormat(timestamps24hr.getValue() ? "k:mm" : "h:mm aa").format(new Date()) + ": " + getPacketName(event.getPacket());
            loggedPackets.add(0, str);

            if (writeToFile.getValue()) {
                try {
                    updateDumpFile(str);
                } catch (Exception e) {
                    BaseCenter.log.error("Smt went wrong while trying to log packet to file");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if (receive.getValue() && isInPacketsFile(getPacketName(event.getPacket()))) {
            String str = new SimpleDateFormat(timestamps24hr.getValue() ? "k:mm" : "h:mm aa").format(new Date()) + ": " + getPacketName(event.getPacket());
            loggedPackets.add(0, str);

            if (writeToFile.getValue()) {
                try {
                    updateDumpFile(str);
                } catch (Exception e) {
                    BaseCenter.log.error("Smt went wrong while trying to log packet to file");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onTickCollector() {
        if (((StringSetting) packets).feederList.size() < prevCacheSize) {
            try {
                updateJSon();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove packet names");
                e.printStackTrace();
            }
        }
        prevCacheSize = ((StringSetting) packets).feederList.size();

        if (!((StringSetting) packets).listening && !Objects.equals(packets.getValue(), "")) {
            writeToPacketsFile(packets.getValue());
            packets.setValue("");
        }
    }

    public void clearLogFile() {
        File file = new File(PACKETLOG_FILE);
        if (file.exists()) {
            try {
                PrintWriter file1 = new PrintWriter(new FileWriter(PACKETLOG_FILE));
                file1.close();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to clear packetlog file");
                e.printStackTrace();
            }
        }
    }

    private void trimLoggedPackets() {
        while(loggedPackets.size() * textSeparationOffset.getValue() > panelHeight.getValue() + textSeparationOffset.getValue()) {
            loggedPackets.remove(loggedPackets.size() - 1);
        }
    }

    private String getPacketName(Packet<?> packet) {
        String packetRawName = packet.getClass().getName();
        return packetRawName.substring(packetRawName.lastIndexOf(".") + 1).replaceAll("\\$", ".");
    }

    private boolean isInPacketsFile(String str) {
        for (String value : ((StringSetting) packets).feederList) {
            if (Objects.equals(str.toLowerCase(), value.toLowerCase())) {
                return packetFilterMode.getValue() == PacketFilterMode.Whitelist;
            }
        }
        return packetFilterMode.getValue() != PacketFilterMode.Whitelist;
    }

    private void updateJSon() throws IOException {
        JsonObject json = new JsonObject();

        for (String str : ((StringSetting) packets).feederList) {
            json.addProperty(str, "");
        }

        PrintWriter saveJSon = new PrintWriter(new FileWriter(PACKETS_FILE));
        saveJSon.println((new GsonBuilder().setPrettyPrinting().create()).toJson(json));
        saveJSon.close();
    }

    private void writeToPacketsFile(String message) {
        try {
            if (!PACKETS_FILE.exists()) {
                PACKETS_FILE.getParentFile().mkdirs();
                try {
                    PACKETS_FILE.createNewFile();
                } catch (Exception ignored) {}
            }

            ((StringSetting) packets).feederList.remove(message);
            ((StringSetting) packets).feederList.add(message);
            updateJSon();
        }
        catch (Exception e) {
            BaseCenter.log.error("Smt went wrong while trying to save packet to packets file");
            e.printStackTrace();
        }
    }

    public void syncToPacketsFile() {
        ((StringSetting) packets).feederList.clear();
        if (PACKETS_FILE.exists()) {
            try {
                BufferedReader loadJson = new BufferedReader(new FileReader(PACKETS_FILE));
                JsonObject json = (JsonObject) (new JsonParser()).parse(loadJson);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    ((StringSetting) packets).feederList.add(entry.getKey());
                }
            }
            catch (IOException e) {
                BaseCenter.log.error("Smt went wrong while loading packets file");
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateDumpFile(String str) throws IOException {
        File file = new File(PACKETLOG_FILE);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception ignored) {}
        }
        else {
            List<String> stuff = FileUtil.readTextFileAllLines(PACKETLOG_FILE);
            PrintWriter file1 = new PrintWriter(new FileWriter(PACKETLOG_FILE));

            stuff.add(str);

            for (String string : stuff) {
                file1.println(string);
            }

            file1.close();
        }
    }

    enum PacketFilterMode {
        Whitelist,
        BlackList
    }
}
