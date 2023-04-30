package me.afterdarkness.moloch.module.modules.visuals;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.afterdarkness.moloch.core.common.Color;
import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.task.VoidTask;
import net.spartanb312.base.core.concurrent.utils.ThreadUtil;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.graphics.RenderHelper;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Parallel(runnable = true)
@ModuleInfo(name = "Search", category = Category.VISUALS, description = "Highlights whitelisted blocks (may work a bit slowly on low end PCs so be patient lol)", hasCollector = true)
public class Search extends Module {

    Setting<Page> page = setting("Page", Page.General);
    Setting<Integer> updateDelay = setting("UpdateDelay", 50, 1, 2000).des("Delay between updating holes in milliseconds").whenAtMode(page, Page.General);
    Setting<String> blocksInput = setting("BlocksInput", "", true, new ArrayList<>(), false).des("Type in blocks to higlight here (Example: IRON_ORE)").whenAtMode(page, Page.General);
    Setting<Float> range = setting("Range", 100.0f, 0.0f, 256.0f).des("Radius of sphere to begin marking blocks to highlight (may fry your PC if you turn it up too far kek)").whenAtMode(page, Page.General);
    Setting<Integer> maxBlocks = setting("MaxBlocks", 400, 0, 3000).des("Maximum amount of blocks to be rendered at once").whenAtMode(page, Page.General);
    Setting<Boolean> solid = setting("Solid", true).des("Draw a solid box on highlighted block").whenAtMode(page, Page.General);
    Setting<Boolean> lines = setting("Lines", true).des("Draw a wireframe box on highlighted block").whenAtMode(page, Page.General);
    Setting<Boolean> outlineOnly = setting("OutlineOnly", false).des("Only draws lines around the edges of highlighted block").whenTrue(lines).whenAtMode(page, Page.General);
    Setting<Float> linesWidth = setting("LinesWidth", 1.0f, 1.0f, 5.0f).des("Width of wireframe box").whenTrue(lines).whenAtMode(page, Page.General);
    Setting<Boolean> tracer = setting("Tracer", false).des("Draw a line to the highlighted block").whenAtMode(page, Page.General);
    Setting<Float> tracerWidth = setting("TracerWidth", 1.0f, 1.0f, 5.0f).des("Width of tracer").whenTrue(tracer).whenAtMode(page, Page.General);

