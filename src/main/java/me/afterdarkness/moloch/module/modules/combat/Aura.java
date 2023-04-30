package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.RotationManager;
import me.afterdarkness.moloch.core.LockTask;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.player.OnUpdateWalkingPlayerEvent;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import me.afterdarkness.moloch.module.modules.other.Freecam;
import me.afterdarkness.moloch.client.SwapManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.*;
import net.spartanb312.base.utils.graphics.RenderHelper;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import net.spartanb312.base.utils.math.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;
import static net.spartanb312.base.utils.RotationUtil.*;
//TODO: add aura render modes (after chams rewrite)
@Parallel(runnable = true)
@ModuleInfo(name = "Aura", category = Category.COMBAT, description = "Attacks entities around you")
public class Aura extends Module {

    Setting<Page> page = setting("Page", Page.Aura);

    Setting<Boolean> delay = setting("ModifyDelay", false).des("Don't use attack cooldown").whenAtMode(page, Page.Aura);
    Setting<Integer> attackDelay = setting("AttackDelay", 0, 0, 1000).des("Delay to attack target").whenTrue(delay).whenAtMode(page, Page.Aura);
    Setting<Float> range = setting("Range", 6.0f, 0.0f, 10.0f).des("Range to start attacking target").whenAtMode(page, Page.Aura);
    Setting<Boolean> targetPlayers = setting("TargetPlayers", true).des("Target players").whenAtMode(page, Page.Aura);
    Setting<Boolean> targetMonsters = setting("TargetMonsters", true).des("Target monsters").whenAtMode(page, Page.Aura);
    Setting<Boolean> targetAnimals = setting("TargetAnimals", false).des("Target animals").whenAtMode(page, Page.Aura);
    Setting<Boolean> targetMiscEntities = setting("TargetOtherEntities", false).des("Target other entities").whenAtMode(page, Page.Aura);
    Setting<Boolean> ignoreInvisible = setting("IgnoreInvisible", false).des("Doesn't target invisible entities").whenAtMode(page, Page.Aura);
    Setting<Boolean> legitMode = setting("LegitMode", false).des("Makes aura act more like actual player attack with left click").whenAtMode(page, Page.Aura);
    Setting<Boolean> packetAttack = setting("PacketAttack", false).des("Directsly sends CPacketUseEntity to hit entity").whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Boolean> stopSprint = setting("StopSprint", false).des("Stops sprinting during hit").whenAtMode(page, Page.Aura);
    Setting<Integer> randomClickPercent = setting("RandomClickPercent", 100, 1, 100).des("Chance of how likely aura will attack when aimed at target").whenTrue(legitMode).whenAtMode(page, Page.Aura);
    Setting<Boolean> checkWall = setting("CheckWall", false).des("Only attack target if they aren't behind wall").whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Float> wallRange = setting("WallRange", 3.0f, 0.0f, 10.0f).des("Range to start attacking target when target is behind a block").whenFalse(legitMode).whenFalse(checkWall).whenAtMode(page, Page.Aura);
    Setting<Boolean> rotate = setting("Rotate", false).des("Rotate to attack target").whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Boolean> slowRotate = setting("SlowRotate", false).des("Rotate more smoothly for strict servers").whenTrue(rotate).whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Integer> yawSpeed = setting("YawSpeed", 50, 1, 180).des("Yaw speed i think").whenTrue(slowRotate).whenTrue(rotate).whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Float> attackYawRange = setting("YawHitRange", 11.8f, 0.0f, 90.0f).des("Yaw range in degrees to start attacking target").whenTrue(slowRotate).whenTrue(rotate).whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Boolean> triggerMode = setting("TriggerMode", false).des("Only attack target when facing it").whenFalse(legitMode).whenFalse(rotate).whenAtMode(page, Page.Aura);
    public Setting<Boolean> autoSwitch = setting("AutoSwitch", false).des("Automatically switch to weapon").whenFalse(legitMode).whenAtMode(page, Page.Aura);
    public Setting<Weapon> preferredWeapon = setting("PreferredWeapon", Weapon.Sword).des("Preferred weapon to switch to").whenFalse(legitMode).whenAtMode(page, Page.Aura);
    Setting<Boolean> switchBack = setting("SwitchBack", false).des("Switch back to previous slot on disable or when nothing is targeted").whenFalse(legitMode).whenTrue(autoSwitch).whenAtMode(page, Page.Aura);

