package me.afterdarkness.moloch.module.modules.client;

import me.afterdarkness.moloch.client.EnemyManager;
import net.minecraft.entity.Entity;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.client.FriendManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.math.Pair;

import java.util.*;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "Friends/Enemies", category = Category.CLIENT, description = "Manages friends and enemies", hasCollector = true)
public class FriendsEnemies extends Module {

    public Setting<String> friend = setting("Friend", "", true, new ArrayList<>(), true).des("Enter name of friend to add here");
    public Setting<String> enemy = setting("Enemy", "", true, new ArrayList<>(), true).des("Enter name of enemy to add here");
    Setting<Integer> entitiesUpdateDelay = setting("EntityUpdateDelay", 100, 1, 5000).des("Delay in milliseconds to update entity list");
    Setting<Integer> friendEnemyUpdateDelay = setting("FriendEnemyUpdateDelay", 100, 1, 5000).des("Delay in milliseconds to update friends and enemies");

    public static FriendsEnemies INSTANCE;
    public final HashMap<Entity, Pair<Boolean, Boolean>> entityData = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private int prevFriendSize = ((StringSetting) friend).feederList.size();
    private int prevEnemySize = ((StringSetting) enemy).feederList.size();
    private final Timer updateTimer = new Timer();

    public FriendsEnemies() {
        INSTANCE = this;
        repeatUnits.add(updateEntities);
        this.initRepeatUnits(false);
    }

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

    RepeatUnit updateEntities = new RepeatUnit(() -> entitiesUpdateDelay.getValue(), () -> {
        if (mc.world == null) {
            return;
        }
        List<Entity> entities = new ArrayList<>(mc.world.loadedEntityList);
        HashMap<Entity, Pair<Boolean, Boolean>> tempEntityData = new HashMap<>();
        entities.forEach(entity -> tempEntityData.put(entity, new Pair<>(
                entityData.get(entity) != null && entityData.get(entity).a,
                entityData.get(entity) != null && entityData.get(entity).b)));
        synchronized (entityData) {
            entityData.clear();
            entityData.putAll(tempEntityData);
        }

        if (updateTimer.passed(friendEnemyUpdateDelay.getValue())) {
            synchronized (entityData) {
                entityData.keySet().forEach(key -> entityData.put(key, new Pair<>(FriendManager.isFriend(key), EnemyManager.isEnemy(key))));
            }
            updateTimer.reset();
        }
    });

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
        enable();
    }

    @Override
    public void onTickCollector() {
        if (((StringSetting) friend).feederList.size() < prevFriendSize) {
            try {
                ConfigManager.getInstance().saveFriendsEnemies();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove friend");
                e.printStackTrace();
            }
        }
        prevFriendSize = ((StringSetting) friend).feederList.size();

        if (((StringSetting) enemy).feederList.size() < prevEnemySize) {
            try {
                ConfigManager.getInstance().saveFriendsEnemies();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove enemy");
                e.printStackTrace();
            }
        }
        prevEnemySize = ((StringSetting) enemy).feederList.size();

        if (!((StringSetting) friend).listening && !Objects.equals(friend.getValue(), "")) {
            FriendManager.add(friend.getValue());
            friend.setValue("");
        }

        if (!((StringSetting) enemy).listening && !Objects.equals(enemy.getValue(), "")) {
            EnemyManager.add(enemy.getValue());
            enemy.setValue("");
        }
    }
}
