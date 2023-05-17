package net.spartanb312.base;

import me.afterdarkness.moloch.client.EnemyManager;
import me.afterdarkness.moloch.client.PopManager;
import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.ServerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.spartanb312.base.client.*;
import net.spartanb312.base.core.event.EventManager;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.event.Priority;
import net.spartanb312.base.event.events.client.InitializationEvent;
import net.spartanb312.base.module.modules.client.ClickGUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runBlocking;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runTiming;

/**
 * Author B_312
 * Since 05/01/2021
 * Last update on 09/21/2021
 */

@Mod(modid = "moloch", name = "moloch.su", version = "b4")
public class BaseCenter {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static FontManager fontManager = new FontManager();

    public static final String AUTHOR = "傻卵 && popbob && B_312";
    public static final String GITHUB = "base -> https://github.com/SpartanB312/Cursa";
    public static final String VERSION = "b4";

    public static final Logger log = LogManager.getLogger("moloch.su");
    private static Thread mainThread;

    @Listener(priority = Priority.HIGHEST)
    public void preInitialize(InitializationEvent.PreInitialize event) {
        mainThread = Thread.currentThread();
    }
    @Mod.EventHandler
    public void Init(FMLInitializationEvent event) {
    }

    @Listener(priority = Priority.HIGHEST)
    public void initialize(InitializationEvent.Initialize event) {
        long tookTime = runTiming(() -> {
            Display.setTitle("moloch.su - " + VERSION);

            //Parallel load managers
            runBlocking(it -> {
                BaseCenter.log.info("Loading Font Manager");
                FontManager.init();

                BaseCenter.log.info("Loading Module Manager");
                ModuleManager.init();

                BaseCenter.log.info("Loading GUI Manager");
                it.launch(GUIManager::init);

                BaseCenter.log.info("Loading Command Manager");
                it.launch(CommandManager::init);

                BaseCenter.log.info("Loading Friend Manager");
                it.launch(FriendManager::init);

                BaseCenter.log.info("Loading Enemy Manager");
                it.launch(EnemyManager::init);

                BaseCenter.log.info("Loading Server Manager");
                it.launch(ServerManager::init);

                BaseCenter.log.info("Loading Pop Manager");
                it.launch(PopManager::init);

                BaseCenter.log.info("Loading Rotation Manager");
                it.launch(RotationManager::init);

                BaseCenter.log.info("Loading Config Manager");
                it.launch(ConfigManager::init);
            });
        });
        log.info("Launched in " + tookTime);
    }

    @Listener(priority = Priority.HIGHEST)
    public void postInitialize(InitializationEvent.PostInitialize event) {
        ClickGUI.instance.disable();
    }

    public static boolean isMainThread(Thread thread) {
        return thread == mainThread;
    }

    public static EventManager EVENT_BUS = new EventManager();
    public static ModuleBus MODULE_BUS = new ModuleBus();

    public static final BaseCenter instance = new BaseCenter();

}
