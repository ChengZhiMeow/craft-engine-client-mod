package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Minecraft12111ItemStackDecoder {
    static final int VANILLA_BLOCK_STATE_COUNT = 29_671;
    private static final List<ResourceLocation> ITEMS = loadRegistry("items", 1505);
    private static final List<ResourceLocation> COMPONENTS = loadRegistry("components", 104);
    private static final Map<ResourceLocation, ResourceLocation> ITEM_FALLBACKS = Map.ofEntries(
            fallback("nautilus_spawn_egg", "squid_spawn_egg"),
            fallback("zombie_nautilus_spawn_egg", "glow_squid_spawn_egg"),
            fallback("wooden_spear", "wooden_sword"),
            fallback("stone_spear", "stone_sword"),
            fallback("copper_spear", "copper_sword"),
            fallback("iron_spear", "iron_sword"),
            fallback("golden_spear", "golden_sword"),
            fallback("diamond_spear", "diamond_sword"),
            fallback("netherite_spear", "netherite_sword"),
            fallback("iron_nautilus_armor", "iron_horse_armor"),
            fallback("golden_nautilus_armor", "golden_horse_armor"),
            fallback("diamond_nautilus_armor", "diamond_horse_armor"),
            fallback("netherite_nautilus_armor", "leather_horse_armor"),
            fallback("copper_nautilus_armor", "copper_horse_armor"),
            fallback("camel_husk_spawn_egg", "camel_spawn_egg"),
            fallback("parched_spawn_egg", "skeleton_spawn_egg"),
            fallback("netherite_horse_armor", "leather_horse_armor")
    );
    private static final Map<ResourceLocation, Integer> FALLBACK_CUSTOM_MODEL_DATA = Map.ofEntries(
            fallbackModel("nautilus_spawn_egg", 816),
            fallbackModel("zombie_nautilus_spawn_egg", 817),
            fallbackModel("wooden_spear", 818),
            fallbackModel("stone_spear", 819),
            fallbackModel("copper_spear", 820),
            fallbackModel("iron_spear", 821),
            fallbackModel("golden_spear", 822),
            fallbackModel("diamond_spear", 823),
            fallbackModel("netherite_spear", 824),
            fallbackModel("iron_nautilus_armor", 825),
            fallbackModel("golden_nautilus_armor", 826),
            fallbackModel("diamond_nautilus_armor", 827),
            fallbackModel("netherite_nautilus_armor", 828),
            fallbackModel("copper_nautilus_armor", 829),
            fallbackModel("camel_husk_spawn_egg", 830),
            fallbackModel("parched_spawn_egg", 831),
            fallbackModel("netherite_horse_armor", 832)
    );
    private static final int MAX_ITEMS_PER_PACKET = 4096;
    private static final int MAX_COMPONENTS_PER_ITEM = 256;
    private static final int MAX_STACK_COUNT = 99;

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
        if (count > MAX_STACK_COUNT) {
            throw new IllegalArgumentException("Invalid 1.21.11 item count: " + count);
        }

        int serverItemId = buf.readVarInt();
        ResourceLocation itemKey = byId(ITEMS, serverItemId, "item");
        ResourceLocation clientItemKey = ITEM_FALLBACKS.getOrDefault(itemKey, itemKey);
        Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(clientItemKey)
                .orElseThrow(() -> new IllegalArgumentException("1.21.11 item is unavailable in 1.21.10: " + itemKey));
        DataComponentPatch patch = decodePatch(buf);
        ItemStack stack = new ItemStack(item, count, patch);
        Integer customModelData = FALLBACK_CUSTOM_MODEL_DATA.get(itemKey);
        if (customModelData != null && !stack.has(DataComponents.CUSTOM_MODEL_DATA)) {
            stack.set(
                    DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(customModelData.floatValue()), List.of(), List.of(), List.of())
            );
        }
        return stack;
    }

    private static DataComponentPatch decodePatch(RegistryFriendlyByteBuf buf) {
        int added = buf.readVarInt();
        int removed = buf.readVarInt();
        if (added < 0
                || removed < 0
                || added > MAX_COMPONENTS_PER_ITEM
                || removed > MAX_COMPONENTS_PER_ITEM - added) {
            throw new IllegalArgumentException("Invalid 1.21.11 component patch size: " + added + "+" + removed);
        }

        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        for (int i = 0; i < added; i++) {
            ResourceLocation componentKey = byId(COMPONENTS, buf.readVarInt(), "component");
            DataComponentType<?> component = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentKey);
            if (component == null) {
                skipNewComponent(buf, componentKey);
                continue;
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

    private static void skipNewComponent(RegistryFriendlyByteBuf buf, ResourceLocation key) {
        switch (key.getPath()) {
            case "attack_range" -> {
                for (int i = 0; i < 6; i++) {
                    buf.readFloat();
                }
            }
            case "damage_type", "zombie_nautilus/variant" -> skipIdOrResourceLocation(buf);
            case "kinetic_weapon" -> skipKineticWeapon(buf);
            case "minimum_attack_charge" -> buf.readFloat();
            case "piercing_weapon" -> {
                buf.readBoolean();
                buf.readBoolean();
                skipOptionalSoundEvent(buf);
                skipOptionalSoundEvent(buf);
            }
            case "swing_animation" -> {
                buf.readVarInt();
                buf.readVarInt();
            }
            case "use_effects" -> {
                buf.readBoolean();
                buf.readBoolean();
                buf.readFloat();
            }
            default -> throw new IllegalArgumentException("Unsupported 1.21.11 component on 1.21.10: " + key);
        }
    }

    private static void skipKineticWeapon(RegistryFriendlyByteBuf buf) {
        buf.readVarInt();
        buf.readVarInt();
        skipOptionalKineticCondition(buf);
        skipOptionalKineticCondition(buf);
        skipOptionalKineticCondition(buf);
        buf.readFloat();
        buf.readFloat();
        skipOptionalSoundEvent(buf);
        skipOptionalSoundEvent(buf);
    }

    private static void skipOptionalKineticCondition(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            buf.readVarInt();
            buf.readFloat();
            buf.readFloat();
        }
    }

    private static void skipIdOrResourceLocation(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            buf.readVarInt();
        } else {
            buf.readResourceLocation();
        }
    }

    private static void skipOptionalSoundEvent(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return;
        }
        int holderId = buf.readVarInt();
        if (holderId == 0) {
            buf.readResourceLocation();
            if (buf.readBoolean()) {
                buf.readFloat();
            }
        }
    }

    private static ResourceLocation byId(List<ResourceLocation> registry, int id, String kind) {
        if (id < 0 || id >= registry.size()) {
            throw new IllegalArgumentException("Unknown 1.21.11 " + kind + " id: " + id);
        }
        return registry.get(id);
    }

    static int itemId(ResourceLocation key) {
        return ITEMS.indexOf(key);
    }

    static int componentId(ResourceLocation key) {
        return COMPONENTS.indexOf(key);
    }

    static int itemRegistrySize() {
        return ITEMS.size();
    }

    static int componentRegistrySize() {
        return COMPONENTS.size();
    }

    static ResourceLocation fallbackItem(ResourceLocation key) {
        return ITEM_FALLBACKS.get(key);
    }

    static Integer fallbackCustomModelData(ResourceLocation key) {
        return FALLBACK_CUSTOM_MODEL_DATA.get(key);
    }

    static int fallbackItemCount() {
        return ITEM_FALLBACKS.size();
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> fallback(String source, String target) {
        return Map.entry(ResourceLocation.withDefaultNamespace(source), ResourceLocation.withDefaultNamespace(target));
    }

    private static Map.Entry<ResourceLocation, Integer> fallbackModel(String source, int customModelData) {
        return Map.entry(ResourceLocation.withDefaultNamespace(source), customModelData);
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
