package me.afterdarkness.moloch.utils;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.client.ServerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.RotationUtil;
import net.spartanb312.base.utils.graphics.SpartanTessellator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.init.Enchantments.EFFICIENCY;
import static net.spartanb312.base.BaseCenter.mc;

public class BlockUtil {

    public static long packetMineStartTime = 0L;
    public static boolean packetMiningFlag = false;

    public static List<BlockPos> getSphere(Vec3d loc, float r, boolean excludeAir) {
        List<BlockPos> circleBlocks = new ArrayList<>();
        double cx = loc.x;
        double cy = loc.y;
        double cz = loc.z;
        int floorX = (int) MathUtilFuckYou.trollFloor(cx);
        int floorY = (int) MathUtilFuckYou.trollFloor(cy);
        int floorZ = (int) MathUtilFuckYou.trollFloor(cz);

        int ceilRange = (int) MathUtilFuckYou.trollCeil(r);

        for (double x = floorX - ceilRange; x <= floorX + ceilRange; x++) {
            for (double z = floorZ - ceilRange; z <= floorZ + ceilRange; z++) {
                for (double y = floorY - ceilRange; y < floorY + ceilRange; y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y);
                    if (dist <= r * r) {
                        BlockPos l = new BlockPos(x, y, z);
                        if (!excludeAir || mc.world.getBlockState(l).getBlock() != Blocks.AIR) {
                            circleBlocks.add(l);
                        }
                    }
                }
            }
        }
        return circleBlocks;
    }

    public static List<BlockPos> getSphereRounded(BlockPos loc, float r, boolean excludeAir) {
        List<BlockPos> circleBlocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();

        for (int x = cx - (int) r; x <= cx + (int) r; x++) {
            for (int z = cz - (int) r; z <= cz + (int) r; z++) {
                for (int y = cy - (int) r; y < cy + (int) r; y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y);
                    if (dist <= r * r) {
                        BlockPos l = new BlockPos(x, y, z);
                        if (!excludeAir || mc.world.getBlockState(l).getBlock() != Blocks.AIR) {
                            circleBlocks.add(l);
                        }
                    }
                }
            }
        }
        return circleBlocks;
    }

    public static BlockPos extrudeBlock(BlockPos pos, EnumFacing direction) {
        switch (direction) {
            case WEST: return new BlockPos(pos.add(-1.0, 0.0, 0.0));

            case EAST: return new BlockPos(pos.add(1.0, 0.0, 0.0));

            case NORTH: return new BlockPos(pos.add(0.0, 0.0, -1.0));

            case SOUTH: return new BlockPos(pos.add(0.0, 0.0, 1.0));

            case UP: return new BlockPos(pos.add(0.0, 1.0, 0.0));

            case DOWN: return new BlockPos(pos.add(0.0, -1.0, 0.0));
        }
        return pos;
    }

    public static boolean isBlockPlaceable(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block != Blocks.AIR && block != Blocks.WATER && block != Blocks.FLOWING_WATER && block != Blocks.LAVA && block != Blocks.FLOWING_LAVA;
    }

    public static boolean isFacePlaceble(BlockPos pos, EnumFacing facing, boolean checkEntity) {
        BlockPos pos1 = BlockUtil.extrudeBlock(pos, facing);
        return !mc.world.getBlockState(pos).getMaterial().isReplaceable() && mc.world.getBlockState(pos1).getMaterial().isReplaceable() && (!checkEntity || EntityUtil.isPosEmpty(pos1, false));
    }
    public static Vec3d getBlockVecFaceCenter(BlockPos blockPos, EnumFacing face) {
        BlockPos pos = new BlockPos(MathUtilFuckYou.trollFloor(blockPos.x), MathUtilFuckYou.trollFloor(blockPos.y), MathUtilFuckYou.trollFloor(blockPos.z));
        switch (face) {
            case UP: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 1.0,
                        pos.z + 0.5
                );
            }

            case DOWN: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y,
                        pos.z + 0.5
                );
            }

            case EAST: {
                return new Vec3d(
                        pos.x + 1.0,
                        pos.y + 0.5,
                        pos.z + 0.5
                );
            }

            case WEST: {
                return new Vec3d(
                        pos.x,
                        pos.y + 0.5,
                        pos.z + 0.5
                );
            }

            case NORTH: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 0.5,
                        pos.z + 1.0
                );
            }

            case SOUTH: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 0.5,
                        pos.z
                );
            }
        }

        return new Vec3d(0, 0, 0);
   }

   public static EnumFacing getVisibleBlockSide(Vec3d blockVec) {
       Vec3d eyeVec = mc.player.getPositionEyes(mc.getRenderPartialTicks()).subtract(blockVec);
       return EnumFacing.getFacingFromVector((float)eyeVec.x, (float)eyeVec.y, (float)eyeVec.z);
   }

    public static Vec3d getVec3dBlock(BlockPos blockPos, EnumFacing face) {
        return new Vec3d(blockPos).add(0.5, 0.5, 0.5).add(new Vec3d(face.getDirectionVec()).scale(0.5));
    }

    public static float blockBreakSpeed(IBlockState blockMaterial, ItemStack tool) {
        float mineSpeed = tool.getDestroySpeed(blockMaterial);
        int efficiencyFactor = EnchantmentHelper.getEnchantmentLevel(EFFICIENCY, tool);

        mineSpeed = (float) (mineSpeed > 1.0 && efficiencyFactor > 0 ? (efficiencyFactor * efficiencyFactor + mineSpeed + 1.0) : mineSpeed);

        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            mineSpeed *= 1.0f + Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.HASTE)).getAmplifier() * 0.2f;
        }

        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            switch (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) {
                case 0 : {
                    mineSpeed *= 0.3f;
                    break;
                }

                case 1: {
                    mineSpeed *= 0.09f;
                    break;
                }

                case 2: {
                    mineSpeed *= 0.0027f;
                    break;
                }

                default: {
                    mineSpeed *= 0.00081f;
                }
            }
        }

        if (!mc.player.onGround) {
            mineSpeed /= 5.0f;
        }

        return mineSpeed;
    }

    public static double blockBrokenTime(BlockPos pos, ItemStack tool) {
        IBlockState blockMaterial = mc.world.getBlockState(pos);
        float damageTicks = blockBreakSpeed(blockMaterial, tool) /
                blockMaterial.getBlockHardness(mc.world, pos) / (tool.canHarvestBlock(blockMaterial) ? 30.0f : 100.0f);
        return (MathUtilFuckYou.trollCeil(1.0f / damageTicks) * 50.0f) * (20.0f / ServerManager.getTPS());
    }

    public static void placeBlock(BlockPos pos, EnumFacing facing, boolean packetRotate, boolean packetPlace, boolean offHand) {
        if (!mc.player.isSneaking() && mc.world.getBlockState(pos).getBlock() instanceof BlockContainer)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

        Vec3d blockVec = BlockUtil.getVec3dBlock(pos, facing);

        if (packetRotate) {
            float[] r = RotationUtil.getRotationsBlock(pos, facing, true);
            RotationManager.setYawAndPitchBlock(r[0], r[1]);
        }

        if (packetPlace) {
            float x = (float)(blockVec.x - pos.getX());
            float y = (float)(blockVec.y - pos.getY());
            float z = (float)(blockVec.z - pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, x, y, z));
        }
        else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, blockVec, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }

        mc.player.swingArm(offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket(new CPacketAnimation(offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));

        if (packetRotate) {
            RotationManager.resetRotationBlock();
        }

        if (!mc.player.isSneaking() && mc.world.getBlockState(pos).getBlock() instanceof BlockContainer)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    }

    public static void mineBlock(BlockPos pos, EnumFacing face, boolean packetMine) {
        if (packetMine) {
            packetMineStartTime = System.currentTimeMillis();
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, face));
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        else if (mc.playerController.onPlayerDamageBlock(pos, face)) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    //for some reason pos1 == pos2 doesnt work so i have to use this instead :shrug:
    //14/7/22 update - trollhack compatibility issues with comparing blockpos to objectMouseOver blockpos so now theres a bb check too :even_bigger_shrug:
    public static boolean isSameBlockPos(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) return false;
        AxisAlignedBB bb1 = SpartanTessellator.getBoundingFromPos(pos1);
        AxisAlignedBB bb2 = SpartanTessellator.getBoundingFromPos(pos2);
        return bb1.maxX == bb2.maxX && bb1.maxY == bb2.maxY && bb1.maxZ == bb2.maxZ;
    }

    public static AxisAlignedBB interpNonLinearBB(AxisAlignedBB start, AxisAlignedBB end, float progress, float factor) {
        double minX = MathUtilFuckYou.interpNonLinear(start.minX, end.minX, progress, factor);
        double minY = MathUtilFuckYou.interpNonLinear(start.minY, end.minY, progress, factor);
        double minZ = MathUtilFuckYou.interpNonLinear(start.minZ, end.minZ, progress, factor);

        double maxX = MathUtilFuckYou.interpNonLinear(start.maxX, end.maxX, progress, factor);
        double maxY = MathUtilFuckYou.interpNonLinear(start.maxY, end.maxY, progress, factor);
        double maxZ = MathUtilFuckYou.interpNonLinear(start.maxZ, end.maxZ, progress, factor);

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Vec3d interpNonLinearVec(Vec3d start, Vec3d end, float progress, float factor) {
        double x = MathUtilFuckYou.interpNonLinear(start.x, end.x, progress, factor);
        double y = MathUtilFuckYou.interpNonLinear(start.y, end.y, progress, factor);
        double z = MathUtilFuckYou.interpNonLinear(start.z, end.z, progress, factor);

        return new Vec3d(x, y, z);
    }

    public static BlockPos floorPos(BlockPos pos) {
        return new BlockPos(MathUtilFuckYou.trollFloor(pos.x), MathUtilFuckYou.trollFloor(pos.y), MathUtilFuckYou.trollFloor(pos.z));
    }

    public static BlockPos floorPos(Vec3d vec) {
        return new BlockPos(MathUtilFuckYou.trollFloor(vec.x), MathUtilFuckYou.trollFloor(vec.y), MathUtilFuckYou.trollFloor(vec.z));
    }

    public static Vec3d[] getBlockVertices(BlockPos pos) {
        pos = floorPos(pos);
        Vec3d vec = new Vec3d(pos);
        return new Vec3d[]{vec.add(0.01, 0.01, 0.01), vec.add(0.99, 0.99, 0.99), vec.add(0.99, 0.0, 0.99), vec.add(0.99, 0.0, 0.0), vec.add(0.0, 0.99, 0.0), vec.add(0.0, 0.0, 0.99), vec.add(0.0, 0.99, 0.99), vec.add(0.99, 0.99, 0.0)};
    }

    public static boolean canSeeVec(Vec3d vecIn) {
        if (mc.player == null) return true;

        Vec3d startVec = EntityUtil.getEyePos(mc.player);
        Vec3d difVec = new Vec3d(vecIn.x - startVec.x, vecIn.y - startVec.y, vecIn.z - startVec.z);

        for (int i = 0; i < 500; i++) {
            BlockPos currentBlockPos = new BlockPos(startVec.add(difVec.x * i / 500.0f, difVec.y * i / 500.0f, difVec.z * i / 500.0f));
            if (!isSameBlockPos(floorPos(currentBlockPos), floorPos(vecIn)) && !isRayTraceable(mc.world.getBlockState(currentBlockPos).getBlock())) {
                return false;
            }
        }

        return true;
    }

    public static boolean canSeeBlock(BlockPos pos) {
        if (mc.player == null) return true;

        for (Vec3d vertex : getBlockVertices(pos)) {
            if (canSeeVec(vertex)) return true;
        }

        return false;
    }

    public static boolean isRayTraceable(Block block) {
        return block == Blocks.AIR || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA || block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.FIRE;
    }

    public static boolean anyEntityBlocking(BlockPos[] poses) {
        for (BlockPos pos : poses) {
            if (!EntityUtil.isPosEmpty(pos, false))
                return true;
        }
        return false;
    }

    public static boolean allAir(BlockPos[] poses) {
        for (BlockPos pos : poses) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR)
                return false;
        }
        return true;
    }
}
