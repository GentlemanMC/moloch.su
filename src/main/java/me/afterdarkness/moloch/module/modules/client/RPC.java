package me.afterdarkness.moloch.module.modules.client;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiMainMenu;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.Timer;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

//TODO: fix custom loading time
@Parallel(runnable = true)
@ModuleInfo(name = "DiscordRPC", category = Category.CLIENT, description = "Show people how cool you are :3", hasCollector = true)
public class RPC extends Module {


    public Setting<Boolean> custom = setting("Custom", false).des("Uses your own rpc application from discord developer portal for custom image and name");
    public Setting<String> applicationID = setting("CustomApplicationID", "", false, null, true).des("Application ID of custom rpc").whenTrue(custom);
    public Setting<String> customImageName = setting("CustomImageName", "", false, null, false).des("Name of image file in your custom rpc application (img size should be a square bteween 1024x and 512x)").whenTrue(custom);
    public Setting<Integer> rpcUpdateDelay = setting("UpdateDelay", 3000, 1000, 20000).des("Milliseconds it takes for RPC to change images or text");
    public Setting<Image> imageMode = setting("ImageMode", Image.Logo).des("What type of image to display on RPC").whenFalse(custom);
    public Setting<Boolean> larryImageRandom = setting("LarryImageRandom", true).des("Larry image randomized").whenAtMode(imageMode, Image.Larry).whenFalse(custom);
    public Setting<Larry> larryImage = setting("LarryImage", Larry.Loaf).des("Which larry image to display").whenFalse(larryImageRandom).whenAtMode(imageMode, Image.Larry).whenFalse(custom);
    public Setting<String> imageText = setting("ImageText", "its not ratted i swear", false, null, false).des("Text to show when image is hovered");
    public Setting<Boolean> randomizedStatus = setting("RandomizedStatus", false).des("Randomizes status from json file");
    public Setting<String> randomizedStatusInput = setting("RandomizedStatusInput", "", true, new ArrayList<>(), false).des("Input stuff to randomized status json").whenTrue(randomizedStatus);
    public Setting<String> status = setting("Status", "I stuck my dick in a meat grinder", false, null, false).des("Shows some message in RPC above play time").whenFalse(randomizedStatus);
    public Setting<Boolean> serverDetails = setting("ServerDetails", true).des("Shows current server information in details section");
    public Setting<Boolean> showIP = setting("ShowIP", true).des("Shows the server IP in your Discord RPC").whenTrue(serverDetails);
    public Setting<String> details = setting("Details", "Staring at my wall and vividly hallucinating rn", false, null, false).whenFalse(serverDetails);

    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    public static final DiscordRichPresence presence = new DiscordRichPresence();
    public static RPC INSTANCE;
    public File STATUS_RANDOM_FILE = new File("moloch.su/config/moloch_Random_Status.json");
    private boolean prevCustomValue = false;
    private final Timer updateTimer = new Timer();
    private final Random rnd = new Random();
    private int prevCacheSize = ((StringSetting) randomizedStatusInput).feederList.size();

    private final String[] larryImagesKey = new String[]{
            "bread",
            "image0",
            "larry5",
            "larry4",
            "larry3",
            "larry2",
            "larrybgless"
    };

    public RPC() {
        INSTANCE = this;
        syncRandomStatusList();
    }

    @Override
    public void onTick() {
        if (updateTimer.passed(rpcUpdateDelay.getValue())) {
            updateRPC();
            updateTimer.reset();
        }
    }

    @Override
    public void onTickCollector() {
        if (((StringSetting) randomizedStatusInput).feederList.size() < prevCacheSize) {
            try {
                updateJSon();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove randomized status message");
                e.printStackTrace();
            }
        }
        prevCacheSize = ((StringSetting) randomizedStatusInput).feederList.size();

        if (!((StringSetting) randomizedStatusInput).listening && !Objects.equals(randomizedStatusInput.getValue(), "")) {
            writeToRandomStatuslist(randomizedStatusInput.getValue());
            randomizedStatusInput.setValue("");
        }
    }

    @Override
    public void onEnable() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize(custom.getValue() ? applicationID.getValue() : "1000211386514800670", handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        rpc.Discord_UpdatePresence(presence);
    }

