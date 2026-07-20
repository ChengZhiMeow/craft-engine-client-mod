package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.item.ItemManager;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record ClientboundCreativeModeTabItemsPacket(Action action,
                                                    FriendlyByteBuf itemStacks) implements ClientCustomPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "creative_mode_tab_items");
    public static final Type<ClientboundCreativeModeTabItemsPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundCreativeModeTabItemsPacket> CODEC = ClientCustomPacket.codec(
            ClientboundCreativeModeTabItemsPacket::encode,
            ClientboundCreativeModeTabItemsPacket::decode
    );
    private static boolean compatibilityModeLogged;

    private static ClientboundCreativeModeTabItemsPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        if (action == Action.CLEAR) {
            return new ClientboundCreativeModeTabItemsPacket(Action.CLEAR, null);
        } else {
            return new ClientboundCreativeModeTabItemsPacket(action, new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
        }
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        if (this.action == Action.CLEAR) return;
        buf.writeBytes(itemStacks);
    }


    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundCreativeModeTabItemsPacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ClientboundCreativeModeTabItemsPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(Context context) {
        if (!(context.networkHandler() instanceof ClientPacketListener listener)) return;
        int start = this.itemStacks.readerIndex();
        Throwable nativeFailure;
        try {
            RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(this.itemStacks, listener.registryAccess());
            List<ItemStack> list = byteBuf.readCollection(ArrayList::new, $ -> ItemStack.OPTIONAL_STREAM_CODEC.decode(byteBuf));
            if (byteBuf.isReadable()) {
                throw new IllegalArgumentException("Trailing bytes after native item list: " + byteBuf.readableBytes());
            }
            this.action.execute(list);
            return;
        } catch (Throwable t) {
            nativeFailure = t;
        }

        this.itemStacks.readerIndex(start);
        try {
            RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(this.itemStacks, listener.registryAccess());
            List<ItemStack> list = Minecraft12111ItemStackDecoder.decodeList(byteBuf);
            this.action.execute(list);
            if (!compatibilityModeLogged) {
                compatibilityModeLogged = true;
                CraftEngineNeoForgeMod.instance().logger().info("Decoded CraftEngine creative items using Minecraft 1.21.11 compatibility mode");
            }
        } catch (Throwable compatibilityFailure) {
            compatibilityFailure.addSuppressed(nativeFailure);
            CraftEngineNeoForgeMod.instance().logger().warn("Failed to decode CraftEngine creative items as Minecraft 1.21.8 or 1.21.11", compatibilityFailure);
        } finally {
            this.itemStacks.readerIndex(start);
        }
    }

    public enum Action {
        ADD(list -> {
            ItemManager itemManager = ItemManager.instance();
            List<ItemStack> newList = itemManager.creativeTabItems();
            newList.addAll(list);
            itemManager.loadFromNetwork(newList);
        }),
        CLEAR($ -> ItemManager.instance().clearCreativeTabItems()),
        SET(ItemManager.instance()::loadFromNetwork);

        private final Consumer<List<ItemStack>> action;

        Action(Consumer<List<ItemStack>> action) {
            this.action = action;
        }

        public void execute(List<ItemStack> list) {
            this.action.accept(list);
        }
    }
}
