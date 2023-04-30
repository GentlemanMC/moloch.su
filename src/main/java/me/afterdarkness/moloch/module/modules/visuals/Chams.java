package me.afterdarkness.moloch.module.modules.visuals;

import me.afterdarkness.moloch.client.EnemyManager;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.event.events.render.RenderEntityEvent;
import me.afterdarkness.moloch.event.events.render.RenderEntityInvokeEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.spartanb312.base.client.FriendManager;
import net.spartanb312.base.client.ModuleManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.graphics.RenderHelper;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;
import static org.lwjgl.opengl.GL11.*;
//TODO: add distance color
@Parallel(runnable = true)
@ModuleInfo(name = "Chams", category = Category.VISUALS, description = "Do weird stuff with entity rendering")
public class Chams extends Module {

    public static Chams instance;
    public final ResourceLocation loadedTexturePackGlint = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    public final ResourceLocation gradientGlint = new ResourceLocation("moloch:textures/glints/gradient.png");
    public final ResourceLocation lightningGlint = new ResourceLocation("moloch:textures/glints/lightning.png");
    public final ResourceLocation linesGlint = new ResourceLocation("moloch:textures/glints/lines.png");
    public final ResourceLocation swirlsGlint = new ResourceLocation("moloch:textures/glints/swirls.png");
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();

    Setting<Page> page = setting("Page", Page.Players);

    public Setting<Boolean> ignoreInvisible = setting("IgnoreInvisible", false).des("Doesn't render chams for invisible entities").when(() -> page.getValue() == Page.Players || page.getValue() == Page.Monsters || page.getValue() == Page.Animals);
    Setting<Boolean> playerCrowdAlpha = setting("PlayerCrowdAlpha", true).des("Reduce alpha of player chams when close to you").whenAtMode(page, Page.Players);
    Setting<Float> playerCrowdAlphaRadius = setting("PlayerCrowdAlphaDist", 2.0f, 0.5f, 4.0f).des("Distance to start reducing alpha of player chams close to you").whenTrue(playerCrowdAlpha).whenAtMode(page, Page.Players);
    Setting<Float> playerCrowdEndAlpha = setting("PlayerCrowdEndAlpha", 0.3f, 0.0f, 1.0f).des("Percentage of alpha when player chams are close to you").whenTrue(playerCrowdAlpha).whenAtMode(page, Page.Players);
    public Setting<Boolean> players = setting("Players", true).des("Render player chams").whenAtMode(page, Page.Players);
    public Setting<Boolean> otherPlayers = setting("OtherPlayers", true).des("Render player chams for other players").whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> self = setting("Self", true).des("Render self chams").whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> playerWall = setting("PlayerWalls", true).des("Render players through wall").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerCancelVanillaRender = setting("PlayerNoVanillaRender", true).des("Cancels normal minecraft player rendering").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerTexture = setting("PlayerTexture", true).des("Render player texture on chams").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> selfTexture = setting("SelfTexture", false).des("Render self texture on chams").whenTrue(self).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerDepthMask = setting("PlayerDepthMask", true).des("Enable depth mask for players (stops entity layers and tile entities from rendering through chams)").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerLighting = setting("PlayerLighting", false).des("Render player chams with lighting").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerCull = setting("PlayerCull", true).des("Don't render sides of player chams that you can't see").whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> playerNoHurt = setting("PlayerNoHurt", true).des("Don't render hurt effect when player is damaged").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerBlend = setting("PlayerBlend", false).des("Use additive blending on player chams").whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> playerWallEffect = setting("PlayerWallEffect", false).des("Render different chams when player is blocked by a wall").whenTrue(players).whenTrue(playerWall).whenAtMode(page, Page.Players);
    Setting<Boolean> playerWallTexture = setting("PlayerWallTexture", false).des("Render texture on player chams behind walls").whenTrue(playerWallEffect).whenTrue(players).whenTrue(playerWall).whenAtMode(page, Page.Players);
    Setting<Boolean> playerWallBlend = setting("PlayerWallBlend", false).des("Use additive blending for player chams behind walls").whenTrue(playerWallEffect).whenTrue(players).whenTrue(playerWall).whenAtMode(page, Page.Players);
    Setting<Boolean> playerWallGlint = setting("PlayerWallGlint", false).des("Render glint texture on player chams behind walls").whenTrue(playerWallEffect).whenTrue(players).whenTrue(playerWall).whenAtMode(page, Page.Players);
    Setting<Color> playerWallColor = setting("PlayerWallColor", new Color(new java.awt.Color(255, 100, 100, 113).getRGB())).des("Player chams color behind walls").whenTrue(playerWallEffect).whenTrue(playerWall).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> friendWallColor = setting("FriendWallColor", new Color(new java.awt.Color(50, 100, 255, 100).getRGB())).des("Friend chams color behind walls").whenTrue(playerWallEffect).whenTrue(playerWall).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> enemyWallColor = setting("EnemyWallColor", new Color(new java.awt.Color(255, 100, 50, 100).getRGB())).des("Enemy chams color behind walls").whenTrue(playerWallEffect).whenTrue(playerWall).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerGlint = setting("PlayerGlint", false).des("Render glint texture on player chams").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> selfGlint = setting("SelfGlint", false).des("Render glint texture on yourself").whenTrue(self).whenTrue(playerGlint).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<GlintMode> playerGlintMode = setting("PlayerGlintMode", GlintMode.Swirls).des("Texture of player chams glint").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Boolean> playerGlintMove = setting("PlayerGlintMove", true).des("Player chams glint move").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Float> playerGlintMoveSpeed = setting("PlayerGlintMoveSpeed", 0.4f, 0.1f, 1.0f).des("Player chams glint move speed").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(playerGlint).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Float> playerGlintScale = setting("PlayerGlintScale", 4.0f, 0.1f, 4.0f).des("Size of player chams glint texture").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> playerGlintColor = setting("PlayerGlintColor", new Color(new java.awt.Color(125, 40, 255, 144).getRGB())).des("Player chams glint color").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> friendGlintColor = setting("FriendGlintColor", new Color(new java.awt.Color(50, 200, 255, 100).getRGB())).des("Friend chams glint color").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> enemyGlintColor = setting("EnemyGlintColor", new Color(new java.awt.Color(255, 50, 50, 100).getRGB())).des("Enemy chams glint color").when(() -> playerGlint.getValue() || playerWallGlint.getValue()).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> selfGlintColor = setting("SelfGlintColor", new Color(new java.awt.Color(125, 50, 255, 84).getRGB())).des("Self chams glint color").whenTrue(self).whenTrue(selfGlint).whenTrue(playerGlint).whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> playerBypassArmor = setting("PlayerThroughArmor", true).des("Render player chams through armor").whenTrue(playerWall).whenFalse(playerWallEffect).whenTrue(players).whenAtMode(page, Page.Players);
    public Setting<Boolean> playerBypassArmorWall = setting("PlayerThroughArmorWall", false).des("Render player chams through armor only through wall").whenTrue(playerWall).whenFalse(playerWallEffect).whenTrue(playerBypassArmor).whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> playerColor = setting("PlayerColor", new Color(new java.awt.Color(255, 255, 255, 153).getRGB())).des("Player chams color").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> friendColor = setting("FriendColor", new Color(new java.awt.Color(100, 200, 255, 255).getRGB())).des("Friend chams color").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> enemyColor = setting("EnemyColor", new Color(new java.awt.Color(255, 100, 100, 255).getRGB())).des("Enemy chams color").whenTrue(players).whenAtMode(page, Page.Players);
    Setting<Color> selfColor = setting("SelfColor", new Color(new java.awt.Color(255, 255, 255, 156).getRGB())).des("Self chams color").whenTrue(self).whenTrue(players).whenAtMode(page, Page.Players);

