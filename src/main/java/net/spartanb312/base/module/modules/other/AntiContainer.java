package net.spartanb312.base.module.modules.other;

import me.afterdarkness.moloch.event.events.player.BlockInteractionEvent;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.RayTraceResult;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Parallel(runnable = true)
@ModuleInfo(name = "AntiContainer", category = Category.OTHER, description = "Treats containers as normal blocks")
public class AntiContainer extends Module {

    Setting<Boolean> chest = setting("Chest", true);
    Setting<Boolean> enderChest = setting("EnderChest", true);
    Setting<Boolean> trappedChest = setting("TrappedChest", true);
    Setting<Boolean> hopper = setting("Hopper", true);
    Setting<Boolean> dispenser = setting("Dispenser", true);
    Setting<Boolean> furnace = setting("Furnace", true);
    Setting<Boolean> beacon = setting("Beacon", true);
    Setting<Boolean> craftingTable = setting("CraftingTable", true);
    Setting<Boolean> anvil = setting("Anvil", true);
    Setting<Boolean> enchantingTable = setting("EnchantingTable", true);
    Setting<Boolean> brewingStand = setting("BrewingStand", true);
    Setting<Boolean> shulkerBox = setting("ShulkerBox", true);

    @Listener
    public void processRightClickBlockPre(BlockInteractionEvent.RightClickPre event) {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) return;
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if (check(pos) && !mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }
    }

    @Listener
    public void processRightClickBlockPost(BlockInteractionEvent.RightClickPost event) {
        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
            mc.playerController.updateController();
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            mc.player.setSneaking(false);
            mc.playerController.updateController();
        }
    }

    public boolean check(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return ((block == Blocks.CHEST && chest.getValue())
                || (block == Blocks.ENDER_CHEST && enderChest.getValue())
                || (block == Blocks.TRAPPED_CHEST && trappedChest.getValue())
                || (block == Blocks.HOPPER && hopper.getValue())
                || (block == Blocks.DISPENSER && dispenser.getValue())
                || (block == Blocks.FURNACE && furnace.getValue())
                || (block == Blocks.BEACON && beacon.getValue())
                || (block == Blocks.CRAFTING_TABLE && craftingTable.getValue())
                || (block == Blocks.ANVIL && anvil.getValue())
                || (block == Blocks.ENCHANTING_TABLE && enchantingTable.getValue())
                || (block == Blocks.BREWING_STAND && brewingStand.getValue())
                || (block instanceof BlockShulkerBox) && shulkerBox.getValue());
    }
}
