package me.afterdarkness.moloch.event.events.render;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.spartanb312.base.event.EventCenter;

public class RenderHeldItemEvent extends EventCenter {
    public EntityLivingBase holdingEntity;
    public ItemStack stack;
    public ItemCameraTransforms.TransformType transform;
    public boolean leftHanded;

    public RenderHeldItemEvent(ItemStack stack, EntityLivingBase holdingEntity, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        this.stack = stack;
        this.holdingEntity = holdingEntity;
        this.transform = transform;
        this.leftHanded = leftHanded;
    }
}