    Setting<Boolean> customStorageColors = setting("CustomStorageColors", false).des("Colors different storage blocks different colors").whenAtMode(page, Page.Colors);
    Setting<Color> solidColor = setting("SolidColor", new Color(new java.awt.Color(255, 50, 50, 40).getRGB())).whenTrue(solid).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorChest = setting("SolidColorChest", new Color(new java.awt.Color(255, 200, 50, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.CHEST) || isInWhitelist(Blocks.TRAPPED_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorEChest = setting("SolidColorEChest", new Color(new java.awt.Color(125, 50, 255, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.ENDER_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorShulker = setting("SolidColorShulker", new Color(new java.awt.Color(255, 150, 255, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.PURPLE_SHULKER_BOX) || isInWhitelist(Blocks.WHITE_SHULKER_BOX) || isInWhitelist(Blocks.ORANGE_SHULKER_BOX) || isInWhitelist(Blocks.MAGENTA_SHULKER_BOX) || isInWhitelist(Blocks.LIGHT_BLUE_SHULKER_BOX) || isInWhitelist(Blocks.YELLOW_SHULKER_BOX) || isInWhitelist(Blocks.LIME_SHULKER_BOX) || isInWhitelist(Blocks.PINK_SHULKER_BOX) || isInWhitelist(Blocks.GRAY_SHULKER_BOX) || isInWhitelist(Blocks.SILVER_SHULKER_BOX) || isInWhitelist(Blocks.CYAN_SHULKER_BOX) || isInWhitelist(Blocks.BLUE_SHULKER_BOX) || isInWhitelist(Blocks.BROWN_SHULKER_BOX) || isInWhitelist(Blocks.GREEN_SHULKER_BOX) || isInWhitelist(Blocks.RED_SHULKER_BOX) || isInWhitelist(Blocks.BLACK_SHULKER_BOX)).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorHopper = setting("SolidColorHopper", new Color(new java.awt.Color(150, 150, 150, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.HOPPER)).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorFurnace = setting("SolidColorFurnace", new Color(new java.awt.Color(200, 170, 170, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.FURNACE) || isInWhitelist(Blocks.LIT_FURNACE)).whenAtMode(page, Page.Colors);
    Setting<Color> solidColorDispenser = setting("SolidColorDispenser", new Color(new java.awt.Color(150, 200, 150, 40).getRGB())).whenTrue(solid).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.DISPENSER)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColor = setting("LinesColor", new Color(new java.awt.Color(255, 50, 50, 120).getRGB())).whenTrue(lines).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorChest = setting("LinesColorChest", new Color(new java.awt.Color(255, 200, 50, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.CHEST) || isInWhitelist(Blocks.TRAPPED_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorEChest = setting("LinesColorEChest", new Color(new java.awt.Color(125, 50, 255, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.ENDER_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorShulker = setting("LinesColorShulker", new Color(new java.awt.Color(255, 150, 255, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.PURPLE_SHULKER_BOX) || isInWhitelist(Blocks.WHITE_SHULKER_BOX) || isInWhitelist(Blocks.ORANGE_SHULKER_BOX) || isInWhitelist(Blocks.MAGENTA_SHULKER_BOX) || isInWhitelist(Blocks.LIGHT_BLUE_SHULKER_BOX) || isInWhitelist(Blocks.YELLOW_SHULKER_BOX) || isInWhitelist(Blocks.LIME_SHULKER_BOX) || isInWhitelist(Blocks.PINK_SHULKER_BOX) || isInWhitelist(Blocks.GRAY_SHULKER_BOX) || isInWhitelist(Blocks.SILVER_SHULKER_BOX) || isInWhitelist(Blocks.CYAN_SHULKER_BOX) || isInWhitelist(Blocks.BLUE_SHULKER_BOX) || isInWhitelist(Blocks.BROWN_SHULKER_BOX) || isInWhitelist(Blocks.GREEN_SHULKER_BOX) || isInWhitelist(Blocks.RED_SHULKER_BOX) || isInWhitelist(Blocks.BLACK_SHULKER_BOX)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorHopper = setting("LinesColorHopper", new Color(new java.awt.Color(150, 150, 150, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.HOPPER)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorFurnace = setting("LinesColorFurnace", new Color(new java.awt.Color(200, 170, 170, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.FURNACE) || isInWhitelist(Blocks.LIT_FURNACE)).whenAtMode(page, Page.Colors);
    Setting<Color> linesColorDispenser = setting("LinesColorDispenser", new Color(new java.awt.Color(150, 200, 150, 120).getRGB())).whenTrue(lines).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.DISPENSER)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColor = setting("TracerColor", new Color(new java.awt.Color(255, 50, 50, 120).getRGB())).whenTrue(tracer).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorChest = setting("TracerColorChest", new Color(new java.awt.Color(255, 200, 50, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.CHEST) || isInWhitelist(Blocks.TRAPPED_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorEChest = setting("TracerColorEChest", new Color(new java.awt.Color(125, 50, 255, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.ENDER_CHEST)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorShulker = setting("TracerColorShulker", new Color(new java.awt.Color(255, 150, 255, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.PURPLE_SHULKER_BOX) || isInWhitelist(Blocks.WHITE_SHULKER_BOX) || isInWhitelist(Blocks.ORANGE_SHULKER_BOX) || isInWhitelist(Blocks.MAGENTA_SHULKER_BOX) || isInWhitelist(Blocks.LIGHT_BLUE_SHULKER_BOX) || isInWhitelist(Blocks.YELLOW_SHULKER_BOX) || isInWhitelist(Blocks.LIME_SHULKER_BOX) || isInWhitelist(Blocks.PINK_SHULKER_BOX) || isInWhitelist(Blocks.GRAY_SHULKER_BOX) || isInWhitelist(Blocks.SILVER_SHULKER_BOX) || isInWhitelist(Blocks.CYAN_SHULKER_BOX) || isInWhitelist(Blocks.BLUE_SHULKER_BOX) || isInWhitelist(Blocks.BROWN_SHULKER_BOX) || isInWhitelist(Blocks.GREEN_SHULKER_BOX) || isInWhitelist(Blocks.RED_SHULKER_BOX) || isInWhitelist(Blocks.BLACK_SHULKER_BOX)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorHopper = setting("TracerColorHopper", new Color(new java.awt.Color(150, 150, 150, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.HOPPER)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorFurnace = setting("TracerColorFurnace", new Color(new java.awt.Color(200, 170, 170, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.FURNACE) || isInWhitelist(Blocks.LIT_FURNACE)).whenAtMode(page, Page.Colors);
    Setting<Color> tracerColorDispenser = setting("TracerColorDispenser", new Color(new java.awt.Color(150, 200, 150, 120).getRGB())).whenTrue(tracer).whenTrue(customStorageColors).when(() -> isInWhitelist(Blocks.DISPENSER)).whenAtMode(page, Page.Colors);

    public File WHITELISTED_BLOCKS_FILE = new File("moloch.su/config/moloch_Search_Whitelisted_Blocks.json");

    private List<BlockPos> toRenderPos = new ArrayList<>();
    private int prevCacheSize = ((StringSetting) blocksInput).feederList.size();
    private final List<VoidTask> tasks = new ArrayList<>();
    private final Timer updateTimer = new Timer();
    public static Search INSTANCE;

    public Search() {
        INSTANCE = this;
        new Thread(() -> {
            while (true) {
                if (!(mc.world == null || mc.player == null || tasks.isEmpty())) {
                    ConcurrentTaskManager.runBlocking(content -> tasks.forEach(content::launch));
                    tasks.clear();
                }
                ThreadUtil.delay();
            }
        }).start();
        syncWhitelist();
    }

    @Override
    public void onTick() {
        if (updateTimer.passed(updateDelay.getValue()) && tasks.isEmpty()) {
            tasks.add(() -> {
                toRenderPos = BlockUtil.getSphereRounded(EntityUtil.floorEntity(mc.player), range.getValue(), true).stream()
                        .filter(pos -> isInWhitelist(mc.world.getBlockState(pos).getBlock()))
                        .collect(Collectors.toList());

                if (toRenderPos.size() > maxBlocks.getValue()) {
                    toRenderPos = toRenderPos.subList(0, maxBlocks.getValue());
                }
            });

            updateTimer.reset();
        }
    }

    @Override
    public void onDisable() {
        toRenderPos.clear();
    }

    @Override
    public void onRenderWorld(RenderEvent event) {
        if (outlineOnly.getValue()) {
            GL11.glLineWidth(linesWidth.getValue());
            SpartanTessellator.outline1();
            renderBoxes(true, false, true, linesColor.getValue().getColor(), linesColor.getValue().getColor());
            SpartanTessellator.outline2();
            renderBoxes(true, false, true, linesColor.getValue().getColor(), linesColor.getValue().getColor());
            SpartanTessellator.outline3();
            renderBoxes(true, false, true, linesColor.getValue().getColor(), linesColor.getValue().getColor());
            SpartanTessellator.outlineRelease();
        }
        else {
            renderBoxes(solid.getValue(), lines.getValue(), false, solidColor.getValue().getColor(), linesColor.getValue().getColor());
        }

        if (tracer.getValue()) {
            GL11.glPushMatrix();
            int tracerColor = this.tracerColor.getValue().getColor();
            for (BlockPos pos : new ArrayList<>(toRenderPos)) {
                if (customStorageColors.getValue()) {
                    tracerColor = this.tracerColor.getValue().getColor();
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
                        tracerColor = tracerColorChest.getValue().getColor();
                    }
                    else if (block == Blocks.ENDER_CHEST) {
                        tracerColor = tracerColorEChest.getValue().getColor();
                    }
                    else if (block == Blocks.PURPLE_SHULKER_BOX || block == Blocks.WHITE_SHULKER_BOX || block == Blocks.ORANGE_SHULKER_BOX || block == Blocks.MAGENTA_SHULKER_BOX || block == Blocks.LIGHT_BLUE_SHULKER_BOX || block == Blocks.YELLOW_SHULKER_BOX || block == Blocks.LIME_SHULKER_BOX || block == Blocks.PINK_SHULKER_BOX || block == Blocks.GRAY_SHULKER_BOX || block == Blocks.SILVER_SHULKER_BOX || block == Blocks.CYAN_SHULKER_BOX || block == Blocks.BLUE_SHULKER_BOX || block == Blocks.BROWN_SHULKER_BOX || block == Blocks.GREEN_SHULKER_BOX || block == Blocks.RED_SHULKER_BOX || block == Blocks.BLACK_SHULKER_BOX) {
                        tracerColor = tracerColorShulker.getValue().getColor();
                    }
                    else if (block == Blocks.HOPPER) {
                        tracerColor = tracerColorHopper.getValue().getColor();
                    }
                    else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE) {
                        tracerColor = tracerColorFurnace.getValue().getColor();
                    }
                    else if (block == Blocks.DISPENSER) {
                        tracerColor = tracerColorDispenser.getValue().getColor();
                    }
                }

                SpartanTessellator.drawTracer(new Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5), tracerWidth.getValue(), tracerColor);
            }
            GL11.glPopMatrix();
        }
    }

    @Override
    public void onTickCollector() {
        if (((StringSetting) blocksInput).feederList.size() < prevCacheSize) {
            try {
                updateJSon();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove block names from whitelist");
                e.printStackTrace();
            }
        }
        prevCacheSize = ((StringSetting) blocksInput).feederList.size();

        if (!((StringSetting) blocksInput).listening && !Objects.equals(blocksInput.getValue(), "")) {
            writeToWhitelist(blocksInput.getValue());
            blocksInput.setValue("");
        }
    }

    private void renderBoxes(boolean solid, boolean lines, boolean outlineOnly, int solidColor, int linesColor) {
        int solidColor1 = solidColor;
        int linesColor1 = linesColor;
        for (BlockPos pos : new ArrayList<>(toRenderPos)) {
            if (RenderHelper.isInViewFrustrum(mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos))) {
                if (customStorageColors.getValue()) {
                    solidColor = solidColor1;
                    linesColor = linesColor1;
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
                        solidColor = outlineOnly ? linesColorChest.getValue().getColor() : solidColorChest.getValue().getColor();
                        linesColor = linesColorChest.getValue().getColor();
                    }
                    else if (block == Blocks.ENDER_CHEST) {
                        solidColor = outlineOnly ? linesColorEChest.getValue().getColor() : solidColorEChest.getValue().getColor();
                        linesColor = linesColorEChest.getValue().getColor();
                    }
                    else if (block == Blocks.PURPLE_SHULKER_BOX || block == Blocks.WHITE_SHULKER_BOX || block == Blocks.ORANGE_SHULKER_BOX || block == Blocks.MAGENTA_SHULKER_BOX || block == Blocks.LIGHT_BLUE_SHULKER_BOX || block == Blocks.YELLOW_SHULKER_BOX || block == Blocks.LIME_SHULKER_BOX || block == Blocks.PINK_SHULKER_BOX || block == Blocks.GRAY_SHULKER_BOX || block == Blocks.SILVER_SHULKER_BOX || block == Blocks.CYAN_SHULKER_BOX || block == Blocks.BLUE_SHULKER_BOX || block == Blocks.BROWN_SHULKER_BOX || block == Blocks.GREEN_SHULKER_BOX || block == Blocks.RED_SHULKER_BOX || block == Blocks.BLACK_SHULKER_BOX) {
                        solidColor = outlineOnly ? linesColorShulker.getValue().getColor() : solidColorShulker.getValue().getColor();
                        linesColor = linesColorShulker.getValue().getColor();
                    }
                    else if (block == Blocks.HOPPER) {
                        solidColor = outlineOnly ? linesColorHopper.getValue().getColor() : solidColorHopper.getValue().getColor();
                        linesColor = linesColorHopper.getValue().getColor();
                    }
                    else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE) {
                        solidColor = outlineOnly ? linesColorFurnace.getValue().getColor() : solidColorFurnace.getValue().getColor();
                        linesColor = linesColorFurnace.getValue().getColor();
                    }
                    else if (block == Blocks.DISPENSER) {
                        solidColor = outlineOnly ? linesColorDispenser.getValue().getColor() : solidColorDispenser.getValue().getColor();
                        linesColor = linesColorDispenser.getValue().getColor();
                    }
                }

                if (solid) {
                    SpartanTessellator.drawBlockBBFullBox(pos, 1.0f, solidColor);
                }

                if (lines) {
                    SpartanTessellator.drawBlockBBLineBox(pos, 1.0f, linesWidth.getValue(), linesColor);
                }
            }
        }
    }

    private boolean isInWhitelist(Block block) {
        for (String value : ((StringSetting) blocksInput).feederList) {
            if (Block.getBlockFromName(value.toUpperCase()) != null && Block.getBlockFromName(value.toUpperCase()) == block) {
                return true;
            }
        }
        return false;
    }

    private void updateJSon() throws IOException {
        JsonObject json = new JsonObject();

        for (String str : ((StringSetting) blocksInput).feederList) {
            json.addProperty(str, "");
        }

        PrintWriter saveJSon = new PrintWriter(new FileWriter(WHITELISTED_BLOCKS_FILE));
        saveJSon.println((new GsonBuilder().setPrettyPrinting().create()).toJson(json));
        saveJSon.close();
    }

    private void writeToWhitelist(String message) {
        try {
            if (!WHITELISTED_BLOCKS_FILE.exists()) {
                WHITELISTED_BLOCKS_FILE.getParentFile().mkdirs();
                try {
                    WHITELISTED_BLOCKS_FILE.createNewFile();
                } catch (Exception ignored) {}
            }

            ((StringSetting) blocksInput).feederList.remove(message);
            ((StringSetting) blocksInput).feederList.add(message);
            updateJSon();
        }
        catch (Exception e) {
            BaseCenter.log.error("Smt went wrong while trying to save block name to whitelist");
            e.printStackTrace();
        }
    }

    public void syncWhitelist() {
        ((StringSetting) blocksInput).feederList.clear();
        if (WHITELISTED_BLOCKS_FILE.exists()) {
            try {
                BufferedReader loadJson = new BufferedReader(new FileReader(WHITELISTED_BLOCKS_FILE));
                JsonObject json = (JsonObject) (new JsonParser()).parse(loadJson);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    ((StringSetting) blocksInput).feederList.add(entry.getKey());
                }
            }
            catch (IOException e) {
                BaseCenter.log.error("Smt went wrong while loading Search block whitelist");
                e.printStackTrace();
            }
        }
    }

    enum Page {
        General,
        Colors
    }
}
