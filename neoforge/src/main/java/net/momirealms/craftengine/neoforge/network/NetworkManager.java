package net.momirealms.craftengine.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.network.protocol.*;
import net.momirealms.craftengine.neoforge.registries.BuiltInRegistries;
import net.momirealms.craftengine.neoforge.registries.Registries;
import net.momirealms.craftengine.neoforge.util.BlockStateUtils;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.registration.ChannelAttributes;

public class NetworkManager {
    public static final int PROTOCOL_VERSION = 1;
    public static final StreamCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchStartPacket> VISUAL_BLOCK_STATE_BATCH_START = registerClientbound(ClientboundVisualBlockStateBatchStartPacket.TYPE, ClientboundVisualBlockStateBatchStartPacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ClientboundVisualBlockStateBatchFinishedPacket> VISUAL_BLOCK_STATE_BATCH_FINISHED = registerClientbound(ClientboundVisualBlockStateBatchFinishedPacket.TYPE, ClientboundVisualBlockStateBatchFinishedPacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ClientboundVisualBlockStatesPacket> VISUAL_BLOCK_STATES = registerClientbound(ClientboundVisualBlockStatesPacket.TYPE, ClientboundVisualBlockStatesPacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ClientboundCancelBlockUpdateResponsePacket> CANCEL_BLOCK_UPDATE_RESPONSE = registerClientbound(ClientboundCancelBlockUpdateResponsePacket.TYPE, ClientboundCancelBlockUpdateResponsePacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ClientboundCreativeModeTabItemsPacket> CREATIVE_MODE_TAB_ITEMS = registerClientbound(ClientboundCreativeModeTabItemsPacket.TYPE, ClientboundCreativeModeTabItemsPacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ServerboundHandshakePacket> HANDSHAKE = registerServerbound(ServerboundHandshakePacket.TYPE, ServerboundHandshakePacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ServerboundEnableClientCustomBlockPacket> ENABLE_CLIENT_CUSTOM_BLOCK = registerServerbound(ServerboundEnableClientCustomBlockPacket.TYPE, ServerboundEnableClientCustomBlockPacket.CODEC);
    public static final StreamCodec<FriendlyByteBuf, ServerboundCancelBlockUpdateRequestPacket> CANCEL_BLOCK_UPDATE_REQUEST = registerServerbound(ServerboundCancelBlockUpdateRequestPacket.TYPE, ServerboundCancelBlockUpdateRequestPacket.CODEC);
    private static NetworkManager instance;
    private final CraftEngineNeoForgeMod mod;
    private ICommonPacketListener configurationListener;
    private boolean handshakeSent;
    private boolean serverInstalled = false;

    public NetworkManager(CraftEngineNeoForgeMod mod) {
        instance = this;
        this.mod = mod;
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            configurationListener = null;
            handshakeSent = false;
            serverInstalled(false);
        });
    }

    public static NetworkManager instance() {
        return instance;
    }

    public static <T extends ClientCustomPacket> StreamCodec<FriendlyByteBuf, T> registerClientbound(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec) {
        ((WritableRegistry<StreamCodec<FriendlyByteBuf, ? extends ClientCustomPacket>>) BuiltInRegistries.CLIENT_MOD_PACKET)
                .register(ResourceKey.create(Registries.CLIENT_MOD_PACKET, type.id()), codec, RegistrationInfo.BUILT_IN);
        return codec;
    }

    public static <T extends ServerCustomPacket> StreamCodec<FriendlyByteBuf, T> registerServerbound(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec) {
        ((WritableRegistry<StreamCodec<FriendlyByteBuf, ? extends ServerCustomPacket>>) BuiltInRegistries.SERVER_MOD_PACKET)
                .register(ResourceKey.create(Registries.SERVER_MOD_PACKET, type.id()), codec, RegistrationInfo.BUILT_IN);
        return codec;
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Integer.toString(PROTOCOL_VERSION)).optional();
        registrar.commonToClient(ClientboundVisualBlockStateBatchStartPacket.TYPE, VISUAL_BLOCK_STATE_BATCH_START,
                (payload, context) -> payload.handle(Context.of(context)));
        registrar.commonToClient(ClientboundVisualBlockStateBatchFinishedPacket.TYPE, VISUAL_BLOCK_STATE_BATCH_FINISHED,
                (payload, context) -> payload.handle(Context.of(context)));
        registrar.commonToClient(ClientboundVisualBlockStatesPacket.TYPE, VISUAL_BLOCK_STATES,
                (payload, context) -> payload.handle(Context.of(context)));
        registrar.commonToClient(ClientboundCancelBlockUpdateResponsePacket.TYPE, CANCEL_BLOCK_UPDATE_RESPONSE,
                (payload, context) -> payload.handle(Context.of(context)));
        registrar.commonToClient(ClientboundCreativeModeTabItemsPacket.TYPE, CREATIVE_MODE_TAB_ITEMS,
                (payload, context) -> payload.handle(Context.of(context)));
        registrar.commonToServer(ServerboundHandshakePacket.TYPE, HANDSHAKE, (payload, context) -> {});
        registrar.commonToServer(ServerboundEnableClientCustomBlockPacket.TYPE, ENABLE_CLIENT_CUSTOM_BLOCK, (payload, context) -> {});
        registrar.commonToServer(ServerboundCancelBlockUpdateRequestPacket.TYPE, CANCEL_BLOCK_UPDATE_REQUEST, (payload, context) -> {});
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean serverInstalled() {
        return this.serverInstalled;
    }

    public void serverInstalled(boolean serverInstalled) {
        this.serverInstalled = serverInstalled;
    }

    public void initChannel(ICommonPacketListener handler) {
        this.configurationListener = handler;
        this.handshakeSent = false;
        handler.getConnection().channel().eventLoop().execute(() -> {
            if (configurationListener == handler) {
                sendInitialPackets(handler);
            }
        });
    }

    public void onChannelsUpdated() {
        if (configurationListener != null) {
            sendInitialPackets(configurationListener);
        }
    }

    private void sendInitialPackets(ICommonPacketListener handler) {
        if (handshakeSent || !sendCustomPacket(handler, new ServerboundHandshakePacket(PROTOCOL_VERSION, Block.BLOCK_STATE_REGISTRY.size()))) {
            return;
        }
        handshakeSent = true;
        if (ModConfig.INSTANCE.enableClientCustomBlock()) {
            sendCustomPacket(handler, new ServerboundEnableClientCustomBlockPacket(BlockStateUtils.vanillaStateSize(), Block.BLOCK_STATE_REGISTRY.size()));
        } else if (ModConfig.INSTANCE.enableCancelBlockUpdate()) {
            sendCustomPacket(handler, ServerboundCancelBlockUpdateRequestPacket.INSTANCE);
        }
    }

    public void sendCustomPacket(ServerCustomPacket data) {
        if (Minecraft.getInstance().getConnection() != null) {
            sendCustomPacket((ICommonPacketListener) Minecraft.getInstance().getConnection(), data);
        } else if (configurationListener != null) {
            sendCustomPacket(configurationListener, data);
        }
    }

    private boolean sendCustomPacket(ICommonPacketListener listener, ServerCustomPacket data) {
        if (!listener.hasChannel(data)) {
            if (listener.getConnection().isMemoryConnection() || !listener.getConnectionType().isOther()) {
                return false;
            }
            ChannelAttributes.getOrCreateAdHocChannels(listener.getConnection()).add(data.type().id());
        }
        listener.send(data);
        return true;
    }
}
