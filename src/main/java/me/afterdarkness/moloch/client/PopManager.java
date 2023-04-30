package me.afterdarkness.moloch.client;

import me.afterdarkness.moloch.module.modules.visuals.Nametags;
import me.afterdarkness.moloch.event.events.entity.DeathEvent;
import me.afterdarkness.moloch.event.events.player.DisconnectEvent;
import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.utils.ChatUtil;

import java.util.HashMap;

import static net.spartanb312.base.utils.ItemUtils.mc;

public class PopManager {
    public static final HashMap<Entity, Integer> popMap = new HashMap<>();
    public static final HashMap<Entity, Integer> deathPopMap = new HashMap<>();

    public static void init() {
        BaseCenter.EVENT_BUS.register(new PopManager());
    }

    @Listener
    public void onDisconnect(DisconnectEvent event) {
        popMap.clear();
        deathPopMap.clear();
    }

    @Listener
    public void onDeath(DeathEvent event) {
        if (event.entity instanceof EntityPlayer && (Nametags.INSTANCE.popCount.getValue() != Nametags.TextMode.None || ChatSettings.INSTANCE.popNotifications.getValue())) {
            if (ChatSettings.INSTANCE.popNotifications.getValue()) {
                if (ChatSettings.INSTANCE.popNotificationsMarked.getValue()) {
                    ChatUtil.printChatMessage(ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsDeathColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationsDeathEffect) + event.entity.getName() + " just fucking died after popping " + (popMap.get(event.entity) == null ? 0 : popMap.get(event.entity)) + " totems" +"!");
                }
                else {
                    ChatUtil.printRawChatMessage(ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsDeathColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationsDeathEffect) + event.entity.getName() + " just fucking died after popping " + (popMap.get(event.entity) == null ? 0 : popMap.get(event.entity)) + " totems" +"!");
                }
            }

            deathPopMap.put(event.entity, popMap.get(event.entity) == null ? 0 : popMap.get(event.entity));
            popMap.put(event.entity, 0);
        }
    }

    @Listener
    public void onPacketReceive(PacketEvent.Receive event) {
        if ((Nametags.INSTANCE.popCount.getValue() != Nametags.TextMode.None || ChatSettings.INSTANCE.popNotifications.getValue())
                && event.getPacket() instanceof SPacketEntityStatus
                && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {

            Entity entity = ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world);
            if (!(entity instanceof EntityPlayer)) return;

            popMap.put(entity, (popMap.get(entity) == null ? 0 : popMap.get(entity)) + 1);
            int currentPops = popMap.get(entity);

            if (ChatSettings.INSTANCE.popNotifications.getValue()) {
                if (ChatSettings.INSTANCE.popNotificationsMarked.getValue()) {
                    ChatUtil.printChatMessage(ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationNameColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationNameEffect) + entity.getName() + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationsEffect) + " popped " + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsPopNumColor) + currentPops + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsColor) + " time" + (currentPops > 1 ? "s" : "") + "!");
                }
                else {
                    ChatUtil.printRawChatMessage(ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationNameColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationNameEffect) + entity.getName() + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsColor) + ChatUtil.effectString(ChatSettings.INSTANCE.popNotificationsEffect) + " popped " + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsPopNumColor) + currentPops + ChatUtil.SECTIONSIGN + ChatSettings.INSTANCE.colorString(ChatSettings.INSTANCE.popNotificationsColor) + " time" + (currentPops > 1 ? "s" : "") + "!");
                }
            }
        }
    }
}
