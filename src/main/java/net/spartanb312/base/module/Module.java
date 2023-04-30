package net.spartanb312.base.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.afterdarkness.moloch.core.common.Visibility;
import me.afterdarkness.moloch.core.setting.settings.ColorSetting;
import me.afterdarkness.moloch.core.setting.settings.VisibilitySetting;
import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import net.minecraft.client.Minecraft;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.common.KeyBind;
import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.core.config.ListenableContainer;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.*;
import net.spartanb312.base.event.events.client.InputUpdateEvent;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.event.events.render.RenderOverlayEvent;
import net.spartanb312.base.gui.ClickGUIFinal;
import net.spartanb312.base.gui.HUDEditorFinal;
import net.spartanb312.base.notification.NotificationManager;
import net.spartanb312.base.utils.ChatUtil;

import java.io.*;
import java.util.*;

public class Module extends ListenableContainer {

    public final String name = getAnnotation().name();
    public final Category category = getAnnotation().category();
    public final Parallel annotation = getClass().getAnnotation(Parallel.class);
    public final boolean parallelRunnable = annotation != null && annotation.runnable();
    public final String description = getAnnotation().description();
    public final List<KeyBind> keyBinds = new ArrayList<>();
    public boolean enabled = false;
    public boolean moduleToggleFlag = false;

    protected final Setting<Boolean> enabledSetting = setting("Enabled", false).when(() -> false);
    public final Setting<KeyBind> bindSetting = setting("Bind", subscribeKey(new KeyBind(getAnnotation().keyCode(), this::toggle))).des("The key bind of this module");
    public final Setting<Visibility> visibleSetting = setting("Visible", new Visibility(true)).des("Determine the visibility of the module");
    public final Setting<String> displayName = setting("DisplayName", name, false, null, false).des("Display name of module on arraylist and toggle notifications");

    public static Minecraft mc = Minecraft.getMinecraft();

    public void onSave() {
        enabledSetting.setValue(enabled);
        saveConfig();
    }

    public void onLoad() {
        readConfig();
        if (enabledSetting.getValue() && !enabled) enable();
        else if (!enabledSetting.getValue() && enabled) disable();
    }

