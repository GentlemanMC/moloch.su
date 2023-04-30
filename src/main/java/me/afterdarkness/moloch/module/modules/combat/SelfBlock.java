package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.notification.NotificationManager;
import net.spartanb312.base.utils.*;

@Parallel(runnable = true)
@ModuleInfo(name = "SelfBlock", category = Category.COMBAT, description = "Fuck a block")
public class SelfBlock extends Module {

    Setting<String> block = setting("Block", "OBSIDIAN", false, null, false).des("Name of block to place with (i.e. SKULL, OBSIDIAN, ENDER_CHEST, etc...)");
    Setting<Mode> selfBlockMode = setting("SelfBlockMode", Mode.Packet).des("Type of selfblock");
    Setting<Boolean> rotate = setting("Rotate", true).des("Rotate to burrow");
    Setting<Boolean> breakCrystals = setting("BreakCrystals", true).des("If an end crystal's hitbox is blocking your place positions, try and break it");
    Setting<Boolean> antiSuicideCrystal = setting("AntiSuicideCrystal", true).des("Breaks crystal as long as it doesn't make you go below a certain health amount").whenTrue(breakCrystals);
    Setting<Float> minHealthRemaining = setting("MinHealthRemain", 8.0f, 1.0f, 36.0f).des("Min health that crystal should leave you with after you break it").whenTrue(antiSuicideCrystal).whenTrue(breakCrystals);
    Setting<Float> maxCrystalDamage = setting("MaxCrystalDamage", 11.0f, 0.0f, 36.0f).des("Don't break a crystal if it's damage to you exceeds this amount").whenFalse(antiSuicideCrystal).whenTrue(breakCrystals);
    Setting<Boolean> toggle = setting("Toggle", true).des("Disable when done").when(() -> selfBlockMode.getValue() != Mode.NoLag);
    //auto disables on shutting down client bc it spams errors if u reload it with it on (see MixinMinecraft)
    Setting<Integer> delay = setting("Delay", 292, 0, 1000).des("No toggle block place delay").when(() -> selfBlockMode.getValue() != Mode.NoLag).whenFalse(toggle);
    Setting<Boolean> antiStuck = setting("AntiStuck", true).des("Stops trying to place when stuck").when(() -> selfBlockMode.getValue() != Mode.NoLag).whenFalse(toggle);
    Setting<Boolean> waitPlace = setting("WaitPlace", false).des("Waits until no other entities are blocking place pos to place").when(() -> selfBlockMode.getValue() != Mode.NoLag).whenFalse(toggle);
    Setting<Integer> maxTry = setting("MaxTry", 4, 1, 20).when(() -> selfBlockMode.getValue() != Mode.NoLag).whenFalse(toggle).whenFalse(waitPlace).whenTrue(antiStuck);
    Setting<DisableMode> disableMode = setting("DisableCheckMode", DisableMode.Both).des("No toggle auto disable check mode").when(() -> selfBlockMode.getValue() != Mode.NoLag).whenFalse(toggle);
    Setting<Double> yPower = setting("YPower", 0.9d, -10.0d, 10.0d).des("Y motion").when(() -> selfBlockMode.getValue() != Mode.NoLag);
    Setting<Boolean> center = setting("Center", false).des("Center player on burrow").when(() -> selfBlockMode.getValue() != Mode.NoLag);

    private static final Timer timer = new Timer();
    private double prevPlayerPosY;
    private int prevSlot;
    private BlockPos pos;
    private boolean flag;
    private double originalPosX;
    private double originalPosY;
    private double originalPosZ;
    private int failedSelfBlockNum = 0;
    private EntityEnderCrystal attackingCrystal;

