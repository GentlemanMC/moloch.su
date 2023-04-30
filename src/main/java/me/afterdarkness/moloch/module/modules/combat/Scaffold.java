package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.event.events.player.*;
import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.module.modules.movement.SafeWalk;
import me.afterdarkness.moloch.utils.BlockUtil;
import me.afterdarkness.moloch.utils.math.Triple;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.mixin.mixins.accessor.AccessorMinecraft;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.*;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "Scaffold", category = Category.COMBAT, description = "Automatically places blocks under you")
public class Scaffold extends Module {

    Setting<Page> page = setting("Page", Page.General);

    Setting<Boolean> antiGhostBlock = setting("AntiGhostBlock", true).des("Hits block after placing it to make sure it isn't a client-side only block").whenAtMode(page, Page.General);
    Setting<Boolean> onlyBelow = setting("OnlyBelow", false).des("Only places when there aren't any blocks under you").whenAtMode(page, Page.General);
    Setting<Boolean> safeWalk = setting("SafeWalk", true).des("Stops you from walking off the edge of blocks").whenAtMode(page, Page.General);
    Setting<JumpMode> tower = setting("Tower", JumpMode.Vanilla).des("Place up when jumping").whenAtMode(page, Page.General);
    Setting<Integer> towerPacketDelay = setting("TowerPacketDelay", 50, 1, 1000).des("Milliseconds between each position packet").whenAtMode(tower, JumpMode.Packet).whenAtMode(page, Page.General);
    Setting<Boolean> stopYMotion = setting("StopYMotion", false).des("Periodically stops Y motion").when(() -> tower.getValue() != JumpMode.None).whenAtMode(page, Page.General);
    Setting<Integer> stopYMotionDelay = setting("StopYMotionDelay", 1500, 1, 2000).des("Milliseconds between attempts to stop Y motion").whenTrue(stopYMotion).when(() -> tower.getValue() != JumpMode.None).whenAtMode(page, Page.General);
    Setting<Boolean> descend = setting("Descend", true).des("Place down when sneaking").whenAtMode(page, Page.General);
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotates head to look at place position").whenAtMode(page, Page.General);
    Setting<Boolean> stopMotion = setting("StopMotion", false).des("Periodically stop player motion to prevent rubberbanding").whenAtMode(page, Page.General);
    Setting<Integer> stopMotionDelay = setting("StopMotionDelay", 1500, 1, 4000).des("Milliseconds between each attempt to stop motion").whenTrue(stopMotion).whenAtMode(page, Page.General);
    Setting<SwapMode> swapMode = setting("SwapMode", SwapMode.Keep).des("Ways to swap to preferred block").whenAtMode(page, Page.General);
    Setting<Boolean> onlyPreferredBlock = setting("OnlyPreferredBlock", true).des("Only place using the preferred block").whenAtMode(page, Page.General);
    Setting<Boolean> toggle = setting("Toggle", false).des("Disables module when preferred block isn't in hotbar").whenAtMode(page, Page.General);
    Setting<String> preferredBlock = setting("PreferredBlock", "OBSIDIAN", false, null, false).des("Name of block to prefer").whenAtMode(page, Page.General);
    Setting<Integer> delay = setting("Delay", 50, 1, 500).des("Milliseconds between each place attempt").whenAtMode(page, Page.General);
    Setting<Float> predictRange = setting("PredictRange", 1.0f, 0.5f, 4.0f).des("How many blocks ahead to start placing").whenFalse(onlyBelow).whenAtMode(page, Page.General);

    Setting<RenderPage> renderPage = setting("RenderPage", RenderPage.PlacePos).whenAtMode(page, Page.Render);

