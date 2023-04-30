package net.spartanb312.base.core.config;

import com.google.gson.*;
import net.spartanb312.base.core.common.KeyBind;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.*;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.core.common.Visibility;
import me.afterdarkness.moloch.core.setting.settings.ColorSetting;
import me.afterdarkness.moloch.core.setting.settings.VisibilitySetting;

import java.util.ArrayList;
import java.util.List;

public class ConfigContainer {

    protected static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    protected static final JsonParser jsonParser = new JsonParser();

    protected final List<Setting<?>> settings = new ArrayList<>();

    public ConfigContainer() {
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public Setting<KeyBind> setting(String name, KeyBind defaultValue) {
        BindSetting setting = new BindSetting(name, defaultValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Visibility> setting(String name, Visibility defaultValue) {
        VisibilitySetting setting = new VisibilitySetting(name, defaultValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Color> setting(String name, Color defaultValue) {
        ColorSetting setting = new ColorSetting(name, defaultValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Boolean> setting(String name, boolean defaultValue) {
        BooleanSetting setting = new BooleanSetting(name, defaultValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Double> setting(String name, double defaultValue, double minValue, double maxValue) {
        DoubleSetting setting = new DoubleSetting(name, defaultValue, minValue, maxValue);
        settings.add(setting);
        return setting;
    }


    public <E extends Enum<E>> Setting<E> setting(String name, E defaultValue) {
        EnumSetting<E> setting = new EnumSetting<>(name, defaultValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Float> setting(String name, float defaultValue, float minValue, float maxValue) {
        FloatSetting setting = new FloatSetting(name, defaultValue, minValue, maxValue);
        settings.add(setting);
        return setting;
    }

    public Setting<Integer> setting(String name, int defaultValue, int minValue, int maxValue) {
        IntSetting setting = new IntSetting(name, defaultValue, minValue, maxValue);
        settings.add(setting);
        return setting;
    }

    public Setting<String> setting(String name, String defaultValue, boolean isCollector, List<String> feederList, boolean noSymbols) {
        StringSetting setting = new StringSetting(name, defaultValue, isCollector, feederList, noSymbols);
        settings.add(setting);
        return setting;
    }
}
