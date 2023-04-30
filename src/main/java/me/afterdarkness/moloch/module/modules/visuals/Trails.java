package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import me.afterdarkness.moloch.utils.math.Triple;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.ColorUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

//TODO: glow dot mode   &&   X dot mode   &&   rolling rainbow   &&   rolling colors
@Parallel(runnable = true)
@ModuleInfo(name = "Trails", category = Category.VISUALS, description = "Renders trails behind entities showing previous positions")
public class Trails extends Module {

    Setting<Page> page = setting("Page", Page.Entities);

    Setting<Boolean> self = setting("Self", true).des("Renders trails for yourself").whenAtMode(page, Page.Entities);
    Setting<Boolean> players = setting("Players", true).des("Renders trails for other players").whenAtMode(page, Page.Entities);
    Setting<Boolean> chorusingSelf = setting("ChorusingSelf", false).des("Renders trail when you chorus teleport").whenAtMode(page, Page.Entities);
    Setting<Boolean> chorusingPlayer = setting("ChorusingPlayer", true).des("Renders trail when player chorus teleports").whenAtMode(page, Page.Entities);
    Setting<Boolean> arrows = setting("Arrows", false).des("Renders trails for arrows").whenAtMode(page, Page.Entities);
    Setting<Boolean> pearls = setting("Pearls", true).des("Renders trails for pearls").whenAtMode(page, Page.Entities);
    Setting<Boolean> snowballs = setting("Snowballs", false).des("Renders trails for snowballs").whenAtMode(page, Page.Entities);

    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 500).des("Milliseconds between each attempt to mark entity positions for trail rendering").whenAtMode(page, Page.Rendering);
    Setting<Float> lineWidth = setting("LineWidth", 1.0f, 1.0f, 5.0f).des("Width of trail line").whenAtMode(page, Page.Rendering);
    Setting<Boolean> fade = setting("Fade", false).des("Fades out trail for self and other players").whenAtMode(page, Page.Rendering);
    Setting<Integer> fadeDelay = setting("FadeDelay", 500, 0, 15000).des("Milliseconds to wait until trail starts fading").when(() -> fade.getValue() || chorusingPlayer.getValue() || pearls.getValue() || arrows.getValue() || snowballs.getValue()).whenAtMode(page, Page.Rendering);
    Setting<Float> fadeSpeed = setting("FadeSpeed", 1.0f, 0.1f, 5.0f).des("Speed of trail fading").when(() -> fade.getValue() && (self.getValue() || players.getValue())).whenAtMode(page, Page.Rendering);
    Setting<Integer> chorusTimeout = setting("ChorusTimeout", 700, 1, 1500).des("Milliseconds after player stops eating chorus to keep drawing trail").whenTrue(chorusingPlayer).whenAtMode(page, Page.Rendering);
    Setting<Integer> chorusfadeDelay = setting("ChorusFadeDelay", 500, 0, 15000).des("Milliseconds to wait until chorusing player's trail starts fading").whenTrue(chorusingPlayer).whenAtMode(page, Page.Rendering);
    Setting<Float> chorusFadeSpeed = setting("ChorusFadeSpeed", 0.5f, 0.1f, 5.0f).des("Speed of chorusing player trail fading").whenTrue(chorusingPlayer).whenAtMode(page, Page.Rendering);
    Setting<Integer> projectilefadeDelay = setting("ProjectileFadeDelay", 0, 0, 15000).des("Milliseconds to wait until projectile's trail starts fading").when(() -> pearls.getValue() || arrows.getValue() || snowballs.getValue()).whenAtMode(page, Page.Rendering);
    Setting<Float> projectileFadeSpeed = setting("ProjectileFadeSpeed", 1.0f, 0.1f, 5.0f).des("Speed of projectile trail fading").when(() -> pearls.getValue() || arrows.getValue() || snowballs.getValue()).whenAtMode(page, Page.Rendering);
    Setting<Boolean> selfClear = setting("SelfClear", false).des("Clear your own trail").whenTrue(self).whenAtMode(page, Page.Rendering);
    Setting<Boolean> playerClear = setting("PlayerClear", false).des("Clear the trails of other players").whenTrue(players).whenAtMode(page, Page.Rendering);
    Setting<Boolean> stopRecordingSelf = setting("StopRecordingSelf", false).des("Doesn't record your trail").whenTrue(self).whenFalse(fade).whenAtMode(page, Page.Rendering);
    Setting<Boolean> stopRecordingPlayers = setting("StopRecordingOtherPlayers", false).des("Doesn't record other player's trails").whenTrue(players).whenFalse(fade).whenAtMode(page, Page.Rendering);

    Setting<Color> selfColor = setting("SelfColor", new Color(new java.awt.Color(167, 147, 241, 200).getRGB())).whenTrue(self).whenAtMode(page, Page.Colors);
    Setting<Color> playerColor = setting("PlayerColor", new Color(new java.awt.Color(100, 61, 255, 200).getRGB())).whenTrue(players).whenAtMode(page, Page.Colors);
    Setting<Color> friendColor = setting("FriendColor", new Color(new java.awt.Color(50, 200, 255, 200).getRGB())).whenTrue(players).whenAtMode(page, Page.Colors);
    Setting<Color> enemyColor = setting("EnemyColor", new Color(new java.awt.Color(255, 50, 50, 200).getRGB())).whenTrue(players).whenAtMode(page, Page.Colors);
    Setting<Color> chorusSelfColor = setting("ChorusSelfColor", new Color(new java.awt.Color(229, 103, 167, 200).getRGB())).whenTrue(chorusingSelf).whenAtMode(page, Page.Colors);
    Setting<Color> chorusPlayerColor = setting("ChorusPlayerColor", new Color(new java.awt.Color(229, 103, 167, 200).getRGB())).whenTrue(chorusingPlayer).whenAtMode(page, Page.Colors);
    Setting<Color> chorusingFriendColor = setting("ChorusFriendColor", new Color(new java.awt.Color(50, 200, 255, 200).getRGB())).whenTrue(chorusingPlayer).whenAtMode(page, Page.Colors);
    Setting<Color> chorusingEnemyColor = setting("ChorusEnemyColor", new Color(new java.awt.Color(255, 50, 50, 200).getRGB())).whenTrue(chorusingPlayer).whenAtMode(page, Page.Colors);
    Setting<Color> arrowColor = setting("ArrowColor", new Color(new java.awt.Color(210, 210, 210, 200).getRGB())).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> pearlColor = setting("PearlColor", new Color(new java.awt.Color(224, 144, 255, 200).getRGB())).whenTrue(pearls).whenAtMode(page, Page.Colors);
    Setting<Color> snowballColor = setting("SnowballColor", new Color(new java.awt.Color(130, 202, 255, 200).getRGB())).whenTrue(snowballs).whenAtMode(page, Page.Colors);


    private final HashMap<Integer, Triple<List<Pair<Vec3d, Long>>, Integer, Pair<Integer, Float>>> trailMap = new HashMap<>();
    private final HashMap<Integer, Pair<List<Pair<Vec3d, Long>>, Pair<Boolean, Boolean>>> playerTrailMap = new HashMap<>();
    private final HashMap<Integer, Pair<List<Pair<Vec3d, Long>>, Pair<Boolean, Boolean>>> chorusMap = new HashMap<>();
    private final HashMap<Integer, Pair<Long, Pair<Boolean, Boolean>>> chorusMap2 = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();

    public Trails() {
        repeatUnits.add(update);
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

    RepeatUnit update = new RepeatUnit(() -> updateDelay.getValue(), () -> {
        if (mc.world == null || mc.renderViewEntity == null || mc.player.ticksExisted < 10) return;

        HashMap<Entity, Pair<Boolean, Boolean>> map;
        synchronized (FriendsEnemies.INSTANCE.entityData) {
            map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
        }
        map.forEach((key, value) -> {
            if (key == mc.renderViewEntity && mc.renderViewEntity != mc.player) return;

            if (players.getValue() && key instanceof EntityPlayer && key != mc.renderViewEntity && !(stopRecordingPlayers.getValue() && !fade.getValue())) {
                List<Pair<Vec3d, Long>> tempList = (playerTrailMap.get(key.getEntityId()) == null || playerTrailMap.get(key.getEntityId()).a == null) ? new ArrayList<>() : playerTrailMap.get(key.getEntityId()).a;
                tempList.add(new Pair<>(key.getPositionVector(), System.currentTimeMillis()));

                synchronized (playerTrailMap) {
                    playerTrailMap.put(key.getEntityId(), new Pair<>(tempList, new Pair<>(value.a, value.b)));
                }
            }

            if (checkEntity(key) && !(stopRecordingSelf.getValue() && !fade.getValue() && self.getValue() && key == mc.player)) {
                List<Pair<Vec3d, Long>> tempList = (trailMap.get(key.getEntityId()) == null || trailMap.get(key.getEntityId()).a == null) ? new ArrayList<>() : trailMap.get(key.getEntityId()).a;
                tempList.add(new Pair<>(key.getPositionVector(), System.currentTimeMillis()));

                synchronized (trailMap) {
                    trailMap.put(key.getEntityId(), new Triple<>(tempList, getEntityColor(key == mc.player, key instanceof EntityArrow, key instanceof EntityEnderPearl, key instanceof EntitySnowball), new Pair<>(key == mc.player ? fadeDelay.getValue() : projectilefadeDelay.getValue(), key == mc.player ? fadeSpeed.getValue() : projectileFadeSpeed.getValue())));
                }
            }

            if (chorusingPlayer.getValue() && key instanceof EntityPlayer && (chorusingSelf.getValue() || key != mc.player)) {
                if (((EntityPlayer) key).isHandActive() && ((EntityPlayer) key).getHeldItemMainhand().getItem() == Items.CHORUS_FRUIT) {
                    synchronized (chorusMap2) {
                        chorusMap2.put(key.getEntityId(), new Pair<>(System.currentTimeMillis(), new Pair<>(value.a, value.b)));
                    }
                }
                else if (chorusMap.get(key.getEntityId()) != null && chorusMap.get(key.getEntityId()).a.isEmpty()) {
                    synchronized (chorusMap) {
                        chorusMap.remove(key.getEntityId());
                    }
                }
            }
        });

        if (chorusingPlayer.getValue()) {
            HashMap<Integer, Pair<Long, Pair<Boolean, Boolean>>> tempChorusMap2;
            synchronized (chorusMap2) {
                tempChorusMap2 = new HashMap<>(chorusMap2);
            }
            tempChorusMap2.forEach((key, value) -> {
                if (System.currentTimeMillis() - value.a < chorusTimeout.getValue()) {
                    Entity entity = mc.world.getEntityByID(key);
                    if (entity != null) {
                        List<Pair<Vec3d, Long>> tempList = (chorusMap.get(key) == null || chorusMap.get(key).a == null) ? new ArrayList<>() : chorusMap.get(key).a;
                        tempList.add(new Pair<>(entity.getPositionVector(), System.currentTimeMillis()));

                        chorusMap.put(key, new Pair<>(tempList, new Pair<>(value.b.a, value.b.b)));
                    }
                } else {
                    chorusMap2.remove(key);
                }
            });
        }
    });

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if ((pearls.getValue() || arrows.getValue() || snowballs.getValue()) && event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject packet = ((SPacketSpawnObject) event.getPacket());

            if ((arrows.getValue() && packet.getType() == 60) || (snowballs.getValue() && packet.getType() == 61) || (pearls.getValue() && packet.getType() == 65)) {
                List<Pair<Vec3d, Long>> tempList = new ArrayList<>();
                tempList.add(new Pair<>(new Vec3d(packet.getX(), packet.getY(), packet.getZ()), System.currentTimeMillis()));

                synchronized (trailMap) {
                    trailMap.put(packet.getEntityID(), new Triple<>(tempList, getEntityColor(false, arrows.getValue() && packet.getType() == 60, pearls.getValue() && packet.getType() == 65, snowballs.getValue() && packet.getType() == 61), new Pair<>(projectilefadeDelay.getValue(), projectileFadeSpeed.getValue())));
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (!fade.getValue() && selfClear.getValue()) {
            synchronized (trailMap) {
                trailMap.remove(mc.player.getEntityId());
            }
            selfClear.setValue(false);
        }

        if (!fade.getValue() && playerClear.getValue()) {
            synchronized (playerTrailMap) {
                playerTrailMap.clear();
            }
            playerClear.setValue(false);
        }
    }

    @Override
    public void onRenderWorld(RenderEvent event) {
        GL11.glLineWidth(lineWidth.getValue());
        GL11.glEnable(GL_LINE_SMOOTH);

        HashMap<Integer, Pair<List<Pair<Vec3d, Long>>, Pair<Boolean, Boolean>>> localPlayerTrailMap;
        synchronized (playerTrailMap) {
            localPlayerTrailMap = new HashMap<>(playerTrailMap);
        }
        localPlayerTrailMap.forEach((key, value) -> {
            GL11.glBegin(GL_LINE_STRIP);

            int color = getPlayerColor(value.b.a, value.b.b);
            new ArrayList<>(value.a).forEach(data -> {
                if (data != null) {
                    GL11.glColor4f(ColorUtil.getRed(color) / 255.0f, ColorUtil.getGreen(color) / 255.0f, ColorUtil.getBlue(color) / 255.0f, fade.getValue() ? getAlpha(fadeDelay.getValue(), fadeSpeed.getValue(), data.b, ColorUtil.getAlpha(color)) : ColorUtil.getAlpha(color) / 255.0f);
                    GL11.glVertex3d(data.a.x - mc.getRenderManager().renderPosX, data.a.y - mc.getRenderManager().renderPosY, data.a.z - mc.getRenderManager().renderPosZ);

                    if (fade.getValue() && getAlpha(fadeDelay.getValue(), fadeSpeed.getValue(), data.b, playerColor.getValue().getAlpha()) <= 0.0f) {
                        playerTrailMap.get(key).a.remove(data);
                    }
                }
            });

            GL11.glEnd();
            GL11.glColor4f(1, 1, 1, 1);
        });


        HashMap<Integer, Triple<List<Pair<Vec3d, Long>>, Integer, Pair<Integer, Float>>> localTrailMap;
        synchronized (trailMap) {
            localTrailMap = new HashMap<>(trailMap);
        }
        localTrailMap.forEach((key, value) -> {
            GL11.glBegin(GL_LINE_STRIP);

            new ArrayList<>(value.a).forEach(data -> {
                if (data != null) {
                    GL11.glColor4f(ColorUtil.getRed(value.b) / 255.0f, ColorUtil.getGreen(value.b) / 255.0f, ColorUtil.getBlue(value.b) / 255.0f, (fade.getValue() || mc.world.getEntityByID(key) == null || mc.world.getEntityByID(key) instanceof EntityEnderPearl || mc.world.getEntityByID(key) instanceof EntityArrow || mc.world.getEntityByID(key) instanceof EntitySnowball) ? getAlpha(value.c.a, value.c.b, data.b, ColorUtil.getAlpha(value.b)) : ColorUtil.getAlpha(value.b) / 255.0f);
                    GL11.glVertex3d(data.a.x - mc.getRenderManager().renderPosX, data.a.y - mc.getRenderManager().renderPosY, data.a.z - mc.getRenderManager().renderPosZ);

                    if (fade.getValue() && getAlpha(value.c.a, value.c.b, data.b, playerColor.getValue().getAlpha()) <= 0.0f) {
                        trailMap.get(key).a.remove(data);
                    }
                }
            });

            GL11.glEnd();
            GL11.glColor4f(1, 1, 1, 1);
        });

        if (chorusingPlayer.getValue()) {
            HashMap<Integer, Pair<List<Pair<Vec3d, Long>>, Pair<Boolean, Boolean>>> localChorusMap;
            synchronized (chorusMap) {
                localChorusMap = new HashMap<>(chorusMap);
            }
            localChorusMap.forEach((key, value) -> {
                GL11.glBegin(GL_LINE_STRIP);

                int color = getChorusPlayerColor(mc.world.getEntityByID(key) == mc.player, value.b.a, value.b.b);
                new ArrayList<>(value.a).forEach(data -> {
                    if (data != null) {
                        GL11.glColor4f(ColorUtil.getRed(color) / 255.0f, ColorUtil.getGreen(color) / 255.0f, ColorUtil.getBlue(color) / 255.0f, getAlpha(chorusfadeDelay.getValue(), chorusFadeSpeed.getValue(), data.b, ColorUtil.getAlpha(color)));
                        GL11.glVertex3d(data.a.x - mc.getRenderManager().renderPosX, data.a.y - mc.getRenderManager().renderPosY, data.a.z - mc.getRenderManager().renderPosZ);

                        if (fade.getValue() && getAlpha(chorusfadeDelay.getValue(), chorusFadeSpeed.getValue(), data.b, playerColor.getValue().getAlpha()) <= 0.0f) {
                            chorusMap.get(key).a.remove(data);
                        }
                    }
                });

                GL11.glEnd();
                GL11.glColor4f(1, 1, 1, 1);
            });
        }

        GL11.glDisable(GL_LINE_SMOOTH);
    }

    private boolean checkEntity(Entity entity) {
        return (entity == mc.player && self.getValue()) || (entity instanceof EntityArrow && arrows.getValue()) || (entity instanceof EntityEnderPearl && pearls.getValue()) || (entity instanceof EntitySnowball && snowballs.getValue());
    }

    private int getEntityColor(boolean isSelf, boolean isArrow, boolean isPearl, boolean isSnowball) {
        if (isSelf) {
            return selfColor.getValue().getColor();
        } else if (isArrow) {
            return arrowColor.getValue().getColor();
        } else if (isPearl) {
            return pearlColor.getValue().getColor();
        } else if (isSnowball) {
            return snowballColor.getValue().getColor();
        }

        return java.awt.Color.WHITE.getRGB();
    }

    private int getPlayerColor(boolean isFriend, boolean isEnemy) {
        if (isFriend) {
            return friendColor.getValue().getColor();
        } else if (isEnemy) {
            return enemyColor.getValue().getColor();
        }

        return playerColor.getValue().getColor();
    }

    private int getChorusPlayerColor(boolean isSelf, boolean isFriend, boolean isEnemy) {
        if (isFriend) {
            return chorusingFriendColor.getValue().getColor();
        } else if (isEnemy) {
            return chorusingEnemyColor.getValue().getColor();
        }

        if (isSelf) {
            return chorusSelfColor.getValue().getColor();
        } else {
            return chorusPlayerColor.getValue().getColor();
        }
    }

    /**
     * @param alpha - range 0.0f - 255.0f
     */
    private float getAlpha(int fadeDelay, float fadeSpeed, long startTime, float alpha) {
        if (System.currentTimeMillis() - startTime < fadeDelay) return 1.0f;

        return MathUtilFuckYou.clamp((alpha - fadeSpeed * (System.currentTimeMillis() - (startTime + fadeDelay)) / 3.0f) / 255.0f, 0.0f, 1.0f);
    }

    enum Page {
        Entities,
        Rendering,
        Colors
    }
}
