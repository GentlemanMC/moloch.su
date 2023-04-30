package me.afterdarkness.moloch.module.modules.other;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import me.afterdarkness.moloch.event.events.player.BlockInteractionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;

import javax.annotation.Nullable;
import java.util.List;

@Parallel(runnable = true)
@ModuleInfo(name = "NoEntityBlock", category = Category.OTHER, description = "Allows you to interact with blocks through entities")
public class NoEntityBlock extends Module {

    Setting<Page> page = setting("Page", Page.General);
    Setting<Boolean> onlyBlocks = setting("OnlyBlocks", true).des("Only interact with blocks through entities when you can hit a block").whenAtMode(page, Page.General);
    Setting<Boolean> whenNotSneak = setting("WhenNotSneak", false).des("Be able to interact with any entity when sneaking").whenAtMode(page, Page.General);
    Setting<Boolean> everything = setting("Everything", false).des("Interact with blocks through entities while anything is in your hands").whenAtMode(page, Page.General);
    Setting<Boolean> pickaxe = setting("Pickaxe", true).des("Interact with blocks through entities while holding pickaxe").whenFalse(everything).whenAtMode(page, Page.General);
    Setting<Boolean> sword = setting("Sword", false).des("Interact with blocks through entities while holding sword").whenFalse(everything).whenAtMode(page, Page.General);
    Setting<Boolean> gapple = setting("Gapple", false).des("Interact with blocks through entities while holding gapple").whenFalse(everything).whenAtMode(page, Page.General);
    Setting<Boolean> crystal = setting("Crystal", false).des("Interact with blocks through entities while holding crystal").whenFalse(everything).whenAtMode(page, Page.General);

    Setting<Boolean> allEntities = setting("AllEntities", true).des("Interact with blocks through any entities when holding the right item").whenAtMode(page, Page.Entities);
    Setting<Boolean> players = setting("Players", false).des("Interact with blocks through players").whenFalse(allEntities).whenAtMode(page, Page.Entities);
    Setting<Boolean> monsters = setting("Monsters", false).des("Interact with blocks through monsters").whenFalse(allEntities).whenAtMode(page, Page.Entities);
    Setting<Boolean> animals = setting("Animals", false).des("Interact with blocks through animals").whenFalse(allEntities).whenAtMode(page, Page.Entities);
    Setting<Boolean> crystals = setting("Crystals", false).des("Interact with blocks through crystals").whenFalse(allEntities).whenAtMode(page, Page.Entities);
    Setting<Boolean> vehicles = setting("Vehicles", false).des("Interact with blocks through vehicles").whenFalse(allEntities).whenAtMode(page, Page.Entities);

    @Listener
    public void onBlockInteract(BlockInteractionEvent event) {
        if (whenNotSneak.getValue() && mc.player.isSneaking()) return;

        RayTraceResult mouseObject = mc.objectMouseOver;
        Item heldItem = mc.player.getHeldItemMainhand().getItem();

        if (entityCheck() && (!onlyBlocks.getValue() || mouseObject != null && mouseObject.typeOfHit == RayTraceResult.Type.BLOCK)) {
            if (everything.getValue() || ((pickaxe.getValue() && heldItem instanceof ItemPickaxe) || (sword.getValue() && heldItem instanceof ItemSword) || (gapple.getValue() && heldItem == Items.GOLDEN_APPLE) || (crystal.getValue() && heldItem == Items.END_CRYSTAL))) {
                event.cancel();
            }
        }
    }

    private boolean entityCheck() {
        Entity hoveredEntity = hoveredEntity();

        if (allEntities.getValue() || hoveredEntity == null)
            return true;

        return (hoveredEntity instanceof EntityPlayer && players.getValue()) || (EntityUtil.isEntityMonster(hoveredEntity) && monsters.getValue()) || (EntityUtil.isEntityAnimal(hoveredEntity) && animals.getValue()) || (hoveredEntity instanceof EntityEnderCrystal && crystals.getValue()) || (EntityUtil.isEntityVehicle(hoveredEntity) && vehicles.getValue());
    }

    //from mc code
    private Entity hoveredEntity() {
        Entity entity = mc.getRenderViewEntity();
        Vec3d vec3d = entity.getPositionEyes(mc.getRenderPartialTicks());
        double d0 = mc.playerController.getBlockReachDistance();
        Vec3d vec3d1 = entity.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
        List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        }));

        for (Entity entity1 : list) {
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double) entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (axisalignedbb.contains(vec3d)) {
                if (d0 >= 0.0D) {
                    return entity1;
                }
            } else if (raytraceresult != null) {
                double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                if (d3 < d0 || d0 == 0.0D) {
                    if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                        if (d0 == 0.0D) {
                            return entity1;
                        }
                    } else {
                        return entity1;
                    }
                }
            }
        }
        return null;
    }

    enum Page {
        General,
        Entities
    }
}