    @Override
    public void onEnable() {
        prevPlayerPosY = mc.player.posY;
        pos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY)), MathUtilFuckYou.trollFloor(mc.player.posZ));

        if (selfBlockMode.getValue() != Mode.Packet) {
            mc.player.jump();
        }

        originalPosX = mc.player.posX;
        originalPosY = mc.player.posY;
        originalPosZ = mc.player.posZ;
    }

    @Override
    public void onDisable() {
        failedSelfBlockNum = 0;
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (rotate.getValue()) {
            if (breakCrystals.getValue() && attackingCrystal != null && attackingCrystal.isEntityAlive()) {
                RotationManager.setYawAndPitchMotionEvent(event, mc.player.getPositionEyes(mc.getRenderPartialTicks()), attackingCrystal.getPositionVector());
            } else if (selfBlockMode.getValue() == Mode.NoLag && mc.player.posY >= prevPlayerPosY + 1.04) {
                RotationManager.setYawAndPitchMotionEvent(event, BlockUtil.extrudeBlock(pos, EnumFacing.DOWN), EnumFacing.UP);
            }
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (!ItemUtils.isItemInHotbar(burrowItem())) {
            NotificationManager.error("No blocks to place!");
            ModuleManager.getModule(SelfBlock.class).disable();
            return;
        }

        if (breakCrystals.getValue()) {
            attackingCrystal = CrystalUtil.breakBlockingCrystals(mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos), antiSuicideCrystal.getValue(), minHealthRemaining.getValue(), maxCrystalDamage.getValue());
            if (attackingCrystal != null && attackingCrystal.isEntityAlive())
                return;
        } else {
            attackingCrystal = null;
        }

        int toSwitchSlot = ItemUtils.findItemInHotBar(burrowItem());

        switch (selfBlockMode.getValue()) {
            case Packet: {
                SwapManager.swapInvoke(this.name, false, !toggle.getValue() || toSwitchSlot != mc.player.inventory.currentItem, () -> {
                    if (!toggle.getValue()) {
                        if (!EntityUtil.isBurrowed(mc.player)) {
                            if (antiStuck.getValue() ? failedSelfBlockNum >= maxTry.getValue() : failedSelfBlockNum == -999) {
                                this.disable();
                            }
                            else if (timer.passed(delay.getValue()) && (!waitPlace.getValue() || EntityUtil.isPosEmpty(new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY)), MathUtilFuckYou.trollFloor(mc.player.posZ)), true))) {
                                if (center.getValue()) EntityUtil.setCenter();
                                packetBurrow(burrowBlock() != Blocks.WEB && burrowBlock() != Blocks.SKULL);
                                timer.reset();
                                if (antiStuck.getValue()) {
                                    BlockPos pos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(mc.player.posY + 0.2), MathUtilFuckYou.trollFloor(mc.player.posZ));
                                    if (mc.world.getBlockState(pos).getBlock() != burrowBlock()) {
                                        failedSelfBlockNum += 1;
                                    }
                                }
                            }
                        }
                        else {
                            if ((((originalPosX > EntityUtil.selfCenterPos().x + 0.6 || originalPosX < EntityUtil.selfCenterPos().x - 0.6) || (originalPosZ > EntityUtil.selfCenterPos().z + 0.6 || originalPosZ < EntityUtil.selfCenterPos().z - 0.6)) && (disableMode.getValue() == DisableMode.Horizontal || disableMode.getValue() == DisableMode.Both)) || (originalPosY != mc.player.posY && (disableMode.getValue() == DisableMode.Vertical || disableMode.getValue() == DisableMode.Both)))
                                this.disable();
                        }
                    }

                    if (toggle.getValue()) {
                        if (center.getValue()) EntityUtil.setCenter();
                        packetBurrow(burrowBlock() != Blocks.WEB && burrowBlock() != Blocks.SKULL);
                        this.disable();
                    }
                });
                break;
            }

            case NoLag: {
                SwapManager.swapInvoke(this.name, false, toSwitchSlot != mc.player.inventory.currentItem, () -> {
                    if (mc.player.onGround)
                        mc.player.jump();

                    if (mc.player.posY >= prevPlayerPosY + 1.04) {
                        prevSlot = mc.player.inventory.currentItem;
                        if (toSwitchSlot != prevSlot)
                            ItemUtils.switchToSlot(toSwitchSlot, false);

                        BlockUtil.placeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.DOWN), EnumFacing.UP, false, true, false);

                        if (toSwitchSlot != prevSlot)
                            ItemUtils.switchToSlot(prevSlot, false);
                        mc.player.motionY = 0.0f;
                        flag = true;
                    }

                    if (!mc.player.onGround && flag) {
                        flag = false;
                        this.disable();
                    }
                });
            }
        }
    }

    private void packetBurrow(boolean posPackets) {
        if (posPackets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));
        }

        prevSlot = mc.player.inventory.currentItem;
        int toSwitchSlot = ItemUtils.findItemInHotBar(burrowItem());
        if (toSwitchSlot != prevSlot) {
            ItemUtils.switchToSlot(toSwitchSlot, false);
        }

        pos = new BlockPos(MathUtilFuckYou.trollFloor(mc.player.posX), MathUtilFuckYou.trollFloor(Math.round(mc.player.posY)), MathUtilFuckYou.trollFloor(mc.player.posZ));
        BlockUtil.placeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.DOWN), EnumFacing.UP, selfBlockMode.getValue() == Mode.Packet && rotate.getValue(), true, false);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + yPower.getValue(), mc.player.posZ, false));

        if (toSwitchSlot != prevSlot) {
            ItemUtils.switchToSlot(prevSlot, false);
        }
    }

    private Block burrowBlock() {
       Block block1 = Block.getBlockFromName(block.getValue().toUpperCase());
        if (block1 != null) {
            return block1;
        }
        return Blocks.OBSIDIAN;
    }

    private Item burrowItem() {
        return Item.getItemFromBlock(burrowBlock());
    }

    enum BlockMode {
        Obsidian,
        EnderChest,
        WitherSkull,
        EndRod,
        Web
    }

    enum Mode {
        Packet,
        NoLag
    }

    enum DisableMode {
        Horizontal,
        Vertical,
        Both
    }
}
