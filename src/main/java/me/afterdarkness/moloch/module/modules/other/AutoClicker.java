package me.afterdarkness.moloch.module.modules.other;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.common.KeyBind;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.ItemUtils;
import net.spartanb312.base.utils.Timer;

import java.util.ArrayList;
import java.util.List;
//TODO: replace entityCollectorClear with action button
@Parallel(runnable = true)
@ModuleInfo(name = "AutoClicker", category = Category.OTHER, description = "Clicks automatically")
public class AutoClicker extends Module {

    private boolean isCollecting;

    Setting<Boolean> frameDupe = setting("FrameDupe", false).des("Itemframe dupe for servers like 6b6t.org");
    Setting<KeyBind> entityCollectorBind = setting("EntityCollectorBind", subscribeKey(new KeyBind(getAnnotation().keyCode(), () -> isCollecting = !isCollecting))).des("Bind to collect itemframes to dupe from (interact with an itemframe to add it to the collector list)").whenTrue(frameDupe);
    Setting<Integer> frameDupeDelay = setting("FrameDupeDelay", 200, 1, 1000).des("Milliseconds between frame dupe attempts").whenTrue(frameDupe);
    Setting<Boolean> entityCollectorClear = setting("EntityCollectorClear", false).des("Clear cached itemframes").whenTrue(frameDupe);
    Setting<Boolean> frameDupeSwitchShulker = setting("FrameDupeSwitchShulker", false).des("Switch to shulker when filling item frame").whenTrue(frameDupe);
    Setting<Boolean> rightClick = setting("RightClick", true).des("Click right mouse button").whenFalse(frameDupe);
    Setting<Integer> rightClickDelay = setting("RightClickDelay", 1000, 1, 10000).des("Delay between right clicks in milliseconds").whenTrue(rightClick).whenFalse(frameDupe);
    Setting<Boolean> leftClick = setting("LeftClick", true).des("Click left mouse button").whenFalse(frameDupe);
    Setting<Integer> leftClickDelay = setting("LeftClickDelay", 1000, 1, 10000).des("Delay between left clicks in milliseconds").whenTrue(leftClick).whenFalse(frameDupe);

    private final Timer rightClickTimer = new Timer();
    private final Timer leftClickTimer = new Timer();
    private final List<EntityItemFrame> collectedFrames = new ArrayList<>();
    private final Timer frameDupeTimer = new Timer();
    private int frameDupeIndex;

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        Entity frame;
        if (frameDupe.getValue() && event.getPacket() instanceof CPacketUseEntity && (frame = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)) instanceof EntityItemFrame) {
            if (isCollecting && !collectedFrames.contains(frame)) {
                collectedFrames.add((EntityItemFrame) frame);
                event.cancel();
            }
        }
    }

    @Override
    public String getModuleInfo() {
        return frameDupe.getValue() ? (collectedFrames.size() + (isCollecting ? ", Collecting" : "")) : "";
    }

    @Override
    public void onRenderTick() {
        if (frameDupe.getValue()) {
            if (entityCollectorClear.getValue()) {
                entityCollectorClear.setValue(false);
                collectedFrames.clear();
            }

            if (!isCollecting && frameDupeTimer.passed(frameDupeDelay.getValue())) {
                EntityItemFrame frame = collectedFrames.get(frameDupeIndex);

                if (frame.getDisplayedItem().isEmpty) {
                    if (frameDupeSwitchShulker.getValue()) {
                        int slot = ItemUtils.findShulkerInHotBar();
                        if (slot != -1) ItemUtils.switchToSlot(slot, false);
                    }
                    mc.player.connection.sendPacket(new CPacketUseEntity(frame, EnumHand.MAIN_HAND));
                } else {
                    mc.player.connection.sendPacket(new CPacketUseEntity(frame));
                }

                frameDupeIndex++;
                if (frameDupeIndex > collectedFrames.size()) {
                    frameDupeIndex = 0;
                }
                frameDupeTimer.reset();
            }
        }
        else {
            if (rightClick.getValue() && rightClickTimer.passed(rightClickDelay.getValue())) {
                mc.rightClickMouse();
                rightClickTimer.reset();
            }

            if (leftClick.getValue() && leftClickTimer.passed(leftClickDelay.getValue())) {
                mc.clickMouse();
                leftClickTimer.reset();
            }
        }
    }
}
