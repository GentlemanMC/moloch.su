package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
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
import net.spartanb312.base.mixin.mixins.accessor.AccessorMinecraft;
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

//TODO: anti hitbox city extend
@Parallel(runnable = true)
@ModuleInfo(name = "Surround", category = Category.COMBAT, description = "Put obsidian around your feet to protect them from crystal damage")
public class Surround extends Module {

    Setting<Page> page = setting("Page", Page.General);

    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 500).des("Milliseconds between each attempt to update tick place positions").whenAtMode(page, Page.General);
    Setting<Integer> placeDelay = setting("PlaceDelay", 70, 1, 500).des("Delay between place attempts in milliseconds").whenAtMode(page, Page.General);
    Setting<Integer> multiPlace = setting("MultiPlace", 4, 1, 5).des("Blocks to place at once").whenAtMode(page, Page.General);
    Setting<Boolean> onPacket = setting("OnPacketBlockChange", true).des("Tries to place on SPacketBlockChange").whenAtMode(page, Page.General);
    Setting<Integer> onPacketDelay = setting("OnPacketBlockChangeDelay", 20, 1, 500).des("Milliseconds between each place attempt on SPacketBlockChange").whenTrue(onPacket).whenAtMode(page, Page.General);
    Setting<Boolean> packetPlace = setting("PacketPlace", true).des("Uses packets to place blocks").whenAtMode(page, Page.General);
    Setting<Boolean> antiGhostBlock = setting("AntiGhostBlock", true).des("Hits blocks after placing to remove it if its a client side only (ghost) block").whenAtMode(page, Page.General);
    Setting<Boolean> rotate = setting("Rotate", false).des("Spoofs rotations to place blocks").whenAtMode(page, Page.General);
    Setting<Boolean> center = setting("Center", false).des("Moves you to the center of the blockpos").whenAtMode(page, Page.General);
    Setting<Boolean> disableOnLeaveHole = setting("DisableOnLeaveHole", true).des("Automatically disables module when you aren't in the same blockpos anymore").whenAtMode(page, Page.General);
    Setting<Boolean> extend = setting("Extend", false).des("Extends surround if somebody tries to mine part of it to prevent being citied").whenAtMode(page, Page.General);
    Setting<Boolean> breakCrystals = setting("BreakCrystals", true).des("Breaks crystals that are blocking surround").whenAtMode(page, Page.General);
    Setting<Integer> breakCrystalsDelay = setting("BreakCrystalsDelay", 50, 1, 1000).des("Delay in milliseconds between attempts to break crystal").whenTrue(breakCrystals).whenAtMode(page, Page.General);
    Setting<Boolean> breakCrystalTickDelay = setting("BreakCrystalsTickDelay", false).des("Doesn't try to place on the tick that you are breaking a crystal").whenAtMode(page, Page.General);
    Setting<Boolean> antiSuicideCrystal = setting("AntiSuicideCrystal", true).des("Breaks crystal as long as it doesn't make you go below a certain health amount").whenTrue(breakCrystals).whenAtMode(page, Page.General);
    Setting<Float> minHealthRemaining = setting("MinHealthRemain", 8.0f, 1.0f, 36.0f).des("Min health that crystal should leave you with after you break it").whenTrue(antiSuicideCrystal).whenTrue(breakCrystals).whenAtMode(page, Page.General);
    Setting<Float> maxCrystalDamage = setting("MaxCrystalDamage", 11.0f, 0.0f, 36.0f).des("Don't break crystal if it could deal this much damage or more").whenFalse(antiSuicideCrystal).whenTrue(breakCrystals).whenAtMode(page, Page.General);
    Setting<Boolean> onlyVisible = setting("OnlyVisible", false).des("Only tries to place on sides of blocks that you can see").whenAtMode(page, Page.General);
    Setting<Boolean> useEnderChest = setting("UseEnderChest", false).des("Uses ender chests when you run out of obsidian").whenAtMode(page, Page.General);

    Setting<Boolean> render = setting("RenderPlacePos", true).des("Render a box for positions to be placed in").whenAtMode(page, Page.Render);
    Setting<Boolean> fade = setting("Fade", false).des("Fades alpha of render after blocks are placed").whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> fadeSpeed = setting("FadeSpeed", 2.0f, 0.1f, 3.0f).des("Fade speed of render").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Movement> movement = setting("Movement", Movement.Up).des("Move render in a direction when a position has been placed in").whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> movementSpeed = setting("MovementSpeed", 1.0f, 0.1f, 3.0f).des("Speed of render's movement").when(() -> movement.getValue() != Movement.None).whenTrue(fade).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Boolean> solid = setting("Solid", true).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(100, 61, 255, 19).getRGB())).whenTrue(render).whenTrue(solid).whenAtMode(page, Page.Render);
    Setting<Boolean> lines = setting("Lines", true).whenTrue(render).whenAtMode(page, Page.Render);
    Setting<Float> linesWidth = setting("LinesWidth", 1.0f, 1.0f, 5.0f).whenTrue(render).whenTrue(lines).whenAtMode(page, Page.Render);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(100, 61, 255, 101).getRGB())).whenTrue(render).whenTrue(lines).whenAtMode(page, Page.Render);

    private final Timer placeTimer = new Timer();
    private final Timer fadeTimer = new Timer();
    private final Timer onPacketTimer = new Timer();
    private final Timer breakCrystalsTimer = new Timer();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final List<Pair<BlockPos, EnumFacing>> toTickPlacePos = new ArrayList<>();
    public final HashMap<BlockPos, Pair<Float, Float>> toRenderPos = new HashMap<>();
    private BlockPos currentPlayerPos = null;
    private boolean centeredFlag = false;
    private boolean isTickPlacingFlag = false;
    private BlockPos placePos;
    private EnumFacing placeFace;
    public static Surround INSTANCE;
    private EntityEnderCrystal attackingCrystal;

    public Surround() {
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
        if (mc.world != null && mc.player != null) {
            if (render.getValue() && !fade.getValue()) toRenderPos.clear();

            if (!ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) && (!useEnderChest.getValue() || !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)))) {
                toggle();
                ChatUtil.sendNoSpamErrorMessage("No blocks to place!");
                return;
            }

            if (center.getValue() && !centeredFlag) {
                EntityUtil.setCenter();
                centeredFlag = true;
            }

            BlockPos playerPos = EntityUtil.floorEntity(mc.player);
            if (currentPlayerPos == null) currentPlayerPos = playerPos.add(0.0, -1.0, 0.0);

            if (disableOnLeaveHole.getValue() && !BlockUtil.isSameBlockPos(currentPlayerPos, new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY) - 1), MathUtilFuckYou.trollFloor(mc.player.posZ)))) {
                toggle();
                return;
            }

            List<Pair<BlockPos, EnumFacing>> tempTickPlacePos = new ArrayList<>();

            for (int i = 1; i > -1; i--) {
                getPlacePoses(tempTickPlacePos, playerPos, i);
            }

            toTickPlacePos.clear();
            toTickPlacePos.addAll(tempTickPlacePos);
        }
    });

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (render.getValue()) {
            int passedms = (int) fadeTimer.hasPassed();
            fadeTimer.reset();

            HashMap<BlockPos, Pair<Float, Float>> localRenderMap;
            synchronized (toRenderPos) {
                localRenderMap = new HashMap<>(toRenderPos);
            }
            localRenderMap.forEach((key, value) -> {
                float animationDelta = MathUtilFuckYou.clamp(value.a, 0.0f, 300.0f);

                if (fade.getValue() && movement.getValue() != Movement.None)
                    GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? 1.0f : -1.0f), 0.0f);

                if (solid.getValue())
                    SpartanTessellator.drawBlockFullBox(new Vec3d(key), false, 1.0f, new java.awt.Color(solidColor.getValue().getColorColor().getRed(), solidColor.getValue().getColorColor().getGreen(), solidColor.getValue().getColorColor().getBlue(), (int)(solidColor.getValue().getAlpha() * animationDelta / 300.0f)).getRGB());

                if (lines.getValue())
                    SpartanTessellator.drawBlockLineBox(new Vec3d(key), false, 1.0f, linesWidth.getValue(), new java.awt.Color(linesColor.getValue().getColorColor().getRed(), linesColor.getValue().getColorColor().getGreen(), linesColor.getValue().getColorColor().getBlue(), (int)(linesColor.getValue().getAlpha() * animationDelta / 300.0f)).getRGB());

                if (fade.getValue() && movement.getValue() != Movement.None)
                    GL11.glTranslatef(0.0f, value.b * (movement.getValue() == Movement.Up ? -1.0f : 1.0f), 0.0f);

                if ((!fade.getValue() && !mc.world.getBlockState(key).getMaterial().isReplaceable()) || (fade.getValue() && animationDelta <= 0.0f)) {
                    toRenderPos.remove(key);
                } else if (passedms < 1000) {
                    toRenderPos.put(key, new Pair<>(animationDelta - passedms * fadeSpeed.getValue(), value.b + passedms * movementSpeed.getValue() / 1400.0f));
                }
            });
        }
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
        toTickPlacePos.clear();
        currentPlayerPos = null;
        centeredFlag = false;
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!isTickPlacingFlag && mc.world != null && mc.player != null) {
            if (placeTimer.passed(placeDelay.getValue()) && extend.getValue() && event.getPacket() instanceof SPacketBlockBreakAnim) {
                SwapManager.swapInvoke(this.name, false, true, () -> {
                    SPacketBlockBreakAnim packet = ((SPacketBlockBreakAnim) event.getPacket());
                    BlockPos playerPos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY)), MathUtilFuckYou.trollFloor(mc.player.posZ));

                    if (placePoses(false).contains(packet.getPosition())) {
                        BlockPos extendedPos = new BlockPos((packet.getPosition().x * 2.0f) - playerPos.x, (packet.getPosition().y * 2.0f) - playerPos.y, (packet.getPosition().z * 2.0f) - playerPos.z);
                        extendedPos = BlockUtil.extrudeBlock(extendedPos, EnumFacing.DOWN);

                        if (BlockUtil.isFacePlaceble(extendedPos, EnumFacing.UP, true) && (!onlyVisible.getValue() || BlockUtil.canSeeVec(BlockUtil.getVec3dBlock(extendedPos, EnumFacing.UP)))) {
                            int prevSlot = mc.player.inventory.currentItem;
                            int toSwitchSlot = ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) ? ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN) : (useEnderChest.getValue() && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) ? ItemUtils.findBlockInHotBar(Blocks.ENDER_CHEST) : -1;
                            ItemUtils.switchToSlot(toSwitchSlot, false);

                            BlockUtil.placeBlock(extendedPos, EnumFacing.UP, rotate.getValue(), packetPlace.getValue(), false);
                            if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE) mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP)))));
                            if (render.getValue())
                                toRenderPos.put(BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP), new Pair<>(300.0f, 0.0f));

                            ItemUtils.switchToSlot(prevSlot, false);
                        }
                    }
                });
            }

            if (onPacketTimer.passed(onPacketDelay.getValue()) && onPacket.getValue() && event.getPacket() instanceof SPacketBlockChange) {
                SwapManager.swapInvoke(this.name, false, true, () -> {
                    BlockPos playerPos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY) - 1), MathUtilFuckYou.trollFloor(mc.player.posZ));
                    if (!placePoses(true).contains(((SPacketBlockChange) event.packet).getBlockPosition())) return;
                    if (!((SPacketBlockChange) event.packet).getBlockState().getMaterial().isReplaceable()) return;

                    Pair<BlockPos, EnumFacing> data = getPlaceData(((SPacketBlockChange) event.packet).getBlockPosition());
                    if (data == null) return;

                    if (!onlyVisible.getValue() && BlockUtil.isSameBlockPos(data.a, playerPos) && BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(data.a, data.b), EnumFacing.UP)))
                        return;

                    if (!BlockUtil.isFacePlaceble(data.a, data.b, true)) return;

                    int prevSlot = mc.player.inventory.currentItem;
                    int toSwitchSlot = ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) ? ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN) : (useEnderChest.getValue() && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) ? ItemUtils.findBlockInHotBar(Blocks.ENDER_CHEST) : -1;
                    ItemUtils.switchToSlot(toSwitchSlot, false);

                    BlockUtil.placeBlock(data.a, data.b, rotate.getValue(), packetPlace.getValue(), false);
                    if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE) mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(data.a, data.b), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(data.a, data.b)))));
                    if (render.getValue())
                        toRenderPos.put(BlockUtil.extrudeBlock(data.a, data.b), new Pair<>(300.0f, 0.0f));

                    ItemUtils.switchToSlot(prevSlot, false);
                });

                onPacketTimer.reset();
            }
        }
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (toTickPlacePos.isEmpty()) return;

        Pair<BlockPos, EnumFacing> data = toTickPlacePos.get(0);

        placePos = data.a;
        placeFace = data.b;

        if (rotate.getValue()) {
            if (breakCrystals.getValue() && attackingCrystal != null && attackingCrystal.isEntityAlive()) {
                RotationManager.setYawAndPitchMotionEvent(event, mc.player.getPositionEyes(mc.getRenderPartialTicks()), attackingCrystal.getPositionVector());
            } else if (multiPlace.getValue() == 1 && mc.world != null && mc.player != null && placeTimer.passed(placeDelay.getValue()) && !toTickPlacePos.isEmpty()) {
                RotationManager.setYawAndPitchMotionEvent(event, placePos, placeFace);
            }
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (mc.world != null && mc.player != null && breakCrystalsTimer.passed(breakCrystalsDelay.getValue()) && breakCrystals.getValue()) {
            for (BlockPos pos : placePoses(true)) {
                if (breakCrystals.getValue()) {
                    attackingCrystal = CrystalUtil.breakBlockingCrystals(mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos), antiSuicideCrystal.getValue(), minHealthRemaining.getValue(), maxCrystalDamage.getValue());
                    if (attackingCrystal != null && attackingCrystal.isEntityAlive() && breakCrystalTickDelay.getValue())
                        return;
                } else {
                    attackingCrystal = null;
                }
            }
            breakCrystalsTimer.reset();
        }

        ((AccessorMinecraft) mc).setRightClickDelayTimer(0);
        tickPlace(event);
        ((AccessorMinecraft) mc).setRightClickDelayTimer(4);
    }

    private void tickPlace(OnUpdateWalkingPlayerEvent event) {
        if (mc.world != null && mc.player != null && placeTimer.passed(placeDelay.getValue())) {
            isTickPlacingFlag = true;
            if (!toTickPlacePos.isEmpty()) {
                SwapManager.swapInvoke(this.name, false, true, () -> {
                    int prevSlot = mc.player.inventory.currentItem;
                    int toSwitchSlot = ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) ? ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN) : (useEnderChest.getValue() && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) ? ItemUtils.findBlockInHotBar(Blocks.ENDER_CHEST) : -1;

                    for (int i = 0; i < multiPlace.getValue(); i++) {
                        if (multiPlace.getValue() > 1) {
                            Pair<BlockPos, EnumFacing> data = toTickPlacePos.get(i);
                            placePos = data.a;
                            placeFace = data.b;
                        }

                        ItemUtils.switchToSlot(toSwitchSlot, false);

                        BlockUtil.placeBlock(placePos, placeFace, rotate.getValue() && multiPlace.getValue() > 1, packetPlace.getValue(), false);
                        if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE)
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(placePos, placeFace), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(placePos, placeFace)))));

                        ItemUtils.switchToSlot(prevSlot, false);
                    }
                });

                placeTimer.reset();
            }
            isTickPlacingFlag = false;
        }
    }

    private Pair<BlockPos, EnumFacing> getPlaceData(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP) continue;

            BlockPos extrudedPos = BlockUtil.extrudeBlock(pos, facing);
            if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                return new Pair<>(extrudedPos, facing.getOpposite());
            }
        }
        return null;
    }

    private List<BlockPos> placePoses(boolean includeBottom) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos pos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY) - 1), MathUtilFuckYou.trollFloor(mc.player.posZ));

        if (includeBottom) {
            list.add(new BlockPos(pos.x + 1, pos.y, pos.z));
            list.add(new BlockPos(pos.x - 1, pos.y, pos.z));
            list.add(new BlockPos(pos.x, pos.y, pos.z + 1));
            list.add(new BlockPos(pos.x, pos.y, pos.z - 1));
        }
        list.add(new BlockPos(pos.x + 1, pos.y + 1, pos.z));
        list.add(new BlockPos(pos.x - 1, pos.y + 1, pos.z));
        list.add(new BlockPos(pos.x, pos.y + 1, pos.z + 1));
        list.add(new BlockPos(pos.x, pos.y + 1, pos.z - 1));

        return list;
    }

    private void getPlacePoses(List<Pair<BlockPos, EnumFacing>> outList, BlockPos playerPos, int index) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos extrudedPos = BlockUtil.extrudeBlock(playerPos.add(0.0, -index, 0.0), facing);

            if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()
                    || (index == 1 && (!mc.world.getBlockState(extrudedPos.add(0.0, 1.0, 0.0)).getMaterial().isReplaceable() || getBlockPlaceableFace(extrudedPos.add(0.0, 1.0, 0.0)) != null)))
                continue;

            Pair<BlockPos, EnumFacing> data = getBlockPlaceableFace(extrudedPos);

            if (data != null) {
                outList.add(outList.size(), new Pair<>(data.a, data.b));
                if (render.getValue())
                    toRenderPos.put(extrudedPos, new Pair<>(300.0f, 0.0f));
            }
        }
    }

    private Pair<BlockPos, EnumFacing> getBlockPlaceableFace(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP) continue;

            BlockPos extrudedPos = BlockUtil.extrudeBlock(pos, facing);
            if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()
                    && (!(new AxisAlignedBB(pos.x, pos.y, pos.z, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0).intersects(mc.player.getEntityBoundingBox())))
                    && (!onlyVisible.getValue() || BlockUtil.canSeeVec(BlockUtil.getVec3dBlock(extrudedPos, facing.getOpposite())))) {
                return new Pair<>(extrudedPos, facing.getOpposite());
            }
        }
        return null;
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
