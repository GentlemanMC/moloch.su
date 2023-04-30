package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.event.events.render.RenderOverlayEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.*;
import net.spartanb312.base.utils.graphics.RenderHelper;
import net.spartanb312.base.utils.graphics.RenderUtils2D;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
//TODO: Roll arrow colors between 2 colors,   add other arrow shapes
@Parallel(runnable = true)
@ModuleInfo(name = "EntityPointer", category = Category.VISUALS, description = "Draws stuff to point at where entities are")
public class EntityPointer extends Module {

    Setting<Page> page = setting("Page", Page.Tracers);
    Setting<Float> range = setting("Range", 256.0f, 1.0f, 256.0f).des("Distance to start drawing tracers");
    Setting<Boolean> tracers = setting("Tracers", true).des("Draw lines to entities").whenAtMode(page, Page.Tracers);
    Setting<Float> lineWidth = setting("LineWidth", 1.0f, 1.0f, 5.0f).des("Width of tracer lines").whenTrue(tracers).whenAtMode(page, Page.Tracers);
    Setting<Boolean> spine = setting("Spine", true).des("Draw a line going up the entity's bounding box").whenTrue(tracers).whenAtMode(page, Page.Tracers);

    Setting<Boolean> arrows = setting("Arrows", false).des("Draw arrows around crosshairs to point at entities").whenAtMode(page, Page.Arrows);
    Setting<Float> arrowOffset = setting("ArrowOffset", 15.0f, 1.0f, 100.0f).des("Distance from crosshairs that the arrows should render").whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Boolean> offscreenOnly = setting("OffscreenOnly", false).des("Only draw arrows to entities that are offscreen").whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Boolean> offscreenFade = setting("OffscreenFade", false).des("Fade arrows when an entity comes onto screen or goes offscreen").whenTrue(offscreenOnly).whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Float> offscreenFadeFactor = setting("OffscreenFadeFactor", 1.0f, 0.1f, 10.0f).des("Speed of arrows fading").whenTrue(offscreenFade).whenTrue(offscreenOnly).whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Float> arrowWidth = setting("ArrowWidth", 5.0f, 0.0f, 20.0f).des("Width of arrow").whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Float> arrowHeight = setting("ArrowHeight", 5.0f, 0.0f, 20.0f).des("Height of arrow").whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Boolean> arrowLines = setting("ArrowLines", true).des("Draw an outline on arrows").whenTrue(arrows).whenAtMode(page, Page.Arrows);
    Setting<Float> arrowLinesWidth = setting("ArrowLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of arrows outline").whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Arrows);

    Setting<Boolean> players = setting("Players", true).des("Draw stuff to point at players").whenAtMode(page, Page.Entities);
    Setting<Boolean> monsters = setting("Monsters", false).des("Draw stuff to point at monsters").whenAtMode(page, Page.Entities);
    Setting<Boolean> animals = setting("Animals", false).des("Draw stuff to point at animals").whenAtMode(page, Page.Entities);
    Setting<Boolean> items = setting("Items", false).des("Draw stuff to point at dropped items").whenAtMode(page, Page.Entities);
    Setting<Boolean> pearls = setting("Pearls", false).des("Draw stuff to point at pearls").whenAtMode(page, Page.Entities);

    Setting<Boolean> playerDistanceColor = setting("PlayerDistColor", false).des("Change color depending on distance").whenAtMode(page, Page.Colors);
    Setting<Color> playerColor = setting("PlayerColor", new Color(new java.awt.Color(255, 255, 50, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> playerColorFar = setting("PlayerColorFar", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(playerDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Boolean> playerArrowRainbowWheel = setting("PlayerArrowRainbowWheel", false).des("Colors arrows in a circular rainbow").whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> playerArrowLinesRainbowWheel = setting("PArrowLinesRainbowWheel", false).des("Colors arrows outline in a circular rainbow").whenTrue(arrows).whenTrue(arrowLines).whenAtMode(page, Page.Colors);
    Setting<Float> playerArrowRainbowWheelSpeed = setting("PArrowRainbowWheelSpeed", 0.5f, 0.1f, 3.0f).des("Speed of circular rainbow wave").when(() -> playerArrowRainbowWheel.getValue() || playerArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> playerArrowRainbowWheelSaturation = setting("PArrowRainbowWheelSaturation", 0.75f, 0.0f, 1.0f).des("Saturation of circular rainbow wave").when(() -> playerArrowRainbowWheel.getValue() || playerArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> playerArrowRainbowWheelBrightness = setting("PArrowRainbowWheelBrightness", 0.9f, 0.0f, 1.0f).des("Brightness of circular rainbow wave").when(() -> playerArrowRainbowWheel.getValue() || playerArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> playerColorArrowLines = setting("PlayerColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenFalse(playerArrowLinesRainbowWheel).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> friendColor = setting("FriendColor", new Color(new java.awt.Color(50, 255, 255, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> friendColorFar = setting("FriendColorFar", new Color(new java.awt.Color(100, 100, 255, 175).getRGB())).whenTrue(playerDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Color> friendColorArrowLines = setting("FriendColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> enemyColor = setting("EnemyColor", new Color(new java.awt.Color(255, 0, 0, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> enemyColorFar = setting("EnemyColorFar", new Color(new java.awt.Color(255, 100, 100, 175).getRGB())).whenTrue(playerDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Color> enemyColorArrowLines = setting("EnemyColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> monsterDistanceColor = setting("MonsterDistColor", false).des("Change color depending on distance").whenAtMode(page, Page.Colors);
    Setting<Color> monsterColor = setting("MonsterColor", new Color(new java.awt.Color(255, 170, 50, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> monsterColorFar = setting("MonsterColorFar", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(monsterDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Boolean> monsterArrowRainbowWheel = setting("MonsterArrowRainbowWheel", false).des("Colors arrows in a circular rainbow").whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> monsterArrowLinesRainbowWheel = setting("MArrowLinesRainbowWheel", false).des("Colors arrows outline in a circular rainbow").whenTrue(arrows).whenTrue(arrowLines).whenAtMode(page, Page.Colors);
    Setting<Float> monsterArrowRainbowWheelSpeed = setting("MArrowRainbowWheelSpeed", 0.5f, 0.1f, 3.0f).des("Speed of circular rainbow wave").when(() -> monsterArrowRainbowWheel.getValue() || monsterArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> monsterArrowRainbowWheelSaturation = setting("MArrowRainbowWheelSaturation", 0.75f, 0.0f, 1.0f).des("Saturation of circular rainbow wave").when(() -> monsterArrowRainbowWheel.getValue() || monsterArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> monsterArrowRainbowWheelBrightness = setting("MArrowRainbowWheelBrightness", 0.9f, 0.0f, 1.0f).des("Brightness of circular rainbow wave").when(() -> monsterArrowRainbowWheel.getValue() || monsterArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> monsterColorArrowLines = setting("MonsterColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenFalse(monsterArrowLinesRainbowWheel).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> animalDistanceColor = setting("AnimalDistColor", false).des("Change color depending on distance").whenAtMode(page, Page.Colors);
    Setting<Color> animalColor = setting("AnimalColor", new Color(new java.awt.Color(50, 170, 255, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> animalColorFar = setting("AnimalColorFar", new Color(new java.awt.Color(50, 50, 255, 175).getRGB())).whenTrue(animalDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Boolean> animalArrowRainbowWheel = setting("AnimalArrowRainbowWheel", false).des("Colors arrows in a circular rainbow").whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> animalArrowLinesRainbowWheel = setting("AArrowLinesRainbowWheel", false).des("Colors arrows outline in a circular rainbow").whenTrue(arrows).whenTrue(arrowLines).whenAtMode(page, Page.Colors);
    Setting<Float> animalArrowRainbowWheelSpeed = setting("AArrowRainbowWheelSpeed", 0.5f, 0.1f, 3.0f).des("Speed of circular rainbow wave").when(() -> animalArrowRainbowWheel.getValue() || animalArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> animalArrowRainbowWheelSaturation = setting("AArrowRainbowWheelSaturation", 0.75f, 0.0f, 1.0f).des("Saturation of circular rainbow wave").when(() -> animalArrowRainbowWheel.getValue() || animalArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> animalArrowRainbowWheelBrightness = setting("AArrowRainbowWheelBrightness", 0.9f, 0.0f, 1.0f).des("Brightness of circular rainbow wave").when(() -> animalArrowRainbowWheel.getValue() || animalArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> animalColorArrowLines = setting("AnimalColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenFalse(animalArrowLinesRainbowWheel).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> itemDistanceColor = setting("ItemDistColor", false).des("Change color depending on distance").whenAtMode(page, Page.Colors);
    Setting<Color> itemColor = setting("ItemColor", new Color(new java.awt.Color(255, 50, 255, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> itemColorFar = setting("ItemColorFar", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(itemDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Boolean> itemArrowRainbowWheel = setting("ItemArrowRainbowWheel", false).des("Colors arrows in a circular rainbow").whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> itemArrowLinesRainbowWheel = setting("IArrowLinesRainbowWheel", false).des("Colors arrows outline in a circular rainbow").whenTrue(arrows).whenTrue(arrowLines).whenAtMode(page, Page.Colors);
    Setting<Float> itemArrowRainbowWheelSpeed = setting("IArrowRainbowWheelSpeed", 0.5f, 0.1f, 3.0f).des("Speed of circular rainbow wave").when(() -> itemArrowRainbowWheel.getValue() || itemArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> itemArrowRainbowWheelSaturation = setting("IArrowRainbowWheelSaturation", 0.75f, 0.0f, 1.0f).des("Saturation of circular rainbow wave").when(() -> itemArrowRainbowWheel.getValue() || itemArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> itemArrowRainbowWheelBrightness = setting("IArrowRainbowWheelBrightness", 0.9f, 0.0f, 1.0f).des("Brightness of circular rainbow wave").when(() -> itemArrowRainbowWheel.getValue() || itemArrowLinesRainbowWheel.getValue()).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Color> itemColorArrowLines = setting("ItemColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenFalse(itemArrowLinesRainbowWheel).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Boolean> pearlDistanceColor = setting("PearlDistColor", false).des("Change color depending on distance").whenAtMode(page, Page.Colors);
    Setting<Color> pearlColor = setting("PearlColor", new Color(new java.awt.Color(50, 255, 50, 175).getRGB())).whenAtMode(page, Page.Colors);
    Setting<Color> pearlColorFar = setting("PearlColorFar", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(pearlDistanceColor).whenAtMode(page, Page.Colors);
    Setting<Color> pearlColorArrowLines = setting("PearlColorArrowLines", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).whenTrue(arrowLines).whenTrue(arrows).whenAtMode(page, Page.Colors);
    Setting<Float> distanceFactor = setting("DistanceFactor", 0.5f, 0.1f, 1.0f).des("Fraction of range to have entity be considered at the farthest range").when(() -> playerDistanceColor.getValue() || monsterDistanceColor.getValue() || animalDistanceColor.getValue() || itemDistanceColor.getValue() || pearlDistanceColor.getValue()).whenAtMode(page, Page.Colors);

    private final HashMap<Entity, Float> arrowFadeMap = new HashMap<>();
    private HashMap<Entity, Pair<Boolean, Boolean>> arrowEntityData = new HashMap<>();
    private HashMap<Entity, Float> arrowEntityData2 = new HashMap<>();
    private final Timer arrowTimer = new Timer();

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (tracers.getValue()) {
            GL11.glPushMatrix();
            HashMap<Entity, Pair<Boolean, Boolean>> map;
            synchronized (FriendsEnemies.INSTANCE.entityData) {
                map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
            }
            map.entrySet().stream()
                    .filter(e -> e.getKey() != mc.renderViewEntity)
                    .filter(e -> !(EntityUtil.getInterpDistance(mc.getRenderPartialTicks(), mc.player, e.getKey()) > range.getValue()))
                    .forEach(entry -> {
                        Entity entity = entry.getKey();
                        float distanceFactor = MathUtilFuckYou.clamp((float)EntityUtil.getInterpDistance(mc.getRenderPartialTicks(), mc.player, entity) / (range.getValue() * this.distanceFactor.getValue()), 0.0f, 1.0f);

                        if (players.getValue() && entity instanceof EntityPlayer) {
                            int color;

                            if (entry.getValue().a) {
                                color = playerDistanceColor.getValue() ? ColorUtil.colorShift(friendColor.getValue().getColor(), friendColorFar.getValue().getColor(), distanceFactor) : friendColor.getValue().getColor();
                            } else if (entry.getValue().b) {
                                color = playerDistanceColor.getValue() ? ColorUtil.colorShift(enemyColor.getValue().getColor(), enemyColorFar.getValue().getColor(), distanceFactor) : enemyColor.getValue().getColor();
                            } else {
                                color = playerDistanceColor.getValue() ? ColorUtil.colorShift(playerColor.getValue().getColor(), playerColorFar.getValue().getColor(), distanceFactor) : playerColor.getValue().getColor();
                            }

                            SpartanTessellator.drawTracer(entity, lineWidth.getValue(), spine.getValue(), color);
                        }

                        if (monsters.getValue() && EntityUtil.isEntityMonster(entity)) {
                            SpartanTessellator.drawTracer(entity, lineWidth.getValue(), spine.getValue(), monsterDistanceColor.getValue() ? ColorUtil.colorShift(monsterColor.getValue().getColor(), monsterColorFar.getValue().getColor(), distanceFactor) : monsterColor.getValue().getColor());
                        }

                        if (animals.getValue() && EntityUtil.isEntityAnimal(entity)) {
                            SpartanTessellator.drawTracer(entity, lineWidth.getValue(), spine.getValue(), animalDistanceColor.getValue() ? ColorUtil.colorShift(animalColor.getValue().getColor(), animalColorFar.getValue().getColor(), distanceFactor) : animalColor.getValue().getColor());
                        }

                        if (items.getValue() && entity instanceof EntityItem) {
                            SpartanTessellator.drawTracer(entity, lineWidth.getValue(), spine.getValue(), itemDistanceColor.getValue() ? ColorUtil.colorShift(itemColor.getValue().getColor(), itemColorFar.getValue().getColor(), distanceFactor) : itemColor.getValue().getColor());
                        }

                        if (pearls.getValue() && entity instanceof EntityEnderPearl) {
                            SpartanTessellator.drawTracer(entity, lineWidth.getValue(), spine.getValue(), pearlDistanceColor.getValue() ? ColorUtil.colorShift(pearlColor.getValue().getColor(), pearlColorFar.getValue().getColor(), distanceFactor) : pearlColor.getValue().getColor());
                        }
                    });
            GL11.glPopMatrix();
        }
    }

    @Override
    public void onRender(RenderOverlayEvent event) {
        if (arrows.getValue()) {
            RenderUtils2D.prepareGl();
            arrowEntityData.forEach((entity, value) -> {
                float distanceFactor = MathUtilFuckYou.clamp((float) EntityUtil.getInterpDistance(mc.getRenderPartialTicks(), mc.player, entity) / (range.getValue() * this.distanceFactor.getValue()), 0.0f, 1.0f);
                float alphaFactor = offscreenOnly.getValue() && offscreenFade.getValue() ? (arrowFadeMap.get(entity) == null ? 0.0f : arrowFadeMap.get(entity)) : 300.0f;

                if (alphaFactor > 0.0f) {
                    float rotation = getYawToEntity(entity) - mc.renderViewEntity.rotationYaw + (mc.gameSettings.thirdPersonView == 2 ? 0.0f : 180.0f);

                    if (players.getValue() && entity instanceof EntityPlayer) {
                        int color;
                        int linesColor;

                        if (value.a) {
                            color = playerDistanceColor.getValue() ? ColorUtil.colorShift(friendColor.getValue().getColor(), friendColorFar.getValue().getColor(), distanceFactor) : friendColor.getValue().getColor();
                            linesColor = friendColorArrowLines.getValue().getColor();
                        } else if (value.b) {
                            color = playerDistanceColor.getValue() ? ColorUtil.colorShift(enemyColor.getValue().getColor(), enemyColorFar.getValue().getColor(), distanceFactor) : enemyColor.getValue().getColor();
                            linesColor = enemyColorArrowLines.getValue().getColor();
                        } else {
                            color = playerDistanceColor.getValue() ? ColorUtil.colorShift(playerColor.getValue().getColor(), playerColorFar.getValue().getColor(), distanceFactor) : playerColor.getValue().getColor();
                            linesColor = playerColorArrowLines.getValue().getColor();

                            if (playerArrowRainbowWheel.getValue()) {
                                color = ColorUtil.rolledRainbowCircular((int) rotation, playerArrowRainbowWheelSpeed.getValue() / 10.0f, playerArrowRainbowWheelSaturation.getValue(), playerArrowRainbowWheelBrightness.getValue());
                            }

                            if (playerArrowLinesRainbowWheel.getValue()) {
                                linesColor = ColorUtil.rolledRainbowCircular((int) rotation, playerArrowRainbowWheelSpeed.getValue() / 10.0f, playerArrowRainbowWheelSaturation.getValue(), playerArrowRainbowWheelBrightness.getValue());
                            }
                        }

                        drawArrow(color, linesColor, alphaFactor / 300.0f, arrowEntityData2.get(entity));
                    }

                    if (monsters.getValue() && EntityUtil.isEntityMonster(entity)) {
                        int color = monsterDistanceColor.getValue() ? ColorUtil.colorShift(monsterColor.getValue().getColor(), monsterColorFar.getValue().getColor(), distanceFactor) : monsterColor.getValue().getColor();
                        int linesColor = monsterColorArrowLines.getValue().getColor();

                        if (monsterArrowRainbowWheel.getValue()) {
                            color = ColorUtil.rolledRainbowCircular((int) rotation, monsterArrowRainbowWheelSpeed.getValue() / 10.0f, monsterArrowRainbowWheelSaturation.getValue(), monsterArrowRainbowWheelBrightness.getValue());
                        }

                        if (monsterArrowLinesRainbowWheel.getValue()) {
                            linesColor = ColorUtil.rolledRainbowCircular((int) rotation, monsterArrowRainbowWheelSpeed.getValue() / 10.0f, monsterArrowRainbowWheelSaturation.getValue(), monsterArrowRainbowWheelBrightness.getValue());
                        }

                        drawArrow(color, linesColor, alphaFactor / 300.0f, arrowEntityData2.get(entity));
                    }

                    if (animals.getValue() && EntityUtil.isEntityAnimal(entity)) {
                        int color = animalDistanceColor.getValue() ? ColorUtil.colorShift(animalColor.getValue().getColor(), animalColorFar.getValue().getColor(), distanceFactor) : animalColor.getValue().getColor();
                        int linesColor = animalColorArrowLines.getValue().getColor();

                        if (animalArrowRainbowWheel.getValue()) {
                            color = ColorUtil.rolledRainbowCircular((int) rotation, animalArrowRainbowWheelSpeed.getValue() / 10.0f, animalArrowRainbowWheelSaturation.getValue(), animalArrowRainbowWheelBrightness.getValue());
                        }

                        if (animalArrowLinesRainbowWheel.getValue()) {
                            linesColor = ColorUtil.rolledRainbowCircular((int) rotation, animalArrowRainbowWheelSpeed.getValue() / 10.0f, animalArrowRainbowWheelSaturation.getValue(), animalArrowRainbowWheelBrightness.getValue());
                        }

                        drawArrow(color, linesColor, alphaFactor / 300.0f, arrowEntityData2.get(entity));
                    }

                    if (items.getValue() && entity instanceof EntityItem) {
                        int color = itemDistanceColor.getValue() ? ColorUtil.colorShift(itemColor.getValue().getColor(), itemColorFar.getValue().getColor(), distanceFactor) : itemColor.getValue().getColor();
                        int linesColor = itemColorArrowLines.getValue().getColor();

                        if (itemArrowRainbowWheel.getValue()) {
                            color = ColorUtil.rolledRainbowCircular((int) rotation, itemArrowRainbowWheelSpeed.getValue() / 10.0f, itemArrowRainbowWheelSaturation.getValue(), itemArrowRainbowWheelBrightness.getValue());
                        }

                        if (itemArrowLinesRainbowWheel.getValue()) {
                            linesColor = ColorUtil.rolledRainbowCircular((int) rotation, itemArrowRainbowWheelSpeed.getValue() / 10.0f, itemArrowRainbowWheelSaturation.getValue(), itemArrowRainbowWheelBrightness.getValue());
                        }

                        drawArrow(color, linesColor, alphaFactor / 300.0f, arrowEntityData2.get(entity));
                    }

                    if (pearls.getValue() && entity instanceof EntityEnderPearl) {
                        drawArrow(pearlDistanceColor.getValue() ? ColorUtil.colorShift(pearlColor.getValue().getColor(), pearlColorFar.getValue().getColor(), distanceFactor) : pearlColor.getValue().getColor(), pearlColorArrowLines.getValue().getColor(), alphaFactor / 300.0f, arrowEntityData2.get(entity));
                    }
                }
            });
            RenderUtils2D.releaseGl();
            
        }
    }

    @Override
    public void onRenderTick() {
        if (arrows.getValue()) {
            HashMap<Entity, Pair<Boolean, Boolean>> arrowEntityDataTemp = new HashMap<>();
            HashMap<Entity, Float> arrowEntityData2Temp = new HashMap<>();
            int passedms = (int) arrowTimer.hasPassed();
            arrowTimer.reset();

            HashMap<Entity, Pair<Boolean, Boolean>> map;
            synchronized (FriendsEnemies.INSTANCE.entityData) {
                map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
            }
            map.entrySet().stream()
                    .filter(e -> e.getKey() != mc.renderViewEntity)
                    .filter(e -> !(EntityUtil.getInterpDistance(mc.getRenderPartialTicks(), mc.player, e.getKey()) > range.getValue()))
                    .filter(e -> !offscreenOnly.getValue() || offscreenFade.getValue() || !RenderHelper.isInViewFrustrum(e.getKey()))
                    .forEach(entry -> {
                        if (offscreenOnly.getValue() && offscreenFade.getValue()) {
                            if (!arrowFadeMap.containsKey(entry.getKey())) {
                                arrowFadeMap.put(entry.getKey(), 0.0f);
                            }

                            float alphaFactor = arrowFadeMap.get(entry.getKey());

                            if (passedms < 1000) {
                                if (RenderHelper.isInViewFrustrum(entry.getKey())) {
                                    alphaFactor -= (offscreenFadeFactor.getValue() / 10.0f) * passedms;
                                }
                                else {
                                    alphaFactor += (offscreenFadeFactor.getValue() / 10.0f) * passedms;
                                }
                            }
                            alphaFactor = MathUtilFuckYou.clamp(alphaFactor, 0.0f, 300.0f);
                            arrowFadeMap.put(entry.getKey(), alphaFactor);

                            if (alphaFactor < 0.0f) {
                                arrowFadeMap.remove(entry.getKey());
                            }
                        }

                        arrowEntityDataTemp.put(entry.getKey(), entry.getValue());
                        arrowEntityData2Temp.put(entry.getKey(), getYawToEntity(entry.getKey()) - mc.renderViewEntity.rotationYaw + (mc.gameSettings.thirdPersonView == 2 ? 0.0f : 180.0f));
                    });

            arrowEntityData = arrowEntityDataTemp;
            arrowEntityData2 = arrowEntityData2Temp;
        }
    }

    private void drawArrow(int color, int linesColor, float alphaFactor, float rotation) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        int a = ColorUtil.getAlpha(color);

        int lr = ColorUtil.getRed(linesColor);
        int lg = ColorUtil.getGreen(linesColor);
        int lb = ColorUtil.getBlue(linesColor);
        int la = ColorUtil.getAlpha(linesColor);

        GL11.glTranslatef(scaledResolution.getScaledWidth() / 2.0f, scaledResolution.getScaledHeight() / 2.0f, 0.0f);
        GL11.glRotatef(rotation, 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-scaledResolution.getScaledWidth() / 2.0f, -scaledResolution.getScaledHeight() / 2.0f, 0.0f);

        GL11.glTranslatef(scaledResolution.getScaledWidth() / 2.0f, scaledResolution.getScaledHeight() / 2.0f, 0.0f);
        RenderUtils2D.drawTriangle(0.0f, arrowOffset.getValue() + (arrowHeight.getValue() / 2.0f), arrowWidth.getValue() / 2.0f, arrowOffset.getValue() - (arrowHeight.getValue() / 2.0f), -arrowWidth.getValue() / 2.0f, arrowOffset.getValue() - (arrowHeight.getValue() / 2.0f),
                new java.awt.Color(r, g, b, (int)(a * alphaFactor)).getRGB());
        if (arrowLines.getValue()) {
            RenderUtils2D.drawTriangleOutline(0.0f, arrowOffset.getValue() + (arrowHeight.getValue() / 2.0f), arrowWidth.getValue() / 2.0f, arrowOffset.getValue() - (arrowHeight.getValue() / 2.0f), -arrowWidth.getValue() / 2.0f, arrowOffset.getValue() - (arrowHeight.getValue() / 2.0f),
                    arrowLinesWidth.getValue(), new java.awt.Color(lr, lg, lb, (int)(la * alphaFactor)).getRGB());
        }
        GL11.glTranslatef(-scaledResolution.getScaledWidth() / 2.0f, -scaledResolution.getScaledHeight() / 2.0f, 0.0f);

        GL11.glTranslatef(scaledResolution.getScaledWidth() / 2.0f, scaledResolution.getScaledHeight() / 2.0f, 0.0f);
        GL11.glRotatef(-rotation, 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-scaledResolution.getScaledWidth() / 2.0f, -scaledResolution.getScaledHeight() / 2.0f, 0.0f);
    }

    private float getYawToEntity(Entity entity) {
        Vec3d youVec = EntityUtil.interpolateEntity(mc.renderViewEntity, mc.getRenderPartialTicks());
        Vec3d themVec = EntityUtil.interpolateEntity(entity, mc.getRenderPartialTicks());
        double x = themVec.x - youVec.x;
        double z = themVec.z - youVec.z;
        return (float)(-(Math.atan2(x, z) * 57.29577951308232));
    }

    enum Page {
        Tracers,
        Arrows,
        Entities,
        Colors
    }
}
