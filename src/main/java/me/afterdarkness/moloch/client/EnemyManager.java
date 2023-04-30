package me.afterdarkness.moloch.client;

import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.minecraft.entity.Entity;
import net.spartanb312.base.client.ConfigManager;
import net.spartanb312.base.core.setting.settings.StringSetting;

public class EnemyManager {

    public static void init() {
        instance = new EnemyManager();
    }

    public static boolean isEnemy(Entity entity) {
        return isEnemy(entity.getName());
    }

    public static boolean isEnemy(String name) {
        return ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.contains(name);
    }

    public static void add(String name) {
        if (!((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.contains(name)) {
            ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.add(name);
            ConfigManager.getInstance().saveFriendsEnemies();
        }
    }

    public static void add(Entity entity) {
        if (!((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.contains(entity.getName())) {
            ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.add(entity.getName());
            ConfigManager.getInstance().saveFriendsEnemies();
        }
    }

    public static void remove(String name) {
        ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.remove(name);
        ConfigManager.getInstance().saveFriendsEnemies();
    }

    public static void remove(Entity entity) {
        ((StringSetting) FriendsEnemies.INSTANCE.enemy).feederList.remove(entity.getName());
        ConfigManager.getInstance().saveFriendsEnemies();
    }

    private static EnemyManager instance;

    public static EnemyManager getInstance() {
        if (instance == null) instance = new EnemyManager();
        return instance;
    }

}
