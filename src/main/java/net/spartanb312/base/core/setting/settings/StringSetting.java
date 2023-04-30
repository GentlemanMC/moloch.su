package net.spartanb312.base.core.setting.settings;

import net.spartanb312.base.core.setting.Setting;

import java.util.List;

public class StringSetting extends Setting<String> {
    public boolean listening = false;
    public boolean isCollector;
    public List<String> feederList;
    public boolean noSymbols;

    public StringSetting(String name, String defaultValue, boolean isCollector, List<String> feederList, boolean noSymbols) {
        super(name, defaultValue);
        this.isCollector = isCollector;
        this.feederList = feederList;
        this.noSymbols = noSymbols;
    }
}
