package net.momirealms.craftengine.neoforge.network.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class Minecraft12111ItemStackDecoderTest {
    private Minecraft12111ItemStackDecoderTest() {
    }

    @Test
    void decodesMinecraft12111ItemAndComponentIdsByResourceLocation() {
        RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        RegistryFriendlyByteBuf wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);

        wire.writeVarInt(1);
        wire.writeVarInt(1);
        wire.writeVarInt(1245);
        wire.writeVarInt(4);
        wire.writeVarInt(0);
        writeComponent(wire, 9, DataComponents.ITEM_NAME, Component.literal("兼容名称"));
        writeComponent(wire, 11, DataComponents.LORE, new ItemLore(List.of(Component.literal("兼容描述"))));
        CompoundTag tag = new CompoundTag();
        tag.putString("craftengine:id", "default:test_item");
        writeComponent(wire, 0, DataComponents.CUSTOM_DATA, CustomData.of(tag));
        writeComponent(wire, 10, DataComponents.ITEM_MODEL, ResourceLocation.parse("minecraft:nether_brick"));

        wire.readerIndex(0);
        List<ItemStack> decoded = Minecraft12111ItemStackDecoder.decodeList(wire);
        assertEquals(1, decoded.size(), "decoded list size");
        ItemStack stack = decoded.getFirst();
        assertEquals(ResourceLocation.parse("minecraft:nether_brick"), BuiltInRegistries.ITEM.getKey(stack.getItem()), "server item id mapping");
        assertEquals("兼容名称", stack.get(DataComponents.ITEM_NAME).getString(), "item name component");
        assertEquals("兼容描述", stack.get(DataComponents.LORE).lines().getFirst().getString(), "lore component");
        assertTrue(stack.get(DataComponents.CUSTOM_DATA).contains("craftengine:id"), "custom data component");
        assertEquals(ResourceLocation.parse("minecraft:nether_brick"), stack.get(DataComponents.ITEM_MODEL), "item model component");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void writeComponent(RegistryFriendlyByteBuf buf, int serverId, DataComponentType<T> type, T value) {
        buf.writeVarInt(serverId);
        type.streamCodec().encode(buf, value);
    }
}
