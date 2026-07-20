package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.momirealms.craftengine.neoforge.network.NetworkManager;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
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

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("RETURN"))
    private void craftengine$channelsUpdated(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        NetworkManager.instance().onChannelsUpdated();
    }
}
