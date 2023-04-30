package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.utils.BlockUtil;
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
import java.util.HashMap;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "SelfTrap", category = Category.COMBAT, description = "Covers yourself in obsidian to prevent players from getting in your hole")
public class SelfTrap extends Module {

    Setting<Page> page = setting("Page", Page.General);

    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 200).des("Milliseconds between updating the selftrap positions").whenAtMode(page, Page.General);
    Setting<Boolean> packetPlace = setting("PacketPlace", false).des("Uses packets to place blocks").whenAtMode(page, Page.General);
    Setting<Boolean> antiGhostBlock = setting("AntiGhostBlock", true).des("Hits a block after placing it to make sure it's not a client-side only block").whenAtMode(page, Page.General);
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotates player to face block placing position").whenAtMode(page, Page.General);
    Setting<Boolean> toggle = setting("Toggle", true).des("Automatically disables itself when done placing").whenAtMode(page, Page.General);
    Setting<Boolean> fullTrap = setting("Fulltrap", false).des("Completely traps yourself in obsidian versus only trapping your head").whenAtMode(page, Page.General);
    Setting<Boolean> topTrap = setting("TopTrap", false).des("Places extra block above you").whenAtMode(page, Page.General);
    Setting<Integer> delay = setting("Delay", 1, 1, 1000).des("Milliseconds between each place attempt").whenAtMode(page, Page.General);

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

    private final List<Pair<BlockPos, EnumFacing>> toPlacePoses = new ArrayList<>();
    private final HashMap<BlockPos, Pair<Float, Float>> renderMap = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final Timer delayTimer = new Timer();
    private final Timer renderTimer = new Timer();
    public static SelfTrap INSTANCE;
    private int index;

    public SelfTrap() {
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
                    SpartanTessellator.drawBlockFullBox(posVec, false, 1.0f, new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(solidColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(solidColor)).getRGB());
                }

                if (lines.getValue()) {
                    SpartanTessellator.drawBlockLineBox(posVec, false, 1.0f, linesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor), fade.getValue() ? ((int) (ColorUtil.getAlpha(linesColor) * (300 - value.a) / 300.0f)) : ColorUtil.getAlpha(linesColor)).getRGB());
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
        if (delayTimer.passed(delay.getValue()) && rotate.getValue() && delayTimer.passed(delay.getValue()) && toPlacePoses.size() > 0 && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
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

                Pair<BlockPos, EnumFacing> currentPlaceData = toPlacePoses.get((int) MathUtilFuckYou.clamp(index, 0, toPlacePoses.size() - 1));

                if (!ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
                    toggle();
                    ChatUtil.sendNoSpamErrorMessage("No blocks to place!");
                    return;
                }

                int prevSlot = mc.player.inventory.currentItem;
                int toSwitchSlot = ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN);
                if (toSwitchSlot != prevSlot) {
                    ItemUtils.switchToSlot(toSwitchSlot, false);
                }

                BlockUtil.placeBlock(currentPlaceData.a, currentPlaceData.b, false, packetPlace.getValue(), false);
                if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE)
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPlaceData.a, BlockUtil.getVisibleBlockSide(new Vec3d(currentPlaceData.a))));

                if (toSwitchSlot != prevSlot) {
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
        toPlacePoses.clear();
        AutoTrap.INSTANCE.placePoses(mc.player, fullTrap.getValue(), topTrap.getValue(), false, toggle.getValue(), null, 0).stream()
                .filter(data -> BlockUtil.isFacePlaceble(data.a, data.b, false))
                .forEach(data -> {
                    toPlacePoses.add(data);
                    if (render.getValue())
                        renderMap.put(BlockUtil.extrudeBlock(data.a, data.b), new Pair<>(0.0f, 0.0f));
                });

        if (toggle.getValue() && toPlacePoses.isEmpty())
            toggle();
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
