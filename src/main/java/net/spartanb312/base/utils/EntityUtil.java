package net.spartanb312.base.utils;

import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.utils.graphics.RenderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityUtil {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static boolean isEntityPlayerLoaded = false;
    public static boolean isEntityMonsterLoaded = false;
    public static boolean isEntityAnimalLoaded = false;
    public static boolean isEntityCrystalLoaded = false;
    public static boolean isEntityProjectileLoaded = false;
    
    public static boolean isEntityMonster(Entity entity) {
        return (entity instanceof EntityMob || entity instanceof EntityShulker || entity instanceof EntitySlime || entity instanceof EntityGhast);
    }

    public static boolean isEntityAnimal(Entity entity) {
        return (entity instanceof EntityAnimal || entity instanceof EntitySquid);
    }

    public static boolean isEntityVehicle(Entity entity) {
        return (entity instanceof EntityMinecart || entity instanceof EntityBoat);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedEntityPos(Entity entity, double ticks) {
        return new Vec3d(entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * ticks), entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * ticks), entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * ticks));
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static boolean isPlayerInHole() {
        BlockPos blockPos = floorEntity(mc.player);

        IBlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
                {blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        return validHorizontalBlocks >= 4;
    }

    public static boolean isFakeLocalPlayer(Entity entity) {
        return entity != null && entity.getEntityId() == -100 && mc.player != entity;
    }

    public static boolean isPassive(Entity e) {
        if (e instanceof EntityWolf && ((EntityWolf) e).isAngry()) {
            return false;
        }
        if (e instanceof EntityAgeable || e instanceof EntityAmbientCreature || e instanceof EntitySquid) {
            return true;
        }
        return e instanceof EntityIronGolem && ((EntityIronGolem) e).getRevengeTarget() == null;
    }

    public static boolean isLiving(Entity e) {
        return e instanceof EntityLivingBase;
    }

    public static void runEntityCheck() {
        isEntityPlayerLoaded = false;
        isEntityMonsterLoaded = false;
        isEntityAnimalLoaded = false;
        isEntityCrystalLoaded = false;
        isEntityProjectileLoaded = false;
        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
            if (!RenderHelper.isInViewFrustrum(entity)) continue;
            if (entity instanceof EntityPlayer && entity != mc.player) isEntityPlayerLoaded = true;
            if (entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityGhast || entity instanceof EntityDragon) isEntityMonsterLoaded = true;
            if (EntityUtil.isEntityAnimal(entity)) isEntityAnimalLoaded = true;
            if (entity instanceof EntityEnderCrystal) isEntityCrystalLoaded = true;
            if (entity instanceof IProjectile || entity instanceof EntityShulkerBullet || entity instanceof EntityFireball || entity instanceof EntityEnderEye) isEntityProjectileLoaded = true;
        }
    }

    public static double calculateDistanceWithPartialTicks(double originalPos, double finalPos, float renderPartialTicks) {
        return finalPos + (originalPos - finalPos) * (double)renderPartialTicks;
    }

    public static Vec3d interpolateEntity(Entity entity, float renderPartialTicks) {
        return new Vec3d(calculateDistanceWithPartialTicks(entity.posX, entity.lastTickPosX, renderPartialTicks), calculateDistanceWithPartialTicks(entity.posY, entity.lastTickPosY, renderPartialTicks), calculateDistanceWithPartialTicks(entity.posZ, entity.lastTickPosZ, renderPartialTicks));
    }

    public static Vec3d interpolateEntityRender(Entity entity, float renderPartialTicks) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * renderPartialTicks - mc.getRenderManager().renderPosX, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * renderPartialTicks - mc.getRenderManager().renderPosY, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * renderPartialTicks - mc.getRenderManager().renderPosZ);
    }

    public static boolean isBurrowed(Entity entity) {
        BlockPos pos = new BlockPos(MathUtilFuckYou.trollFloor(entity.posX), MathUtilFuckYou.trollFloor(entity.posY + 0.2), MathUtilFuckYou.trollFloor(entity.posZ));
        return mc.world.getBlockState(pos).getBlock() != Blocks.AIR && mc.world.getBlockState(pos).getBlock() != Blocks.WATER && mc.world.getBlockState(pos).getBlock() != Blocks.FLOWING_WATER && mc.world.getBlockState(pos).getBlock() != Blocks.LAVA && mc.world.getBlockState(pos).getBlock() != Blocks.FLOWING_LAVA;
    }

    public static boolean isPosEmpty(BlockPos pos, boolean excludeSelf) {
        BlockPos pos1 = BlockUtil.floorPos(pos);
        List<Entity> intersectingEntities = mc.world.loadedEntityList.stream()
                .filter(entity -> MathUtilFuckYou.isWithinRange(new Vec3d(pos), entity.getPositionVector(), 3.0))
                .filter(entity -> !(entity instanceof EntityItem || entity instanceof EntityXPOrb))
                .filter(Entity::isEntityAlive)
                .filter(entity -> new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos1.x + 1.0, pos1.y + 1.0, pos1.z + 1.0).intersects(entity.getEntityBoundingBox()))
                .filter(entity -> !excludeSelf || entity != mc.player)
                .collect(Collectors.toList());

        return intersectingEntities.isEmpty();
    }

    private static void centerPlayer(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, true));
        mc.player.setPosition(x, y, z);
    }

    private static double getDst(Vec3d vec) {
        return mc.player.getPositionVector().distanceTo(vec);
    }

    public static void setCenter() {
        BlockPos centerPos = mc.player.getPosition();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();

        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);

        if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() + 0.5;
        }
        if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() + 0.5;
        }
        centerPlayer(x, y, z);
    }

    public static Vec3d selfCenterPos() {
        BlockPos centerPos = mc.player.getPosition();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();

        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);

        if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() + 0.5;
        }
        if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() + 0.5;
        }
        return new Vec3d(x, y, z);
    }

    public static boolean isEntityVisible(Entity entity) {
        return mc.player.canEntityBeSeen(entity);
    }

    public static double getInterpDistance(float partialTicks, Entity entity, Entity entity2) {
        return Math.sqrt(getInterpDistanceSq(partialTicks, entity, entity2));
    }

    public static double getInterpDistanceSq(float partialTicks, Entity entity, Entity entity2) {
        Vec3d interp = interpolateEntity(entity, partialTicks);
        Vec3d interp2 = interpolateEntity(entity2, partialTicks);

        double x = interp.x - interp2.x;
        double y = interp.y - interp2.y;
        double z = interp.z - interp2.z;

        return (x * x) + (y * y) + (z * z);
    }

    public static ModelBase getModel(Entity entity, boolean isChild, float swingProgress) {
        ModelBase model = null;
        if (entity instanceof EntityPlayer) {
            model = new ModelPlayer(0.0f, ((AbstractClientPlayer) entity).getSkinType().equals("slim"));
            ((ModelBiped) model).isSneak = entity.isSneaking();
        }
        else if (entity instanceof EntityBat) model = new ModelBat();
        else if (entity instanceof EntityBlaze) model = new ModelBlaze();
        else if (entity instanceof EntitySpider) model = new ModelSpider();
        else if (entity instanceof EntityChicken) model = new ModelChicken();
        else if (entity instanceof EntityCow) model = new ModelCow();
        else if (entity instanceof EntityCreeper) model = new ModelCreeper();
        else if (entity instanceof EntityDonkey || entity instanceof EntityHorse || entity instanceof EntityMule || entity instanceof EntitySkeletonHorse || entity instanceof EntityZombieHorse) model = new ModelHorse();
        else if (entity instanceof EntityGuardian) model = new ModelGuardian();
        else if (entity instanceof EntityEnderCrystal) model = new ModelEnderCrystal(0.0f, false);
        else if (entity instanceof EntityDragon) model = new ModelDragon(0.0f);
        else if (entity instanceof EntityEnderman) model = new ModelEnderman(0.0f);
        else if (entity instanceof EntityEndermite) model = new ModelEnderMite();
        else if (entity instanceof EntityEvoker || entity instanceof EntityIllusionIllager || entity instanceof EntityVindicator) model = new ModelIllager(0.0f, 0.0f, 64, 64);
        else if (entity instanceof EntityGhast) model = new ModelGhast();
        else if (entity instanceof EntityZombieVillager) model = new ModelZombieVillager();
        else if (entity instanceof EntityGiantZombie || entity instanceof EntityZombie) model = new ModelZombie();
        else if (entity instanceof EntityLlama) model = new ModelLlama(0.0f);
        else if (entity instanceof EntityMagmaCube) model = new ModelMagmaCube();
        else if (entity instanceof EntityOcelot) model = new ModelOcelot();
        else if (entity instanceof EntityParrot) model = new ModelParrot();
        else if (entity instanceof EntityPig) model = new ModelPig();
        else if (entity instanceof EntityPolarBear) model = new ModelPolarBear();
        else if (entity instanceof EntityRabbit) model = new ModelRabbit();
        else if (entity instanceof EntitySheep) model = new ModelSheep2();
        else if (entity instanceof EntityShulker) model = new ModelShulker();
        else if (entity instanceof EntitySilverfish) model = new ModelSilverfish();
        else if (entity instanceof EntitySkeleton || entity instanceof EntityStray || entity instanceof EntityWitherSkeleton) model = new ModelSkeleton();
        else if (entity instanceof EntitySlime) model = new ModelSlime(16);
        else if (entity instanceof EntitySnowman) model = new ModelSnowMan();
        else if (entity instanceof EntitySquid) model = new ModelSquid();
        else if (entity instanceof EntityVex) model = new ModelVex();
        else if (entity instanceof EntityVillager) model = new ModelVillager(0.0f);
        else if (entity instanceof EntityIronGolem) model = new ModelIronGolem();
        else if (entity instanceof EntityWitch) model = new ModelWitch(0.0f);
        else if (entity instanceof EntityWither) model = new ModelWither(0.0f);
        else if (entity instanceof EntityWolf) model = new ModelWolf();

        if (model != null) {
            model.swingProgress = swingProgress;
            model.isChild = isChild;
            return model;
        }
        return null;
    }

    /**
     * @param height range: 0.0f - 1.0f
     */
    public static AxisAlignedBB scaleBB(AxisAlignedBB bb, float scale, float height) {
        bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, MathUtilFuckYou.linearInterp(bb.minY, bb.maxY, height * 300.0f), bb.maxZ);
        Vec3d center = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * 0.5f), bb.minY + ((bb.maxY - bb.minY) * 0.5f), bb.minZ + ((bb.maxZ - bb.minZ) * 0.5f));
        double newWidth = (bb.maxX - bb.minX) * 0.5f * scale;
        double newHeight = (bb.maxY - bb.minY) * 0.5f * scale;
        double newLength = (bb.maxZ - bb.minZ) * 0.5f * scale;
        return new AxisAlignedBB(center.x + newWidth, center.y + newHeight, center.z + newLength,
                center.x - newWidth, center.y - newHeight, center.z - newLength);
    }

    public static boolean canStep() {
        return mc.world != null && mc.player != null && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isOnLadder() && !mc.gameSettings.keyBindJump.isKeyDown();
    }

    public static boolean isOnGround(double height) {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -height, 0.0)).isEmpty();
    }

    public static Vec3d predictConstant(Vec3d vec, float magnitude, double xOffset, double yOffset, double zOffset) {
        double originalXOffset = xOffset;
        double originalYOffset = yOffset;
        double originalZOffset = zOffset;

        if (xOffset < 0.0) xOffset *= -1.0;
        if (yOffset < 0.0) yOffset *= -1.0;
        if (zOffset < 0.0) zOffset *= -1.0;

        double[] d = MathUtilFuckYou.cartesianToPolar3d(xOffset, yOffset, zOffset);
        double[] d1 = MathUtilFuckYou.polarToCartesian3d(magnitude, d[1], d[2]);

        if (originalXOffset < 0.0) d1[0] *= -1.0;
        if (originalYOffset < 0.0) d1[1] *= -1.0;
        if (originalZOffset < 0.0) d1[2] *= -1.0;

        return new Vec3d(vec.x + (xOffset != 0.0 ? d1[0] : 0.0), vec.y + (yOffset != 0.0 ? d1[1] : 0.0), vec.z + (zOffset != 0.0 ? d1[2] : 0.0));
    }

    public static Vec3d predictConstant(Entity entity, float factor, boolean useY) {
        return predictConstant(entity.getPositionVector(), factor, entity.motionX, useY ? entity.motionY : 0.0, entity.motionZ);
    }

    public static AxisAlignedBB predictBBConstant(AxisAlignedBB bb, float magnitude, double xOffset, double yOffset, double zOffset) {
        double originalXOffset = xOffset;
        double originalYOffset = yOffset;
        double originalZOffset = zOffset;

        if (xOffset < 0.0) xOffset *= -1.0;
        if (yOffset < 0.0) yOffset *= -1.0;
        if (zOffset < 0.0) zOffset *= -1.0;

        double[] d = MathUtilFuckYou.cartesianToPolar3d(xOffset, yOffset, zOffset);
        double[] d1 = MathUtilFuckYou.polarToCartesian3d(magnitude, d[1], d[2]);

        if (originalXOffset < 0.0) d1[0] *= -1.0;
        if (originalYOffset < 0.0) d1[1] *= -1.0;
        if (originalZOffset < 0.0) d1[2] *= -1.0;

        return new AxisAlignedBB(bb.minX + (xOffset != 0.0 ? d1[0] : 0.0), bb.minY + (yOffset != 0.0 ? d1[1] : 0.0), bb.minZ + (zOffset != 0.0 ? d1[2] : 0.0), bb.maxX + (xOffset != 0.0 ? d1[0] : 0.0), bb.maxY + (yOffset != 0.0 ? d1[1] : 0.0), bb.maxZ + (zOffset != 0.0 ? d1[2] : 0.0));
    }

    public static AxisAlignedBB predictBBConstant(Entity entity, float factor, boolean useY) {
        return predictBBConstant(entity.getEntityBoundingBox(), factor, entity.motionX, useY ? entity.motionY : 0.0, entity.motionZ);
    }

    public static Vec3d predict(Vec3d vec, float factor, double xOffset, double yOffset, double zOffset) {
        return vec.add(xOffset * factor, yOffset * factor, zOffset * factor);
    }

    public static Vec3d predict(Entity entity, float factor, boolean useY) {
        return predict(entity.getPositionVector(), factor, entity.motionX, useY ? entity.motionY : 0.0, entity.motionZ);
    }

    //pasted from trollheck <3
    public static double getMovementYaw() {
        float forward = mc.player.movementInput.moveForward > 0.0f ? 1.0f :
                        mc.player.movementInput.moveForward < 0.0f ? -1.0f : 0.0f;
        float strafe = mc.player.movementInput.moveStrafe > 0.0f ? 1.0f :
                        mc.player.movementInput.moveStrafe < 0.0f ? -1.0f : 0.0f;

        float s = 90.0f * strafe;
        s *= forward != 0.0f ? forward * 0.5f : 1.0f;
        float yaw = mc.player.rotationYaw - s;
        yaw -= forward == -1.0f ? 180.0f : 0.0f;

        return yaw * (Math.PI / 180.0f);
    }

    public static Vec3d getEyePos(Entity entity) {
        return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }

    public static BlockPos floorEntity(Entity entity) {
        return new BlockPos(MathUtilFuckYou.trollFloor(entity.posX), MathUtilFuckYou.trollFloor(entity.posY), MathUtilFuckYou.trollFloor(entity.posZ));
    }

    public static boolean isStill() {
        return mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f;
    }
}
