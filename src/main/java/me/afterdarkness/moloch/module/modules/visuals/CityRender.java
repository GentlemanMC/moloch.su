package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.CrystalUtil;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "CityRender", category = Category.VISUALS, description = "Renders stuff to indicate blocks that can be used to city someone (mined out to crystal them)")
public class CityRender extends Module {

    Setting<Float> range = setting("Range", 6.0f, 0.0f, 15.0f).des("Range to start checking players for cityable blocks");
    Setting<Boolean> checkDiagonalCity = setting("DiagonalCity", true).des("Checks if a player can be citied diagonally instead of just directly next to the player");
    Setting<Boolean> oneBlockCrystalMode = setting("1.13+", false).des("Uses 1.13+ crystal placements to find cityable blocks where crystals can be placed in one block spaces");
    Setting<Boolean> self = setting("Self", true).des("Render cityable blocks for yourself");
    Setting<Boolean> ignoreFriends = setting("IgnoreFriends", true).des("Dont render cityable blocks for friends");
    Setting<Boolean> fade = setting("Fade", true).des("Fade renders in and out when cityable blocks are mined or when the player moves");
    Setting<Float> fadeSpeed = setting("FadeSpeed", 2.0f, 0.1f, 3.0f).des("Speed of how fast renders fade").whenTrue(fade);
    Setting<RenderMode> renderMode = setting("RenderMode", RenderMode.Box);
    Setting<Float> boxHeight = setting("BoxHeight", 1.0f, 0.0f, 1.0f).when(() -> renderMode.getValue() != RenderMode.Flat);
    Setting<Boolean> solid = setting("Solid", true).des("Solid render");
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(255, 50, 50, 19).getRGB())).whenTrue(solid);
    Setting<Color> selfSolidColor = setting("SelfSolidColor", new Color(new java.awt.Color(50, 255, 50, 19).getRGB())).whenTrue(self).whenTrue(solid);
    Setting<Boolean> lines = setting("Lines", true).des("Lines render");
    Setting<Float> linesWidth = setting("LinesWidth", 1.0f, 1.0f, 5.0f).whenTrue(lines);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(255, 50, 50, 101).getRGB())).whenTrue(solid);
    Setting<Color> selfLinesColor = setting("SelfLinesColor", new Color(new java.awt.Color(50, 255, 50, 101).getRGB())).whenTrue(self).whenTrue(lines);

    private HashMap<BlockPos, Float> toRenderEnemyPos = new HashMap<>();
    private final HashMap<BlockPos, Float> toRenderEnemyPos2 = new HashMap<>();
    private HashMap<BlockPos, Float> toRenderSelfPos = new HashMap<>();
    private final HashMap<BlockPos, Float> toRenderSelfPos2 = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final Timer timer = new Timer();

    public CityRender() {
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

    RepeatUnit update = new RepeatUnit(() -> 1, () -> {
        int passedms = (int) timer.hasPassed();
        timer.reset();

        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = EntityUtil.floorEntity(mc.player);

        if (self.getValue()) {
            toRenderSelfPos2.clear();

            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos pos = BlockUtil.extrudeBlock(playerPos, facing);

                if (CrystalUtil.isCityable(pos, facing, checkDiagonalCity.getValue(), oneBlockCrystalMode.getValue())) {
                    toRenderSelfPos2.put(pos, 0.0f);
                    if (fade.getValue()) {
                        toRenderSelfPos.putIfAbsent(pos, 0.0f);
                        if (passedms < 1000)
                            toRenderSelfPos.put(pos, MathUtilFuckYou.clamp(toRenderSelfPos.get(pos) == null ? 0.0f : toRenderSelfPos.get(pos) + (fadeSpeed.getValue() * passedms), 0.0f, 300.0f));
                    }
                    else {
                        toRenderSelfPos.put(pos, 300.0f);
                    }
                }
            }

            HashMap<BlockPos, Float> selfPosMapTemp = new HashMap<>();
            toRenderSelfPos.entrySet().stream()
                    .filter(entry -> fade.getValue() ? entry.getValue() > 0.0f : toRenderSelfPos2.containsKey(entry.getKey()))
                    .forEach(entry -> {
                        if (fade.getValue() && !toRenderSelfPos2.containsKey(entry.getKey()) && passedms < 1000) {
                            selfPosMapTemp.put(entry.getKey(), entry.getValue() - (fadeSpeed.getValue() * passedms));
                        } else {
                            selfPosMapTemp.put(entry.getKey(), entry.getValue());
                        }
                    });

            toRenderSelfPos = selfPosMapTemp;
        }

        toRenderEnemyPos2.clear();
        HashMap<Entity, Pair<Boolean, Boolean>> map;
        synchronized (FriendsEnemies.INSTANCE.entityData) {
            map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
        }
        map.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof EntityPlayer)
                .filter(entry -> entry.getKey() != mc.player)
                .filter(entry -> !ignoreFriends.getValue() || !entry.getValue().a)
                .filter(entry -> EntityUtil.getInterpDistance(mc.getRenderPartialTicks(), entry.getKey(), mc.player) <= range.getValue())
                .forEach(entry -> {
                    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                        BlockPos otherPlayerPos = EntityUtil.floorEntity(entry.getKey());
                        BlockPos pos = BlockUtil.extrudeBlock(otherPlayerPos, facing);

                        if (CrystalUtil.isCityable(pos, facing, checkDiagonalCity.getValue(), oneBlockCrystalMode.getValue())) {
                            toRenderEnemyPos2.put(pos, 0.0f);
                            if (fade.getValue()) {
                                toRenderEnemyPos.putIfAbsent(pos, 0.0f);
                                if (passedms < 1000)
                                    toRenderEnemyPos.put(pos, MathUtilFuckYou.clamp(toRenderEnemyPos.get(pos) + (fadeSpeed.getValue() * passedms), 0.0f, 300.0f));
                            }
                            else {
                                toRenderEnemyPos.put(pos, 300.0f);
                            }
                        }
                    }
                });

        HashMap<BlockPos, Float> enemyPosMapTemp = new HashMap<>();
        toRenderEnemyPos.entrySet().stream()
                .filter(entry -> fade.getValue() ? entry.getValue() > 0.0f : toRenderEnemyPos2.containsKey(entry.getKey()))
                .forEach(entry -> {
                    if (fade.getValue() && !toRenderEnemyPos2.containsKey(entry.getKey()) && passedms < 1000) {
                        enemyPosMapTemp.put(entry.getKey(), entry.getValue() - (fadeSpeed.getValue() * passedms));
                    } else {
                        enemyPosMapTemp.put(entry.getKey(), entry.getValue());
                    }
                });

        toRenderEnemyPos = enemyPosMapTemp;
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
    public void onRenderWorld(RenderEvent event) {
        if (self.getValue())
            toRenderSelfPos.forEach((key, value) ->
                    renderStuff(key, selfSolidColor.getValue(), selfLinesColor.getValue(), fade.getValue() ? value / 300.0f : 1.0f));

        toRenderEnemyPos.forEach((key, value) ->
                renderStuff(key, solidColor.getValue(), linesColor.getValue(), fade.getValue() ? value / 300.0f : 1.0f));
    }

    private void renderStuff(BlockPos pos, Color solidColor, Color linesColor, float alphaFactor) {
        alphaFactor = MathUtilFuckYou.clamp(alphaFactor, 0.0f, 1.0f);
        switch (renderMode.getValue()) {
            case Box: {
                if (solid.getValue()) {
                    SpartanTessellator.drawBlockFullBox(new Vec3d(pos), false, boxHeight.getValue(), new java.awt.Color(solidColor.getColorColor().getRed(), solidColor.getColorColor().getGreen(), solidColor.getColorColor().getBlue(), (int)(solidColor.getAlpha() * alphaFactor)).getRGB());
                }

                if (lines.getValue()) {
                    SpartanTessellator.drawBlockLineBox(new Vec3d(pos), false, boxHeight.getValue(), linesWidth.getValue(), new java.awt.Color(linesColor.getColorColor().getRed(), linesColor.getColorColor().getGreen(), linesColor.getColorColor().getBlue(), (int)(linesColor.getAlpha() * alphaFactor)).getRGB());
                }
                break;
            }

            case Flat: {
                if (solid.getValue()) {
                    SpartanTessellator.drawFlatFullBox(new Vec3d(pos), false, new java.awt.Color(solidColor.getColorColor().getRed(), solidColor.getColorColor().getGreen(), solidColor.getColorColor().getBlue(), (int)(solidColor.getAlpha() * alphaFactor)).getRGB());
                }

                if (lines.getValue()) {
                    SpartanTessellator.drawFlatLineBox(new Vec3d(pos), false, linesWidth.getValue(), new java.awt.Color(linesColor.getColorColor().getRed(), linesColor.getColorColor().getGreen(), linesColor.getColorColor().getBlue(), (int)(linesColor.getAlpha() * alphaFactor)).getRGB());
                }
                break;
            }

            case Pyramid: {
                if (solid.getValue()) {
                    SpartanTessellator.drawPyramidFullBox(new Vec3d(pos), false, false, boxHeight.getValue(), new java.awt.Color(solidColor.getColorColor().getRed(), solidColor.getColorColor().getGreen(), solidColor.getColorColor().getBlue(), (int)(solidColor.getAlpha() * alphaFactor)).getRGB());
                }

                if (lines.getValue()) {
                    SpartanTessellator.drawPyramidLineBox(new Vec3d(pos), false, false, boxHeight.getValue(), linesWidth.getValue(), new java.awt.Color(linesColor.getColorColor().getRed(), linesColor.getColorColor().getGreen(), linesColor.getColorColor().getBlue(), (int)(linesColor.getAlpha() * alphaFactor)).getRGB());
                }
                break;
            }
        }
    }

    enum RenderMode {
        Box,
        Flat,
        Pyramid
    }
}
