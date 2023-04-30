package me.afterdarkness.moloch.module.modules.client;

import me.afterdarkness.moloch.core.common.Visibility;
import me.afterdarkness.moloch.event.events.player.DisconnectEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.graphics.SpartanTessellator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "BackgroundThreadStuff", category = Category.CLIENT, description = "Stuff in bg")
public class BackgroundThreadStuff extends Module {

    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    public final List<VoidTask> skeletonCalcsQueue = new CopyOnWriteArrayList<>();
    public static BackgroundThreadStuff INSTANCE;

    //is always invisible
    //i need to put this stuff somewhere so this is just the garbage dump for thread stuff that doesnt belong in any other module
    public BackgroundThreadStuff() {
        INSTANCE = this;
        repeatUnits.add(updateSkeletonCalc);
        this.initRepeatUnits(false);
        this.visibleSetting.setValue(new Visibility(false));
    }

    RepeatUnit updateSkeletonCalc = new RepeatUnit(() -> 1, () -> {
        if (!skeletonCalcsQueue.isEmpty()) {
            ConcurrentTaskManager.runBlocking(content -> skeletonCalcsQueue.forEach(content::launch));
            skeletonCalcsQueue.clear();
        }

        if (mc.world != null && mc.player != null) {
            for (EntityPlayer player : new HashMap<>(SpartanTessellator.skeletonVerticesMap).keySet()) {
                if (player.isDead || !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), player.getPositionVector(), 300.0f * 300.0f)) {
                    SpartanTessellator.skeletonVerticesMap.remove(player);
                }
            }
        }
    });

    @Override
    public void resetRepeatUnits() {
        repeatUnits.forEach(it -> {
            it.suspend();
            unregisterRepeatUnit(it);
        });
    }

    @Override
    public void initRepeatUnits(boolean resume) {
        repeatUnits.forEach(it -> {
            if (!(resume && isEnabled())) {
                it.suspend();
            }
            runRepeat(it);
            if (resume && isEnabled()) {
                it.resume();
            }
        });
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
        enable();
    }

    @Listener
    public void onDisconnect(DisconnectEvent event) {
        SpartanTessellator.skeletonVerticesMap.clear();
    }
}
