package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.Context;
import org.jetbrains.annotations.NotNull;

public record ClientboundVisualBlockStateBatchStartPacket(int size) implements ClientCustomPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "visual_block_state_batch_start");
    public static final Type<ClientboundVisualBlockStateBatchStartPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchStartPacket> CODEC = ClientCustomPacket.codec(
            (packet, buf) -> buf.writeVarInt(packet.size),
            buf -> new ClientboundVisualBlockStateBatchStartPacket(buf.readVarInt())
    );

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchStartPacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ClientboundVisualBlockStateBatchStartPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(Context context) {
        BlockManager.instance().handleVisualBlockStateBatchStart(this.size);
    }
}
