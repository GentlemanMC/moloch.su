package net.spartanb312.base.client;

import com.google.gson.*;
import me.afterdarkness.moloch.hud.huds.CustomHUDFont;
import me.afterdarkness.moloch.hud.huds.PacketLogger;
import me.afterdarkness.moloch.module.modules.client.*;
import me.afterdarkness.moloch.module.modules.other.EntityAlerter;
import me.afterdarkness.moloch.module.modules.visuals.Search;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.thread.BackgroundMainThread;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.engine.RenderEngine;
import net.spartanb312.base.gui.ClickGUIFinal;
import net.spartanb312.base.gui.Panel;
import net.spartanb312.base.gui.renderers.ClickGUIRenderer;
import net.spartanb312.base.gui.renderers.HUDEditorRenderer;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.module.modules.other.Spammer;
import net.spartanb312.base.utils.ListUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConfigManager {
    private static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser jsonParser = new JsonParser();
    private static final File FRIEND_ENEMY_FILE = new File("moloch.su/moloch_Friend_Enemy_Stuff.json");
    private static final File GUI_FILE = new File("moloch.su/moloch_GUI_Stuff.json");

    private static final File CONFIG_PATH = new File("moloch.su/moloch_CONFIG_PATH.json");
    public static String modulesPath = "moloch.su/config";
    private final List<File> configList = ListUtil.listOf(FRIEND_ENEMY_FILE, GUI_FILE);
    public static boolean isReloading;

    boolean shouldSave = false;

    public void shouldSave() {
        shouldSave = true;
    }

    public void onInit() {
        configList.forEach(it -> {
            if (!it.exists()) {
                shouldSave();
            }
        });
        if (shouldSave) saveAll();
    }

    public void saveGUI() {
        try {
            if (!GUI_FILE.exists()) {
                GUI_FILE.getParentFile().mkdirs();
                try {
                    GUI_FILE.createNewFile();
                } catch (Exception ignored) {
                }
            }
            JsonObject father = new JsonObject();
            List<Panel> panels = new ArrayList<>(ClickGUIRenderer.instance.panels);
            panels.addAll(HUDEditorRenderer.instance.panels);
            for (Panel panel : panels) {
                JsonObject jsonGui = new JsonObject();
                jsonGui.addProperty("X", panel.x);
                jsonGui.addProperty("Y", panel.y);
                jsonGui.addProperty("Extended", panel.extended);
                father.add(panel.category.categoryName, jsonGui);
            }
            JsonObject jsonDesGui = new JsonObject();
            jsonDesGui.addProperty("X", ClickGUIFinal.descriptionHubX);
            jsonDesGui.addProperty("Y", ClickGUIFinal.descriptionHubY);
            father.add("DescriptionHub", jsonDesGui);

            PrintWriter saveJSon = new PrintWriter(new FileWriter(GUI_FILE));
            saveJSon.println(gsonPretty.toJson(father));
            saveJSon.close();
        } catch (Exception e) {
            BaseCenter.log.error("Error while saving GUI config!");
            e.printStackTrace();
        }
    }

    public void loadGUI() {
        if (GUI_FILE.exists()) {
            try {
                BufferedReader loadJson = new BufferedReader(new FileReader(GUI_FILE));
                JsonObject guiJson = (JsonObject) jsonParser.parse(loadJson);
                loadJson.close();
                for (Map.Entry<String, JsonElement> entry : guiJson.entrySet()) {
                    Panel panel = ClickGUIRenderer.instance.getPanelByName(entry.getKey());
                    if (panel == null) panel = HUDEditorRenderer.instance.getPanelByName(entry.getKey());
                    JsonObject jsonGui = (JsonObject) entry.getValue();
                    if (panel != null) {
                        panel.x = jsonGui.get("X").getAsInt();
                        panel.y = jsonGui.get("Y").getAsInt();
                        panel.extended = jsonGui.get("Extended").getAsBoolean();
                    }
                    if (Objects.equals(entry.getKey(), "DescriptionHub")) {
                        ClickGUIFinal.descriptionHubX = jsonGui.get("X").getAsInt();
                        ClickGUIFinal.descriptionHubY = jsonGui.get("Y").getAsInt();
                    }
                }
            } catch (IOException e) {
                BaseCenter.log.error("Error while loading GUI config!");
                e.printStackTrace();
            }
        }
    }

    public void saveFriendsEnemies() {
        try {
            if (!FRIEND_ENEMY_FILE.exists()) {
                FRIEND_ENEMY_FILE.getParentFile().mkdirs();
                try {
                    FRIEND_ENEMY_FILE.createNewFile();
                } catch (Exception ignored) {
                }
            }

            JsonObject father = new JsonObject();

            saveFriend(father);
            saveEnemy(father);

            PrintWriter saveJSon = new PrintWriter(new FileWriter(FRIEND_ENEMY_FILE));
            saveJSon.println(gsonPretty.toJson(father));
            saveJSon.close();
        } catch (Exception e) {
            BaseCenter.log.error("Error while saving client stuff!");
            e.printStackTrace();
        }
    }

    private void loadFriendsEnemies() {
        if (FRIEND_ENEMY_FILE.exists()) {
            try {
                BufferedReader loadJson = new BufferedReader(new FileReader(FRIEND_ENEMY_FILE));
                JsonObject guiJason = (JsonObject) jsonParser.parse(loadJson);
                loadJson.close();
                for (Map.Entry<String, JsonElement> entry : guiJason.entrySet()) {
                    if (entry.getKey().equals("Friends")) {
                        JsonArray array = (JsonArray) entry.getValue();
                        array.forEach(it -> ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.add(it.getAsString()));
                    } else if (entry.getKey().equals("Enemies")) {
                        JsonArray array = (JsonArray) entry.getValue();
                        array.forEach(it -> ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.add(it.getAsString()));
                    }

                }
            } catch (IOException e) {
                BaseCenter.log.error("Error while loading client stuff!");
                e.printStackTrace();
            }
        }
    }

    private void saveFriend(JsonObject father) {
        JsonArray array = new JsonArray();
        ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.forEach(array::add);
        father.add("Friends", array);
    }
    private void saveEnemy(JsonObject father) {
        JsonArray array = new JsonArray();
        ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.forEach(array::add);
        father.add("Enemies", array);
    }

    private void loadModule() {
        ModuleManager.getModules().forEach(Module::onLoad);
    }

    private void saveModule() {
        ModuleManager.getModules().forEach(Module::onSave);
    }

    public static void loadAll(boolean isInit) {
        isReloading = true;
        if (ModuleManager.getModule(CustomHUDFont.class).isDisabled()) {
            ModuleManager.getModule(CustomHUDFont.class).enable();
        }
        if (ModuleManager.getModule(CustomFont.class).isDisabled()) {
            ModuleManager.getModule(CustomFont.class).enable();
        }
        if (ModuleManager.getModule(ChatSettings.class).isDisabled()) {
            ModuleManager.getModule(ChatSettings.class).enable();
        }
        if (ModuleManager.getModule(MoreClickGUI.class).isDisabled()) {
            ModuleManager.getModule(MoreClickGUI.class).enable();
        }
        if (ModuleManager.getModule(GlobalManagers.class).isDisabled()) {
            ModuleManager.getModule(GlobalManagers.class).enable();
        }
        if (ModuleManager.getModule(Configs.class).isDisabled()) {
            ModuleManager.getModule(Configs.class).enable();
        }
        if (ModuleManager.getModule(FriendsEnemies.class).isDisabled()) {
            ModuleManager.getModule(FriendsEnemies.class).enable();
        }
        if (ModuleManager.getModule(BackgroundThreadStuff.class).isDisabled()) {
            ModuleManager.getModule(BackgroundThreadStuff.class).enable();
        }
        if (ModuleManager.getModule(HoleSettings.class).isDisabled()) {
            ModuleManager.getModule(HoleSettings.class).enable();
        }

        getInstance().loadFriendsEnemies();
        getInstance().loadGUI();

        try {
            ModuleManager.getModules().forEach(Module::resetRepeatUnits);
            RenderEngine.stopThread();
            ConcurrentTaskManager.instance.backgroundMainThread.interrupt();
            ConcurrentTaskManager.instance.executor = null;

            getInstance().loadModule();

            ConcurrentTaskManager.instance.backgroundMainThread = new BackgroundMainThread();
            ConcurrentTaskManager.instance.backgroundMainThread.start();
            ConcurrentTaskManager.instance.executor = new ThreadPoolExecutor(ConcurrentTaskManager.workingThreads, Integer.MAX_VALUE, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            ModuleManager.getModules().forEach(module -> module.initRepeatUnits(true));
            RenderEngine.startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RPC.INSTANCE.syncRandomStatusList();
        EntityAlerter.INSTANCE.syncWhitelist();
        Search.INSTANCE.syncWhitelist();
        PacketLogger.INSTANCE.syncToPacketsFile();
        if (isInit) PacketLogger.INSTANCE.clearLogFile();
        Spammer.SPAM_FILE = "moloch.su/" + getConfigPathFromFile() + "/Spammer.txt";
        Spammer.INSTANCE.readSpamFile();
        isReloading = false;
    }

    public static void saveAll() {
        if (isReloading) return;
        getInstance().saveGUI();
        getInstance().saveModule();
    }

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public static void init() {
        instance = new ConfigManager();
        if (!CONFIG_PATH.exists()) {
            createConfigPath();
        }
        syncFileConfigPaths(true);
        instance.onInit();
    }

    public static void createConfigPath() {
        try {
            CONFIG_PATH.getParentFile().mkdirs();
            CONFIG_PATH.createNewFile();
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("config", "");

            PrintWriter saveJSon = new PrintWriter(new FileWriter(CONFIG_PATH));
            saveJSon.println(gsonPretty.toJson(jsonObject));
            saveJSon.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateConfigPath(String path) {
        if (!CONFIG_PATH.exists()) {
            createConfigPath();
        } else {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty(path, "");

            try {
                PrintWriter saveJSon = new PrintWriter(new FileWriter(CONFIG_PATH));
                saveJSon.println(gsonPretty.toJson(jsonObject));
                saveJSon.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getConfigPathFromFile() {
        if (CONFIG_PATH.exists()) {
            try {
                BufferedReader bufferedJson = new BufferedReader(new FileReader(CONFIG_PATH));
                JsonObject jsonObject = (JsonObject) jsonParser.parse(bufferedJson);
                bufferedJson.close();
                Map<String, JsonElement> map = new HashMap<>();
                jsonObject.entrySet().forEach(it -> map.put(it.getKey(), it.getValue()));
                for (String str : map.keySet()) {
                    return str;
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return "config";
    }

    public static void syncFileConfigPaths(boolean isInit) {
        RPC.INSTANCE.STATUS_RANDOM_FILE = new File("moloch.su/" + getConfigPathFromFile() + "/moloch_Random_Status.json");
        EntityAlerter.INSTANCE.WHITELIST_FILE = new File("moloch.su/" + getConfigPathFromFile() + "/moloch_EntityAlerter_Whitelist.json");
        Search.INSTANCE.WHITELISTED_BLOCKS_FILE = new File("moloch.su/" + getConfigPathFromFile() + "/moloch_Search_Whitelisted_Blocks.json");
        PacketLogger.INSTANCE.PACKETS_FILE = new File("moloch.su/" + getConfigPathFromFile() + "/moloch_PacketLogger_Packets.json");
        PacketLogger.INSTANCE.PACKETLOG_FILE = "moloch.su/" + getConfigPathFromFile() + "/moloch_PacketLogger_LogDump.txt";
        modulesPath = "moloch.su/" + getConfigPathFromFile();
        Configs.INSTANCE.loadConfigPath.setValue(getConfigPathFromFile());
        loadAll(isInit);
    }
}
