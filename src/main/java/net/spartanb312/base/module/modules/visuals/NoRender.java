package net.spartanb312.base.module.modules.visuals;

import me.afterdarkness.moloch.event.events.render.RenderBossHealthEvent;
import me.afterdarkness.moloch.event.events.render.RenderEntityPreEvent;
import me.afterdarkness.moloch.event.events.render.RenderThrowableEvent;
import me.afterdarkness.moloch.utils.math.Triple;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraftforge.client.ForgeHooksClient;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "NoRender", category = Category.VISUALS, description = "Stop rendering certain things")
public class NoRender extends Module {

    Setting<Page> page = setting("Page", Page.Overlays);

    Setting<Boolean> blindness = setting("Blindness", true).whenAtMode(page, Page.Overlays);
    Setting<Boolean> nausea = setting("Nausea", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> netherPortal = setting("NetherPortal", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> fire = setting("Fire", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> blockOverlay = setting("BlockOverlay", false).whenAtMode(page, Page.Overlays);
    Setting<BossBarMode> bossBar = setting("BossBar", BossBarMode.None).whenAtMode(page, Page.Overlays);
    public Setting<Float> bossBarSize = setting("BossBarScale", 0.5f, 0.1f, 1.0f).when(() -> bossBar.getValue() == BossBarMode.Stack).whenAtMode(page, Page.Overlays);
    public Setting<TotemMode> totemPop = setting("TotemPop", TotemMode.None).whenAtMode(page, Page.Overlays);
    public Setting<Float> totemSize = setting("TotemSize", 0.5f, 0.1f, 1.0f).whenAtMode(totemPop, TotemMode.Scale).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> waterOverlay = setting("WaterOverlay", false).whenAtMode(page, Page.Overlays);
    Setting<Boolean> tutorial = setting("Tutorial", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> potionIcons = setting("PotionIcons", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> pumpkin = setting("PumpkinOverlay", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> vignette = setting("Vignette", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> hurtCam = setting("HurtCam", false).whenAtMode(page, Page.Overlays);
    public Setting<Boolean> chat = setting("Chat", false).whenAtMode(page, Page.Overlays);
    //See MixinGuiNewChat
    public Setting<Boolean> backgrounds = setting("Backgrounds", false).whenAtMode(page, Page.Overlays);

    public Setting<Boolean> players = setting("Players", false).whenAtMode(page, Page.World);
    public Setting<Boolean> monsters = setting("Monsters", false).whenAtMode(page, Page.World);
    public Setting<Boolean> animals = setting("Animals", false).whenAtMode(page, Page.World);
    Setting<Boolean> items = setting("Items", false).whenAtMode(page, Page.World);
    public Setting<ArmorMode> armor = setting("Armor", ArmorMode.None).whenAtMode(page, Page.World);
    public Setting<Integer> armorAlpha = setting("ArmorAlpha", 150, 0, 255).des("Alpha of armor").whenAtMode(armor, ArmorMode.Alpha).whenAtMode(page, Page.World);
    //See MixinLayerArmorBase && MixinLayerElytra
    public Setting<Boolean> changeCapeAlpha = setting("ChangeCapeAlpha", false).des("Change cape alpha").whenAtMode(page, Page.World);
    //See MixinLayerCape
    public Setting<Integer> capeAlpha = setting("CapeAlpha", 129, 0, 255).des("Alpha of capes").whenTrue(changeCapeAlpha).whenAtMode(page, Page.World);
    Setting<Boolean> projectiles = setting("Projectiles", false).whenAtMode(page, Page.World);
    Setting<Boolean> xpOrb = setting("XPOrb", false).whenAtMode(page, Page.World);
    Setting<XPMode> xpBottle = setting("XPBottle", XPMode.None).whenAtMode(page, Page.World);
    Setting<Integer> xpBottleAlpha = setting("XPBottleAlpha", 100, 0, 255).des("Alpha of thrown xp bottle").whenAtMode(xpBottle, XPMode.Alpha).whenAtMode(page, Page.World);
    Setting<Boolean> explosion = setting("Explosions", true).whenAtMode(page, Page.World);
    public Setting<Boolean> fog = setting("Fog", false).des("Also disables the orange effect inside of lava").whenAtMode(page, Page.World);
    Setting<Boolean> paint = setting("Paintings", false).whenAtMode(page, Page.World);
    public Setting<Boolean> chests = setting("Chests", false).whenAtMode(page, Page.World);
    public Setting<Boolean> enderChests = setting("EnderChests", false).whenAtMode(page, Page.World);
    public Setting<Boolean> enchantingTableBook = setting("EnchantTableBook", false).whenAtMode(page, Page.World);
    public Setting<Boolean> maps = setting("Maps", false).whenAtMode(page, Page.World);
    public Setting<Boolean> signText = setting("SignText", false).whenAtMode(page, Page.World);
    public Setting<Boolean> skyLightUpdate = setting("SkyLightUpdate", false).whenAtMode(page, Page.World);
    Setting<Boolean> fallingBlocks = setting("FallingBlocks", false).whenAtMode(page, Page.World);

    public static NoRender INSTANCE;
    private final HashMap<String, Triple<BossInfoClient, Integer, Integer>> newBossBars = new HashMap<>();
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();

    public NoRender() {
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

    RepeatUnit update = new RepeatUnit(() -> 100, () -> {
        if (mc.world == null) return;

        if (bossBar.getValue() == BossBarMode.Stack) {
            HashMap<String, Triple<BossInfoClient, Integer, Integer>> localNewBossBars = new HashMap<>();

            mc.ingameGUI.getBossOverlay().mapBossInfos.forEach((key, value) -> {
                String bossName = value.getName().getFormattedText();
                if (localNewBossBars.containsKey(bossName)) {
                    localNewBossBars.put(bossName, new Triple<>(localNewBossBars.get(bossName).a, localNewBossBars.get(bossName).b + 1, localNewBossBars.get(bossName).c));
                } else {
                    localNewBossBars.put(bossName, new Triple<>(value, 1, 12 + ((10 + mc.fontRenderer.FONT_HEIGHT) * localNewBossBars.size())));
                }
            });

            newBossBars.clear();
            newBossBars.putAll(localNewBossBars);
        }
    });

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (blindness.getValue())
            mc.player.removeActivePotionEffect(MobEffects.BLINDNESS);

        if (nausea.getValue() || netherPortal.getValue())
            mc.player.removeActivePotionEffect(MobEffects.NAUSEA);

        if (tutorial.getValue())
            mc.gameSettings.tutorialStep = TutorialSteps.NONE;
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if ((packet instanceof SPacketSpawnExperienceOrb && xpOrb.getValue()) || (packet instanceof SPacketExplosion && explosion.getValue()) || (packet instanceof SPacketSpawnPainting && paint.getValue()))
            event.cancel();
    }

    @Listener
    public void onRenderEntityPre(RenderEntityPreEvent event) {
        if ((event.entityIn instanceof EntityPlayer && players.getValue()) || ((EntityUtil.isEntityMonster(event.entityIn) || event.entityIn instanceof EntityDragon) && monsters.getValue()) || ((EntityUtil.isEntityAnimal(event.entityIn)) && animals.getValue()) || (event.entityIn instanceof EntityItem && items.getValue()) || ((event.entityIn instanceof IProjectile || event.entityIn instanceof EntityShulkerBullet || event.entityIn instanceof EntityFireball || event.entityIn instanceof EntityEnderEye) && projectiles.getValue()) || (event.entityIn instanceof EntityFallingBlock && fallingBlocks.getValue()))
            event.cancel();
    }

    @Listener
    public void onRenderBossHealth(RenderBossHealthEvent event) {
        if (bossBar.getValue() == BossBarMode.Stack) {
            int scaledWidth = new ScaledResolution(mc).getScaledWidth();

            newBossBars.forEach((key, value) -> {
                String text = key + " x" + value.b;
                GL11.glScalef(bossBarSize.getValue(), bossBarSize.getValue(), 1.0f);

                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(GuiBossOverlay.GUI_BARS_TEXTURES);
                mc.ingameGUI.getBossOverlay().render((int) ((float) scaledWidth / bossBarSize.getValue() / 2.0f - 91.0f), value.c, value.a);
                mc.fontRenderer.drawStringWithShadow(text, scaledWidth / bossBarSize.getValue() / 2.0f - (mc.fontRenderer.getStringWidth(text) / 2.0f), value.c - 9, 0xFFFFFF);

                GL11.glScalef(1.0f / bossBarSize.getValue(), 1.0f / bossBarSize.getValue(), 1.0f);
            });
        }

        if (bossBar.getValue() != BossBarMode.None)
            event.cancel();
    }

    @Listener
    public void onRenderThrowableHead(RenderThrowableEvent.Head event) {
        if (event.item == Items.EXPERIENCE_BOTTLE && xpBottle.getValue() == XPMode.NoRender)
            event.cancel();
    }

    @Listener
    public void onRenderThrowableInvoke(RenderThrowableEvent.Invoke event) {
        if (event.item == Items.EXPERIENCE_BOTTLE && xpBottle.getValue() == XPMode.Alpha) {
            event.cancel();

            ItemStack stack = new ItemStack(event.item);
            IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, null, null);
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);

            SpartanTessellator.renderItemModelPre(stack);
            SpartanTessellator.renderItemModelVanilla(stack, model, xpBottleAlpha.getValue() / 255.0f, true);
            SpartanTessellator.renderItemModelPost(stack);
            //SpartanTessellator.renderItem(new ItemStack(event.item), ItemCameraTransforms.TransformType.GROUND, xpBottleAlpha.getValue(), false, Color.WHITE.getRGB(), false, false, 1.0f, Color.WHITE.getRGB());
        }
    }

    enum Page {
        Overlays,
        World
    }

    enum BossBarMode {
        Stack,
        NoRender,
        None
    }

    public enum TotemMode {
        Scale,
        NoRender,
        None
    }

    public enum ArmorMode {
        Alpha,
        NoRender,
        None,
    }

    enum XPMode {
        Alpha,
        NoRender,
        None
    }
}