    public void saveConfig() {
        if (Objects.equals(name, "BackgroundThreadStuff")) return;

        File FILE = new File(ConfigManager.modulesPath + "/modules/" + category.categoryName + "/" + (Objects.equals(name, "Friends/Enemies") ? "FriendsEnemies" : name) + ".json");
        if (!FILE.exists()) {
            try {
                FILE.getParentFile().mkdirs();
                FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JsonObject jsonObject = new JsonObject();
            for (Setting<?> setting : settings) {
                if (setting instanceof BindSetting) {
                    jsonObject.addProperty(setting.getName(), ((BindSetting) setting).getValue().getKeyCode());
                }
                if (setting instanceof VisibilitySetting) {
                    jsonObject.addProperty(setting.getName(), ((VisibilitySetting) setting).getValue().getVisible());
                }
                if (setting instanceof ColorSetting) {
                    jsonObject.addProperty(setting.getName(), ((ColorSetting) setting).getValue().getColor());
                    if (!Objects.equals(setting.getName(), "GlobalColor")) {
                        jsonObject.addProperty(setting.getName() + "SyncGlobal", ((ColorSetting) setting).getValue().getSyncGlobal());
                    }
                    jsonObject.addProperty(setting.getName() + "Rainbow", ((ColorSetting) setting).getValue().getRainbow());
                    jsonObject.addProperty(setting.getName() + "RainbowSpeed", ((ColorSetting) setting).getValue().getRainbowSpeed());
                    jsonObject.addProperty(setting.getName() + "RainbowSaturation", ((ColorSetting) setting).getValue().getRainbowSaturation());
                    jsonObject.addProperty(setting.getName() + "RainbowBrightness", ((ColorSetting) setting).getValue().getRainbowBrightness());
                }
                if (setting instanceof BooleanSetting) {
                    jsonObject.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
                }
                if (setting instanceof DoubleSetting) {
                    jsonObject.addProperty(setting.getName(), ((DoubleSetting) setting).getValue());
                }
                if (setting instanceof EnumSetting) {
                    jsonObject.addProperty(setting.getName(), ((EnumSetting<? extends Enum<?>>) setting).getValue().name());
                }
                if (setting instanceof FloatSetting) {
                    jsonObject.addProperty(setting.getName(), ((FloatSetting) setting).getValue());
                }
                if (setting instanceof IntSetting) {
                    jsonObject.addProperty(setting.getName(), ((IntSetting) setting).getValue());
                }
                if (setting instanceof StringSetting) {
                    jsonObject.addProperty(setting.getName(), ((StringSetting) setting).getValue());
                }
            }
            try {
                PrintWriter saveJSon = new PrintWriter(new FileWriter(FILE));
                saveJSon.println(gsonPretty.toJson(jsonObject));
                saveJSon.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readConfig() {
        if (Objects.equals(name, "BackgroundThreadStuff")) return;

        File FILE = new File(ConfigManager.modulesPath + "/modules/" + category.categoryName + "/" + (Objects.equals(name, "Friends/Enemies") ? "FriendsEnemies" : name) + ".json");
        if (FILE.exists()) {
            try {
                BufferedReader bufferedJson = new BufferedReader(new FileReader(FILE));
                JsonObject jsonObject = (JsonObject) jsonParser.parse(bufferedJson);
                bufferedJson.close();
                Map<String, JsonElement> map = new HashMap<>();
                jsonObject.entrySet().forEach(it -> map.put(it.getKey(), it.getValue()));
                for (Setting<?> setting : settings) {

                    JsonElement element = map.get(setting.getName());
                    if (element != null) {

                        if (setting instanceof BindSetting) {
                            ((BindSetting) setting).getValue().setKeyCode(element.getAsInt());
                        } else if (setting instanceof VisibilitySetting) {
                            ((VisibilitySetting) setting).getValue().setVisible(element.getAsBoolean());
                        } else if (setting instanceof ColorSetting) {
                            ((ColorSetting) setting).getValue().setColor(element.getAsInt());
                            if (!Objects.equals(setting.getName(), "GlobalColor")) {
                                ((ColorSetting) setting).getValue().setSyncGlobal(jsonObject.get(setting.getName() + "SyncGlobal").getAsBoolean());
                            }
                            ((ColorSetting) setting).getValue().setRainbow(jsonObject.get(setting.getName() + "Rainbow").getAsBoolean());
                            ((ColorSetting) setting).getValue().setRainbowSpeed(jsonObject.get(setting.getName() + "RainbowSpeed").getAsFloat());
                            ((ColorSetting) setting).getValue().setRainbowSaturation(jsonObject.get(setting.getName() + "RainbowSaturation").getAsFloat());
                            ((ColorSetting) setting).getValue().setRainbowBrightness(jsonObject.get(setting.getName() + "RainbowBrightness").getAsFloat());
                        } else if (setting instanceof DoubleSetting) {
                            ((DoubleSetting) setting).setValue(element.getAsDouble());
                        } else if (setting instanceof EnumSetting) {
                            ((EnumSetting<?>) setting).setByName(element.getAsString());
                        } else if (setting instanceof FloatSetting) {
                            ((FloatSetting) setting).setValue(element.getAsFloat());
                        } else if (setting instanceof IntSetting) {
                            ((IntSetting) setting).setValue(element.getAsInt());
                        } else if (setting instanceof StringSetting) {
                            ((StringSetting) setting).setValue(element.getAsString());
                        } else if (setting instanceof BooleanSetting) {
                            ((BooleanSetting) setting).setValue(element.getAsBoolean());
                        }
                    }

                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            saveConfig();
        }
    }

    public KeyBind subscribeKey(KeyBind keyBind) {
        keyBinds.add(keyBind);
        return keyBind;
    }

    public KeyBind unsubscribeKey(KeyBind keyBind) {
        keyBinds.remove(keyBind);
        return keyBind;
    }

    public void toggle() {
        if (isEnabled()) disable();
        else enable();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public void enable() {
        enabled = true;
        BaseCenter.MODULE_BUS.register(this);
        subscribe();
        if (mc.currentScreen instanceof ClickGUIFinal || mc.currentScreen instanceof HUDEditorFinal) {
            moduleToggleFlag = true;
        }

        if (!(ChatSettings.INSTANCE.invisibleToggleMessages.getValue() && !visibleSetting.getValue().getVisible())) {
            NotificationManager.moduleToggle(this, displayName.getValue(), true);
        }

        onEnable();
    }

    public void disable() {
        enabled = false;
        BaseCenter.MODULE_BUS.unregister(this);
        unsubscribe();
        if (mc.currentScreen instanceof ClickGUIFinal || mc.currentScreen instanceof HUDEditorFinal) {
            moduleToggleFlag = true;
        }

        if (!(ChatSettings.INSTANCE.invisibleToggleMessages.getValue() && !visibleSetting.getValue().getVisible())) {
            NotificationManager.moduleToggle(this, displayName.getValue(), false);
        }

        onDisable();
    }

    public void onPacketReceive(PacketEvent.Receive event) {
    }

    public void onPacketSend(PacketEvent.Send event) {
    }

    public void onTick() {
    }

    public void onRenderTick() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onRender(RenderOverlayEvent event) {
    }

    public void onRenderWorld(RenderEvent event) {
    }

    public void onInputUpdate(InputUpdateEvent event) {
    }

    public void onSettingChange(Setting<?> setting) {
    }

    public void resetRepeatUnits() {
    }

    public void initRepeatUnits(boolean resume) {
    }

    public void onTickCollector() {
    }

    public boolean shouldPersistRender() {
        return false;
    }

    public Setting<VoidTask> actionListener(String name, VoidTask defaultValue) {
        ListenerSetting setting = new ListenerSetting(name, defaultValue);
        getSettings().add(setting);
        return setting;
    }

    @SafeVarargs
    public final <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }

    public ModuleInfo getAnnotation() {
        if (getClass().isAnnotationPresent(ModuleInfo.class)) {
            return getClass().getAnnotation(ModuleInfo.class);
        }
        throw new IllegalStateException("No Annotation on class " + this.getClass().getCanonicalName() + "!");
    }

    public String getModuleInfo() {
        return "";
    }

    public String getHudSuffix() {
        return this.displayName.getValue() + (!this.getModuleInfo().equals("") ? (ChatUtil.colored("7") + "[" + ChatUtil.colored("f") + this.getModuleInfo() + ChatUtil.colored("7") + "]") : this.getModuleInfo());
    }

    public static boolean fullNullCheck() {
        return mc.world != null || mc.player != null;
    }
}