    @Override
    public void onDisable() {
        rpc.Discord_Shutdown();
    }

    private void updateRPC() {
        if (custom.getValue() != prevCustomValue) {
            rpc.Discord_Shutdown();
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize(custom.getValue() ? applicationID.getValue() : "1000211386514800670", handlers, true, "");
            presence.startTimestamp = System.currentTimeMillis() / 1000L;
            rpc.Discord_UpdatePresence(presence);
        }
        prevCustomValue = custom.getValue();

        rpc.Discord_RunCallbacks();
        if (serverDetails.getValue()) {
            presence.details = mc.currentScreen instanceof GuiMainMenu ? "In the main menu." : "Playing " + (mc.currentServerData != null ? (showIP.getValue() ? "on " + mc.currentServerData.serverIP + "." : " multiplayer.") : " singleplayer.");
        }
        else {
            presence.details = details.getValue();
        }

        if (randomizedStatus.getValue() && ((StringSetting) randomizedStatusInput).feederList.size() > 0) {
            presence.state = ((StringSetting) randomizedStatusInput).feederList.get(new Random().nextInt(((StringSetting) randomizedStatusInput).feederList.size()));
        }
        else {
            presence.state = status.getValue();
        }

        if (custom.getValue()) {
            presence.largeImageKey = customImageName.getValue();
        }
        else {
            switch (imageMode.getValue()) {
                case Logo: {
                    presence.largeImageKey = "moloch";
                    break;
                }

                case Larry: {
                    if (larryImageRandom.getValue()) {
                        presence.largeImageKey = larryImagesKey[rnd.nextInt(larryImagesKey.length)];
                    }
                    else {
                        presence.largeImageKey = larryImageKey();
                    }
                    break;
                }

                case NekoSquad: {
                    presence.largeImageKey = "nekosquadlogo";
                    break;
                }
            }
        }

        presence.largeImageText = imageText.getValue();

        rpc.Discord_UpdatePresence(presence);
    }

    private void updateJSon() throws IOException {
        JsonObject json = new JsonObject();

        for (String str : ((StringSetting) randomizedStatusInput).feederList) {
            json.addProperty(str, "");
        }

        PrintWriter saveJSon = new PrintWriter(new FileWriter(STATUS_RANDOM_FILE));
        saveJSon.println((new GsonBuilder().setPrettyPrinting().create()).toJson(json));
        saveJSon.close();
    }

    private void writeToRandomStatuslist(String message) {
        try {
            if (!STATUS_RANDOM_FILE.exists()) {
                STATUS_RANDOM_FILE.getParentFile().mkdirs();
                try {
                    STATUS_RANDOM_FILE.createNewFile();
                } catch (Exception ignored) {}
            }

            ((StringSetting) randomizedStatusInput).feederList.remove(message);
            ((StringSetting) randomizedStatusInput).feederList.add(message);
            updateJSon();
        }
        catch (Exception e) {
            BaseCenter.log.error("Smt went wrong while trying to save entity name to whitelist");
            e.printStackTrace();
        }
    }

    public void syncRandomStatusList() {
        ((StringSetting) randomizedStatusInput).feederList.clear();
        if (STATUS_RANDOM_FILE.exists()) {
            try {
                BufferedReader loadJson = new BufferedReader(new FileReader(STATUS_RANDOM_FILE));
                JsonObject json = (JsonObject) (new JsonParser()).parse(loadJson);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    ((StringSetting) randomizedStatusInput).feederList.add(entry.getKey());
                }
            }
            catch (IOException e) {
                BaseCenter.log.error("Smt went wrong while loading RPC random status messages");
                e.printStackTrace();
            }
        }
    }

    private String larryImageKey() {
        switch (larryImage.getValue()) {
            case Loaf: return "bread";
            case Squint: return "image0";
            case Hoodie: return "larry5";
            case Sleeping: return "larry4";
            case CloseStare: return "larry3";
            case FarStare: return "larry2";
            case Portrait: return "larrybgless";
        }
        return "";
    }

    enum Image {
        Logo,
        Larry,
        NekoSquad
    }

    enum Larry {
        Loaf,
        Squint,
        Hoodie,
        Sleeping,
        CloseStare,
        FarStare,
        Portrait
    }
}

