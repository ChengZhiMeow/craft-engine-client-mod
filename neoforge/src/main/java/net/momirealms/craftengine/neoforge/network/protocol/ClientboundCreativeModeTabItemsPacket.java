package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.item.ItemManager;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.Context;
import org.jetbrains.annotations.NotNull;

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
        if (this.action == Action.CLEAR) {
            this.action.execute(List.of());
            return;
        }

        int start = this.itemStacks.readerIndex();
        try {
            RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(this.itemStacks, listener.registryAccess());
            boolean compatibilityEnabled = ModConfig.INSTANCE.enableMinecraft12111ServerCompatibility();
            List<ItemStack> list = compatibilityEnabled
                    ? Minecraft12111ItemStackDecoder.decodeList(byteBuf)
                    : ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(byteBuf);
            if (byteBuf.isReadable()) {
                throw new IllegalArgumentException("Trailing bytes after creative item list: " + byteBuf.readableBytes());
            }
            this.action.execute(list);
            if (compatibilityEnabled && !compatibilityModeLogged) {
                compatibilityModeLogged = true;
                CraftEngineNeoForgeMod.instance().logger().info(
                        "Decoded CraftEngine creative items using Minecraft 1.21.11 registry compatibility mode"
                );
            }
        } catch (Throwable failure) {
            CraftEngineNeoForgeMod.instance().logger().warn(
                    ModConfig.INSTANCE.enableMinecraft12111ServerCompatibility()
                            ? "Rejected invalid Minecraft 1.21.11 CraftEngine creative item update"
                            : "Rejected invalid Minecraft 1.21.10 CraftEngine creative item update",
                    failure
            );
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
