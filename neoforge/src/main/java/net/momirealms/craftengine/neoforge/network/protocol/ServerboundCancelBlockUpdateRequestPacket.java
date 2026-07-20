package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.network.ServerCustomPacket;
import org.jetbrains.annotations.NotNull;

public final class ServerboundCancelBlockUpdateRequestPacket implements ServerCustomPacket {
    public static final ServerboundCancelBlockUpdateRequestPacket INSTANCE = new ServerboundCancelBlockUpdateRequestPacket();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "cancel_block_update_request");
    public static final Type<ServerboundCancelBlockUpdateRequestPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ServerboundCancelBlockUpdateRequestPacket> CODEC = ServerCustomPacket.codec(
            ($, $$) -> {
            },
            $ -> INSTANCE
    );

    private ServerboundCancelBlockUpdateRequestPacket() {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundCancelBlockUpdateRequestPacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ServerboundCancelBlockUpdateRequestPacket> type() {
        return TYPE;
    }
}