    public Setting<Boolean> monsters = setting("Monsters", false).des("Render monster chams").whenAtMode(page, Page.Monsters);
    public Setting<Boolean> monsterWall = setting("MonsterWalls", true).des("Render monsters through wall").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterCancelVanillaRender = setting("MonsterNoVanillaRender", false).des("Cancels normal minecraft monster rendering").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterTexture = setting("MonsterTexture", false).des("Render monster texture on chams").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterDepthMask = setting("MonsterDepthMask", false).des("Enable depth mask for monsters (stops entity layers and tile entities from rendering through chams)").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterLighting = setting("MonsterLighting", false).des("Render monster chams with lighting").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterCull = setting("MonsterCull", true).des("Don't render sides of monster chams that you can't see").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    public Setting<Boolean> monsterNoHurt = setting("MonsterNoHurt", true).des("Don't render hurt effect when monster is damaged").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterBlend = setting("MonsterBlend", false).des("Use additive blending on monster chams").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterCrowdAlpha = setting("MonsterCrowdAlpha", false).des("Reduce alpha of monster chams when close to you").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    public Setting<Boolean> monsterWallEffect = setting("MonsterWallEffect", false).des("Render different chams when monster is blocked by a wall").whenTrue(monsters).whenTrue(monsterWall).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterWallTexture = setting("MonsterWallTexture", false).des("Render texture on monster chams behind walls").whenTrue(monsterWallEffect).whenTrue(monsters).whenTrue(monsterWall).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterWallBlend = setting("MonsterWallBlend", false).des("Use additive blending for monster chams behind walls").whenTrue(monsterWallEffect).whenTrue(monsters).whenTrue(monsterWall).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterWallGlint = setting("MonsterWallGlint", false).des("Render glint texture on monster chams behind walls").whenTrue(monsterWallEffect).whenTrue(monsters).whenTrue(monsterWall).whenAtMode(page, Page.Monsters);
    Setting<Color> monsterWallColor = setting("MonsterWallColor", new Color(new java.awt.Color(100, 100, 100, 100).getRGB())).des("Monster chams color behind walls").whenTrue(monsterWallEffect).whenTrue(monsterWall).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Float> monsterCrowdAlphaRadius = setting("MonsterCrowdAlphaDist", 1.0f, 0.5f, 4.0f).des("Distance to start reducing alpha of monster chams close to you").whenTrue(monsterCrowdAlpha).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Float> monsterCrowdEndAlpha = setting("MonsterCrowdEndAlpha", 0.5f, 0.0f, 1.0f).des("Percentage of alpha when monster chams are close to you").whenTrue(monsterCrowdAlpha).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterGlint = setting("MonsterGlint", false).des("Render glint texture on monster chams").whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<GlintMode> monsterGlintMode = setting("MonsterGlintMode", GlintMode.Gradient).des("Texture of monster chams glint").when(() -> monsterGlint.getValue() || monsterWallGlint.getValue()).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Boolean> monsterGlintMove = setting("MonsterGlintMove", false).des("Monster chams glint move").when(() -> monsterGlint.getValue() || monsterWallGlint.getValue()).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Float> monsterGlintMoveSpeed = setting("MonsterGlintMoveSpeed", 0.4f, 0.1f, 1.0f).des("Monster chams glint move speed").whenTrue(monsterGlintMove).when(() -> monsterGlint.getValue() || monsterWallGlint.getValue()).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Float> monsterGlintScale = setting("MonsterGlintScale", 1.0f, 0.1f, 4.0f).des("Size of monster chams glint texture").when(() -> monsterGlint.getValue() || monsterWallGlint.getValue()).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Color> monsterGlintColor = setting("MonsterGlintColor", new Color(new java.awt.Color(125, 50, 255, 100).getRGB())).des("Monster chams glint color").when(() -> monsterGlint.getValue() || monsterWallGlint.getValue()).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    public Setting<Boolean> monsterBypassArmor = setting("MonsterThroughArmor", false).des("Render monster chams through armor").whenTrue(monsterWall).whenFalse(monsterWallEffect).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    public Setting<Boolean> monsterBypassArmorWall = setting("MonsterThroughArmorWall", false).des("Render monster chams through armor only through wall").whenTrue(monsterWall).whenFalse(monsterWallEffect).whenTrue(monsterBypassArmor).whenTrue(monsters).whenAtMode(page, Page.Monsters);
    Setting<Color> monsterColor = setting("MonsterColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).des("Monster chams color").whenTrue(monsters).whenAtMode(page, Page.Monsters);

    public Setting<Boolean> animals = setting("Animals", false).des("Render animal chams").whenAtMode(page, Page.Animals);
    public Setting<Boolean> animalWall = setting("AnimalWalls", true).des("Render animals through wall").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalCancelVanillaRender = setting("AnimalNoVanillaRender", false).des("Cancels normal minecraft animal rendering").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalTexture = setting("AnimalTexture", false).des("Render animal texture on chams").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalDepthMask = setting("AnimalDepthMask", false).des("Enable depth mask for animals (stops entity layers and tile entities from rendering through chams)").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalLighting = setting("AnimalLighting", false).des("Render animal chams with lighting").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalCull = setting("AnimalCull", true).des("Don't render sides of animal chams that you can't see").whenTrue(animals).whenAtMode(page, Page.Animals);
    public Setting<Boolean> animalNoHurt = setting("AnimalNoHurt", true).des("Don't render hurt effect when animal is damaged").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalBlend = setting("AnimalBlend", false).des("Use additive blending on animal chams").whenTrue(animals).whenAtMode(page, Page.Animals);
    public Setting<Boolean> animalWallEffect = setting("AnimalWallEffect", false).des("Render different chams when animal is blocked by a wall").whenTrue(animals).whenTrue(animalWall).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalWallTexture = setting("AnimalWallTexture", false).des("Render texture on animal chams behind walls").whenTrue(animalWallEffect).whenTrue(animals).whenTrue(animalWall).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalWallBlend = setting("AnimalWallBlend", false).des("Use additive blending for animal chams behind walls").whenTrue(animalWallEffect).whenTrue(animals).whenTrue(animalWall).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalWallGlint = setting("AnimalWallGlint", false).des("Render glint texture on animal chams behind walls").whenTrue(animalWallEffect).whenTrue(animals).whenTrue(animalWall).whenAtMode(page, Page.Animals);
    Setting<Color> animalWallColor = setting("AnimalWallColor", new Color(new java.awt.Color(100, 100, 100, 100).getRGB())).des("Animal chams color behind walls").whenTrue(animalWallEffect).whenTrue(animalWall).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalCrowdAlpha = setting("AnimalCrowdAlpha", false).des("Reduce alpha of animal chams when close to you").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Float> animalCrowdAlphaRadius = setting("AnimalCrowdAlphaDist", 1.0f, 0.5f, 4.0f).des("Distance to start reducing alpha of animal chams close to you").whenTrue(animalCrowdAlpha).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Float> animalCrowdEndAlpha = setting("AnimalCrowdEndAlpha", 0.5f, 0.0f, 1.0f).des("Percentage of alpha when animal chams are close to you").whenTrue(animalCrowdAlpha).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalGlint = setting("AnimalGlint", false).des("Render glint texture on animal chams").whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<GlintMode> animalGlintMode = setting("AnimalGlintMode", GlintMode.Gradient).des("Texture of animal chams glint").when(() -> animalGlint.getValue() || animalWallGlint.getValue()).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Boolean> animalGlintMove = setting("AnimalGlintMove", false).des("Animal chams glint move").when(() -> animalGlint.getValue() || animalWallGlint.getValue()).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Float> animalGlintMoveSpeed = setting("AnimalGlintMoveSpeed", 0.4f, 0.1f, 1.0f).des("Animal chams glint move speed").whenTrue(animalGlintMove).when(() -> animalGlint.getValue() || animalWallGlint.getValue()).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Float> animalGlintScale = setting("AnimalGlintScale", 1.0f, 0.1f, 4.0f).des("Size of animal chams glint texture").when(() -> animalGlint.getValue() || animalWallGlint.getValue()).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Color> animalGlintColor = setting("AnimalGlintColor", new Color(new java.awt.Color(125, 50, 255, 100).getRGB())).des("Animal chams glint color").when(() -> animalGlint.getValue() || animalWallGlint.getValue()).whenTrue(animals).whenAtMode(page, Page.Animals);
    Setting<Color> animalColor = setting("AnimalColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).des("Animal chams color").whenTrue(animals).whenAtMode(page, Page.Animals);

    public Setting<Boolean> crystals = setting("Crystals", false).des("Render crystal chams").whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalWall = setting("CrystalWalls", true).des("Render crystals through wall").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalCancelVanillaRender = setting("CrystalNoVanillaRender", false).des("Cancels normal minecraft crystal rendering").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalTexture = setting("CrystalTexture", false).des("Render crystal texture on chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalDepthMask = setting("CrystalDepthMask", false).des("Enable depth mask for crystals (stops entity layers and tile entities from rendering through chams)").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalLighting = setting("CrystalLighting", false).des("Render crystal chams with lighting").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalCull = setting("CrystalCull", true).des("Don't render sides of crystal chams that you can't see").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalBlend = setting("CrystalBlend", false).des("Use additive blending on crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalWallEffect = setting("CrystalWallEffect", false).des("Render different chams when crystal is blocked by a wall").whenTrue(crystals).whenTrue(crystalWall).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalWallTexture = setting("CrystalWallTexture", false).des("Render texture on crystal chams behind walls").whenTrue(crystalWallEffect).whenTrue(crystals).whenTrue(crystalWall).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalWallBlend = setting("CrystalWallBlend", false).des("Use additive blending for crystal chams behind walls").whenTrue(crystalWallEffect).whenTrue(crystals).whenTrue(crystalWall).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalWallGlint = setting("CrystalWallGlint", false).des("Render glint texture on crystal chams behind walls").whenTrue(crystalWallEffect).whenTrue(crystals).whenTrue(crystalWall).whenAtMode(page, Page.Crystals);
    public Setting<Color> crystalWallColor = setting("CrystalWallColor", new Color(new java.awt.Color(100, 100, 100, 100).getRGB())).des("Crystal chams color behind walls").whenTrue(crystalWallEffect).whenTrue(crystalWall).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalCrowdAlpha = setting("CrystalCrowdAlpha", false).des("Reduce alpha of crystal chams when close to you").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalCrowdAlphaRadius = setting("CrystalCrowdAlphaDist", 1.0f, 0.5f, 4.0f).des("Distance to start reducing alpha of crystal chams close to you").whenTrue(crystalCrowdAlpha).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalCrowdEndAlpha = setting("CrystalCrowdEndAlpha", 0.5f, 0.0f, 1.0f).des("Percentage of alpha when crystal chams are close to you").whenTrue(crystalCrowdAlpha).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalGlint = setting("CrystalGlint", false).des("Render glint texture on crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<GlintMode> crystalGlintMode = setting("CrystalGlintMode", GlintMode.Gradient).des("Texture of crystal chams glint").when(() -> crystalGlint.getValue() || crystalWallGlint.getValue()).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalGlintMove = setting("CrystalGlintMove", false).des("Crystal chams glint move").when(() -> crystalGlint.getValue() || crystalWallGlint.getValue()).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalGlintMoveSpeed = setting("CrystalGlintMoveSpeed", 0.4f, 0.1f, 1.0f).des("Crystal chams glint move speed").whenTrue(crystalGlintMove).when(() -> crystalGlint.getValue() || crystalWallGlint.getValue()).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalGlintScale = setting("CrystalGlintScale", 1.0f, 0.1f, 4.0f).des("Size of crystal chams glint texture").when(() -> crystalGlint.getValue() || crystalWallGlint.getValue()).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Color> crystalGlintColor = setting("CrystalGlintColor", new Color(new java.awt.Color(125, 50, 255, 100).getRGB())).des("Crystal chams glint color").when(() -> crystalGlint.getValue() || crystalWallGlint.getValue()).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalOneGlass = setting("CrystalOneGlass", false).des("Only render one glass cube around crystal").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalYOffset = setting("CrystalYOffset", 0.0f, 0.0f, 5.0f).des("Y offset of crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalBobModify = setting("CrystalBobModify", false).des("Modify bob height of crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalBob = setting("CrystalBob", 1.0f, 0.0f, 2.0f).des("Bob height of crystal chams").whenTrue(crystalBobModify).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalSpinModify = setting("CrystalSpinModify", false).des("Modify spin speed of crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalSpinSpeed = setting("CrystalSpinSpeed", 1.0f, 0.0f, 4.0f).des("Speed of crystal chams spin").whenTrue(crystalSpinModify).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Boolean> crystalScaleModify = setting("CrystalScaleModify", false).des("Modify size of crystal chams").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Float> crystalScale = setting("CrystalScale", 1.0f, 0.0f, 2.0f).des("Size of crystal chams").whenTrue(crystalScaleModify).whenTrue(crystals).whenAtMode(page, Page.Crystals);
    public Setting<Color> crystalColor = setting("CrystalColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).des("Crystal chams color").whenTrue(crystals).whenAtMode(page, Page.Crystals);
    //see MixinRenderEnderCrystal && MixinModelEnderCrystal (for CrystalOneGlass)

    public Setting<Boolean> items = setting("Items", false).des("Render item chams").whenAtMode(page, Page.Items);
    public Setting<Boolean> itemsCancelVanillaRender = setting("ItemsNoVanillaRender", false).whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Boolean> itemsRangeLimit = setting("ItemsRangeLimit", false).des("Item chams limit range").whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Float> itemsRange = setting("ItemsRange", 30.0f, 10.0f, 64.0f).des("Item chams range").whenTrue(itemsRangeLimit).whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Boolean> itemTexture = setting("ItemTexture", false).des("Render item texture on chams").whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Boolean> itemLighting = setting("ItemLighting", false).des("Render item chams with lighting").whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Boolean> itemBlend = setting("ItemBlend", false).des("Use additive blending on item chams").whenTrue(items).whenAtMode(page, Page.Items);
    public Setting<Color> itemColor = setting("ItemColor", new Color(new java.awt.Color(255, 255, 255, 255).getRGB())).des("Item chams color").whenTrue(items).whenAtMode(page, Page.Items);
    //see MixinRenderEntityItem

    public Chams() {
        instance = this;
        repeatUnits.add(noHurt);
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

    RepeatUnit noHurt = new RepeatUnit(() -> 1, () -> {
        if (mc.world == null) return;
        
        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
            if ((entity instanceof EntityPlayer && playerNoHurt.getValue() && players.getValue()) || ((EntityUtil.isEntityMonster(entity)) && monsterNoHurt.getValue() && monsters.getValue()) || ((EntityUtil.isEntityAnimal(entity)) && animalNoHurt.getValue() && animals.getValue()))
                ((EntityLivingBase) entity).hurtTime = 0;
        }
        
    });

    @Override
    public void onRenderTick() {
        if (!(playerNoHurt.getValue() && players.getValue() || monsterNoHurt.getValue() && monsters.getValue() || animalNoHurt.getValue() && animals.getValue())) {
            repeatUnits.forEach(RepeatUnit::suspend);
        }
        else {
            repeatUnits.forEach(RepeatUnit::resume);
        }
    }

    @Override
    public void onEnable() {
        repeatUnits.forEach(RepeatUnit::resume);
    }

    @Override
    public void onDisable() {
        repeatUnits.forEach(RepeatUnit::suspend);
    }

    @Listener
    public void renderEntity(RenderEntityEvent event) {
        if (RenderHelper.isInViewFrustrum(event.entityIn) && (!ignoreInvisible.getValue() || !event.entityIn.isInvisible())) {

            if (event.entityIn instanceof EntityPlayer && players.getValue()) {
                if (ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetPlayers.getValue() && ESP.INSTANCE.espModePlayers.getValue() == ESP.Mode.Outline)
                    return;

                if (event.entityIn == mc.player && !self.getValue()) return;
                if (event.entityIn != mc.player && !otherPlayers.getValue()) {
                    if (playerCrowdAlpha.getValue()) {
                        float alphaCrowdFactor = 1.0f;
                        float radiusSq = playerCrowdAlphaRadius.getValue() * playerCrowdAlphaRadius.getValue();
                        alphaCrowdFactor = MathUtilFuckYou.linearInterp(1.0f, playerCrowdEndAlpha.getValue(),
                                MathUtilFuckYou.clamp((radiusSq - (float) MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), event.entityIn.getPositionVector())) / radiusSq, 0.0f, 1.0f) * 300.0f);
                        GL11.glColor4f(1, 1, 1, alphaCrowdFactor);
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        GlStateManager.alphaFunc(516, 0.003921569F);
                    }
                    return;
                }

                Color glintColor = new Color(0);
                if (playerGlint.getValue()) {
                    if (event.entityIn == mc.player) {
                        glintColor = selfGlintColor.getValue();
                    } else {
                            if (FriendManager.isFriend(event.entityIn)) glintColor = friendGlintColor.getValue();
                            else if (EnemyManager.isEnemy(event.entityIn)) glintColor = enemyGlintColor.getValue();
                            else glintColor = playerGlintColor.getValue();
                    }
                }
                renderChams(event, players, playerTexture, playerLighting, playerWall, playerCull, playerBlend, playerCrowdAlpha, playerCrowdAlphaRadius, playerCrowdEndAlpha, playerWallEffect, playerWallTexture, playerWallBlend, playerWallGlint, playerBypassArmor.getValue(), playerBypassArmorWall.getValue(), playerDepthMask.getValue(), playerGlint.getValue(), playerGlintMode.getValue(), playerGlintMove.getValue(), playerGlintMoveSpeed.getValue(), playerGlintScale.getValue(), glintColor);
            }

            if ((EntityUtil.isEntityMonster(event.entityIn)) && monsters.getValue() && !(ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetMonsters.getValue() && ESP.INSTANCE.espModeMonsters.getValue() == ESP.Mode.Outline))
                renderChams(event, monsters, monsterTexture, monsterLighting, monsterWall, monsterCull, monsterBlend, monsterCrowdAlpha, monsterCrowdAlphaRadius, monsterCrowdEndAlpha, monsterWallEffect, monsterWallTexture, monsterWallBlend, monsterWallGlint, monsterBypassArmor.getValue(), monsterBypassArmorWall.getValue(), monsterDepthMask.getValue(), monsterGlint.getValue(), monsterGlintMode.getValue(), monsterGlintMove.getValue(), monsterGlintMoveSpeed.getValue(), monsterGlintScale.getValue(), monsterGlintColor.getValue());
            if ((EntityUtil.isEntityAnimal(event.entityIn)) && animals.getValue() && !(ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetAnimals.getValue() && ESP.INSTANCE.espModeAnimals.getValue() == ESP.Mode.Outline))
                renderChams(event, animals, animalTexture, animalLighting, animalWall, animalCull, animalBlend, animalCrowdAlpha, animalCrowdAlphaRadius, animalCrowdEndAlpha, animalWallEffect, animalWallTexture, animalWallBlend, animalWallGlint, true, false, animalDepthMask.getValue(), animalGlint.getValue(), animalGlintMode.getValue(), animalGlintMove.getValue(), animalGlintMoveSpeed.getValue(), animalGlintScale.getValue(), animalGlintColor.getValue());
        }
    }

    @Listener
    public void cancelEntity(RenderEntityInvokeEvent event) {
        if (RenderHelper.isInViewFrustrum(event.entityIn)) {
            if (event.entityIn instanceof EntityPlayer && players.getValue()) {

                if (ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetPlayers.getValue() && ESP.INSTANCE.espModePlayers.getValue() == ESP.Mode.Outline)
                    return;

                if (event.entityIn == mc.player && !self.getValue()) return;
                if (event.entityIn != mc.player && !otherPlayers.getValue()) return;

                chamsCancelRender(event, playerCancelVanillaRender);
            }
            if ((EntityUtil.isEntityMonster(event.entityIn)) && monsters.getValue() && !(ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetMonsters.getValue() && ESP.INSTANCE.espModeMonsters.getValue() == ESP.Mode.Outline))
                chamsCancelRender(event, monsterCancelVanillaRender);
            if ((EntityUtil.isEntityAnimal(event.entityIn)) && animals.getValue() && !(ModuleManager.getModule(ESP.class).isEnabled() && ESP.INSTANCE.outlineFlag && ESP.INSTANCE.espTargetAnimals.getValue() && ESP.INSTANCE.espModeAnimals.getValue() == ESP.Mode.Outline))
                chamsCancelRender(event, animalCancelVanillaRender);
        }
    }

    private void renderChams(RenderEntityEvent event, Setting<Boolean> settingTarget, Setting<Boolean> settingTexture, Setting<Boolean> settingLighting, Setting<Boolean> settingWalls, Setting<Boolean> settingCull, Setting<Boolean> settingBlend, Setting<Boolean> settingCrowdAlpha, Setting<Float> settingCrowdAlphaStartRadius, Setting<Float> settingCrowdEndAlpha, Setting<Boolean> settingWallTarget, Setting<Boolean> settingWallTexture, Setting<Boolean> settingWallBlend, Setting<Boolean> settingWallGlint, boolean throughArmor, boolean throughArmorWall, boolean depthMask, boolean glint, GlintMode glintMode, boolean glintMove, float glintMoveSpeed, float glintScale, Color glintColor) {
        float alphaCrowdFactor = 1.0f;
        if (settingCrowdAlpha.getValue() && event.entityIn != mc.player) {
            float radiusSq = settingCrowdAlphaStartRadius.getValue() * settingCrowdAlphaStartRadius.getValue();
            alphaCrowdFactor = MathUtilFuckYou.linearInterp(1.0f, settingCrowdEndAlpha.getValue(),
                    MathUtilFuckYou.clamp((radiusSq - (float) MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), event.entityIn.getPositionVector())) / radiusSq, 0.0f, 1.0f) * 300.0f);
        }
        java.awt.Color color;
        if (settingTarget == players) {
            if (event.entityIn == mc.player) color = selfColor.getValue().getColorColor();
            else {
                if (FriendManager.isFriend(event.entityIn)) color = friendColor.getValue().getColorColor();
                else if (EnemyManager.isEnemy(event.entityIn)) color = enemyColor.getValue().getColorColor();
                else color = playerColor.getValue().getColorColor();
            }
        }
        else if (settingTarget == monsters) color = monsterColor.getValue().getColorColor();
        else if (settingTarget == animals) color = animalColor.getValue().getColorColor();
        else color = itemColor.getValue().getColorColor();


        int alpha;
        if (settingTarget == players) {
            if (event.entityIn == mc.player) alpha = selfColor.getValue().getAlpha();
            else {
                if (FriendManager.isFriend(event.entityIn)) alpha = friendColor.getValue().getAlpha();
                else if (EnemyManager.isEnemy(event.entityIn)) alpha = enemyColor.getValue().getAlpha();
                else alpha = playerColor.getValue().getAlpha();
            }
        }
        else if (settingTarget == monsters) alpha = monsterColor.getValue().getAlpha();
        else if (settingTarget == animals) alpha = animalColor.getValue().getAlpha();
        else alpha = itemColor.getValue().getAlpha();

        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glEnable(GL_POLYGON_SMOOTH);
        GL11.glEnable(GL_BLEND);

        if (settingBlend.getValue()) GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);
        else GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (settingCull.getValue()) GlStateManager.enableCull();
        else GlStateManager.disableCull();

        GlStateManager.disableAlpha();
        GlStateManager.depthMask(depthMask);

        if (settingWalls.getValue() && throughArmor && !settingWallTarget.getValue()) {
            GL11.glDepthRange(0.0, 0.01);

            if (throughArmorWall)
                GlStateManager.depthMask(false);
        }

        if (settingLighting.getValue()) GL11.glEnable(GL_LIGHTING);
        else GL11.glDisable(GL_LIGHTING);
        GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        if (settingTexture.getValue()) GL11.glEnable(GL_TEXTURE_2D);
        else GL11.glDisable(GL_TEXTURE_2D);

        if (event.entityIn == mc.player) {
            if (selfTexture.getValue()) GL11.glEnable(GL_TEXTURE_2D);
            else GL11.glDisable(GL_TEXTURE_2D);
        }

        GlStateManager.disableAlpha();
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, (alpha / 255.0f) * alphaCrowdFactor);
        event.modelBase.render(event.entityIn, event.limbSwing, event.limbSwingAmount, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
        GlStateManager.enableAlpha();

        if (settingWalls.getValue() && throughArmor && throughArmorWall && !settingWallTarget.getValue()) {
            GlStateManager.depthMask(true);
            GlStateManager.disableAlpha();
            GL11.glDepthRange(0.0, 1.0);
            event.modelBase.render(event.entityIn, event.limbSwing, event.limbSwingAmount, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
            GlStateManager.enableAlpha();
        }

        if (settingWallTarget.getValue() && settingWalls.getValue())
            renderWallEffect(event, alphaCrowdFactor, settingWallTarget, settingWallTexture, settingWallBlend, settingWallGlint, glintMode, glintMove, glintMoveSpeed, glintScale);

        if (glint) {
            if (settingWallTarget.getValue())
                GlStateManager.depthMask(true);
            renderGlint(event, settingWalls.getValue(), alphaCrowdFactor, throughArmor, throughArmorWall, glintMode, glintMove, glintMoveSpeed, glintScale, glintColor, settingWallTarget);
        }

        if (settingWalls.getValue() && throughArmor && !settingWallTarget.getValue()) GL11.glDepthRange(0.0, 1.0);

        if (settingBlend.getValue()) GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        SpartanTessellator.releaseGL();
        GL11.glEnable(GL_BLEND);
        GL11.glEnable(GL_LIGHTING);

        if (settingWallTarget.getValue() && settingWalls.getValue()) {
            GL11.glDepthFunc(GL_LEQUAL);
        }
        GL11.glEnable(GL_TEXTURE_2D);
    }

    private void renderWallEffect(RenderEntityEvent event, float alphaCrowdFactor, Setting<Boolean> settingWallTarget, Setting<Boolean> settingWallTexture, Setting<Boolean> settingWallBlend, Setting<Boolean> settingWallGlint, GlintMode glintMode, boolean glintMove, float glintMoveSpeed, float glintScale) {
        java.awt.Color color;
        if (settingWallTarget == playerWallEffect) {
            if (FriendManager.isFriend(event.entityIn)) color = friendWallColor.getValue().getColorColor();
            else if (EnemyManager.isEnemy(event.entityIn)) color = enemyWallColor.getValue().getColorColor();
            else color = playerWallColor.getValue().getColorColor();
        }
        else if (settingWallTarget == monsterWallEffect) color = monsterWallColor.getValue().getColorColor();
        else if (settingWallTarget == animalWallEffect) color = animalWallColor.getValue().getColorColor();
        else color = itemColor.getValue().getColorColor();

        Color color2;
        if (settingWallTarget == playerWallEffect) {
            if (FriendManager.isFriend(event.entityIn)) color2 = friendWallColor.getValue();
            else if (EnemyManager.isEnemy(event.entityIn)) color2 = enemyWallColor.getValue();
            else color2 = playerWallColor.getValue();
        }
        else if (settingWallTarget == monsterWallEffect) color2 = monsterWallColor.getValue();
        else if (settingWallTarget == animalWallEffect) color2 = animalWallColor.getValue();
        else color2 = itemColor.getValue();


        int alpha;
        if (settingWallTarget == playerWallEffect) {
            if (FriendManager.isFriend(event.entityIn)) alpha = friendWallColor.getValue().getAlpha();
            else if (EnemyManager.isEnemy(event.entityIn)) alpha = enemyWallColor.getValue().getAlpha();
            else alpha = playerWallColor.getValue().getAlpha();
        }
        else if (settingWallTarget == monsterWallEffect) alpha = monsterWallColor.getValue().getAlpha();
        else if (settingWallTarget == animalWallEffect) alpha = animalWallColor.getValue().getAlpha();
        else alpha = itemColor.getValue().getAlpha();

        GlStateManager.depthMask(false);

        if (settingWallTexture.getValue()) GL11.glEnable(GL_TEXTURE_2D);
        else GL11.glDisable(GL_TEXTURE_2D);

        if (settingWallBlend.getValue()) GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);
        else GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDepthFunc(GL_GREATER);

        if (settingWallGlint.getValue())
            renderGlint(event, false, alphaCrowdFactor, false, false, glintMode, glintMove, glintMoveSpeed, glintScale, color2, settingWallTarget);
        else {
            GlStateManager.disableAlpha();
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, (alpha / 255.0f) * alphaCrowdFactor);
            event.modelBase.render(event.entityIn, event.limbSwing, event.limbSwingAmount, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
            GlStateManager.enableAlpha();
        }

        GL11.glDepthFunc(GL_EQUAL);
    }

    private void renderGlint(RenderEntityEvent event, boolean walls, float alphaCrowdFactor, boolean throughArmor, boolean throughArmorWall, GlintMode glintMode, boolean glintMove, float glintMoveSpeed, float glintScale, Color glintColor, Setting<Boolean> settingWallTarget) {
        if (event.entityIn == mc.player && self.getValue() && !selfGlint.getValue()) return;

        ResourceLocation glintTexture = null;

        switch (glintMode) {
            case LoadedPack: {
                glintTexture = loadedTexturePackGlint;
                break;
            }

            case Gradient: {
                glintTexture = gradientGlint;
                break;
            }

            case Lightning: {
                glintTexture = lightningGlint;
                break;
            }

            case Swirls: {
                glintTexture = swirlsGlint;
                break;
            }

            case Lines: {
                glintTexture = linesGlint;
                break;
            }
        }

        if (glintTexture != null) mc.getTextureManager().bindTexture(glintTexture);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL_LIGHTING);
        GL11.glEnable(GL_BLEND);

        //alpha seems to be broken somehow so ig this would work :shrug:
        float alpha = (glintColor.getAlpha() / 255.0f) * alphaCrowdFactor;
        GL11.glColor4f((glintColor.getColorColor().getRed() / 255.0f) * alpha, (glintColor.getColorColor().getGreen() / 255.0f) * alpha, (glintColor.getColorColor().getBlue() / 255.0f) * alpha, 1.0f);

        GL11.glBlendFunc(GL_SRC_COLOR, GL_ONE);

        if (walls && throughArmor && throughArmorWall && !settingWallTarget.getValue()) {
            GL11.glDepthRange(0.0, 0.01);
            GlStateManager.depthMask(false);
        }

        doRenderGlint(event, glintMove, glintMoveSpeed, glintScale);

        if (walls && throughArmor && throughArmorWall && !settingWallTarget.getValue()) {
            GlStateManager.depthMask(true);
            GL11.glDepthRange(0.0, 1.0);

            doRenderGlint(event, glintMove, glintMoveSpeed, glintScale);
        }

        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void doRenderGlint(RenderEntityEvent event, boolean glintMove, float glintMoveSpeed, float glintScale) {
        for (int i = 0; i < 2; ++i) {
            GL11.glMatrixMode(GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glScalef(glintScale, glintScale, glintScale);
            if (glintMove) {
                GL11.glTranslatef(event.entityIn.ticksExisted * 0.01f * glintMoveSpeed, 0.0f, 0.0f);
            }
            GL11.glRotatef(30.0f - (i * 60.0f), 0.0f, 0.0f, 1.0f);
            GL11.glMatrixMode(GL_MODELVIEW);

            event.modelBase.render(event.entityIn, event.limbSwing, event.limbSwingAmount, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
        }
        GL11.glMatrixMode(GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL_MODELVIEW);
    }

    private void chamsCancelRender(RenderEntityInvokeEvent event, Setting<Boolean> settingCancel) {
        if (settingCancel.getValue()) event.cancel();
    }

    enum Page {
        Players,
        Monsters,
        Animals,
        Crystals,
        Items
    }

    public enum GlintMode {
        LoadedPack,
        Gradient,
        Lightning,
        Swirls,
        Lines
    }
}
