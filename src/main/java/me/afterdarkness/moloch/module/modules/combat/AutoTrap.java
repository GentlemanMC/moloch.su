package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.*;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "AutoTrap", category = Category.COMBAT, description = "Traps opponents in obsidian")
public class AutoTrap extends Module {

    Setting<Page> page = setting("Page", Page.General);

    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 200).des("Milliseconds delay between updating place positions").whenAtMode(page, Page.General);
    Setting<Boolean> fullTrap = setting("FullTrap", true).des("Traps entire player versus only trapping their head").whenAtMode(page, Page.General);
    Setting<Boolean> packetPlace = setting("PacketPlace", false).des("Uses packets to place blocks").whenAtMode(page, Page.General);
    Setting<Boolean> antiGhostBlock = setting("AntiGhostBlock", true).des("Hits block after placing it to prevent placing clientside blocks").whenAtMode(page, Page.General);
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotates to look at place position").whenAtMode(page, Page.General);
    Setting<Boolean> toggle = setting("Toggle", false).des("Automatically disables module when done trapping all players once").whenAtMode(page, Page.General);
    Setting<Boolean> topExtend = setting("TopExtend", false).des("Places an extra block on top to prevent players from mining the top block and escaping").whenAtMode(page, Page.General);
    Setting<Boolean> topExtendOnMine = setting("TopExtendOnMine", false).des("Only extends top block if target player is mining it").whenFalse(toggle).whenTrue(topExtend).whenAtMode(page, Page.General);
    Setting<Integer> topExtendOnMineTimeout = setting("TopExtendOnMineTimeout", 700, 1, 1000).des("Milliseconds after player starts mining their top block to attempt to extend top block").whenTrue(topExtendOnMine).whenFalse(toggle).whenTrue(topExtend).whenAtMode(page, Page.General);
    Setting<Integer> delay = setting("Delay", 1, 1, 1000).des("Milliseconds between each place attempt").whenAtMode(page, Page.General);
    Setting<Float> range = setting("Range", 6.0f, 0.0f, 6.0f).des("Distance to start placing").whenAtMode(page, Page.General);
    Setting<Float> wallRange = setting("WallRange", 4.0f, 0.0f, 6.0f).des("Distance to start placing if place position isn't visible").whenAtMode(page, Page.General);

    Setting<Boolean> render = setting("Render", true).des("Renders stuff at positions to place blocks").whenAtMode(page, Page.Render);
    Setting<Boolean> fade = setting("Fade", true).des("Fades out positions to be placed in").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> fadeSpeed = setting("FadeSpeed", 2.0f, 0.1f, 3.0f).des("Speed of fade animation").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Movement> movement = setting("Movement", Movement.Up).des("Move render in a direction when a position has been placed in").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> movementSpeed = setting("MovementSpeed", 1.0f, 0.1f, 3.0f).des("Speed of render's movement").when(() -> movement.getValue() != Movement.None).whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Boolean> solid = setting("Solid", true).des("Solid render for positions to place blocks").whenAtMode(page, Page.Render);
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(100, 61, 255, 40).getRGB())).whenTrue(solid).whenAtMode(page, Page.Render);
    Setting<Boolean> lines = setting("Lines", true).des("Wireframe render for positions to place blocks").whenAtMode(page, Page.Render);
    Setting<Float> linesWidth = setting("LinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe render lines").whenTrue(lines).whenAtMode(page, Page.Render);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(100, 61, 255, 150).getRGB())).whenTrue(lines).whenAtMode(page, Page.Render);

    public static AutoTrap INSTANCE;
    private final List<Pair<BlockPos, EnumFacing>> toPlacePoses = new ArrayList<>();
    private final Timer delayTimer = new Timer();
    private final Timer topPlaceTimer = new Timer();
    private final Timer renderTimer = new Timer();
    private final HashMap<Entity, BlockPos> topPlacePoses = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final HashMap<BlockPos, Pair<Float, Float>> renderMap = new HashMap<>();
    private int index;

    public AutoTrap() {
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

    RepeatUnit update = new RepeatUnit(() -> updateDelay.getValue(), this::findPlacePoses);

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
        if (topExtend.getValue() && topExtendOnMine.getValue() && event.getPacket() instanceof SPacketBlockBreakAnim && !topPlacePoses.isEmpty()) {
            HashMap<Entity, BlockPos> localTopPlacePoses;
            synchronized (topPlacePoses) {
                localTopPlacePoses = new HashMap<>(topPlacePoses);
            }
            localTopPlacePoses.entrySet().stream()
                    .filter(entry -> BlockUtil.isSameBlockPos(BlockUtil.floorPos(((SPacketBlockBreakAnim) event.getPacket()).getPosition()), entry.getValue()))
                    .forEach(entry -> topPlaceTimer.reset());
        }
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

                if (solid.getValue())
                    SpartanTessellator.drawBlockFullBox(posVec, false, 1.0f, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(solidColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(solidColor)).getRGB());

                if (lines.getValue())
                    SpartanTessellator.drawBlockLineBox(posVec, false, 1.0f, linesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(linesColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(linesColor)).getRGB());

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
        if (delayTimer.passed(delay.getValue()) && rotate.getValue() && toPlacePoses.size() > 0 && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
            Pair<BlockPos, EnumFacing> currentPlaceData = toPlacePoses.get((int) MathUtilFuckYou.clamp(index, 0, toPlacePoses.size() - 1));
            RotationManager.setYawAndPitchMotionEvent(event, currentPlaceData.a, currentPlaceData.b);
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (delayTimer.passed(delay.getValue())) {
            SwapManager.swapInvoke(this.name, false, true, () -> {
                if (toPlacePoses.size() <= 0) {
                    return;
                }

                if (!ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
                    toggle();
                    ChatUtil.sendNoSpamErrorMessage("No blocks to place!");
                    return;
                }

                Pair<BlockPos, EnumFacing> currentPlaceData = toPlacePoses.get((int) MathUtilFuckYou.clamp(index, 0, toPlacePoses.size() - 1));

                int prevSlot = mc.player.inventory.currentItem;
                int toSwitchSlot = ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN);
                if (toSwitchSlot != -1) {
                    ItemUtils.switchToSlot(toSwitchSlot, false);
                }

                BlockUtil.placeBlock(currentPlaceData.a, currentPlaceData.b, false, packetPlace.getValue(), false);
                if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE)
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPlaceData.a, BlockUtil.getVisibleBlockSide(new Vec3d(currentPlaceData.a))));

                if (toSwitchSlot != -1) {
                    ItemUtils.switchToSlot(prevSlot, false);
                }

                index++;
                if (index > toPlacePoses.size() - 1) {
                    index = 0;
                }
            });

            delayTimer.reset();
        }
    }

    private void findPlacePoses() {
        HashMap<Entity, BlockPos> topPlacePosesTemp = new HashMap<>();
        List<Pair<BlockPos, EnumFacing>> toPlacePosesTemp = new ArrayList<>();
        HashMap<Entity, Pair<Boolean, Boolean>> toTrapPlayers = new HashMap<>();
        HashMap<Entity, Pair<Boolean, Boolean>> map;
        synchronized (FriendsEnemies.INSTANCE.entityData) {
            map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
        }
        map.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof EntityPlayer)
                .filter(entry -> entry.getKey() != mc.renderViewEntity)
                .filter(entry -> entry.getKey() != mc.player)
                .filter(entry -> !entry.getValue().a)
                .filter(entry -> MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entry.getKey().getPositionVector(), range.getValue()))
                .forEach(entry -> toTrapPlayers.put(entry.getKey(), entry.getValue()));

        if (!toTrapPlayers.isEmpty()) {
            toTrapPlayers.forEach((key, value) -> {
                placePoses(key, fullTrap.getValue(), topExtend.getValue(), topExtendOnMine.getValue(), toggle.getValue(), topPlaceTimer, topExtendOnMineTimeout.getValue()).stream()
                        .filter(data -> BlockUtil.canSeeBlock(data.a) ? MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), new Vec3d(data.a), range.getValue()) : MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), new Vec3d(data.a), wallRange.getValue()))
                        .forEach(data -> {
                            toPlacePosesTemp.add(new Pair<>(data.a, data.b));
                            renderMap.put(BlockUtil.extrudeBlock(data.a, data.b), new Pair<>(0.0f, 0.0f));
                        });

                topPlacePosesTemp.put(key, EntityUtil.floorEntity(key).add(0.0, 2.0, 0.0));
            });
        }

        toPlacePoses.clear();
        toPlacePoses.addAll(toPlacePosesTemp);
        topPlacePoses.clear();
        topPlacePoses.putAll(topPlacePosesTemp);

        if (toggle.getValue() && toPlacePoses.isEmpty())
            toggle();
    }

    public List<Pair<BlockPos, EnumFacing>> placePoses(Entity entity, boolean fullTrap, boolean topExtend, boolean topExtendOnMine, boolean toggle, Timer topPlaceTimer, int topExtendOnMineTimeout) {
        BlockPos entityPos = EntityUtil.floorEntity(entity);
        Pair<BlockPos, EnumFacing> startData = new Pair<>(entityPos.add(1.0, -1.0, 0.0), getClosestFacing(entityPos));
        if (!BlockUtil.isBlockPlaceable(entityPos.add(0.0, -1.0, 0.0)))
            return new ArrayList<>();

        boolean flag = false;

        for (int i = 0; i < 4; i++) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos extrudedPos = BlockUtil.extrudeBlock(entityPos.add(0.0, 2.0 - i, 0.0), facing);
                if (BlockUtil.isFacePlaceble(extrudedPos, EnumFacing.UP, true)) {
                    startData = new Pair<>(extrudedPos, facing);
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }

        int index = 0;
        int indexCap = -1;
        List<Pair<BlockPos, EnumFacing>> placePoses = getTopPlacePoses(entityPos, startData.b);
        for (Pair<BlockPos, EnumFacing> data : placePoses) {
            if (BlockUtil.isSameBlockPos(startData.a, data.a)) {
                indexCap = index;
                break;
            }
            index++;
        }

        List<Pair<BlockPos, EnumFacing>> topPosesToRemove = new ArrayList<>();
        if (indexCap > 1) {
            for (int i = indexCap - 1; i > -1; i--) {
                topPosesToRemove.add(placePoses.get(i));
            }
            placePoses.removeAll(topPosesToRemove);
        }

        if (fullTrap) {
            for (int i = 0; i < 2; i++) {
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    BlockPos extrudedPos = BlockUtil.extrudeBlock(entityPos.add(0.0, -1.0 + i, 0.0), facing);
                    if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                        if (BlockUtil.isFacePlaceble(extrudedPos, EnumFacing.UP, false)) {
                            placePoses.add(new Pair<>(extrudedPos, EnumFacing.UP));
                        }
                    } else if (i == 0 && mc.world.getBlockState(BlockUtil.extrudeBlock(entityPos, facing)).getMaterial().isReplaceable()) {
                        placePoses.add(new Pair<>(entityPos.add(0.0, -1.0, 0.0), facing));
                    }
                }
            }
        }

        if ((toggle || !topExtendOnMine || !topPlaceTimer.passed(topExtendOnMineTimeout)) && topExtend && BlockUtil.isBlockPlaceable(entityPos.add(0.0, 2.0, 0.0)))
            placePoses.add(new Pair<>(entityPos.add(0.0, 2.0, 0.0), EnumFacing.UP));

        return placePoses.stream()
                .filter(data -> mc.world.getBlockState(BlockUtil.extrudeBlock(data.a, data.b)).getMaterial().isReplaceable())
                .collect(Collectors.toList());
    }

    private List<Pair<BlockPos, EnumFacing>> getTopPlacePoses(BlockPos pos, EnumFacing startDirection) {
        List<Pair<BlockPos, EnumFacing>> list = new ArrayList<>();

        list.add(0, new Pair<>(BlockUtil.extrudeBlock(pos.add(0.0, 2.0, 0.0), startDirection), startDirection.getOpposite()));
        list.add(0, new Pair<>(BlockUtil.extrudeBlock(pos.add(0.0, 1.0, 0.0), startDirection), EnumFacing.UP));
        list.add(0, new Pair<>(BlockUtil.extrudeBlock(pos, startDirection), EnumFacing.UP));
        list.add(0, new Pair<>(BlockUtil.extrudeBlock(pos.add(0.0, -1.0, 0.0), startDirection), EnumFacing.UP));

        if (mc.world.getBlockState(BlockUtil.extrudeBlock(pos.add(0.0, -1.0, 0.0), startDirection)).getMaterial().isReplaceable())
            list.add(0, new Pair<>(pos.add(0.0, -1.0, 0.0), startDirection));

        return list;
    }

    private EnumFacing getClosestFacing(BlockPos flooredPos) {
        EnumFacing facingOut = mc.player.getHorizontalFacing();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (BlockUtil.isFacePlaceble(flooredPos, facing, true)) {
                return facing;
            }
        }

        return facingOut;
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
