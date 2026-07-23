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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        wire.writeVarInt(itemId("nether_brick"));
        wire.writeVarInt(4);
        wire.writeVarInt(0);
        writeComponent(wire, "item_name", DataComponents.ITEM_NAME, Component.literal("兼容名称"));
        writeComponent(wire, "lore", DataComponents.LORE, new ItemLore(List.of(Component.literal("兼容描述"))));
        CompoundTag tag = new CompoundTag();
        tag.putString("craftengine:id", "default:test_item");
        writeComponent(wire, "custom_data", DataComponents.CUSTOM_DATA, CustomData.of(tag));
        writeComponent(wire, "item_model", DataComponents.ITEM_MODEL, ResourceLocation.parse("minecraft:nether_brick"));

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

    @Test
    void usesVerifiedMinecraft12110And12111RegistrySizes() {
        assertEquals(29_671, Block.BLOCK_STATE_REGISTRY.size(), "Minecraft 1.21.10 block state count");
        assertEquals(29_671, Minecraft12111ItemStackDecoder.VANILLA_BLOCK_STATE_COUNT, "Minecraft 1.21.11 block state count");
        assertEquals(1_505, Minecraft12111ItemStackDecoder.itemRegistrySize(), "Minecraft 1.21.11 item count");
        assertEquals(104, Minecraft12111ItemStackDecoder.componentRegistrySize(), "Minecraft 1.21.11 component count");
    }

    @Test
    void downgradesAllMinecraft12111ItemsUsingViaBackwardsMappings() {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("nautilus_spawn_egg", "squid_spawn_egg"),
                Map.entry("zombie_nautilus_spawn_egg", "glow_squid_spawn_egg"),
                Map.entry("wooden_spear", "wooden_sword"),
                Map.entry("stone_spear", "stone_sword"),
                Map.entry("copper_spear", "copper_sword"),
                Map.entry("iron_spear", "iron_sword"),
                Map.entry("golden_spear", "golden_sword"),
                Map.entry("diamond_spear", "diamond_sword"),
                Map.entry("netherite_spear", "netherite_sword"),
                Map.entry("iron_nautilus_armor", "iron_horse_armor"),
                Map.entry("golden_nautilus_armor", "golden_horse_armor"),
                Map.entry("diamond_nautilus_armor", "diamond_horse_armor"),
                Map.entry("netherite_nautilus_armor", "leather_horse_armor"),
                Map.entry("copper_nautilus_armor", "copper_horse_armor"),
                Map.entry("camel_husk_spawn_egg", "camel_spawn_egg"),
                Map.entry("parched_spawn_egg", "skeleton_spawn_egg"),
                Map.entry("netherite_horse_armor", "leather_horse_armor")
        );

        assertEquals(17, expected.size());
        assertEquals(17, Minecraft12111ItemStackDecoder.fallbackItemCount());
        expected.forEach((source, target) -> {
            ResourceLocation sourceKey = minecraft(source);
            assertEquals(minecraft(target), Minecraft12111ItemStackDecoder.fallbackItem(sourceKey), source);
            int model = Minecraft12111ItemStackDecoder.fallbackCustomModelData(sourceKey);
            assertTrue(model >= 816 && model <= 832, source);
            ItemStack decoded = decodeSingleItem(source, wire -> {
                wire.writeVarInt(0);
                wire.writeVarInt(0);
            });
            assertEquals(minecraft(target), BuiltInRegistries.ITEM.getKey(decoded.getItem()), source);
            CustomModelData customModelData = decoded.get(DataComponents.CUSTOM_MODEL_DATA);
            assertEquals(model, customModelData.floats().getFirst().intValue(), source);
        });
    }

    @Test
    void discardsAllEightNewComponentsWithoutLosingFollowingBytes() {
        ItemStack stack = decodeSingleItem("nether_brick", wire -> {
            wire.writeVarInt(9);
            wire.writeVarInt(0);

            writeComponentId(wire, "attack_range");
            for (int i = 0; i < 6; i++) wire.writeFloat(i + 0.25F);

            writeComponentId(wire, "damage_type");
            wire.writeBoolean(true);
            wire.writeVarInt(4);

            writeComponentId(wire, "kinetic_weapon");
            wire.writeVarInt(3);
            wire.writeVarInt(7);
            wire.writeBoolean(false);
            wire.writeBoolean(true);
            wire.writeVarInt(12);
            wire.writeFloat(0.5F);
            wire.writeFloat(0.75F);
            wire.writeBoolean(false);
            wire.writeFloat(1.0F);
            wire.writeFloat(2.0F);
            wire.writeBoolean(false);
            wire.writeBoolean(true);
            wire.writeVarInt(2);

            writeComponentId(wire, "minimum_attack_charge");
            wire.writeFloat(0.8F);

            writeComponentId(wire, "piercing_weapon");
            wire.writeBoolean(true);
            wire.writeBoolean(false);
            wire.writeBoolean(true);
            wire.writeVarInt(0);
            wire.writeResourceLocation(minecraft("entity.arrow.hit"));
            wire.writeBoolean(true);
            wire.writeFloat(24.0F);
            wire.writeBoolean(false);

            writeComponentId(wire, "swing_animation");
            wire.writeVarInt(2);
            wire.writeVarInt(6);

            writeComponentId(wire, "use_effects");
            wire.writeBoolean(true);
            wire.writeBoolean(false);
            wire.writeFloat(0.4F);

            writeComponentId(wire, "zombie_nautilus/variant");
            wire.writeBoolean(false);
            wire.writeResourceLocation(minecraft("warm"));

            writeComponent(wire, "item_name", DataComponents.ITEM_NAME, Component.literal("仍然对齐"));
        });

        assertEquals("仍然对齐", stack.get(DataComponents.ITEM_NAME).getString());
    }

    @Test
    void rejectsTrailingBytesInvalidCountsUnknownIdsAndBrokenComponents() {
        assertThrows(IllegalArgumentException.class, () -> decodeRaw(wire -> {
            wire.writeVarInt(0);
            wire.writeByte(1);
        }), "trailing bytes");

        assertThrows(IllegalArgumentException.class, () -> decodeRaw(wire -> wire.writeVarInt(4_097)), "list limit");

        assertThrows(IllegalArgumentException.class, () -> decodeRaw(wire -> {
            wire.writeVarInt(1);
            wire.writeVarInt(100);
            wire.writeVarInt(itemId("stone"));
        }), "stack count limit");

        assertThrows(IllegalArgumentException.class, () -> decodeRaw(wire -> {
            wire.writeVarInt(1);
            wire.writeVarInt(1);
            wire.writeVarInt(1_505);
        }), "unknown item id");

        assertThrows(RuntimeException.class, () -> decodeRaw(wire -> {
            wire.writeVarInt(1);
            wire.writeVarInt(1);
            wire.writeVarInt(itemId("stone"));
            wire.writeVarInt(1);
            wire.writeVarInt(0);
            writeComponentId(wire, "attack_range");
            wire.writeFloat(1.0F);
        }), "truncated component");
    }

    @Test
    void commonComponentsDoNotUseFallbackTable() {
        assertNull(Minecraft12111ItemStackDecoder.fallbackItem(minecraft("nether_brick")));
    }

    private static ItemStack decodeSingleItem(String item, java.util.function.Consumer<RegistryFriendlyByteBuf> patchWriter) {
        List<ItemStack> result = decodeRaw(wire -> {
            wire.writeVarInt(1);
            wire.writeVarInt(1);
            wire.writeVarInt(itemId(item));
            patchWriter.accept(wire);
        });
        return result.getFirst();
    }

    private static List<ItemStack> decodeRaw(java.util.function.Consumer<RegistryFriendlyByteBuf> writer) {
        RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        RegistryFriendlyByteBuf wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        writer.accept(wire);
        wire.readerIndex(0);
        return Minecraft12111ItemStackDecoder.decodeList(wire);
    }

    private static int itemId(String path) {
        return Minecraft12111ItemStackDecoder.itemId(minecraft(path));
    }

    private static void writeComponentId(RegistryFriendlyByteBuf buf, String path) {
        int id = Minecraft12111ItemStackDecoder.componentId(minecraft(path));
        assertTrue(id >= 0, "missing component " + path);
        buf.writeVarInt(id);
    }

    private static ResourceLocation minecraft(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void writeComponent(RegistryFriendlyByteBuf buf, String serverPath, DataComponentType<T> type, T value) {
        writeComponentId(buf, serverPath);
        type.streamCodec().encode(buf, value);
    }
}