    Setting<Boolean> placePosRender = setting("PlacePosRender", true).des("Renders a box at the place position").whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Boolean> slide = setting("Slide", false).des("Slides render to place positions").whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Float> slideSpeed = setting("SlideSpeed", 2.0f, 0.1f, 10.0f).des("Speed of sliding animation").whenTrue(slide).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Float> slideFactor = setting("SlideFactor", 0.5f, 0.1f, 1.0f).des("Steepness of sliding animation").whenTrue(slide).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Boolean> fade = setting("Fade", true).des("Fades out rendered positions").whenFalse(slide).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Float> fadeSpeed = setting("FadeSpeed", 2.0f, 0.1f, 3.0f).des("Speed that renders fade out").when(() -> fade.getValue() || slide.getValue()).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Movement> movement = setting("Movement", Movement.Up).des("Move render in a direction when a position has been placed in").whenTrue(fade).whenFalse(slide).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Float> movementSpeed = setting("MovementSpeed", 1.0f, 0.1f, 3.0f).des("Speed of render's movement").when(() -> movement.getValue() != Movement.None).whenTrue(fade).whenFalse(slide).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Boolean> placePosSolid = setting("PlacePosSolid", true).des("Renders solid box at place pos").whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Color> placePosSolidColor = setting("PlacePosSolidColor", new Color(new java.awt.Color(100, 61, 255, 40).getRGB())).whenTrue(placePosSolid).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Boolean> placePosLines = setting("PlacePosLines", true).des("Renders wireframe box at place pos").whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Float> placePosLinesWidth = setting("PlacePosLinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe box lines").whenTrue(placePosLines).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);
    Setting<Color> placePosLinesColor = setting("PlacePosLinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(placePosLines).whenTrue(placePosRender).whenAtMode(renderPage, RenderPage.PlacePos).whenAtMode(page, Page.Render);

    Setting<Boolean> footRender = setting("FootRender", false).des("Renders stuff at ur foot when scaffolding").whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderAnimate = setting("FootRenderAnimate", true).des("Animates foot render on scaffold enable and disable").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderAnimateSpeed = setting("FootRenderAnimateSpeed", 2.0f, 0.1f, 3.0f).des("Speed that foot render animates in on scaffold enable and disable").whenTrue(footRenderAnimate).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderRadius = setting("FootRenderRadius", 0.5f, 0.1f, 4.0f).des("Radius of foot render").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Integer> footRenderVertices = setting("FootRenderVertices", 4, 3, 50).des("Vertex count of foot render").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderPlayerYaw = setting("FootRenderPlayerYaw", false).des("Rotates foot render to your yaw").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderRotateAnimate = setting("FootRenderRotateAnimate", false).des("Moves rotation of foot render").whenFalse(footRenderPlayerYaw).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderRotateSpeed = setting("FootRenderRotateSpeed", 1.0f, 0.1f, 5.0f).des("Speed of foot render rotation").whenTrue(footRenderRotateAnimate).whenFalse(footRenderPlayerYaw).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Integer> footRenderAngle = setting("FootRenderAngle", 0, 0, 360).des("Degrees offset of foot render").whenFalse(footRenderRotateAnimate).whenFalse(footRenderPlayerYaw).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderSolid = setting("FootRenderSolid", true).des("Foot render solid").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderSolidInner = setting("FootRenderSolidInner", false).des("Renders another solid shape inside of foot render solid").whenTrue(footRenderSolid).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderSolidInnerScale = setting("FRenderSolidInnerScale", 0.6f, 0.1f, 1.0f).des("Scale of foot render solid inner shape").whenTrue(footRenderSolidInner).whenTrue(footRenderSolid).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Color> footRenderSolidColor = setting("FootRenderSolidColor", new Color(new java.awt.Color(100, 61, 255, 40).getRGB())).whenTrue(footRenderSolid).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderLines = setting("FootRenderLines", true).des("Foot render wireframe").whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderLinesWidth = setting("FootRenderLinesWidth", 1.0f, 1.0f, 5.0f).whenTrue(footRenderLines).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Boolean> footRenderLinesInner = setting("FootRenderLinesInner", false).des("Renders another wireframe shape inside of foot render wireframe").whenTrue(footRenderLines).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Float> footRenderLinesInnerScale = setting("FRenderLinesInnerScale", 0.6f, 0.1f, 1.0f).des("Scale of foot render wireframe inner shape").whenTrue(footRenderLinesInner).whenTrue(footRenderLines).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);
    Setting<Color> footRenderLinesColor = setting("FootRenderLinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(footRenderLines).whenTrue(footRender).whenAtMode(renderPage, RenderPage.FeetRender).whenAtMode(page, Page.Render);

    private final HashMap<BlockPos, Pair<Float, Float>> renderMap = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final Timer renderTimer = new Timer();
    private final Timer delayTimer = new Timer();
    private final Timer stopMotionTimer = new Timer();
    private final Timer placeTimeout = new Timer();
    private final Timer stopMotionYTimer = new Timer();
    private final Timer packetJumpTimer = new Timer();
    private float footRenderDelta;
    private float slidingDelta;
    private float slidingAlpha;
    private Vec3d slideVec;
    private Vec3d prevRenderVec;
    private BlockPos prevPlacePos;
    private BlockPos cachedPlacePos;
    private BlockPos placePos;
    private EnumFacing placeFace;
    private boolean swapFlag;
    private int prevSlot;
    private double prevYPos;

    public Scaffold() {
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
        int passedms = (int) renderTimer.hasPassed();
        renderTimer.reset();

        if (mc.world == null) return;

        if (placePosRender.getValue()) {
            if (slide.getValue()) {
                if (prevPlacePos == null || prevRenderVec == null) return;

                if (placePos != null && placeFace != null && !BlockUtil.isSameBlockPos(prevPlacePos, BlockUtil.extrudeBlock(placePos, placeFace))) {
                    if (slidingAlpha >= 300.0f) {
                        prevPlacePos = BlockUtil.extrudeBlock(placePos, placeFace);
                        prevRenderVec = new Vec3d(prevPlacePos);
                    }

                    slidingDelta = 0.0f;
                    slidingAlpha = 0.0f;
                }

                slidingDelta = MathUtilFuckYou.clamp(slidingDelta + slideSpeed.getValue() * passedms / 10.0f, 0.0f, 300.0f);
                slideVec = BlockUtil.interpNonLinearVec(prevRenderVec, new Vec3d(prevPlacePos), slidingDelta / 300.0f, slideFactor.getValue() / 5.0f);
                slidingAlpha = MathUtilFuckYou.clamp(slidingAlpha + passedms * fadeSpeed.getValue() / 3.0f, 0.0f, 300.0f);
            }
            else if (fade.getValue()) {
                if (placePos != null && placeFace != null)
                    renderMap.put(BlockUtil.extrudeBlock(placePos, placeFace), new Pair<>(0.0f, 0.0f));

                renderMap.forEach((key, value) -> {
                    if ((!fade.getValue() && !mc.world.getBlockState(key).getMaterial().isReplaceable()) || (fade.getValue() && value.a >= 300.0f)) {
                        renderMap.remove(key);
                    } else if (passedms < 1000) {
                        renderMap.put(key, new Pair<>(MathUtilFuckYou.clamp(value.a + passedms * fadeSpeed.getValue() / 3.0f, 0.0f, 300.0f), value.b + passedms * movementSpeed.getValue() / 1400.0f));
                    }
                });
            }
        }

        if (footRender.getValue() && passedms < 1000) {
            footRenderDelta = MathUtilFuckYou.clamp(footRenderDelta + passedms * footRenderAnimateSpeed.getValue() / 3.0f * (isEnabled() ? 1.0f : -1.0f), 0.0f, 300.0f);
        }
    });

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (placePosRender.getValue()) {
            int solidColor = placePosSolidColor.getValue().getColor();
            int linesColor = placePosLinesColor.getValue().getColor();

            if (slide.getValue()) {
                if (slideVec == null) return;

                if (placePosSolid.getValue())
                    SpartanTessellator.drawBlockFullBox(slideVec, false, 1.0f, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), (int) (ColorUtil.getAlpha(solidColor) * (300 - slidingAlpha) / 300.0f)).getRGB());

                if (placePosLines.getValue())
                    SpartanTessellator.drawBlockLineBox(slideVec, false, 1.0f, placePosLinesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), (int) (ColorUtil.getAlpha(linesColor) * (300 - slidingAlpha) / 300.0f)).getRGB());
            }
            else {
                if (fade.getValue()) {
                    HashMap<BlockPos, Pair<Float, Float>> tempRenderMap;
                    synchronized (renderMap) {
                        tempRenderMap = new HashMap<>(renderMap);
                    }
                    tempRenderMap.forEach((key, value) -> {
                        value.a = MathUtilFuckYou.clamp(value.a, 0.0f, 300.0f);

                        if (fade.getValue() && movement.getValue() != Movement.None)
                            GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? 1.0f : -1.0f), 0.0f);

                        if (placePosSolid.getValue())
                            SpartanTessellator.drawBlockFullBox(new Vec3d(key), false, 1.0f, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), (int) (ColorUtil.getAlpha(solidColor) * (300 - value.a) / 300.0f)).getRGB());

                        if (placePosLines.getValue())
                            SpartanTessellator.drawBlockLineBox(new Vec3d(key), false, 1.0f, placePosLinesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), (int) (ColorUtil.getAlpha(linesColor) * (300 - value.a) / 300.0f)).getRGB());

                        if (fade.getValue() && movement.getValue() != Movement.None)
                            GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? -1.0f : 1.0f), 0.0f);
                    });
                }
                else if (placePos != null && placeFace != null) {
                    BlockPos pos = BlockUtil.extrudeBlock(placePos, placeFace);

                    if (placePosSolid.getValue())
                        SpartanTessellator.drawBlockFullBox(new Vec3d(pos), false, 1.0f, solidColor);

                    if (placePosLines.getValue())
                        SpartanTessellator.drawBlockLineBox(new Vec3d(pos), false, 1.0f, placePosLinesWidth.getValue(), linesColor);
                }
            }
        }

        if (footRender.getValue()) {
            int solidColor = footRenderSolidColor.getValue().getColor();
            int linesColor = footRenderLinesColor.getValue().getColor();
            float radius = (footRenderAnimate.getValue() ? footRenderDelta / 300.0f : 1.0f) * footRenderRadius.getValue();
            float theta = footRenderPlayerYaw.getValue() ? (360.0f - mc.player.rotationYaw)
                                : (footRenderRotateAnimate.getValue() ? (float) (System.currentTimeMillis() / 10.0 % 360.0) : footRenderAngle.getValue());

            if (footRenderSolid.getValue()) {
                SpartanTessellator.drawPolygonSolid(EntityUtil.getInterpolatedEntityPos(mc.player, mc.getRenderPartialTicks()), footRenderVertices.getValue(), radius,
                        footRenderRotateAnimate.getValue(), footRenderRotateSpeed.getValue(), theta, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), (int) (ColorUtil.getAlpha(solidColor) * (footRenderAnimate.getValue() ? footRenderDelta / 300.0f : 1.0f))).getRGB());

                if (footRenderSolidInner.getValue())
                    SpartanTessellator.drawPolygonSolid(EntityUtil.getInterpolatedEntityPos(mc.player, mc.getRenderPartialTicks()), footRenderVertices.getValue(), radius * footRenderSolidInnerScale.getValue(),
                            footRenderRotateAnimate.getValue(), footRenderRotateSpeed.getValue(), theta, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), (int) (ColorUtil.getAlpha(solidColor) * (footRenderAnimate.getValue() ? footRenderDelta / 300.0f : 1.0f))).getRGB());
            }

            if (footRenderLines.getValue()) {
                SpartanTessellator.drawPolygonLines(EntityUtil.getInterpolatedEntityPos(mc.player, mc.getRenderPartialTicks()), footRenderVertices.getValue(), footRenderLinesWidth.getValue(), radius,
                        footRenderRotateAnimate.getValue(), footRenderRotateSpeed.getValue(), theta, new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), (int) (ColorUtil.getAlpha(linesColor) * (footRenderAnimate.getValue() ? footRenderDelta / 300.0f : 1.0f))).getRGB());

                if (footRenderLinesInner.getValue())
                    SpartanTessellator.drawPolygonLines(EntityUtil.getInterpolatedEntityPos(mc.player, mc.getRenderPartialTicks()), footRenderVertices.getValue(), footRenderLinesWidth.getValue(), radius * footRenderLinesInnerScale.getValue(),
                            footRenderRotateAnimate.getValue(), footRenderRotateSpeed.getValue(), theta, new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), (int) (ColorUtil.getAlpha(linesColor) * (footRenderAnimate.getValue() ? footRenderDelta / 300.0f : 1.0f))).getRGB());
            }
        }
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
        stopMotionYTimer.reset();
    }

    @Override
    public void onDisable() {
        placePos = null;
        prevYPos = 0.0;
        placeFace = null;
        renderMap.clear();

        if (swapMode.getValue() == SwapMode.Keep && swapFlag) {
            ItemUtils.switchToSlot(prevSlot, false);
            swapFlag = false;
        }
    }

    @Override
    public boolean shouldPersistRender() {
        if ((footRender.getValue() && footRenderAnimate.getValue() && footRenderDelta > 0.0f)
                || (placePosRender.getValue() && slide.getValue() && slidingAlpha < 300.0f)
                || (placePosRender.getValue() && fade.getValue() && !renderMap.isEmpty())) {
            return true;
        }
        else {
            repeatUnits.forEach(RepeatUnit::suspend);
        }
        return false;
    }

    @Listener
    public void onRubberband(RubberbandEvent event) {
        placePos = null;
        prevYPos = MathUtilFuckYou.trollFloor(mc.player.posY);
        placeFace = null;
    }

    @Listener
    public void setSneak(SetSneakEvent event) {
        if (mc.player.onGround && descend.getValue() && mc.player.movementInput.sneak) {
            event.cancel();
            event.isSneaking = false;
        }
    }

    @Listener
    public void setJump(JumpEvent event) {
        if (mc.player.movementInput.jump && tower.getValue() == JumpMode.Packet && EntityUtil.isStill()) {
            event.cancel();
        }
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (delayTimer.passed(delay.getValue())) {
            if (cachedPlacePos != null) {
                prevPlacePos = cachedPlacePos;
                if (placePosRender.getValue() && slide.getValue())
                    prevRenderVec = slideVec == null ? new Vec3d(cachedPlacePos) : slideVec;
            }

            findPlacePos();
            if (placePos != null && placeFace != null)
                cachedPlacePos = BlockUtil.extrudeBlock(placePos, placeFace);

            if (rotate.getValue() && !(onlyPreferredBlock.getValue() && (Block.getBlockFromName(preferredBlock.getValue().toUpperCase()) == null || !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Block.getBlockFromName(preferredBlock.getValue().toUpperCase())))))) {
                if (placePos != null && placeFace != null && !(swapMode.getValue() == SwapMode.None && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) && BlockUtil.isFacePlaceble(placePos, placeFace, false)) {
                    RotationManager.setYawAndPitchMotionEvent(event, placePos, placeFace);
                }
            }
        }

        if (placePos != null && placeFace != null && !mc.world.getBlockState(BlockUtil.extrudeBlock(placePos, placeFace)).getMaterial().isReplaceable()) {
            placePos = null;
            placeFace = null;
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (!mc.world.getBlockState(EntityUtil.floorEntity(mc.player).add(0.0, -1.0, 0.0)).getMaterial().isReplaceable()) {
            prevYPos = MathUtilFuckYou.trollFloor(mc.player.posY);
        }

        if (placePos != null && !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), new Vec3d(placePos).add(0.5, 0.5, 0.5), 5.0f)) {
            placePos = null;
            prevYPos = 0.0;
            placeFace = null;
        }

        if (!canTower() && !placeTimeout.passed(700) && stopMotionTimer.passed(stopMotionDelay.getValue())) {
            mc.player.motionX = 0.0;
            mc.player.motionY = 0.0;
            mc.player.motionZ = 0.0;
            stopMotionTimer.reset();
        }

        if (delayTimer.passed(delay.getValue())) {
            SwapManager.swapInvoke(this.name, swapMode.getValue() == SwapMode.None, true, this::doScaffold);
            delayTimer.reset();
        }
    }

    @Listener
    public void onPlayerMove(PlayerMoveEvent event) {
        if (safeWalk.getValue()) {
            SafeWalk.INSTANCE.preventFall(event, descend.getValue() && mc.gameSettings.keyBindSneak.isKeyDown() ? 1.1 : 0.01, !(descend.getValue() && mc.gameSettings.keyBindSneak.isKeyDown()));
        }
    }

    @Listener
    public void onInput(MovementInputEvent event) {
        if (safeWalk.getValue() && descend.getValue() && mc.player.movementInput.sneak) {
            mc.player.movementInput.moveStrafe /= 0.3f;
            mc.player.movementInput.moveForward /= 0.3f;
        }
    }

    private void doScaffold() {
        if (toggle.getValue() && getPreferredBlock() == null) {
            toggle();
            ChatUtil.sendNoSpamErrorMessage("No blocks to place!");
            return;
        }

        if (canTower() && mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f) {
            ((AccessorMinecraft) mc).setRightClickDelayTimer(3);
            towerMove();

            if (stopYMotion.getValue() && stopMotionYTimer.passed(stopYMotionDelay.getValue())) {
                mc.player.motionY = -0.28;
                stopMotionYTimer.reset();
            }
        } else {
            stopMotionYTimer.reset();
        }

        if (onlyPreferredBlock.getValue() && (Block.getBlockFromName(preferredBlock.getValue().toUpperCase()) == null || !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Block.getBlockFromName(preferredBlock.getValue().toUpperCase()))))) {
            if (swapFlag) {
                ItemUtils.switchToSlot(prevSlot, false);
                swapFlag = false;
            }
            return;
        }

        int toSwitchSlot = ItemUtils.findBlockInHotBar(getPreferredBlock());

        if (!swapFlag) {
            prevSlot = mc.player.inventory.currentItem;
            swapFlag = true;
        }

        if (toSwitchSlot != -1 && swapMode.getValue() == SwapMode.Keep) {
            if (mc.player.inventory.currentItem != toSwitchSlot) ItemUtils.switchToSlot(toSwitchSlot, false);
        } else if (swapFlag) {
            ItemUtils.switchToSlot(prevSlot, false);
            swapFlag = false;
        }

        if (placePos == null || placeFace == null
                || (swapMode.getValue() == SwapMode.None && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock))
                || !BlockUtil.isFacePlaceble(placePos, placeFace, false)
                || (onlyBelow.getValue() && !mc.world.getBlockState(EntityUtil.floorEntity(mc.player).add(0.0, -1.0, 0.0)).getMaterial().isReplaceable()))
            return;

        if (toSwitchSlot != -1 && mc.player.inventory.currentItem != toSwitchSlot) {
            if (swapMode.getValue() == SwapMode.SwapBack) {
                ItemUtils.switchToSlot(toSwitchSlot, false);
            }
        }

        BlockUtil.placeBlock(placePos, placeFace, false, false, false);
        if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE)
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, placePos, BlockUtil.getVisibleBlockSide(new Vec3d(placePos))));
        placeTimeout.reset();

        if (toSwitchSlot != -1) {
            if (swapMode.getValue() == SwapMode.SwapBack) {
                ItemUtils.switchToSlot(prevSlot, false);
            }
        }
    }

    private void findPlacePos() {
        if (mc.world == null || mc.player == null) return;

        Vec3d predictedSelfVec = EntityUtil.predictConstant(mc.player, mc.player.motionY == 0.0 ? 0.1f : predictRange.getValue(), false);
        BlockPos selfDownPos = EntityUtil.floorEntity(mc.player).add(0.0, -1.0, 0.0);
        BlockPos predictDownPos = BlockUtil.floorPos(predictedSelfVec).add(0.0, -1.0, 0.0);
        List<Pair<BlockPos, EnumFacing>> toPlacePoses = new ArrayList<>();
        List<BlockPos> closePoses = BlockUtil.getSphere(mc.player.getPositionVector(), 4.0f, true);

        if (closePoses.size() <= 0) return;

        if (canTower()) {
            placePos = selfDownPos.add(0.0, -1.0, 0.0);
            placeFace = EnumFacing.UP;
            return;
        }

        if (descend.getValue() && mc.player.movementInput.sneak) {
            getDescendPos(selfDownPos);
            return;
        }

        if (mc.world.getBlockState(selfDownPos).getMaterial().isReplaceable()
                && !mc.world.getBlockState(selfDownPos.add(0.0, -1.0, 0.0)).getMaterial().isReplaceable()) {
            boolean flag = false;
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos extrudedPos = BlockUtil.extrudeBlock(selfDownPos, facing);
                if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable())
                    flag = true;
            }
            if (!flag) {
                placePos = selfDownPos.add(0.0, -2.0, 0.0);
                placeFace = EnumFacing.UP;
                return;
            }
        }

        List<BlockPos> currentPoses = closePoses.stream()
                .filter(pos -> !mc.world.getBlockState(pos).getMaterial().isReplaceable())
                .filter(pos -> pos.getY() == selfDownPos.getY())
                .sorted(Comparator.comparing(pos -> MathUtilFuckYou.getDistSq(new Vec3d(pos), mc.player.getPositionVector())))
                .collect(Collectors.toList());

        if (currentPoses.isEmpty()) return;

        BlockPos currentPos = currentPoses.get(0);
        EnumFacing currentFacing = null;

        if (onlyBelow.getValue()) {
            predictDownPos = selfDownPos;
        }

        for (int i = 0; i < 10; i++) {
            if (mc.world.getBlockState(currentPos).getMaterial().isReplaceable()) {
                if (currentFacing == null) {
                    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                        BlockPos extrudedPos = BlockUtil.extrudeBlock(currentPos, facing);
                        if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                            placePos = extrudedPos;
                            placeFace = facing.getOpposite();
                        }
                    }
                } else {
                    toPlacePoses.stream()
                            .filter(data -> BlockUtil.isFacePlaceble(data.a, data.b, false))
                            .forEach(data -> {
                                placePos = data.a;
                                placeFace = data.b;
                            });
                }
                break;
            }

            if (BlockUtil.isSameBlockPos(currentPos, predictDownPos))
                break;

            BlockPos prevPos = new BlockPos(currentPos);
            List<Triple<Double, BlockPos, EnumFacing>> list = new ArrayList<>();
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos extrudedPos = BlockUtil.extrudeBlock(currentPos, facing);
                double currentDist = MathUtilFuckYou.getDistSq(new Vec3d(extrudedPos), new Vec3d(predictDownPos));

                list.add(new Triple<>(currentDist, extrudedPos, currentFacing));
            }

            final Triple<Double, BlockPos, EnumFacing> data = list.stream()
                    .sorted(Comparator.comparing(data1 -> data1.a))
                    .collect(Collectors.toList()).get(0);

            toPlacePoses.add(new Pair<>(prevPos, data.c));
            currentPos = data.b;
            currentFacing = data.c;
        }
    }

    private void getDescendPos(BlockPos selfDownPos) {
        BlockPos selfDownDownPos = selfDownPos.add(0.0, -1.0, 0.0);

        if (mc.world.getBlockState(selfDownDownPos).getMaterial().isReplaceable()) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos extrudedPos = BlockUtil.extrudeBlock(selfDownDownPos, facing);
                if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                    placePos = extrudedPos;
                    placeFace = facing.getOpposite();
                    if (!mc.world.getBlockState(BlockUtil.extrudeBlock(placePos, placeFace).add(0.0, 2.0, 0.0)).getMaterial().isReplaceable()) {
                        placePos = null;
                        placeFace = null;
                    }
                }
            }
        }
    }

    private Block getPreferredBlock() {
        Block preferredBlock1 = Block.getBlockFromName(preferredBlock.getValue().toUpperCase());
        if (preferredBlock1 == null) return null;
        if (ItemUtils.isItemInHotbar(Item.getItemFromBlock(preferredBlock1))) {
            return preferredBlock1;
        } else {
            Block outBlock = null;
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
                if (itemStack.getItem() instanceof ItemBlock)
                    outBlock = ((ItemBlock) itemStack.getItem()).getBlock();
            }
            return outBlock;
        }
    }

    private boolean canTower() {
        return mc.player.movementInput.jump && (tower.getValue() == JumpMode.Packet || !mc.player.onGround);
    }

    private void towerMove() {
        switch (tower.getValue()) {
            case Vanilla: {
                if (mc.player.posY - prevYPos >= 1.0) mc.player.jump();
                break;
            }

            case Packet: {
                if (mc.player.onGround && packetJumpTimer.passed(towerPacketDelay.getValue())) {
                    mc.player.isAirBorne = true;
                    mc.player.addStat(StatList.JUMP, 1);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 1.2, mc.player.posZ);
                    packetJumpTimer.reset();
                }
                break;
            }

            case Motion: {
                if (mc.player.posY - prevYPos >= 1.0) {
                    mc.player.isAirBorne = true;
                    mc.player.addStat(StatList.JUMP, 1);
                    mc.player.motionY = 0.42;
                }
                break;
            }
        }
    }

    enum Page {
        General,
        Render
    }

    enum RenderPage {
        PlacePos,
        FeetRender
    }

    enum SwapMode {
        Keep,
        SwapBack,
        None
    }

    enum JumpMode {
        Vanilla,
        Packet,
        Motion,
        None
    }

    enum Movement {
        Up,
        Down,
        None
    }
}
