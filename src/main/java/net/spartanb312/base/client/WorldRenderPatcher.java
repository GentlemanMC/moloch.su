package net.spartanb312.base.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.event.events.render.RenderEvent;
import net.spartanb312.base.event.events.render.RenderWorldEvent;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.notification.NotificationManager;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.graphics.SpartanTessellator;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

import static net.spartanb312.base.utils.ItemUtils.mc;
import static org.lwjgl.opengl.GL11.*;

public class WorldRenderPatcher {

    public static WorldRenderPatcher INSTANCE = new WorldRenderPatcher();

    public void patch(RenderWorldEvent event) {
        mc.profiler.startSection("moloch.su");

        mc.profiler.startSection("setup");

        SpartanTessellator.prepareGL();
        GlStateManager.shadeModel(GL_SMOOTH);
        GL11.glDisable(GL_LIGHTING);
        GL11.glEnable(GL_CULL_FACE);

        Vec3d renderPos = getInterpolatedPos(Objects.requireNonNull(mc.getRenderViewEntity()), event.getPartialTicks());

        RenderEvent e = new RenderEvent(SpartanTessellator.INSTANCE, renderPos);
        e.resetTranslation();

        RenderEvent.Extra1 e1 = new RenderEvent.Extra1(SpartanTessellator.INSTANCE, renderPos);
        e1.resetTranslation();

        mc.profiler.endSection();

        try {
            BaseCenter.MODULE_BUS.getModules().forEach(it -> it.onRenderWorld(e));

            ModuleManager.getModules().stream()
                    .filter(Module::shouldPersistRender)
                    .forEach(it -> it.onRenderWorld(e));
        }
        catch (Exception exception) {
            NotificationManager.fatal("Error while running onRenderWorld!");
            exception.printStackTrace();
        }

        BaseCenter.EVENT_BUS.post(e1);

        mc.profiler.startSection("release");
        GlStateManager.glLineWidth(1f);
        GlStateManager.shadeModel(GL_FLAT);
        SpartanTessellator.releaseGL();
        GL11.glEnable(GL_LIGHTING);

        mc.profiler.endSection();
    }

    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(EntityUtil.getInterpolatedAmount(entity, ticks));
    }

}
