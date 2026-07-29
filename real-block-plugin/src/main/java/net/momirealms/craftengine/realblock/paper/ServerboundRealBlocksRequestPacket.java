package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.mod.CustomPackets;
import net.momirealms.craftengine.core.plugin.network.mod.ServerCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

final class ServerboundRealBlocksRequestPacket implements ServerCustomPacket {
    private static final ServerboundRealBlocksRequestPacket INSTANCE = new ServerboundRealBlocksRequestPacket();
    private static final Key ID = Key.ce("request_real_blocks");
    private static final NetworkCodec<FriendlyByteBuf, ServerboundRealBlocksRequestPacket> CODEC =
            ServerCustomPacket.codec((packet, buffer) -> {
            }, buffer -> INSTANCE);
    private static volatile RealBlockManager manager;

    private ServerboundRealBlocksRequestPacket() {
    }

    static void register(RealBlockManager realBlockManager) {
        manager = realBlockManager;
        CustomPackets.registerServerbound(ID, CODEC, CustomPackets.ALWAYS_ALLOWED);
    }

    static void unregister() {
        manager = null;
    }

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ServerboundRealBlocksRequestPacket> codec() {
        return CODEC;
    }

    @Override
    public void handle(NetWorkUser user, ByteBufPacketEvent event) {
        RealBlockManager current = manager;
        if (current != null && user.hasClientMod()) {
            current.sendDefinitions(user);
        }
    }
}
