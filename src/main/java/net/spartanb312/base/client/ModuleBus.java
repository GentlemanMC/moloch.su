package net.spartanb312.base.client;

import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.event.Priority;
import net.spartanb312.base.core.event.decentralization.ListenableImpl;
import net.spartanb312.base.event.decentraliized.DecentralizedClientTickEvent;
import net.spartanb312.base.event.decentraliized.DecentralizedPacketEvent;
import net.spartanb312.base.event.decentraliized.DecentralizedRenderTickEvent;
import net.spartanb312.base.event.decentraliized.DecentralizedRenderWorldEvent;
import net.spartanb312.base.event.events.client.InputUpdateEvent;
import net.spartanb312.base.event.events.client.SettingUpdateEvent;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.event.events.render.RenderOverlayEvent;
import net.spartanb312.base.event.events.render.RenderWorldEvent;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.notification.NotificationManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleBus extends ListenableImpl {

    public ModuleBus() {
        BaseCenter.EVENT_BUS.register(this);
        listener(DecentralizedClientTickEvent.class, it -> onTick());
        listener(DecentralizedRenderTickEvent.class, this::onRenderTick);
        listener(DecentralizedRenderWorldEvent.class, this::onRenderWorld);
        listener(DecentralizedPacketEvent.Send.class, this::onPacketSend);
        listener(DecentralizedPacketEvent.Receive.class, this::onPacketReceive);
        subscribe();
    }

    private final List<Module> modules = new CopyOnWriteArrayList<>();

    public synchronized void register(Module module) {
        modules.add(module);
        BaseCenter.EVENT_BUS.register(module);
    }

    public void unregister(Module module) {
        modules.remove(module);
        BaseCenter.EVENT_BUS.unregister(module);
    }

    public boolean isRegistered(Module module) {
        return modules.contains(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    @Listener(priority = Priority.HIGHEST)
    public void onKey(InputUpdateEvent event) {
        modules.forEach(mod -> mod.onInputUpdate(event));
    }

    public void onTick() {
        try {
            ConcurrentTaskManager.runBlocking(it -> modules.forEach(module -> {
                if (module.parallelRunnable) {
                    it.launch(module::onTick);
                } else {
                    module.onTick();
                }
            }));
        } catch (Exception exception) {
            NotificationManager.fatal("Error while running Tick!");
            exception.printStackTrace();
        }
    }

    @Listener(priority = Priority.HIGHEST)
    public void onRenderTick(RenderOverlayEvent event) {
        try {
            ConcurrentTaskManager.runBlocking(it -> modules.forEach(module -> {
                module.onRender(event);
                if (module.parallelRunnable) {
                    it.launch(module::onRenderTick);
                } else {
                    module.onRenderTick();
                }
            }));
        } catch (Exception exception) {
            NotificationManager.fatal("Error while running RenderTick!");
            exception.printStackTrace();
        }
    }

    @Listener(priority = Priority.HIGHEST)
    public void onRenderWorld(RenderWorldEvent event) {
        WorldRenderPatcher.INSTANCE.patch(event);
    }

    @Listener(priority = Priority.HIGHEST)
    public void onPacketSend(PacketEvent.Send event) {
        try {
            modules.forEach(module -> module.onPacketSend(event));
        } catch (Exception exception) {
            NotificationManager.fatal("Error while running PacketSend!");
            exception.printStackTrace();
        }
    }

    @Listener(priority = Priority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        try {
            modules.forEach(module -> module.onPacketReceive(event));
        } catch (Exception exception) {
            NotificationManager.fatal("Error while running PacketReceive!");
            exception.printStackTrace();
        }
    }

    @Listener(priority = Priority.HIGHEST)
    public void onSettingChange(SettingUpdateEvent event) {
        try {
            modules.forEach(it -> it.onSettingChange(event.getSetting()));
        } catch (Exception exception) {
            NotificationManager.fatal("Error while running onSettingChange!");
            exception.printStackTrace();
        }
    }
}
