package net.spartanb312.base.client;

import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.minecraft.entity.Entity;
import net.spartanb312.base.core.setting.settings.StringSetting;

public class FriendManager {

    public static void init() {
        instance = new FriendManager();
    }

    public static boolean isFriend(Entity entity) {
        return isFriend(entity.getName());
    }

    public static boolean isFriend(String name) {
        return ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.contains(name);
    }

    public static void add(String name) {
        if (!((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.contains(name)) {
            ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.add(name);
            ConfigManager.getInstance().saveFriendsEnemies();
        }
    }

    public static void add(Entity entity) {
        if (!((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.contains(entity.getName())) {
            ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.add(entity.getName());
            ConfigManager.getInstance().saveFriendsEnemies();
        }
    }

    public static void remove(String name) {
        ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.remove(name);
        ConfigManager.getInstance().saveFriendsEnemies();
    }

    public static void remove(Entity entity) {
        ((StringSetting) FriendsEnemies.INSTANCE.friend).feederList.remove(entity.getName());
        ConfigManager.getInstance().saveFriendsEnemies();
    }

    private static FriendManager instance;

    public static FriendManager getInstance() {
        if (instance == null) instance = new FriendManager();
        return instance;
    }

}
