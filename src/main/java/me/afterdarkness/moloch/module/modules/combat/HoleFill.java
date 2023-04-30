package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.module.modules.client.HoleSettings;
import me.afterdarkness.moloch.utils.BlockUtil;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerDigging;
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
@ModuleInfo(name = "HoleFill", category = Category.COMBAT, description = "Fills holes within a radius of you")
public class HoleFill extends Module {

    Setting<Page> page = setting("Page", Page.General);

    Setting<Integer> multiPlace = setting("MultiPlace", 1, 1, 5).des("Blocks to place in a tick").whenAtMode(page, Page.General);
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotates to look at holes to fill").whenAtMode(page, Page.General);
    Setting<Boolean> packetPlace = setting("PacketPlace", false).des("Uses packets to place blocks").whenAtMode(page, Page.General);
    Setting<Boolean> antiGhostBlock = setting("AntiGhostBlock", true).des("Hits block after placing to make sure it isn't a clientside only block").whenAtMode(page, Page.General);
    Setting<Boolean> closestPriority = setting("ClosestPriority", true).des("Fills closest holes first").whenAtMode(page, Page.General);
    public Setting<Boolean> doubleHoles = setting("DoubleHoles", true).des("Holefills 2x1 holes").whenAtMode(page, Page.General);
    Setting<Boolean> webs = setting("Webs", false).des("Uses webs to holefill instead of obsidian").whenAtMode(page, Page.General);
    Setting<Boolean> ignoreSelf = setting("IgnoreSelf", false).des("Doesn't web your own hole").whenTrue(webs).whenAtMode(page, Page.General);
    Setting<Boolean> waitUntilLeave = setting("WaitUntilLeave", false).des("If hole is occupied, don't web until player leaves").whenTrue(webs).whenAtMode(page, Page.General);
    Setting<Boolean> toggle = setting("Toggle", false).des("Automatically disables module when all holes are filled").whenAtMode(page, Page.General);
    Setting<Boolean> smart = setting("Smart", false).des("Only fills holes near players").whenAtMode(page, Page.General);
    Setting<Float> playerRadius = setting("PlayerRadius", 4.0f, 0.0f, 6.0f).des("Radius around players to start placing").whenTrue(smart).whenAtMode(page, Page.General);
    Setting<Boolean> motionPredict = setting("MotionPredict", true).des("Attempts to fill holes in a position where a player will be").whenTrue(smart).whenAtMode(page, Page.General);
    Setting<Float> motionPredictFactor = setting("MotionPredictFactor", 3.0f, 0.0f, 20.0f).des("Factor to multiply the motion predict").whenTrue(motionPredict).whenTrue(smart).whenAtMode(page, Page.General);
    Setting<Integer> delay = setting("Delay", 1, 1, 1000).des("Milliseconds between each block place").whenAtMode(page, Page.General);
    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 200).des("Milliseconds to update holes to fill from HoleSetting holes").whenAtMode(page, Page.General);
    Setting<Float> range = setting("Range", 6.0f, 0.0f, 6.0f).des("Distance to start filling in holes (cannot be bigger than HoleSettings hole range)").whenAtMode(page, Page.General);
    Setting<Float> wallRange = setting("WallRange", 4.0f, 0.0f, 6.0f).des("Distance to start filling in holes if hole is blocked by a wall").whenAtMode(page, Page.General);

    Setting<Boolean> render = setting("Render", true).des("Render positions to be filled in").whenAtMode(page, Page.Render);
    Setting<Boolean> fade = setting("Fade", true).des("Fades out positions to be filled in once they are filled").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> fadeSpeed = setting("FadeSpeed", 2.0f, 0.1f, 3.0f).des("Speed of fade animation").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Movement> movement = setting("Movement", Movement.Up).des("Move render in a direction when it's filled").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> movementSpeed = setting("MovementSpeed", 1.0f, 0.1f, 3.0f).des("Speed of render's movement").when(() -> movement.getValue() != Movement.None).whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> height = setting("Height", 1.0f, 0.1f, 2.0f).des("Height of render for positions to be filled").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Boolean> solid = setting("Solid", true).des("Solid render for positions to be filled in").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(100, 61, 255, 40).getRGB())).whenTrue(solid).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Boolean> lines = setting("Lines", true).des("Wireframe render for positions to be filled in").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> linesWidth = setting("LinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe render lines").whenTrue(render).whenTrue(lines).whenAtMode(page, Page.Render);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(lines).whenTrue(render).whenAtMode(page, Page.Render);

    public static HoleFill INSTANCE;
    private final List<BlockPos> toFillPos = new ArrayList<>();
    private final Timer delayTimer = new Timer();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final HashMap<BlockPos, Pair<Float, Float>> renderMap = new HashMap<>();
    private final Timer renderTimer = new Timer();
    private int index;

    public HoleFill() {
        INSTANCE = this;
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
        if (mc.world == null) return;

        HashMap<BlockPos, Integer> localHolePositions;
        synchronized (HoleSettings.INSTANCE.holePositions) {
            localHolePositions = new HashMap<>(HoleSettings.INSTANCE.holePositions);
        }

        if (doubleHoles.getValue()) {
            synchronized (HoleSettings.INSTANCE.doubleHolePositions) {
                localHolePositions.putAll(HoleSettings.INSTANCE.doubleHolePositions);
            }
        }

        List<BlockPos> tempToFillPos = new ArrayList<>();
        localHolePositions.keySet().forEach(pos -> {
            double dist = MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), new Vec3d(pos));
            if (mc.world.getBlockState(pos).getMaterial().isReplaceable() && (BlockUtil.canSeeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.DOWN).add(0.5, 0.5, 0.5)) ? dist <= range.getValue() * range.getValue() : dist <= wallRange.getValue() * wallRange.getValue())) {
                tempToFillPos.add(pos);
            }
        });

        if (smart.getValue()) {
            List<BlockPos> tempToFillPos2 = new ArrayList<>();
            HashMap<Entity, Pair<Boolean, Boolean>> map;
            synchronized (FriendsEnemies.INSTANCE.entityData) {
                map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
            }
            map.entrySet().stream()
                    .filter(entry -> entry.getKey() != mc.renderViewEntity)
                    .filter(entry -> entry.getKey() != mc.player)
                    .filter(entry -> !entry.getValue().a)
                    .filter(entry -> entry.getKey() instanceof EntityPlayer)
                    .forEach(entry ->
                            tempToFillPos.stream()
                            .filter(pos -> MathUtilFuckYou.isWithinRange(motionPredict.getValue() ? EntityUtil.predict(entry.getKey(), motionPredictFactor.getValue(), true) : entry.getKey().getPositionVector(), new Vec3d(pos.add(0.5, 0.5, 0.5)), playerRadius.getValue()))
                            .forEach(tempToFillPos2::add));
            toFillPos.clear();
            toFillPos.addAll(tempToFillPos2);
            tempToFillPos2.forEach(pos -> renderMap.put(pos, new Pair<>(0.0f, 0.0f)));
        } else {
            toFillPos.clear();
            toFillPos.addAll(tempToFillPos);
            tempToFillPos.forEach(pos -> renderMap.put(pos, new Pair<>(0.0f, 0.0f)));
        }

        if (toggle.getValue() && toFillPos.size() <= 0) {
            toggle();
            return;
        }

        if (closestPriority.getValue()) {
            List<BlockPos> localToFillPos;
            localToFillPos = toFillPos.stream()
                    .sorted(Comparator.comparing(pos -> MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), new Vec3d(pos.add(0.5, 0.5, 0.5)))))
                    .collect(Collectors.toList());
            toFillPos.clear();
            toFillPos.addAll(localToFillPos);
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

    //See WorldRenderPatcher for disabled rendering
    @Override
    public void onRenderWorld(RenderEvent event) {
        if (render.getValue()) {
            int passedms = (int) renderTimer.hasPassed();
            renderTimer.reset();

            HashMap<BlockPos, Pair<Float, Float>> localRenderMap;
            synchronized (renderMap) {
                localRenderMap = new HashMap<>(renderMap);
            }
            localRenderMap.forEach((key, value) -> {
                Vec3d posVec = new Vec3d(key);
                value.a = MathUtilFuckYou.clamp(value.a, 0.0f, 300.0f);
                int solidColor = this.solidColor.getValue().getColor();
                int linesColor = this.linesColor.getValue().getColor();

                if (fade.getValue() && movement.getValue() != Movement.None)
                    GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? 1.0f : -1.0f), 0.0f);

                if (solid.getValue()) {
                    SpartanTessellator.drawBlockFullBox(posVec, false, height.getValue(), new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(solidColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(solidColor)).getRGB());
                }

                if (lines.getValue()) {
                    SpartanTessellator.drawBlockLineBox(posVec, false, height.getValue(), linesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(linesColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(linesColor)).getRGB());
                }

                if (fade.getValue() && movement.getValue() != Movement.None)
                    GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? -1.0f : 1.0f), 0.0f);

                if ((!fade.getValue() && !mc.world.getBlockState(key).getMaterial().isReplaceable()) || (fade.getValue() && value.a >= 300.0f)) {
                    renderMap.remove(key);
                } else if (passedms < 1000) {
                    renderMap.put(key, new Pair<>(MathUtilFuckYou.clamp(value.a + passedms * fadeSpeed.getValue() / 3.0f, 0.0f, 300.0f), value.b + passedms * movementSpeed.getValue() / 1400.0f));
                }
            });
        }
    }

    @Override
    public boolean shouldPersistRender() {
        return !renderMap.isEmpty();
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (rotate.getValue() && multiPlace.getValue() == 1 && delayTimer.passed(delay.getValue()) && toFillPos.size() > 0 && !((webs.getValue() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) || (!webs.getValue() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))))) {
            BlockPos currentFillPos = toFillPos.get((int) MathUtilFuckYou.clamp(index, 0, toFillPos.size() - 1));
            boolean flag = false;
            for (Entity entity : mc.world.loadedEntityList) {
                if (!MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), range.getValue() + 2.0f))
                    continue;

                if (webs.getValue() && webs.getValue()) {
                    if ((mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(mc.player.getEntityBoundingBox()) && ignoreSelf.getValue())
                            || (waitUntilLeave.getValue() && entity instanceof EntityPlayer && entity != mc.player && entity != mc.renderViewEntity && mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(entity.getEntityBoundingBox())))
                        flag = true;
                } else {
                    if (!(entity instanceof EntityPlayer || entity instanceof EntityEnderCrystal))
                        continue;

                    if (mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(entity.getEntityBoundingBox()))
                        flag = true;
                }
            }
            if (flag) {
                index++;
                if (index > toFillPos.size() - 1) {
                    index = 0;
                }
                return;
            }

            RotationManager.setYawAndPitchMotionEvent(event, currentFillPos.add(0.0, -1.0, 0.0), EnumFacing.UP);
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (delayTimer.passed(delay.getValue())) {
            SwapManager.swapInvoke(this.name, false, true, () -> {
                for (int i = 0; i < multiPlace.getValue(); i++) {
                    if (toFillPos.size() <= 0) {
                        continue;
                    }
                    if ((webs.getValue() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) || (!webs.getValue() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)))) {
                        toggle();
                        ChatUtil.sendNoSpamErrorMessage("No blocks to place!");
                        return;
                    }

                    BlockPos currentFillPos = toFillPos.get((int) MathUtilFuckYou.clamp(index, 0, toFillPos.size() - 1));

                    boolean flag = false;
                    for (Entity entity : mc.world.loadedEntityList) {
                        if (!MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), range.getValue() + 2.0f))
                            continue;

                        if (webs.getValue() && webs.getValue()) {
                            if ((mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(mc.player.getEntityBoundingBox()) && ignoreSelf.getValue())
                                    || (waitUntilLeave.getValue() && entity instanceof EntityPlayer && entity != mc.player && entity != mc.renderViewEntity && mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(entity.getEntityBoundingBox())))
                                flag = true;
                        } else {
                            if (!(entity instanceof EntityPlayer || entity instanceof EntityEnderCrystal))
                                continue;

                            if (mc.world.getBlockState(currentFillPos).getSelectedBoundingBox(mc.world, currentFillPos).intersects(entity.getEntityBoundingBox()))
                                flag = true;
                        }
                    }
                    if (flag) {
                        index++;
                        if (index > toFillPos.size() - 1) {
                            index = 0;
                        }
                        continue;
                    }

                    int prevSlot = mc.player.inventory.currentItem;
                    int toSwitchSlot = getFillBlockSlot();
                    if (toSwitchSlot != -1) {
                        ItemUtils.switchToSlot(toSwitchSlot, false);
                    }

                    BlockUtil.placeBlock(BlockUtil.extrudeBlock(currentFillPos, EnumFacing.DOWN), EnumFacing.UP, rotate.getValue() && multiPlace.getValue() > 1, packetPlace.getValue(), false);
                    if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE)
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(currentFillPos, EnumFacing.DOWN), EnumFacing.UP), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(currentFillPos, EnumFacing.DOWN), EnumFacing.UP)))));

                    if (toSwitchSlot != -1) {
                        ItemUtils.switchToSlot(prevSlot, false);
                    }

                    index++;
                    if (index > toFillPos.size() - 1) {
                        index = 0;
                    }
                }
            });

            delayTimer.reset();
        }
    }

    private int getFillBlockSlot() {
        if (webs.getValue()) {
            return ItemUtils.findBlockInHotBar(Blocks.WEB);
        }
        return ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN);
    }

    enum Page {
        General,
        Render
    }

    enum Movement {
        Up,
        Down,
        None
    }
}
