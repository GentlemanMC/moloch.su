package me.afterdarkness.moloch.module.modules.client;

import com.google.common.collect.Sets;
import me.afterdarkness.moloch.module.modules.visuals.HoleRender;
import me.afterdarkness.moloch.module.modules.combat.HoleFill;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.math.Pair;

import java.util.*;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "HoleSettings", category = Category.CLIENT, description = "Manages global hole settings")
public class HoleSettings extends Module {

    Setting<Integer> holeUpdateDelay = setting("HoleUpdateDelay", 1, 1, 200).des("Milliseconds between every attempt to check world for holes");
    Setting<Boolean> advancedHoleCheck = setting("AdvancedHoleCheck", true).des("Checks for a 2 block space just above the hole to avoid rendering inaccessible holes (will decrease performance)");
    Setting<Boolean> miscBlocks = setting("MiscBlocks", true).des("Count other blocks that are blast resistant in hole calc");
    public Setting<Float> range = setting("Range", 8.0f, 1.0f, 30.0f).des("Range to start calculating holes");

    private static final List<RepeatUnit> repeatUnits = new ArrayList<>();
    public static HoleSettings INSTANCE;
    public final HashMap<BlockPos, Integer> holePositions = new HashMap<>(); //values: 1 -> single safe, 2 -> single unsafe, 3 -> double safe, 4 -> double unsafe
    public final HashMap<BlockPos, Integer> doubleHolePositions = new HashMap<>();
    public final HashMap<Pair<BlockPos, BlockPos>, Integer> mergedHolePositions = new HashMap<>();
    RepeatUnit update = new RepeatUnit(() -> holeUpdateDelay.getValue(), this::updateHoles);

