package me.afterdarkness.moloch.module.modules.other;

import me.afterdarkness.moloch.event.events.player.ProcessRightClickBlockEvent;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;

@Parallel(runnable = true)
@ModuleInfo(name = "PearlBypass", category = Category.OTHER, description = "cc phase bypass frfr || By Kisman")
public class PearlBypass extends Module {
    @SubscribeEvent
    public void onRightClickEvent(ProcessRightClickBlockEvent event) {
        if(PearlBypass.fullNullCheck()) {
            return;
        }
        if(PearlBypass.mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() == Items.ENDER_PEARL) {
            PearlBypass.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(event.hand));
            event.setCancelled(true);
        }
    }
}
