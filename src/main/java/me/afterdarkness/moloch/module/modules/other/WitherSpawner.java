package me.afterdarkness.moloch.module.modules.other;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.event.events.player.SpawnWitherEvent;
import me.afterdarkness.moloch.utils.BlockUtil;
import me.afterdarkness.moloch.client.SwapManager;
import me.afterdarkness.moloch.hud.huds.DebugThing;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "WitherSpawner", category = Category.OTHER, description = "Spawns withers automatically")
public class WitherSpawner extends Module {

    Setting<Boolean> inventorySwap = setting("InventorySwap", true).des("Swap wither materials from inventory");
    Setting<Boolean> toggle = setting("Toggle", true).des("Auto disables when you don't have enough materials to make a wither");
    Setting<Boolean> packetPlace = setting("PacketPlace", false).des("Uses packets to place blocks");
    Setting<Boolean> autoName = setting("AutoName", false).des("Automatically names withers on spawn");
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotate to face place position");
    Setting<Boolean> limit = setting("Limit", false).des("Limits amount to withers to spawn every time this module is enabled");
    Setting<Integer> limitCount = setting("LimitCount", 5, 1, 20).des("Maximum amount of withers to spawn every time this module is enabled").whenTrue(limit);
    Setting<Integer> delay = setting("Delay", 70, 1, 1000).des("Milliseconds to wait between each block placement");

    private final List<EntityWither> withers = new ArrayList<>();
    private final List<BlockPos> takenPoses = new ArrayList<>();
    private final List<BlockPos[]> toPlacePoses = new ArrayList<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    private final Timer delayTimer = new Timer();
    private int index;
    private int subIndex;
    private int prevSoulSandSlot;
    private int prevWitherSkullSlot;
    private boolean shouldInvSwitchBack;
    private int switchSlot;
    private int spawnCount;

