package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class Minecraft12111ItemStackDecoder {
    private static final List<ResourceLocation> ITEMS = loadRegistry("items", 1505);
    private static final List<ResourceLocation> COMPONENTS = loadRegistry("components", 104);
    private static final int MAX_ITEMS_PER_PACKET = 4096;
    private static final int MAX_COMPONENTS_PER_ITEM = 256;

    private Minecraft12111ItemStackDecoder() {
    }

    static List<ItemStack> decodeList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ITEMS_PER_PACKET) {
            throw new IllegalArgumentException("Invalid 1.21.11 item list size: " + size);
        }

        List<ItemStack> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(decodeItem(buf));
        }
        if (buf.isReadable()) {
            throw new IllegalArgumentException("Trailing bytes after 1.21.11 item list: " + buf.readableBytes());
        }
        return result;
    }

    private static ItemStack decodeItem(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        int serverItemId = buf.readVarInt();
        ResourceLocation itemKey = byId(ITEMS, serverItemId, "item");
        Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(itemKey)
                .orElseThrow(() -> new IllegalArgumentException("1.21.11 item is unavailable in 1.21.8: " + itemKey));
        DataComponentPatch patch = decodePatch(buf);
        return new ItemStack(item, count, patch);
    }

    private static DataComponentPatch decodePatch(RegistryFriendlyByteBuf buf) {
        int added = buf.readVarInt();
        int removed = buf.readVarInt();
        if (added < 0 || removed < 0 || added + removed > MAX_COMPONENTS_PER_ITEM) {
            throw new IllegalArgumentException("Invalid 1.21.11 component patch size: " + added + "+" + removed);
        }

        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        for (int i = 0; i < added; i++) {
            ResourceLocation componentKey = byId(COMPONENTS, buf.readVarInt(), "component");
            DataComponentType<?> component = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentKey);
            if (component == null) {
                throw new IllegalArgumentException("Unsupported 1.21.11 component on 1.21.8: " + componentKey);
            }
            decodeAndSet(buf, patch, component);
        }
        for (int i = 0; i < removed; i++) {
            ResourceLocation componentKey = byId(COMPONENTS, buf.readVarInt(), "removed component");
            DataComponentType<?> component = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentKey);
            if (component != null) {
                remove(patch, component);
            }
        }
        return patch.build();
    }

    private static ResourceLocation byId(List<ResourceLocation> registry, int id, String kind) {
        if (id < 0 || id >= registry.size()) {
            throw new IllegalArgumentException("Unknown 1.21.11 " + kind + " id: " + id);
        }
        return registry.get(id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void decodeAndSet(RegistryFriendlyByteBuf buf, DataComponentPatch.Builder patch, DataComponentType component) {
        StreamCodec<RegistryFriendlyByteBuf, Object> codec = (StreamCodec<RegistryFriendlyByteBuf, Object>) component.streamCodec();
        patch.set(component, codec.decode(buf));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void remove(DataComponentPatch.Builder patch, DataComponentType component) {
        patch.remove(component);
    }

    private static List<ResourceLocation> loadRegistry(String name, int expectedSize) {
        String path = "/assets/craftengine/compat/" + name + "-1.21.11.txt";
        InputStream input = Minecraft12111ItemStackDecoder.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalStateException("Missing compatibility registry: " + path);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            List<ResourceLocation> entries = reader.lines()
                    .map(line -> line.replace("\uFEFF", "").strip())
                    .filter(line -> !line.isEmpty())
                    .map(ResourceLocation::parse)
                    .toList();
            if (entries.size() != expectedSize) {
                throw new IllegalStateException("Invalid compatibility registry size for " + name + ": " + entries.size());
            }
            return entries;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load compatibility registry: " + path, e);
        }
    }
}
