package net.spartanb312.base.mixin.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.afterdarkness.moloch.event.events.player.DisconnectEvent;
import me.afterdarkness.moloch.event.events.player.RubberbandEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.event.decentraliized.DecentralizedPacketEvent;
import net.spartanb312.base.event.events.network.PacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.spartanb312.base.utils.ItemUtils.mc;

@Mixin(value = NetworkManager.class, priority = 312312)
public abstract class MixinNetWork {
    @Shadow
    protected abstract void flushOutboundQueue();
    @Shadow
    public INetHandler packetListener;
    @Shadow
    public Channel channel;
    @Shadow
    public abstract boolean isChannelOpen();

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void packetReceived(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        if (mc.player != null && mc.world != null) {
            final PacketEvent.Receive event = new PacketEvent.Receive(packet);
            DecentralizedPacketEvent.Receive.instance.post(event);
            BaseCenter.EVENT_BUS.post(event);
            if (event.isCancelled() && callbackInfo.isCancellable()) {
                callbackInfo.cancel();
            }

            if (event.getPacket() instanceof SPacketPlayerPosLook && mc.player.ticksExisted >= 20.0f) {
                SPacketPlayerPosLook posLookPacket = (SPacketPlayerPosLook) event.getPacket();
                RubberbandEvent event1 = new RubberbandEvent(new Vec3d(posLookPacket.x, posLookPacket.y, posLookPacket.z).subtract(mc.player.getPositionVector()).length(),
                        new float[]{posLookPacket.yaw - mc.player.rotationYaw, posLookPacket.pitch - mc.player.rotationPitch});
                BaseCenter.EVENT_BUS.post(event1);
            }
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacket(Packet<?> packetIn, CallbackInfo callbackInfo) {
        if (mc.player != null && mc.world != null) {
            final PacketEvent.Send event = new PacketEvent.Send(packetIn);
            DecentralizedPacketEvent.Send.instance.post(event);
            BaseCenter.EVENT_BUS.post(event);
            if (event.isCancelled() && callbackInfo.isCancellable()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "closeChannel", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;isOpen()Z", remap = false))
    public void closeChannelHook(ITextComponent message, CallbackInfo ci) {
        if (isChannelOpen()) {
            DisconnectEvent event = new DisconnectEvent();
            BaseCenter.EVENT_BUS.post(event);
        }
    }

    //idk any non chinese way to fix guimultiplayer crashing randomly when trying to ping servers
    @Overwrite
    public void processReceivedPackets() {
        try {
            flushOutboundQueue();

            if (packetListener instanceof ITickable) {
                ((ITickable)packetListener).update();
            }

            if (channel != null) {
                channel.flush();
            }
        }
        catch (Exception ignored) {}
    }
}
