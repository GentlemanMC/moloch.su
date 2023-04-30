package me.afterdarkness.moloch.module.modules.client;

import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

@Parallel(runnable = true)
@ModuleInfo(name = "GlobalManagers", category = Category.CLIENT, description = "Change client global manager stuff that doesn't fit in any specific module")
public class GlobalManagers extends Module {

    Setting<Page> page = setting("Page", Page.Client);

    public Setting<String> clientName = setting("ClientName", "moloch.su", false, null, false).whenAtMode(page, Page.Client);
    public Setting<String> clientPrefix = setting("ClientPrefix", "+", false, null, false).whenAtMode(page, Page.Client);

    public Setting<Integer> swapTimeout = setting("SwapTimeout", 100, 1, 1000).des("Milliseconds to wait after each swap before another module can swap to their target item, to avoid conflicting swaps with multiple active swapping modules (i.e. surround swaps to obsidian, XP+ must wait this many milliseconds before it can swap to xp bottles)").whenAtMode(page, Page.Swap);

    public static GlobalManagers INSTANCE;
    public final String clientVersion = "b4";

    public GlobalManagers() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        enable();
    }

    enum Page {
        Client,
        Swap
    }
}
