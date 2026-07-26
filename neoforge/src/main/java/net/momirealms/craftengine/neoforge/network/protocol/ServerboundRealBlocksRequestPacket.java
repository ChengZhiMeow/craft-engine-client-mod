package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.network.ServerCustomPacket;
import org.jetbrains.annotations.NotNull;

public final class ServerboundRealBlocksRequestPacket implements ServerCustomPacket {
    public static final ServerboundRealBlocksRequestPacket INSTANCE = new ServerboundRealBlocksRequestPacket();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "request_real_blocks");
    public static final Type<ServerboundRealBlocksRequestPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ServerboundRealBlocksRequestPacket> CODEC =
            StreamCodec.unit(INSTANCE);

    private ServerboundRealBlocksRequestPacket() {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundRealBlocksRequestPacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ServerboundRealBlocksRequestPacket> type() {
        return TYPE;
    }
}
