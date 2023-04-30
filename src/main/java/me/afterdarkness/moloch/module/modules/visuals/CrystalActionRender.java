package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.client.ServerManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "CrystalActionRender", category = Category.VISUALS, description = "Render stuff on crystal place position")
public class CrystalActionRender extends Module {

    Setting<Page> page = setting("Page", Page.Place);

    Setting<Boolean> multiRender = setting("MultiRender", true).des("Allows for multiple crystal bases to be rendered at once").whenAtMode(page, Page.General);
    Setting<Boolean> slide = setting("Slide", false).des("Slides crystal render from one position to another").whenFalse(multiRender).whenAtMode(page, Page.General);
    Setting<Float> slideSpeed = setting("SlideSpeed", 2.0f, 0.1f, 10.0f).des("Speed of slide render animation").whenTrue(slide).whenFalse(multiRender).whenAtMode(page, Page.General);
    Setting<Float> slideFactor = setting("SlideFactor", 0.5f, 0.1f, 1.0f).des("Steepness of slide render animation").whenTrue(slide).whenFalse(multiRender).whenAtMode(page, Page.General);

    Setting<Boolean> placeRender = setting("PlaceRender", true).whenAtMode(page, Page.Place);
    Setting<PlacePage> placePage = setting("PlacePage", PlacePage.Base).whenTrue(placeRender).whenAtMode(page, Page.Place);
    Setting<Transition> placeFade = setting("PlaceFade", Transition.Alpha).des("Way of fading place render out").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Boolean> breakSyncScale = setting("BreakSyncScale", true).des("Scales down break render with place render").when(() -> placeFade.getValue() == Transition.Scale || placeFade.getValue() == Transition.Both).whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Float> placeFadeSpeed = setting("PlaceFadeSpeed", 1.0f, 0.1f, 5.0f).des("Fade speed of place render").whenTrue(placeRender).when(() -> placeFade.getValue() != Transition.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Integer> placeLength = setting("PlaceLength", 1000, 1, 5000).des("Amount of milliseconds the box renders in full for after crystal place").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Float> placeHeight = setting("PlaceHeight", 1.0f, 0.1f, 1.0f).des("Height of place render").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Float> placeY = setting("PlaceY", 0.0f, 0.0f, 1.0f).des("Y offset of place render").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Shape> placeSolidShape = setting("PlaceSolidShape", Shape.Box).des("Solid place render mode").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Boolean> placeSolidGradient = setting("PlaceSolidGradient", false).des("Solid gradient place render").whenTrue(placeRender).when(() -> placeSolidShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Boolean> placeSolidCull = setting("PlaceSolidCull", false).des("Doesn't render interior faces of place render").whenTrue(placeSolidGradient).whenTrue(placeRender).when(() -> placeSolidShape.getValue() == Shape.Box || placeSolidShape.getValue() == Shape.Pyramid).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeSolidColor = setting("PlaceSolidColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(placeRender).when(() -> placeSolidShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeSolidGradientColor = setting("PlaceSolidGradientColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(placeSolidGradient).whenTrue(placeRender).when(() -> placeSolidShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeSolidCrownInnerColor = setting("PlaceSolidCrownInnerColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(placeRender).when(() -> placeSolidShape.getValue() == Shape.Crown).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Shape> placeLinesShape = setting("PlaceLinesShape", Shape.Box).des("Wireframe place render shape").whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Boolean> placeLinesGradient = setting("PlaceLinesGradient", false).des("Wireframe gradient place render").whenTrue(placeRender).when(() -> placeLinesShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Float> placeLinesWidth = setting("PlaceLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe place render lines").whenTrue(placeRender).when(() -> placeLinesShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeLinesColor = setting("PlaceLinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(placeRender).when(() -> placeLinesShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeLinesGradientColor = setting("PlaceLinesGradientColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(placeLinesGradient).whenTrue(placeRender).when(() -> placeLinesShape.getValue() != Shape.None).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Color> placeLinesCrownInnerColor = setting("PlaceLinesCrownInnerColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(placeRender).when(() -> placeLinesShape.getValue() == Shape.Crown).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Boolean> placeFlipRender = setting("PlaceFlipRender", false).des("Flip place render").when(() -> placeSolidShape.getValue() == Shape.Pyramid || placeSolidShape.getValue() == Shape.Crown || placeLinesShape.getValue() == Shape.Pyramid || placeLinesShape.getValue() == Shape.Crown).whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);
    Setting<Float> placeCrownInnerHeight = setting("PlaceCrownInnerHeight", 0.3f, 0.0f, 1.0f).des("Inner height of crown place render").when(() -> placeSolidShape.getValue() == Shape.Crown || placeLinesShape.getValue() == Shape.Crown).whenTrue(placeRender).whenAtMode(placePage, PlacePage.Base).whenAtMode(page, Page.Place);

    Setting<Boolean> placeSpawnShape = setting("PlaceSpawnShape", false).des("Renders a shape on the position you place a crystal and translates it up").whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Integer> placeSpawnShapeVertices = setting("PlaceSpawnShapeVertices", 10, 3, 50).des("Number of corners of shape to render on crystal place position").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Float> placeSpawnShapeRadius = setting("PlaceSpawnShapeRadius", 0.4f, 0.1f, 1.0f).des("Distance of line from center of crystal place block").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Boolean> placeSpawnShapeSpin = setting("PlaceSpawnShapeSpin", false).des("Rotate crystal place spawn shape").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Float> placeSpawnShapeSpinSpeed = setting("PlaceSpawnShapeSpinSpeed", 1.0f, 0.1f, 5.0f).des("Speed of crystal place spawn shape rotation").whenTrue(placeSpawnShapeSpin).whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<SpawnScaleAnimate> placeSpawnShapeScaleAnimateMode = setting("PSpawnShapeScaleMode", SpawnScaleAnimate.None).des("Mode to scale crystal place spawn shape").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Float> placeSpawnShapeAnimateSpeed = setting("PSpawnShapeAnimateSpeed", 1.0f, 0.1f, 4.0f).des("Speed of crystal place spawn shape animation").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Float> placeSpawnShapeLineWidth = setting("PSpawnShapeLineWidth", 1.0f, 1.0f, 5.0f).des("Width of crystal place spawn shape lines").whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);
    Setting<Color> placeSpawnShapeColor = setting("PlaceSpawnShapeColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(placeSpawnShape).whenAtMode(placePage, PlacePage.SpawnShape).whenAtMode(page, Page.Place);

    Setting<Boolean> breakRender = setting("BreakRender", false).des("Renders stuff when you break a crystal").whenAtMode(page, Page.Break);
    Setting<Transition> breakFade = setting("BreakFade", Transition.Alpha).des("Way of fading break render out").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Float> breakFadeSpeed = setting("BreakFadeSpeed", 1.0f, 0.1f, 5.0f).des("Fade speed of break render").whenTrue(breakRender).when(() -> breakFade.getValue() != Transition.None).whenAtMode(page, Page.Break);
    Setting<Integer> breakLength = setting("BreakLength", 150, 1, 2000).des("Amount of milliseconds break render renders in full for").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Float> breakHeight = setting("BreakHeight", 1.0f, 0.1f, 1.0f).des("Height of break render").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Float> breakY = setting("BreakY", 0.0f, 0.0f, 1.0f).des("Y offset of break render").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Shape> breakSolidShape = setting("BreakSolidShape", Shape.Box).des("Solid break render shape").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Boolean> breakSolidGradient = setting("BreakSolidGradient", false).des("Solid gradient break render").whenTrue(breakRender).when(() -> breakSolidShape.getValue() == Shape.Box || breakSolidShape.getValue() == Shape.Pyramid).whenAtMode(page, Page.Break);
    Setting<Boolean> breakSolidCull = setting("BreakSolidCull", false).des("Don't render the interior faces of break render").whenTrue(breakSolidGradient).whenTrue(breakRender).when(() -> breakSolidShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakSolidColor = setting("BreakSolidColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(breakRender).when(() -> breakSolidShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakSolidGradientColor = setting("BreakSolidGradientColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(breakSolidGradient).whenTrue(breakRender).when(() -> breakSolidShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakSolidCrownInnerColor = setting("BreakSolidCrownInnerColor", new Color(new java.awt.Color(100, 61, 255, 50).getRGB())).whenTrue(breakRender).when(() -> breakSolidShape.getValue() == Shape.Crown).whenAtMode(page, Page.Break);
    Setting<Shape> breakLinesShape = setting("BreakLinesShape", Shape.Box).des("Wireframe break render shape").whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Boolean> breakLinesGradient = setting("BreakLinesGradient", false).des("Wireframe gradient break render").whenTrue(breakRender).when(() -> breakLinesShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Float> breakLinesWidth = setting("BreakLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe break render lines").whenTrue(breakRender).when(() -> breakLinesShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakLinesColor = setting("BreakLinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(breakRender).when(() -> breakLinesShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakLinesGradientColor = setting("BreakLinesGradientColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(breakLinesGradient).whenTrue(breakRender).when(() -> breakLinesShape.getValue() != Shape.None).whenAtMode(page, Page.Break);
    Setting<Color> breakLinesCrownInnerColor = setting("BreakLinesCrownInnerColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(breakRender).when(() -> breakLinesShape.getValue() == Shape.Crown).whenAtMode(page, Page.Break);
    Setting<Boolean> breakFlipRender = setting("BreakFlipRender", false).des("Flip break render").when(() -> breakSolidShape.getValue() == Shape.Pyramid || breakSolidShape.getValue() == Shape.Crown || breakLinesShape.getValue() == Shape.Pyramid || breakLinesShape.getValue() == Shape.Crown).whenTrue(breakRender).whenAtMode(page, Page.Break);
    Setting<Float> breakCrownInnerHeight = setting("BreakCrownInnerHeight", 0.3f, 0.0f, 1.0f).des("Inner height of crown break render").when(() -> breakSolidShape.getValue() == Shape.Crown || breakLinesShape.getValue() == Shape.Crown).whenTrue(breakRender).whenAtMode(page, Page.Break);

    private HashMap<BlockPos, Pair<Long, Float>> placeRenderMap = new HashMap<>();
    private HashMap<BlockPos, Pair<Long, Float>> breakRenderMap = new HashMap<>();
    private HashMap<Integer, Pair<Vec3d, Float>> placeSpawnShapeMap = new HashMap<>();
    private final List<EntityEnderCrystal> localCrystalList = new ArrayList<>();
    private CrystalRender placeSingleRender;
    private CrystalRender breakSingleRender;
    private float slidingDelta;
    private AxisAlignedBB prevBB;
    private AxisAlignedBB bb;
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer fadeTimer = new Timer();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private BlockPos lastPlacePos;

    public CrystalActionRender() {
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
        int passedms = (int) fadeTimer.hasPassed();
        fadeTimer.reset();

        if (mc.world == null) return;

        if (multiRender.getValue()) {
            if (placeRender.getValue()) {
                placeRenderMap = updateMap(placeRenderMap, placeFade.getValue(), placeLength.getValue(), placeFadeSpeed.getValue(), passedms);
            }

            if (breakRender.getValue()) {
                breakRenderMap = updateMap(breakRenderMap, breakFade.getValue(), breakLength.getValue(), breakFadeSpeed.getValue(), passedms);
            }
        }
        else {
            if (!placeRender.getValue()) placeSingleRender = null;
            if (slide.getValue())
                slidingDelta = MathUtilFuckYou.clamp(slidingDelta + slideSpeed.getValue() * passedms / 10.0f, 0.0f, 300.0f);

            boolean isPlacing = placeSingleRender != null && !placeTimer.passed(placeLength.getValue());
            boolean isBreaking = placeSingleRender != null && !breakTimer.passed(breakLength.getValue());

            if (placeSingleRender != null) {
                bb = (slide.getValue() && prevBB != null) ? BlockUtil.interpNonLinearBB(prevBB, mc.world.getBlockState(placeSingleRender.pos).getSelectedBoundingBox(mc.world, placeSingleRender.pos), slidingDelta / 300.0f, slideFactor.getValue() / 5.0f)
                        : mc.world.getBlockState(placeSingleRender.pos).getSelectedBoundingBox(mc.world, placeSingleRender.pos);
            }

            if (placeRender.getValue() && placeSingleRender != null) {
                if (placeFade.getValue() != Transition.None && passedms < 1000) {
                    placeSingleRender.animationDelta = MathUtilFuckYou.clamp(placeSingleRender.animationDelta + placeFadeSpeed.getValue() * (passedms / 3.0f) * (isPlacing ? -1.0f : 1.0f), 0.0f, 300.0f);
                }

                if ((placeFade.getValue() != Transition.None && placeSingleRender.animationDelta >= 300.0f)
                        || (placeFade.getValue() == Transition.None && System.currentTimeMillis() - placeSingleRender.initialTime > placeLength.getValue())) {
                    placeSingleRender = null;
                    if (slide.getValue()) prevBB = null;
                }
            }

            if (breakRender.getValue() && breakSingleRender != null) {
                if (breakFade.getValue() != Transition.None && passedms < 1000) {
                    breakSingleRender.animationDelta = MathUtilFuckYou.clamp(breakSingleRender.animationDelta + breakFadeSpeed.getValue() * (passedms / 3.0f) * (isBreaking ? -1.0f : 1.0f), 0.0f, 300.0f);
                }

                if ((breakFade.getValue() != Transition.None && breakSingleRender.animationDelta >= 300.0f)
                        || (breakFade.getValue() == Transition.None && System.currentTimeMillis() - breakSingleRender.initialTime > breakLength.getValue())
                        || (placeRender.getValue() && placeSingleRender != null && !BlockUtil.isSameBlockPos(placeSingleRender.pos, breakSingleRender.pos))) {
                    breakSingleRender = null;
                }
            }
        }

        if (placeSpawnShape.getValue()) {
            HashMap<Integer, Pair<Vec3d, Float>> placeSpawnShapeMapTemp = new HashMap<>();
            placeSpawnShapeMap.forEach((key, value) -> {
                if (value.b < 300.0f) {
                    placeSpawnShapeMapTemp.put(key, new Pair<>(value.a, MathUtilFuckYou.clamp(value.b + placeSpawnShapeAnimateSpeed.getValue() * ((passedms < 1000 ? passedms : 0.0f) / 3.0f), 0.0f, 300.0f)));
                }
            });
            placeSpawnShapeMap = placeSpawnShapeMapTemp;
        }

        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
            if (!(entity instanceof EntityEnderCrystal)) continue;
            if (!localCrystalList.contains((EntityEnderCrystal) entity)){
                localCrystalList.add((EntityEnderCrystal) entity);
                BlockPos crystalPos = entity.getPosition();

                if (placeRender.getValue() && lastPlacePos != null && placeSpawnShape.getValue() && BlockUtil.isSameBlockPos(new BlockPos(MathUtilFuckYou.trollFloor(crystalPos.x), MathUtilFuckYou.trollFloor(crystalPos.y) - 1, MathUtilFuckYou.trollFloor(crystalPos.z)), BlockUtil.floorPos(lastPlacePos))) {
                    placeSpawnShapeMap.put(entity.getEntityId(), new Pair<>(entity.getPositionVector(), 0.0f));
                }
            }
        }

        localCrystalList.removeIf(crystal -> !new ArrayList<>(mc.world.loadedEntityList).contains(crystal));
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
    public void onPacketSend(PacketEvent.Send event) {
        if (placeRender.getValue()) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand();

                if (mc.world.getBlockState(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos()).getBlock() != Blocks.BEDROCK
                        && mc.world.getBlockState(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos()).getBlock() != Blocks.OBSIDIAN) {
                    return;
                }

                if ((hand == EnumHand.MAIN_HAND && (ServerManager.isServerSideHoldingMain(Items.END_CRYSTAL) || mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL))
                        || (hand == EnumHand.OFF_HAND && mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)) {
                    lastPlacePos = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();
                    placeTimer.reset();

                    if (!multiRender.getValue()) {
                        if (!placeRenderMap.isEmpty()) placeRenderMap.clear();
                        if (slide.getValue() && !BlockUtil.isSameBlockPos(lastPlacePos, placeSingleRender != null ? placeSingleRender.pos : null)) {
                            if (placeSingleRender != null) {
                                prevBB = bb == null ? mc.world.getBlockState(placeSingleRender.pos).getSelectedBoundingBox(mc.world, placeSingleRender.pos)
                                        : bb;
                            }
                            slidingDelta = 0.0f;
                        }

                        placeSingleRender = new CrystalRender(lastPlacePos, System.currentTimeMillis(), (slide.getValue() && placeSingleRender != null) ? placeSingleRender.animationDelta
                                                                : 0.0f);
                    } else {
                        placeRenderMap.put(lastPlacePos, new Pair<>(System.currentTimeMillis(), 0.0f));
                    }
                }
            }
        }

        Entity entity;
        if (breakRender.getValue() && event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK
                && (entity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)) != null && entity instanceof EntityEnderCrystal) {
            BlockPos entityPos = entity.getPosition();
            BlockPos basePos = new BlockPos(MathUtilFuckYou.trollFloor(entityPos.x), MathUtilFuckYou.trollFloor(entityPos.y) - 1, MathUtilFuckYou.trollFloor(entityPos.z));

            if (mc.world.getBlockState(basePos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(basePos).getBlock() != Blocks.OBSIDIAN)
                return;
            if (!multiRender.getValue()) {
                if (!breakRenderMap.isEmpty()) breakRenderMap.clear();
                breakTimer.reset();
                breakSingleRender = new CrystalRender(basePos, System.currentTimeMillis(), 0.0f);
            } else {
                breakRenderMap.put(basePos, new Pair<>(System.currentTimeMillis(), 0.0f));
            }
        }
    }

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (multiRender.getValue()) {
            if (placeRender.getValue()) {
                placeRenderMap.forEach((key, value) ->
                        renderStuff(key, placeSolidShape.getValue(), placeLinesShape.getValue(), placeLinesWidth.getValue(), placeSolidColor.getValue().getColor(), placeLinesColor.getValue().getColor(), (300.0f - value.b) / 300.0f, placeHeight.getValue(), placeY.getValue(), placeSolidGradient.getValue(), placeLinesGradient.getValue(), placeSolidGradientColor.getValue().getColor(), placeLinesGradientColor.getValue().getColor(), placeFade.getValue() == Transition.Alpha || placeFade.getValue() == Transition.Both, placeFade.getValue() == Transition.Scale || placeFade.getValue() == Transition.Both, false, null, false, placeSolidGradient.getValue() && !placeSolidCull.getValue(), placeFlipRender.getValue() && (placeSolidShape.getValue() == Shape.Pyramid || placeSolidShape.getValue() == Shape.Crown || placeLinesShape.getValue() == Shape.Pyramid || placeLinesShape.getValue() == Shape.Crown), placeCrownInnerHeight.getValue(), placeLinesCrownInnerColor.getValue().getColor(), placeSolidCrownInnerColor.getValue().getColor()));
            }

            if (breakRender.getValue()) {
                breakRenderMap.forEach((key, value) ->
                        renderStuff(key, breakSolidShape.getValue(), breakLinesShape.getValue(), breakLinesWidth.getValue(), breakSolidColor.getValue().getColor(), breakLinesColor.getValue().getColor(), (300.0f - value.b) / 300.0f, breakHeight.getValue(), breakY.getValue(), breakSolidGradient.getValue(), breakLinesGradient.getValue(), breakSolidGradientColor.getValue().getColor(), breakLinesGradientColor.getValue().getColor(), breakFade.getValue() == Transition.Alpha || breakFade.getValue() == Transition.Both, breakFade.getValue() == Transition.Scale || breakFade.getValue() == Transition.Both, false, null, true, breakSolidGradient.getValue() && !breakSolidCull.getValue(), breakFlipRender.getValue() && (breakSolidShape.getValue() == Shape.Pyramid || breakSolidShape.getValue() == Shape.Crown || breakLinesShape.getValue() == Shape.Pyramid || breakLinesShape.getValue() == Shape.Crown), breakCrownInnerHeight.getValue(), breakLinesCrownInnerColor.getValue().getColor(), breakSolidCrownInnerColor.getValue().getColor()));
            }
        } else {
            if (placeRender.getValue() && placeSingleRender != null && placeSingleRender.pos != null) {
                renderStuff(placeSingleRender.pos, placeSolidShape.getValue(), placeLinesShape.getValue(), placeLinesWidth.getValue(), placeSolidColor.getValue().getColor(), placeLinesColor.getValue().getColor(), (300.0f - placeSingleRender.animationDelta) / 300.0f, placeHeight.getValue(), placeY.getValue(), placeSolidGradient.getValue(), placeLinesGradient.getValue(), placeSolidGradientColor.getValue().getColor(), placeLinesGradientColor.getValue().getColor(), placeFade.getValue() == Transition.Alpha || placeFade.getValue() == Transition.Both, placeFade.getValue() == Transition.Scale || placeFade.getValue() == Transition.Both, slide.getValue(), slide.getValue() && bb != null ? bb : null, false, placeSolidGradient.getValue() && !placeSolidCull.getValue(), placeFlipRender.getValue() && (placeSolidShape.getValue() == Shape.Pyramid || placeSolidShape.getValue() == Shape.Crown || placeLinesShape.getValue() == Shape.Pyramid || placeLinesShape.getValue() == Shape.Crown), placeCrownInnerHeight.getValue(), placeLinesCrownInnerColor.getValue().getColor(), placeSolidCrownInnerColor.getValue().getColor());
            }

            if (breakRender.getValue() && breakSingleRender != null && breakSingleRender.pos != null) {
                renderStuff(breakSingleRender.pos, breakSolidShape.getValue(), breakLinesShape.getValue(), breakLinesWidth.getValue(), breakSolidColor.getValue().getColor(), breakLinesColor.getValue().getColor(), (300.0f - breakSingleRender.animationDelta) / 300.0f, breakHeight.getValue(), breakY.getValue(), breakSolidGradient.getValue(), breakLinesGradient.getValue(), breakSolidGradientColor.getValue().getColor(), breakLinesGradientColor.getValue().getColor(), breakFade.getValue() == Transition.Alpha || breakFade.getValue() == Transition.Both, breakFade.getValue() == Transition.Scale || breakFade.getValue() == Transition.Both, slide.getValue(), (slide.getValue() && bb != null) ? bb : mc.world.getBlockState(breakSingleRender.pos).getSelectedBoundingBox(mc.world, breakSingleRender.pos), true, breakSolidGradient.getValue() && !breakSolidCull.getValue(), breakFlipRender.getValue() && (breakSolidShape.getValue() == Shape.Pyramid || breakSolidShape.getValue() == Shape.Crown || breakLinesShape.getValue() == Shape.Pyramid || breakLinesShape.getValue() == Shape.Crown), breakCrownInnerHeight.getValue(), breakLinesCrownInnerColor.getValue().getColor(), breakSolidCrownInnerColor.getValue().getColor());
            }
        }

        if (placeSpawnShape.getValue()) {
            placeSpawnShapeMap.forEach((key, value) -> {
                float radius = placeSpawnShapeRadius.getValue();

                switch (placeSpawnShapeScaleAnimateMode.getValue()) {
                    case In: {
                        radius *= (300 - value.b) / 300.0f;
                        break;
                    }

                    case Out: {
                        radius *= 1.0f + (value.b / 300.0f);
                        break;
                    }
                }

                SpartanTessellator.drawPolygonLines(new Vec3d(value.a.x, value.a.y + (value.b / 300.0f), value.a.z), placeSpawnShapeVertices.getValue(), placeSpawnShapeLineWidth.getValue(), radius, placeSpawnShapeSpin.getValue(), placeSpawnShapeSpinSpeed.getValue(), value.b, new java.awt.Color(placeSpawnShapeColor.getValue().getColorColor().getRed(), placeSpawnShapeColor.getValue().getColorColor().getGreen(), placeSpawnShapeColor.getValue().getColorColor().getBlue(), (int)(placeSpawnShapeColor.getValue().getAlpha() * (300 - value.b) / 300.0f)).getRGB());
            });
        }
    }

    /**
     * @param animationFactor range: 0.0f - 1.0f
     */

    private void renderStuff(BlockPos pos, Shape solid, Shape lines, float linesWidth, int solidColor, int linesColor, float animationFactor, float height, float yOffset, boolean gradientSolid, boolean gradientLines, int gradientSolidColor, int gradientLinesColor, boolean alphaFade, boolean scaleFade, boolean useBB, AxisAlignedBB bb, boolean isBreak, boolean stopCull, boolean flipRender, float crownInnerHeight, int crownLinesInnerColor, int crownSolidInnerColor) {
        float scale = (isBreak && placeSingleRender != null && breakSyncScale.getValue() && (placeFade.getValue() == Transition.Scale || placeFade.getValue() == Transition.Both) ? (300.0f - placeSingleRender.animationDelta) / 300.0f : 1.0f) * (scaleFade ? animationFactor : 1.0f);
        int solidColor1 = new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), (int)(ColorUtil.getAlpha(solidColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        int gradientSolidColor1 = new java.awt.Color(ColorUtil.getRed(gradientSolidColor), ColorUtil.getGreen(gradientSolidColor), ColorUtil.getBlue(gradientSolidColor), (int)(ColorUtil.getAlpha(gradientSolidColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        int crownSolidInnerColor1 = new java.awt.Color(ColorUtil.getRed(crownSolidInnerColor), ColorUtil.getGreen(crownSolidInnerColor), ColorUtil.getBlue(crownSolidInnerColor), (int)(ColorUtil.getAlpha(crownSolidInnerColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        int linesColor1 = new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), (int)(ColorUtil.getAlpha(linesColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        int gradientLinesColor1 = new java.awt.Color(ColorUtil.getRed(gradientLinesColor), ColorUtil.getGreen(gradientLinesColor), ColorUtil.getBlue(gradientLinesColor), (int)(ColorUtil.getAlpha(gradientLinesColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        int crownLinesInnerColor1 = new java.awt.Color(ColorUtil.getRed(crownLinesInnerColor), ColorUtil.getGreen(crownLinesInnerColor), ColorUtil.getBlue(crownLinesInnerColor), (int)(ColorUtil.getAlpha(crownLinesInnerColor) * (alphaFade ? animationFactor : 1.0f))).getRGB();
        
        GL11.glTranslatef(0.0f, yOffset, 0.0f);
        if (useBB) {
            switch (solid) {
                case Box: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientBlockBBFullBox(bb, stopCull, scale, height, solidColor1, gradientSolidColor1);
                    } else {
                        SpartanTessellator.drawBlockBBFullBox(bb, scale, height, solidColor1);
                    }
                    break;
                }

                case Pyramid: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientPyramidBBFullBox(bb, stopCull, flipRender, scale, height, solidColor1, gradientSolidColor1);
                    } else {
                        SpartanTessellator.drawPyramidBBFullBox(bb, flipRender, scale, height, solidColor1);
                    }
                    break;
                }

                case Crown: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientCrownBBFullBox(bb, flipRender, scale, height, crownInnerHeight, solidColor1, gradientSolidColor1, crownSolidInnerColor1);
                    } else {
                        SpartanTessellator.drawCrownBBFullBox(bb, flipRender, scale, height, crownInnerHeight, solidColor1, crownSolidInnerColor1);
                    }
                    break;
                }
            }

            switch (lines) {
                case Box: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientBlockBBLineBox(bb, scale, linesWidth, height, linesColor1, gradientLinesColor1);
                    } else {
                        SpartanTessellator.drawBlockBBLineBox(bb, scale, linesWidth, height, linesColor1);
                    }
                    break;
                }

                case Pyramid: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientPyramidBBLineBox(bb, flipRender, scale, linesWidth, height, linesColor1, gradientLinesColor1);
                    } else {
                        SpartanTessellator.drawPyramidBBLineBox(bb, flipRender, scale, linesWidth, height, linesColor1);
                    }
                    break;
                }

                case Crown: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientCrownBBLineBox(bb, flipRender, scale, linesWidth, height, crownInnerHeight, linesColor1, gradientLinesColor1, crownLinesInnerColor1);
                    } else {
                        SpartanTessellator.drawCrownBBLineBox(bb, flipRender, scale, linesWidth, height, crownInnerHeight, linesColor1, crownLinesInnerColor1);
                    }
                    break;
                }
            }
        } else {
            switch (solid) {
                case Box: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientBlockBBFullBox(pos, stopCull, scale, height, solidColor1, gradientSolidColor1);
                    } else {
                        SpartanTessellator.drawBlockBBFullBox(pos, scale, height, solidColor1);
                    }
                    break;
                }

                case Pyramid: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientPyramidBBFullBox(pos, stopCull, flipRender, scale, height, solidColor1, gradientSolidColor1);
                    } else {
                        SpartanTessellator.drawPyramidBBFullBox(pos, flipRender, scale, height, solidColor1);
                    }
                    break;
                }

                case Crown: {
                    if (gradientSolid) {
                        SpartanTessellator.drawGradientCrownBBFullBox(pos, flipRender, scale, height, crownInnerHeight, solidColor1, gradientSolidColor1, crownSolidInnerColor1);
                    } else {
                        SpartanTessellator.drawCrownBBFullBox(pos, flipRender, scale, height, crownInnerHeight, solidColor1, crownSolidInnerColor1);
                    }
                    break;
                }
            }

            switch (lines) {
                case Box: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientBlockBBLineBox(pos, scale, linesWidth, height, linesColor1, gradientLinesColor1);
                    } else {
                        SpartanTessellator.drawBlockBBLineBox(pos, scale, linesWidth, height, linesColor1);
                    }
                    break;
                }

                case Pyramid: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientPyramidBBLineBox(pos, flipRender, scale, linesWidth, height, linesColor1, gradientLinesColor1);
                    } else {
                        SpartanTessellator.drawPyramidBBLineBox(pos, flipRender, scale, linesWidth, height, linesColor1);
                    }
                    break;
                }

                case Crown: {
                    if (gradientLines) {
                        SpartanTessellator.drawGradientCrownBBLineBox(pos, flipRender, scale, linesWidth, height, crownInnerHeight, linesColor1, gradientLinesColor1, crownLinesInnerColor1);
                    } else {
                        SpartanTessellator.drawCrownBBLineBox(pos, flipRender, scale, linesWidth, height, crownInnerHeight, linesColor1, crownLinesInnerColor1);
                    }
                    break;
                }
            }
        }
        GL11.glTranslatef(0.0f, -yOffset, 0.0f);
    }

    private HashMap<BlockPos, Pair<Long, Float>> updateMap(HashMap<BlockPos, Pair<Long, Float>> map, Transition transitionMode, float length, float fadeSpeed, int passedms) {
        HashMap<BlockPos, Pair<Long, Float>> mapTemp = new HashMap<>();
        map.entrySet().stream()
                .filter(entry -> (transitionMode != Transition.None && entry.getValue().b < 300.0f)
                        || (transitionMode == Transition.None && System.currentTimeMillis() - entry.getValue().a <= length))
                .forEach(entry -> {
                    if (transitionMode != Transition.None && System.currentTimeMillis() - entry.getValue().a > length && passedms < 1000) {
                        mapTemp.put(entry.getKey(), new Pair<>(entry.getValue().a, MathUtilFuckYou.clamp(entry.getValue().b + fadeSpeed * (passedms / 3.0f), 0.0f, 300.0f)));
                    } else {
                        mapTemp.put(entry.getKey(), entry.getValue());
                    }
                });
        return mapTemp;
    }

    private static class CrystalRender {
        public BlockPos pos;
        public long initialTime;
        public float animationDelta;

        public CrystalRender(BlockPos pos, long initialTime, float animationDelta) {
            this.pos = pos;
            this.initialTime = initialTime;
            this.animationDelta = animationDelta;
        }
    }

    enum Page {
        General,
        Place,
        Break
    }

    enum PlacePage {
        Base,
        SpawnShape
    }

    enum Transition {
        Alpha,
        Scale,
        Both,
        None
    }

    enum Shape {
        Box,
        Pyramid,
        Crown,
        None
    }

    enum SpawnScaleAnimate {
        Out,
        In,
        None
    }
}
