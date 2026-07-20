package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.Context;
import net.momirealms.craftengine.neoforge.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

public final class ClientboundCancelBlockUpdateResponsePacket implements ClientCustomPacket {
    public static final ClientboundCancelBlockUpdateResponsePacket INSTANCE = new ClientboundCancelBlockUpdateResponsePacket();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "cancel_block_update_response");
    public static final Type<ClientboundCancelBlockUpdateResponsePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundCancelBlockUpdateResponsePacket> CODEC = ClientCustomPacket.codec(
            ($, $$) -> {
            },
            $ -> INSTANCE
    );

    private ClientboundCancelBlockUpdateResponsePacket() {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundCancelBlockUpdateResponsePacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ClientboundCancelBlockUpdateResponsePacket> type() {
        return TYPE;
    }

    @Override
    public void handle(Context context) {
        ModConfig.INSTANCE.enableCancelBlockUpdate(true);
        NetworkManager.instance().serverInstalled(true);
    }
}
