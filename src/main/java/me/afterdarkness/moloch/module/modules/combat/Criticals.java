package me.afterdarkness.moloch.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.Timer;

@Parallel(runnable = true)
@ModuleInfo(name = "Criticals", category = Category.COMBAT, description = "Force attacks to be criticals")
public class Criticals extends Module {

    public Setting<Mode> mode = setting("Mode", Mode.PacketNCP).des("Mode of how to force criticals");
    Setting<Float> jumpHeight = setting("JumpHeight", 0.3f, 0.1f, 0.5f).des("Height of jump for criticals").whenAtMode(mode, Mode.Jump);
    Setting<Boolean> onlyWeapon = setting("OnlyWeapon", true).des("Only force criticals when holding a sword or axe");
    Setting<Boolean> checkRaytrace = setting("CheckRaytrace", false).des("Only force criticals when your mouse is over the target entity").whenAtMode(mode, Mode.Jump);
    public Setting<Boolean> disableWhenAura = setting("AuraNoCrits", false).des("Disable criticals when aura is actively attacking an entity (if you want criticals but don't want it to spam jump when using aura)").whenAtMode(mode, Mode.Jump);
    Setting<Integer> delay = setting("Delay", 500, 1, 2000).des("Milliseconds between each criticals attempt to prevent position packet spamming");

    public boolean flag;
    private boolean flag2;
    private boolean flag3;
    private Entity target;
    public static Criticals INSTANCE;
    private final Timer delayTimer = new Timer();

    public Criticals() {
        INSTANCE = this;
    }

    @Override
    public void onRenderTick() {
        if (mode.getValue() == Mode.Jump) {
            if (target == null) return;

            if (flag && mc.player.fallDistance > 0.1 && canCrit() && (!checkRaytrace.getValue() || mc.objectMouseOver.entityHit == target)) {
                flag = false;
                flag3 = true;
                mc.player.connection.sendPacket(new CPacketUseEntity(target));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                flag3 = false;
                mc.player.resetCooldown();
            }

            if (flag && mc.player.fallDistance > 0.0)
                flag2 = true;

            if (flag2 && mc.player.onGround) {
                flag = false;
                flag2 = false;
                flag3 = true;
                mc.player.connection.sendPacket(new CPacketUseEntity(target));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                flag3 = false;
                mc.player.resetCooldown();
            }
        }

        if (mode.getValue() != Mode.Jump || (flag2 && (mc.player.onGround || mc.player.isInWeb || mc.player.isOnLadder() || mc.player.isRiding() ||
                mc.player.isPotionActive(MobEffects.BLINDNESS) || mc.player.isInWater() || mc.player.isInLava()))) {
            flag = false;
            flag2 = false;
        }
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        CPacketUseEntity packet;
        if (delayTimer.passed(delay.getValue()) && event.getPacket() instanceof CPacketUseEntity && (packet = ((CPacketUseEntity) event.getPacket())).getAction() == CPacketUseEntity.Action.ATTACK && !mc.gameSettings.keyBindJump.isKeyDown() && canCrit() && mc.player.onGround &&
                packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase && !flag && !flag3) {

            switch (mode.getValue()) {
                case PacketNormal: {
                    if (mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625, mc.player.posZ, true));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.1E-5, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    }
                    break;
                }

                case PacketNCP: {
                    if (mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0000013579, mc.player.posZ, false));
                    }
                    break;
                }

                case PacketAlt: {
                    if (mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.062600301692775, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.07260029960661, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    }
                }

                case Jump: {
                    if (!(disableWhenAura.getValue() && ModuleManager.getModule(Aura.class).isEnabled() &&
                            ((Aura.INSTANCE.checkPreferredWeapons() && !Aura.INSTANCE.autoSwitch.getValue()) || (Aura.INSTANCE.autoSwitch.getValue() && Aura.INSTANCE.preferredWeapon.getValue() != Aura.Weapon.None) || (Aura.INSTANCE.preferredWeapon.getValue() == Aura.Weapon.None)))) {
                        doJumpCrit();
                        target = packet.getEntityFromWorld(mc.world);
                        flag = true;
                        event.cancel();
                    }
                    break;
                }
            }
            delayTimer.reset();
        }
    }

    public void doJumpCrit() {
        mc.player.jump();
        mc.player.motionY = jumpHeight.getValue();
    }

    public boolean canCrit() {
        return (!mc.player.isInWeb && !mc.player.isOnLadder() && !mc.player.isRiding() &&
        !mc.player.isPotionActive(MobEffects.BLINDNESS) && !mc.player.isInWater() && !mc.player.isInLava()
        && (!onlyWeapon.getValue() || (isHoldingWeapon())));
    }

    private boolean isHoldingWeapon() {
        return mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD
                || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.STONE_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.STONE_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_AXE;
    }

    enum Mode {
        PacketNormal,
        PacketNCP,
        PacketAlt,
        Jump
    }
}