    Setting<Boolean> offhandSwing = setting("OffhandSwing", false).des("Swing with offhand to attack").whenAtMode(page, Page.Render);
    Setting<RenderType> renderType = setting("RenderType", RenderType.Box).whenAtMode(page, Page.Render);
    Setting<BoxMode> renderBoxMode = setting("BoxMode", BoxMode.Solid).des("Mode of box render").whenAtMode(renderType, RenderType.Box).when(() -> renderType.getValue() != RenderType.None).whenAtMode(page, Page.Render);
    Setting<Float> boxLinesWidth = setting("LineWidth", 1.0f, 1.0f, 5.0f).des("Box render lines width").when(() -> renderType.getValue() == RenderType.Box && renderBoxMode.getValue() != BoxMode.Solid).whenAtMode(page, Page.Render);
    Setting<Color> color = setting("Color", new Color(new java.awt.Color(255, 100, 100, 125).getRGB())).des("Aura target render color").when(() -> renderType.getValue() != RenderType.Box && renderType.getValue() != RenderType.None).whenAtMode(page, Page.Render);
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(255, 100, 100, 14).getRGB())).des("Aura target render fill color").when(() -> renderType.getValue() == RenderType.Box && renderBoxMode.getValue() != BoxMode.Lines).whenAtMode(page, Page.Render);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).des("Aura target render outline color").when(() -> renderType.getValue() == RenderType.Box && renderBoxMode.getValue() != BoxMode.Solid).whenAtMode(page, Page.Render);
    Setting<Boolean> fadeOnTargetChange = setting("FadeOnTargetChange", true).des("Fade color when target is changed to another entity or nothing").when(() -> renderType.getValue() != RenderType.None).whenAtMode(page, Page.Render);
    Setting<Integer> fadeSpeedTargetChange = setting("FadeSpeedOnChange", 25, 2, 50).des("Render target change color fade speed").when(() -> renderType.getValue() != RenderType.None).whenTrue(fadeOnTargetChange).whenAtMode(page, Page.Render);
    Setting<Boolean> changeColorWhenHit = setting("ChangeColorOnHit", true).des("Change and fade color on a hit").when(() -> renderType.getValue() != RenderType.None).whenAtMode(page, Page.Render);
    Setting<Integer> fadeSpeedWhenHit = setting("FadeSpeedOnHit", 25, 2, 50).des("Render hit color fade speed").when(() -> renderType.getValue() != RenderType.None).whenTrue(changeColorWhenHit).whenAtMode(page, Page.Render);
    Setting<Color> hitColor = setting("HitColor", new Color(new java.awt.Color(255, 100, 100, 125).getRGB())).des("Aura target render color").when(() -> renderType.getValue() != RenderType.Box && renderType.getValue() != RenderType.None).whenTrue(changeColorWhenHit).whenAtMode(page, Page.Render);
    Setting<Color> solidHitColor = setting("SolidHitColor", new Color(new java.awt.Color(255, 255, 255, 52).getRGB())).des("Aura target render fill color").when(() -> renderType.getValue() == RenderType.Box && renderBoxMode.getValue() != BoxMode.Lines).whenTrue(changeColorWhenHit).whenAtMode(page, Page.Render);
    Setting<Color> linesHitColor = setting("LinesHitColor", new Color(new java.awt.Color(255, 255, 255, 175).getRGB())).des("Aura target render outline color").when(() -> renderType.getValue() == RenderType.Box && renderBoxMode.getValue() != BoxMode.Solid).whenTrue(changeColorWhenHit).whenAtMode(page, Page.Render);

    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    public static final HashMap<Integer, Integer> moreTargetData = new HashMap<>();
    private static final HashMap<Entity, Integer> lastTargetData = new HashMap<>();
    private static final Timer attackTimer = new Timer();
    private int prevSlot;
    public static Entity target;
    private final LockTask saveSlotTask = new LockTask(() -> prevSlot = mc.player.inventory.currentItem);
    private boolean attackFlag;
    public static final List<Vec3d> entityTriggerVecList = new ArrayList<>();
    private final Timer animationTimer = new Timer();
    public static Aura INSTANCE;

    public Aura() {
        INSTANCE = this;
        repeatUnits.add(updateAura);
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

    RepeatUnit updateAura = new RepeatUnit(() -> 50, () -> {
        if (mc.world == null) return;

        updateRenderMaps();

        if ((checkPreferredWeapons() && !autoSwitch.getValue()) || (autoSwitch.getValue() && preferredWeapon.getValue() != Weapon.None) || (preferredWeapon.getValue() == Weapon.None)) {
            target = calcTarget();
            if (target == null) return;

            SwapManager.swapInvoke(this.name, !autoSwitch.getValue(), true, () -> {
                if (fadeOnTargetChange.getValue() && !target.isDead)
                    lastTargetData.put(target, 0);
                if (changeColorWhenHit.getValue() && !target.isDead && !moreTargetData.containsKey(target.getEntityId()))
                    moreTargetData.put(target.getEntityId(), 300);

                attackTargets();
            });
        }
        else if (!((checkPreferredWeapons() && !autoSwitch.getValue()) || (autoSwitch.getValue() && preferredWeapon.getValue() != Weapon.None) || (preferredWeapon.getValue() == Weapon.None))) {
            target = null;
        }
        else {
            if (autoSwitch.getValue() && switchBack.getValue() && saveSlotTask.getLocked()) {
                mc.player.inventory.currentItem = prevSlot;
                saveSlotTask.setLocked(false);
            }
        }
    });

    @Override
    public String getModuleInfo() {
        if (target != null) return target.getName();
        else return " ";
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
        if (autoSwitch.getValue() && switchBack.getValue() && saveSlotTask.getLocked()) {
            mc.player.inventory.currentItem = prevSlot;
        }
        if (rotate.getValue()) {
            RotationManager.resetRotation();
        }
    }

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (renderType.getValue() == RenderType.Box) {
            int theSolidColor = new java.awt.Color(solidColor.getValue().getColorColor().getRed(), solidColor.getValue().getColorColor().getGreen(), solidColor.getValue().getColorColor().getBlue(), solidColor.getValue().getAlpha()).getRGB();
            int theLinesColor = new java.awt.Color(linesColor.getValue().getColorColor().getRed(), linesColor.getValue().getColorColor().getGreen(), linesColor.getValue().getColorColor().getBlue(), linesColor.getValue().getAlpha()).getRGB();

            if (target != null && moreTargetData.containsKey(target.getEntityId())) {
                if (RenderHelper.isInViewFrustrum(target)) {
                    if (renderBoxMode.getValue() != BoxMode.Lines)
                        SpartanTessellator.drawBBFullBox(target, ColorUtil.colorShift(solidHitColor.getValue().getColor(), theSolidColor, moreTargetData.get(target.getEntityId()) / 300.0f));
                    if (renderBoxMode.getValue() != BoxMode.Solid)
                        SpartanTessellator.drawBBLineBox(target, boxLinesWidth.getValue(), ColorUtil.colorShift(linesHitColor.getValue().getColor(), theLinesColor, moreTargetData.get(target.getEntityId()) / 300.0f));
                }
            }


            if (fadeOnTargetChange.getValue() && !lastTargetData.isEmpty()) {
                HashMap<Entity, Integer> tempLastTargetData;
                synchronized (lastTargetData) {
                    tempLastTargetData = new HashMap<>(lastTargetData);
                }
                tempLastTargetData.entrySet().stream()
                        .filter(entry -> target == null || entry.getKey() != target)
                        .forEach(entry -> {
                            int localValue = entry.getValue();

                            if (localValue < 300) {
                                int solidColor = ColorUtil.colorShift(solidHitColor.getValue().getColor(), theSolidColor, (moreTargetData.get(entry.getKey().getEntityId()) == null ? 300.0f : moreTargetData.get(entry.getKey().getEntityId())) / 300.0f);
                                int linesColor = ColorUtil.colorShift(linesHitColor.getValue().getColor(), theLinesColor, (moreTargetData.get(entry.getKey().getEntityId()) == null ? 300.0f : moreTargetData.get(entry.getKey().getEntityId())) / 300.0f);

                                if (entry.getKey() != null && RenderHelper.isInViewFrustrum(entry.getKey())) {
                                    if (renderBoxMode.getValue() != BoxMode.Lines)
                                        SpartanTessellator.drawBBFullBox(entry.getKey(), new java.awt.Color(ColorUtil.getRed(solidColor), ColorUtil.getGreen(solidColor), ColorUtil.getBlue(solidColor),
                                                (int) MathUtilFuckYou.linearInterp(ColorUtil.getAlpha(solidColor), 0, localValue)).getRGB());
                                    if (renderBoxMode.getValue() != BoxMode.Solid)
                                        SpartanTessellator.drawBBLineBox(entry.getKey(), boxLinesWidth.getValue(), new java.awt.Color(ColorUtil.getRed(linesColor), ColorUtil.getGreen(linesColor), ColorUtil.getBlue(linesColor),
                                                (int) MathUtilFuckYou.linearInterp(ColorUtil.getAlpha(linesColor), 0, localValue)).getRGB());
                                }
                            }
                        });
            }
        }
    }

    @Listener
    public void onUpdateWalkingPlayerPre(OnUpdateWalkingPlayerEvent.Pre event) {
        if (rotate.getValue() && !legitMode.getValue() && mc.player != null) {
            if (target != null) {
                RotationManager.lookAtEntity(event, target, slowRotate.getValue(), yawSpeed.getValue());
            } else {
                RotationManager.resetRotation();
            }
        }
    }

    private void attackTargets() {
        if (target != null) {
            if (!legitMode.getValue() && rotate.getValue() && slowRotate.getValue() && (Math.abs(calcNormalizedAngleDiff(normalizeAngle(getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionVector())[0]), RotationManager.newYaw)) > attackYawRange.getValue())) return;
            if (autoSwitch.getValue()) {
                saveSlotTask.invokeLock();
                ItemUtils.switchToSlot(ItemUtils.findItemInHotBar(preferredWeapon()), false);
            }
            if (!checkWall.getValue() || EntityUtil.isEntityVisible(target)) doAttack(target);
        }
    }

    private void doAttack(Entity entity) {
        boolean activeShield = mc.player.isActiveItemStackBlocking() && mc.player.getHeldItemOffhand().getItem() instanceof ItemShield;
        if (activeShield) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }

        if (ModuleManager.getModule(Criticals.class).isEnabled() && Criticals.INSTANCE.mode.getValue() == Criticals.Mode.Jump &&
                mc.player.onGround && target instanceof EntityLivingBase && Criticals.INSTANCE.canCrit() &&
        !Criticals.INSTANCE.disableWhenAura.getValue()) {

            Criticals.INSTANCE.doJumpCrit();

            if (mc.player.getCooledAttackStrength(0.5f) > 0.9f && mc.player.fallDistance > 0.1) {
                attackFlag = true;

                boolean sprinting = mc.player.isSprinting();

                if (stopSprint.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (packetAttack.getValue()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                }
                else {
                    mc.playerController.attackEntity(mc.player, entity);
                }
                mc.player.swingArm(offhandSwing.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                mc.player.resetCooldown();

                if (stopSprint.getValue() && sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }

                attackTimer.reset();
            }
        }
        else {
            if (delay.getValue() ? attackTimer.passed(attackDelay.getValue()) : mc.player.getCooledAttackStrength(0.5f) > 0.9f) {

                attackFlag = true;

                boolean sprinting = mc.player.isSprinting();

                if (stopSprint.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (legitMode.getValue() && (Math.random() <= (randomClickPercent.getValue() / 100.0f))) {
                    mc.clickMouse();
                }
                else {
                    if (packetAttack.getValue()) {
                        mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                    }
                    else {
                        mc.playerController.attackEntity(mc.player, entity);
                    }
                    mc.player.swingArm(offhandSwing.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                    mc.player.resetCooldown();
                }

                if (stopSprint.getValue() && sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }

                attackTimer.reset();
            }
        }

        if (activeShield) {
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
        }
    }

    private boolean checkEntity(Entity entity, boolean isFriend) {
        if (entity == null || mc.player == null) return false;
        else {
            return (!(isFriend || entity == mc.player) &&
                            ((targetPlayers.getValue() && entity instanceof EntityPlayer) ||
                            (targetMonsters.getValue() && (EntityUtil.isEntityMonster(entity) || entity instanceof EntityDragon)) ||
                            (targetAnimals.getValue() && (EntityUtil.isEntityAnimal(entity))) ||
                            (targetMiscEntities.getValue() && !(entity instanceof EntityPlayer || (EntityUtil.isEntityMonster(entity) || entity instanceof EntityDragon) || EntityUtil.isEntityAnimal(entity) || entity instanceof EntityItem || entity instanceof IProjectile || entity instanceof EntityXPOrb))) &&
                    MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), range.getValue()) && (checkWall.getValue() ||
                    (EntityUtil.isEntityVisible(entity) || MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), entity.getPositionVector(), wallRange.getValue()))) &&
                    !(entity.isDead));
        }
    }

    private Entity calcTarget() {
        if (!rotate.getValue() && triggerMode.getValue()) {
            Vec3d startVec = mc.player.getPositionEyes(mc.getRenderPartialTicks());
            RayTraceResult ray = mc.player.rayTrace(6.0f, mc.getRenderPartialTicks());
            if (ray == null) return null;
            Vec3d raytracedVec = ray.hitVec;

            double[] extendVecHelper = MathUtilFuckYou.cartesianToPolar3d(raytracedVec.x - startVec.x, raytracedVec.y - startVec.y, raytracedVec.z - startVec.z);
            double[] extendVecHelper2 = MathUtilFuckYou.polarToCartesian3d(range.getValue(), extendVecHelper[1], extendVecHelper[2]);

            double extendFactorX = extendVecHelper2[0] / 200.0f;
            double extendFactorY = extendVecHelper2[1] / 200.0f;
            double extendFactorZ = extendVecHelper2[2] / 200.0f;

            entityTriggerVecList.clear();
            for (int i = 0; i < 200; i++) {
                extendFactorX += extendVecHelper2[0] / 200.0f;
                extendFactorY += extendVecHelper2[1] / 200.0f;
                extendFactorZ += extendVecHelper2[2] / 200.0f;
                Vec3d extendVec = new Vec3d(startVec.x + extendFactorX, startVec.y + extendFactorY, startVec.z + extendFactorZ);
                entityTriggerVecList.add(0, extendVec);
            }
        }

        HashMap<Entity, Pair<Boolean, Boolean>> map;
        synchronized (FriendsEnemies.INSTANCE.entityData) {
            map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
        }

        List<Map.Entry<Entity, Pair<Boolean, Boolean>>> entities = map.entrySet().stream()
                .filter(entry -> checkEntity(entry.getKey(), entry.getValue().a))
                .filter(entry -> ModuleManager.getModule(Freecam.class).isDisabled() || Freecam.INSTANCE.camera != entry.getKey())
                .filter(entry -> !ignoreInvisible.getValue() || entry.getKey().isInvisible())
                .filter(entry -> !legitMode.getValue() || mc.objectMouseOver.entityHit == entry.getKey())
                .filter(entry -> rotate.getValue() || !triggerMode.getValue() || checkTriggerMode(entry.getKey()))
                .sorted(Comparator.comparing(entry -> MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), entry.getKey().getPositionVector())))
                .collect(Collectors.toList());

        if (entities.isEmpty()) {
            return null;
        } else {
            return entities.get(0).getKey();
        }
    }

    private void updateRenderMaps() {
        int passedms = (int) animationTimer.hasPassed();
        animationTimer.reset();

        if (fadeOnTargetChange.getValue() && !lastTargetData.isEmpty()) {
            HashMap<Entity, Integer> tempLastTargetData;
            synchronized (lastTargetData) {
                tempLastTargetData = new HashMap<>(lastTargetData);
            }
            tempLastTargetData.forEach((key, value) -> {
                if (value >= 300) {
                    lastTargetData.remove(key);
                } else {
                    lastTargetData.put(key, (int) MathUtilFuckYou.clamp(value + passedms * 0.02f * fadeSpeedTargetChange.getValue(), 0, 300));
                }
            });
        }

        if (changeColorWhenHit.getValue() && !moreTargetData.isEmpty()) {
            HashMap<Integer, Integer> tempMoreTargetData;
            synchronized (moreTargetData) {
                tempMoreTargetData = new HashMap<>(moreTargetData);
            }
            tempMoreTargetData.entrySet().stream()
                    .filter(entry -> mc.world.getEntityByID(entry.getKey()) != null)
                    .forEach(entry -> {
                        Entity entity = mc.world.getEntityByID(entry.getKey());

                        if (entity != null) {
                            int localValue = entry.getValue();

                            if (attackFlag && target != null && entity == target) {
                                localValue = 0;
                                attackFlag = false;
                            }

                            synchronized (moreTargetData) {
                                moreTargetData.put(entry.getKey(), (int) MathUtilFuckYou.clamp(localValue + passedms * 0.02f * fadeSpeedWhenHit.getValue(), 0, 300));
                            }
                        }
                    });

            tempMoreTargetData.entrySet().stream()
                    .filter(entry -> !mc.world.loadedEntityList.contains(mc.world.getEntityByID(entry.getKey())))
                    .forEach(entry -> {
                        synchronized (moreTargetData) {
                            moreTargetData.remove(entry.getKey());
                        }
                    });
        }
    }

    private boolean checkTriggerMode(Entity entity) {
        double collisionBorderSize = entity.getCollisionBorderSize();
        AxisAlignedBB hitbox = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);

        if (checkWall.getValue() && mc.objectMouseOver.entityHit != entity)
            return false;

        for (Vec3d vec : entityTriggerVecList) {
            if (hitbox.contains(vec))
                return true;
        }
        return false;
    }

    private Item preferredWeapon() {
        switch (preferredWeapon.getValue()) {
            case Sword: {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_SWORD)) return Items.DIAMOND_SWORD;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_SWORD)) return Items.IRON_SWORD;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_SWORD)) return Items.STONE_SWORD;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_SWORD)) return Items.WOODEN_SWORD;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_SWORD)) return Items.GOLDEN_SWORD;
                        }
                    }
                }
            }

            case Axe: {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_AXE)) return Items.DIAMOND_AXE;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_AXE)) return Items.IRON_AXE;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_AXE)) return Items.STONE_AXE;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_AXE)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_AXE)) return Items.GOLDEN_AXE;
                        }
                    }
                }
            }

            case PickAxe: {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_PICKAXE)) return Items.DIAMOND_PICKAXE;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_PICKAXE)) return Items.IRON_PICKAXE;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_PICKAXE)) return Items.STONE_PICKAXE;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_PICKAXE)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_PICKAXE)) return Items.GOLDEN_PICKAXE;
                        }
                    }
                }
            }

            case Shovel: {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_SHOVEL)) return Items.DIAMOND_SHOVEL;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_SHOVEL)) return Items.IRON_SHOVEL;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_SHOVEL)) return Items.STONE_SHOVEL;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_SHOVEL)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_SHOVEL)) return Items.GOLDEN_SHOVEL;
                        }
                    }
                }
            }
        }
        return Items.AIR;
    }

    public boolean checkPreferredWeapons() {
        if (mc.player != null) {
            switch (preferredWeapon.getValue()) {
                case Sword:
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.STONE_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SWORD);

                case Axe:
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_AXE || mc.player.getHeldItemMainhand().getItem() == Items.IRON_AXE || mc.player.getHeldItemMainhand().getItem() == Items.STONE_AXE || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_AXE || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_AXE);

                case PickAxe:
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.IRON_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.STONE_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_PICKAXE);

                case Shovel:
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.STONE_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SHOVEL);
            }
        }
        return false;
    }

    enum Page {
        Aura,
        Render
    }

    enum Weapon {
        Sword,
        Axe,
        PickAxe,
        Shovel,
        None
    }

    enum RenderType {
        Box,
        Chams,
        Circle,
        None
    }

    enum BoxMode {
        Lines,
        Solid,
        Both
    }
}