    public HoleSettings() {
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

    @Override
    public void onDisable() {
        enable();
    }

    private final Set<Block> validBlocks = Sets.newHashSet(
            Blocks.BEDROCK,
            Blocks.OBSIDIAN,
            Blocks.ANVIL,
            Blocks.ENDER_CHEST,
            Blocks.BARRIER,
            Blocks.ENCHANTING_TABLE
    );

    private EnumFacing[] doubleHoleDirections(EnumFacing facing) {
        switch (facing) {
            case EAST: {
                return new EnumFacing[] {
                        EnumFacing.NORTH,
                        EnumFacing.SOUTH,
                        EnumFacing.EAST
                };
            }

            case WEST: {
                return new EnumFacing[] {
                        EnumFacing.NORTH,
                        EnumFacing.SOUTH,
                        EnumFacing.WEST
                };
            }

            case NORTH: {
                return new EnumFacing[] {
                        EnumFacing.NORTH,
                        EnumFacing.EAST,
                        EnumFacing.WEST
                };
            }

            case SOUTH: {
                return new EnumFacing[] {
                        EnumFacing.EAST,
                        EnumFacing.SOUTH,
                        EnumFacing.WEST
                };
            }
        }

        return new EnumFacing[] {
                EnumFacing.NORTH,
                EnumFacing.EAST,
                EnumFacing.WEST
        };
    }

    private void updateHoles() {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (!(ModuleManager.getModule(HoleFill.class).isEnabled() || ModuleManager.getModule(HoleRender.class).isEnabled())) {
            return;
        }

        boolean shouldCheckDouble = (HoleFill.INSTANCE.doubleHoles.getValue() && ModuleManager.getModule(HoleFill.class).isEnabled()) || (HoleRender.INSTANCE.doubleHoles.getValue() && ModuleManager.getModule(HoleRender.class).isEnabled());

        HashMap<BlockPos, Integer> cachedDoublePos = new HashMap<>();
        HashMap<BlockPos, Integer> holesTemp = new HashMap<>();
        HashMap<BlockPos, Integer> doubleHolesTemp2 = new HashMap<>();
        HashMap<Pair<BlockPos, BlockPos>, Integer> doubleHolesTemp = new HashMap<>();

        for (BlockPos pos : BlockUtil.getSphere(mc.player.getPositionVector(), range.getValue(), false)) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                continue;
            }

            if (cachedDoublePos.containsKey(pos)) {
                continue;
            }

            Block downBlock = mc.world.getBlockState(BlockUtil.extrudeBlock(pos, EnumFacing.DOWN)).getBlock();

            if (mc.world.getBlockState(BlockUtil.extrudeBlock(pos, EnumFacing.UP)).getBlock() != Blocks.AIR) {
                continue;
            }

            if (mc.world.getBlockState(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.UP), EnumFacing.UP)).getBlock() != Blocks.AIR) {
                continue;
            }

            if (miscBlocks.getValue() ? !validBlocks.contains(downBlock) : (downBlock != Blocks.OBSIDIAN && downBlock != Blocks.BEDROCK)) {
                continue;
            }

            boolean isDouble = false;
            boolean isSafe = true;
            boolean checkForDoubleFlag = false;
            EnumFacing faceToCheckDouble = EnumFacing.EAST;
            BlockPos extendedBlockPos = pos;
            int placeableIndex = 0;
            int validBlocksIndex = 0;
            int advancedCheckIndex = 0;
            int doubleHoleCheckIndex = 0;

            if (downBlock != Blocks.BEDROCK && downBlock != Blocks.BARRIER) {
                isSafe = false;
            }

            //calc single holes
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                extendedBlockPos = BlockUtil.extrudeBlock(pos, facing);
                Block block = mc.world.getBlockState(extendedBlockPos).getBlock();

                if (shouldCheckDouble) {
                    if (!BlockUtil.isBlockPlaceable(extendedBlockPos)) {
                        placeableIndex++;
                        checkForDoubleFlag = true;
                        faceToCheckDouble = facing;
                    }

                    if (placeableIndex > 1 || cachedDoublePos.containsKey(pos)) {
                        checkForDoubleFlag = false;
                    }
                }

                if (miscBlocks.getValue() ? !validBlocks.contains(block) : (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK)) {
                    continue;
                }

                if (block != Blocks.BEDROCK && block != Blocks.BARRIER) {
                    isSafe = false;
                }

                //check if 3 and 4 blocks above hole is blocked -> if 3 above is blocked, if either 2 blocks on hole sides are blocked -> if any are blocked, hole isnt rendered
                //                                                 if 4 above is blocked, check all top blocks (of 2 blocks above hole sides) -> if all top blocks are blocked, hole isnt rendered
                if (advancedHoleCheck.getValue()) {
                    boolean b = false;
                    if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.UP), EnumFacing.UP), EnumFacing.UP))) {
                        if (!(!BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP)) && !BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP), EnumFacing.UP)))) {
                            b = true;
                        }
                    }

                    if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(pos, EnumFacing.UP), EnumFacing.UP), EnumFacing.UP), EnumFacing.UP))) {
                        if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP), EnumFacing.UP))) {
                            b = true;
                        }
                    }

                    if (b) {
                        advancedCheckIndex++;
                    }
                }

                validBlocksIndex++;
            }

            if (advancedHoleCheck.getValue()) {
                if (advancedCheckIndex >= 4) {
                    continue;
                }

                if (shouldCheckDouble && checkForDoubleFlag && advancedCheckIndex == 3) {
                    doubleHoleCheckIndex++;
                }
            }

            //calc double holes
            if (shouldCheckDouble && checkForDoubleFlag) {
                extendedBlockPos = BlockUtil.extrudeBlock(pos, faceToCheckDouble);
                Block doubleDownBlock = mc.world.getBlockState(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.DOWN)).getBlock();

                if (miscBlocks.getValue() ? !validBlocks.contains(doubleDownBlock) : (doubleDownBlock != Blocks.OBSIDIAN && doubleDownBlock != Blocks.BEDROCK)) {
                    continue;
                }

                if (doubleDownBlock != Blocks.BEDROCK && doubleDownBlock != Blocks.BARRIER) {
                    isSafe = false;
                }

                if (advancedHoleCheck.getValue()) {
                    if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP)) || BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP), EnumFacing.UP))) {
                        doubleHoleCheckIndex++;
                    }

                    if (doubleHoleCheckIndex >= 2) {
                        continue;
                    }
                }

                for (EnumFacing facing : doubleHoleDirections(faceToCheckDouble)) {
                    Block doubleBlockExtendedBlock = mc.world.getBlockState(BlockUtil.extrudeBlock(extendedBlockPos, facing)).getBlock();

                    if (miscBlocks.getValue() ? !validBlocks.contains(doubleBlockExtendedBlock) : (doubleBlockExtendedBlock != Blocks.OBSIDIAN && doubleBlockExtendedBlock != Blocks.BEDROCK)) {
                        break;
                    }

                    if (doubleBlockExtendedBlock != Blocks.BEDROCK && doubleBlockExtendedBlock != Blocks.BARRIER) {
                        isSafe = false;
                    }


                    if (advancedHoleCheck.getValue()) {
                        BlockPos doubleExtendedBlockPos = BlockUtil.extrudeBlock(extendedBlockPos, facing);
                        boolean b = false;
                        if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP), EnumFacing.UP), EnumFacing.UP))) {
                            if (!(!BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(doubleExtendedBlockPos, EnumFacing.UP)) && !BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(doubleExtendedBlockPos, EnumFacing.UP), EnumFacing.UP)))) {
                                b = true;
                            }
                        }

                        if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(extendedBlockPos, EnumFacing.UP), EnumFacing.UP), EnumFacing.UP), EnumFacing.UP))) {
                            if (BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(doubleExtendedBlockPos, EnumFacing.UP), EnumFacing.UP))) {
                                b = true;
                            }
                        }

                        if (b) {
                            advancedCheckIndex++;
                        }
                    }

                    validBlocksIndex++;
                }

                if (advancedHoleCheck.getValue() && advancedCheckIndex >= 6) {
                    continue;
                }

                if (validBlocksIndex == 6) {
                    isDouble = true;
                }
                else {
                    continue;
                }
            }

            int sort = sort(isDouble, isSafe);

            if (HoleRender.INSTANCE.mergeDoubleHoles.getValue() && shouldCheckDouble && isDouble) {
                doubleHolesTemp.put(new Pair<>(pos, extendedBlockPos), sort);
                doubleHolesTemp2.put(pos, sort);
                doubleHolesTemp2.put(extendedBlockPos, sort);
                cachedDoublePos.put(extendedBlockPos, sort);
            }
            else {
                if (isDouble || validBlocksIndex >= 4) {
                    if (sort <= 2) {
                        holesTemp.put(pos, sort);
                    } else {
                        doubleHolesTemp2.put(pos, sort);
                    }
                }

                if (isDouble) {
                    cachedDoublePos.put(extendedBlockPos, sort);
                }
            }
        }

        if (!HoleRender.INSTANCE.mergeDoubleHoles.getValue() && shouldCheckDouble && !cachedDoublePos.isEmpty()) {
            for (Map.Entry<BlockPos, Integer> entry : cachedDoublePos.entrySet()) {
                if (!holesTemp.containsKey(entry.getKey())) {
                    doubleHolesTemp2.put(entry.getKey(), entry.getValue());
                }
            }
        }

        synchronized (holePositions) {
            holePositions.clear();
            holePositions.putAll(holesTemp);
        }

        synchronized (doubleHolePositions) {
            doubleHolePositions.clear();
            doubleHolePositions.putAll(doubleHolesTemp2);
        }

        if (HoleRender.INSTANCE.doubleHoles.getValue() && HoleRender.INSTANCE.mergeDoubleHoles.getValue()) {
            synchronized (mergedHolePositions) {
                mergedHolePositions.clear();
                mergedHolePositions.putAll(doubleHolesTemp);
            }
        } else if (!mergedHolePositions.isEmpty()) {
            synchronized (mergedHolePositions) {
                mergedHolePositions.clear();
            }
        }
    }

    private int sort(boolean isDouble, boolean isSafe) {
        if (isDouble && isSafe) {
            return 3;
        }
        else if (isDouble) {
            return 4;
        }

        if (isSafe) {
            return 1;
        }
        else {
            return 2;
        }
    }
}
