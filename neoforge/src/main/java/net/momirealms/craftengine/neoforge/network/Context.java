package net.momirealms.craftengine.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.ConnectionProtocol;
import net.momirealms.craftengine.neoforge.network.ServerCustomPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public final class Context {
    private final IPayloadContext context;

    private Context(IPayloadContext context) {
        this.context = context;
    }

    public static Context of(IPayloadContext context) {
        return new Context(context);
    }

    public Minecraft client() {
        return Minecraft.getInstance();
    }

    public void reply(ServerCustomPacket payload) {
        context.reply(payload);
    }

    public ClientCommonPacketListenerImpl networkHandler() {
        return (ClientCommonPacketListenerImpl) context.listener();
    }

    @Nullable
    public LocalPlayer player() {
        if (context.protocol() != ConnectionProtocol.PLAY) return null;
        return (LocalPlayer) context.player();
    }
}
