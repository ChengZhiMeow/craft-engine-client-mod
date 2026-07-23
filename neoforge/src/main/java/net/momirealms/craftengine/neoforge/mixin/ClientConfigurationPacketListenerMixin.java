package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.NetworkManager;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void craftengine$startConfiguration(CallbackInfo ci) {
        NetworkManager.instance().initChannel((ICommonPacketListener) this);
    }

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("HEAD"))
    private void craftengine$preparePaperPayloadChannel(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!(packet.payload() instanceof ClientCustomPacket)) {
            return;
        }

        ICommonPacketListener listener = (ICommonPacketListener) this;
        if (ChannelAttributes.getPayloadSetup(listener.getConnection()) == null) {
            ChannelAttributes.setPayloadSetup(listener.getConnection(), NetworkPayloadSetup.empty());
            ChannelAttributes.setConnectionType(listener.getConnection(), ConnectionType.OTHER);
        }
        ChannelAttributes.getOrCreateAdHocChannels(listener.getConnection()).add(packet.payload().type().id());
    }

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("RETURN"))
    private void craftengine$channelsUpdated(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        NetworkManager.instance().onChannelsUpdated();
    }
}