    public WitherSpawner() {
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

    RepeatUnit update = new RepeatUnit(() -> 1, this::findSpawnPoses);

    @Override
    public void onTick() {
        if (autoName.getValue() || limit.getValue()) {
            List<EntityWither> localWithers = new ArrayList<>(withers);
            localWithers.forEach(wither -> {
                nameWither(wither);
                withers.remove(wither);
            });
        }
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
        switchSlot = mc.player.inventory.currentItem;
        shouldInvSwitchBack = inventorySwap.getValue() && !(ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.SOUL_SAND)) && ItemUtils.isWitherSkullInHotbar());
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
        if (inventorySwap.getValue() && shouldInvSwitchBack && mc.player != null) {
            int slotID = getInvReplaceSlot();
            if (switchSlot != -1 && slotID != -1) {
                boolean preSwitchIsEmpty = mc.player.inventoryContainer.inventorySlots.get(slotID).getStack().isEmpty;
                mc.playerController.windowClick(0, switchSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
                if (!preSwitchIsEmpty) {
                    mc.playerController.windowClick(0, switchSlot + 36, 0, ClickType.PICKUP, mc.player);
                }
                mc.playerController.updateController();
            }
        }
        takenPoses.clear();
        toPlacePoses.clear();
        subIndex = 0;
        index = 0;
        spawnCount = 0;
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (!(toggle.getValue() && ((inventorySwap.getValue() && !ItemUtils.isWitherSkullInInventory() && !ItemUtils.isItemInInventory(Item.getItemFromBlock(Blocks.SOUL_SAND)))
                || (!inventorySwap.getValue() && !ItemUtils.isWitherSkullInHotbar() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.SOUL_SAND)))))) {
            if (!toPlacePoses.isEmpty() && rotate.getValue()) {
                BlockPos currentPos = toPlacePoses.get(index)[subIndex];
                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos extrudedPos = BlockUtil.extrudeBlock(currentPos, facing);
                    if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                        RotationManager.setYawAndPitchMotionEvent(event, currentPos, facing.getOpposite());
                        break;
                    }
                }
            }
        }
    }

    @Listener
    public void onUpdateWalkingPlayer(OnUpdateWalkingPlayerEvent event) {
        if (toggle.getValue() && ((inventorySwap.getValue() && !ItemUtils.isWitherSkullInInventory() && !ItemUtils.isItemInInventory(Item.getItemFromBlock(Blocks.SOUL_SAND)))
            || (!inventorySwap.getValue() && !ItemUtils.isWitherSkullInHotbar() && !ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.SOUL_SAND))))) {
            toggle();
            ChatUtil.sendNoSpamErrorMessage("No materials!");
            return;
        }

        if (!toPlacePoses.isEmpty()
                && (inventorySwap.getValue() ? (ItemUtils.isWitherSkullInInventory() && ItemUtils.isItemInInventory(Item.getItemFromBlock(Blocks.SOUL_SAND))) : (ItemUtils.isWitherSkullInHotbar() && ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.SOUL_SAND))))
                && delayTimer.passed(delay.getValue())
                && (!limit.getValue() || spawnCount < limitCount.getValue())) {

            SwapManager.swapInvoke(this.name, false, true, () -> {
                DebugThing.debugInt++;

                BlockPos currentPos = toPlacePoses.get(index)[subIndex];
                int prevSlot = mc.player.inventory.currentItem;

                if (inventorySwap.getValue()) {
                    if (subIndex <= 3) {
                        if (!ItemUtils.isItemInHotbar(Item.getItemFromBlock(Blocks.SOUL_SAND))) {
                            prevSoulSandSlot = ItemUtils.itemSlotIDinInventory(Item.getItemFromBlock(Blocks.SOUL_SAND));
                            ItemUtils.swapItemFromInvToHotBar(Item.getItemFromBlock(Blocks.SOUL_SAND), switchSlot);
                        }
                    } else {
                        if (!ItemUtils.isWitherSkullInHotbar()) {
                            prevWitherSkullSlot = ItemUtils.witherSkullSlotIDinInventory();
                            ItemUtils.swapWitherSkullFromInvToHotBar(switchSlot);
                        }
                    }
                }

                if (subIndex <= 3) {
                    ItemUtils.switchToSlot(ItemUtils.findBlockInHotBar(Blocks.SOUL_SAND), false);
                } else {
                    ItemUtils.switchToSlot(ItemUtils.findWitherSkullInHotBar(), false);
                }

                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos extrudedPos = BlockUtil.extrudeBlock(currentPos, facing);
                    if (!mc.world.getBlockState(extrudedPos).getMaterial().isReplaceable()) {
                        BlockUtil.placeBlock(currentPos, facing.getOpposite(), false, packetPlace.getValue(), false);
                        break;
                    }
                }

                ItemUtils.switchToSlot(prevSlot, false);

                subIndex++;

                if (subIndex >= 7) {
                    toPlacePoses.remove(index);
                    subIndex = 0;
                    index++;
                    if (index > toPlacePoses.size() - 1) {
                        index = 0;
                    }
                }
            });

            delayTimer.reset();
        }
    }

    @Listener
    public void onWitherSpawn(SpawnWitherEvent event) {
        if (autoName.getValue()) withers.add(event.wither);
        if (limit.getValue()) spawnCount++;
    }

    private void findSpawnPoses() {
        BlockUtil.getSphereRounded(EntityUtil.floorEntity(mc.player), 4, false).stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
                .filter(pos -> mc.world.getBlockState(pos.add(0.0, -1.0, 0.0)).getMaterial().isReplaceable()
                        && !mc.world.getBlockState(pos.add(0.0, -2.0, 0.0)).getMaterial().isReplaceable())
                .forEach(pos -> {
                    BlockPos[] check1 = new BlockPos[]{
                            pos,
                            pos.add(0.0, 1.0, 0.0),
                            pos.add(0.0, -1.0, 0.0),
                            pos.add(0.0, 0.0, 1.0),
                            pos.add(0.0, 0.0, -1.0),
                            pos.add(0.0, 1.0, 1.0),
                            pos.add(0.0, 1.0, -1.0),
                            pos.add(0.0, -1.0, 1.0),
                            pos.add(0.0, -1.0, -1.0)};
                    BlockPos[] check2 = new BlockPos[]{
                            pos,
                            pos.add(0.0, 1.0, 0.0),
                            pos.add(0.0, -1.0, 0.0),
                            pos.add(1.0, 0.0, 0.0),
                            pos.add(-1.0, 0.0, 0.0),
                            pos.add(1.0, 1.0, 0.0),
                            pos.add(-1.0, 1.0, 0.0),
                            pos.add(1.0, -1.0, 0.0),
                            pos.add(-1.0, -1.0, 0.0)};

                    if (!BlockUtil.anyEntityBlocking(check1) && BlockUtil.allAir(check1)) {
                        boolean flag = false;
                        for (BlockPos pos1 : takenPoses) {
                            for (BlockPos pos2 : check1) {
                                if (BlockUtil.isSameBlockPos(pos1, pos2)) flag = true;
                            }
                        }
                        if (!flag) {
                            takenPoses.addAll(Arrays.asList(check1));
                            toPlacePoses.add(witherPoses(pos.add(0.0, -2.0, 0.0), false));
                        }
                    } else if (!BlockUtil.anyEntityBlocking(check2) && BlockUtil.allAir(check2)) {
                        boolean flag = false;
                        for (BlockPos pos1 : takenPoses) {
                            for (BlockPos pos2 : check2) {
                                if (BlockUtil.isSameBlockPos(pos1, pos2)) flag = true;
                            }
                        }
                        if (!flag) {
                            takenPoses.addAll(Arrays.asList(check2));
                            toPlacePoses.add(witherPoses(pos.add(0.0, -2.0, 0.0), true));
                        }
                    }
                });
    }

    private BlockPos[] witherPoses(BlockPos basePos, boolean facingNorthSouth) {
        if (facingNorthSouth) {
            return new BlockPos[] {
                    //sand
                    basePos.add(0.0, 1.0, 0.0),
                    basePos.add(0.0, 2.0, 0.0),
                    basePos.add(1.0, 2.0, 0.0),
                    basePos.add(-1.0, 2.0, 0.0),

                    //skulls
                    basePos.add(0.0, 3.0, 0.0),
                    basePos.add(1.0, 3.0, 0.0),
                    basePos.add(-1.0, 3.0, 0.0)
            };
        } else {
            return new BlockPos[] {
                    //sand
                    basePos.add(0.0, 1.0, 0.0),
                    basePos.add(0.0, 2.0, 0.0),
                    basePos.add(0.0, 2.0, 1.0),
                    basePos.add(0.0, 2.0, -1.0),

                    //skulls
                    basePos.add(0.0, 3.0, 0.0),
                    basePos.add(0.0, 3.0, 1.0),
                    basePos.add(0.0, 3.0, -1.0)
            };
        }
    }

    private int getInvReplaceSlot() {
        return isSoulSandOrSkull(mc.player.inventoryContainer.inventorySlots.get(prevSoulSandSlot).getStack())
                ? (isSoulSandOrSkull(mc.player.inventoryContainer.inventorySlots.get(prevWitherSkullSlot).getStack()) ? ItemUtils.getEmptySlot() : prevWitherSkullSlot) : prevSoulSandSlot;
    }

    private boolean isSoulSandOrSkull(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND) || stack.getDisplayName().equals("Wither Skeleton Skull");
    }

    private void nameWither(EntityWither wither) {
        int prevSlot = mc.player.inventory.currentItem;
        ItemUtils.switchToSlot(ItemUtils.findItemInHotBar(Items.NAME_TAG), false);

        if (rotate.getValue()) RotationManager.lookAtVec3d(wither.getPositionVector());
        mc.player.connection.sendPacket(new CPacketUseEntity(wither, EnumHand.MAIN_HAND));
        if (rotate.getValue()) RotationManager.resetRotationBlock();

        ItemUtils.switchToSlot(prevSlot, false);
    }
}
