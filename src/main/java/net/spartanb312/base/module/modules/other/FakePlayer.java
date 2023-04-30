package net.spartanb312.base.module.modules.other;

import com.mojang.authlib.GameProfile;
import me.afterdarkness.moloch.event.events.player.DisconnectEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

import java.util.UUID;

@Parallel(runnable = true)
@ModuleInfo(name = "FakePlayer", category = Category.OTHER, description = "Spawn a fake player entity in client side")
public class FakePlayer extends Module {

    Setting<Integer> health = setting("Health", 10, 0, 36).des("Health of fakeplayer");
    Setting<Boolean> sneak = setting("Sneak", false).des("Makes fakeplayer crouch ");
    Setting<String> playerName = setting("Name", "xX_NobleSix_Xx", false, null, false).des("Name of fakeplayer");

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) return;
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("60569353-f22b-42da-b84b-d706a65c5ddf"), playerName.getValue()));
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        for (PotionEffect potionEffect : mc.player.getActivePotionEffects()) {
            fakePlayer.addPotionEffect(potionEffect);
        }
        fakePlayer.setHealth(health.getValue());
        fakePlayer.inventory.copyInventory(mc.player.inventory);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        if (sneak.getValue()) fakePlayer.setSneaking(true);
        mc.world.addEntityToWorld(-666, fakePlayer);
    }

    @Override
    public void onDisable() {
        if (mc.world.loadedEntityList.contains(mc.world.getEntityByID(-666)))
            mc.world.removeEntityFromWorld(-666);
    }

    @Override
    public void onTick() {
        if (mc.player.deathTime > 0) {
            ModuleManager.getModule(FakePlayer.class).disable();
        }
    }

    @Override
    public String getModuleInfo() {
        return playerName.getValue();
    }

    @Listener
    public void onDisconnect(DisconnectEvent event) {
        ModuleManager.getModule(FakePlayer.class).disable();
    }
}
