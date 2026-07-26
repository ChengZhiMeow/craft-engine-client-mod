package net.momirealms.craftengine.neoforge.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.item.ItemManager;
import net.momirealms.craftengine.neoforge.mixin.ItemStackRenderStateAccessor;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BlockItemModelMatcher {
    private static final ResourceLocation RELOAD_LISTENER_ID = ResourceLocation.fromNamespaceAndPath(CraftEngineNeoForgeMod.MOD_ID, "jade_model_matcher");
    private static final String CRAFTENGINE_ID = "craftengine:id";
    private static final UniqueIndex<ModelFingerprint, ItemStack> EXACT_MODELS = new UniqueIndex<>();
    private static final UniqueIndex<ModelFingerprint, ItemStack> TEXTURE_MODELS = new UniqueIndex<>();
    private static final DirectIndex<ResourceLocation, ItemStack> ID_ITEMS = new DirectIndex<>();
    private static final Map<BlockState, Optional<ItemStack>> BLOCK_MATCHES = new IdentityHashMap<>();
    private static boolean indexed;

    private BlockItemModelMatcher() {
    }

    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(RELOAD_LISTENER_ID, (sharedState, preparationExecutor, barrier, reloadExecutor) ->
                barrier.<Void>wait(null).thenRunAsync(BlockItemModelMatcher::invalidate, reloadExecutor));
    }

    public static synchronized void invalidate() {
        indexed = false;
        EXACT_MODELS.clear();
        TEXTURE_MODELS.clear();
        ID_ITEMS.clear();
        BLOCK_MATCHES.clear();
    }

    public static synchronized Optional<ItemStack> findById(ResourceLocation id) {
        ensureIndex();
        if (!indexed) {
            return Optional.empty();
        }
        return ID_ITEMS.find(id).map(ItemStack::copy);
    }

    public static synchronized Optional<ItemStack> find(BlockState blockState) {
        ensureIndex();
        if (!indexed) {
            return Optional.empty();
        }
        if (BLOCK_MATCHES.containsKey(blockState)) {
            return BLOCK_MATCHES.get(blockState).map(ItemStack::copy);
        }

        ModelFingerprints fingerprints = fingerprints(blockState);
        if (fingerprints == null) {
            BLOCK_MATCHES.put(blockState, Optional.empty());
            return Optional.empty();
        }

        ModelFingerprints ownItem = fingerprints(Minecraft.getInstance(), blockState.getBlock().asItem().getDefaultInstance());
        if (ownItem != null && (ownItem.exact().equals(fingerprints.exact()) || ownItem.texture().equals(fingerprints.texture()))) {
            BLOCK_MATCHES.put(blockState, Optional.empty());
            return Optional.empty();
        }

        Optional<ItemStack> exact = EXACT_MODELS.find(fingerprints.exact());
        Optional<ItemStack> result = exact.isPresent() ? exact : TEXTURE_MODELS.find(fingerprints.texture());
        BLOCK_MATCHES.put(blockState, result.map(ItemStack::copy));
        return result.map(ItemStack::copy);
    }

    private static void ensureIndex() {
        if (indexed) {
            return;
        }
        ItemManager itemManager = ItemManager.instance();
        if (itemManager == null) {
            return;
        }

        List<ItemStack> items = itemManager.creativeTabItems();
        if (items.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        for (ItemStack item : items) {
            String customId = customId(item);
            if (customId == null) {
                continue;
            }
            ResourceLocation itemId = ResourceLocation.tryParse(customId);
            if (itemId == null) {
                continue;
            }
            ID_ITEMS.add(itemId, item.copy());
            try {
                ModelFingerprints fingerprints = fingerprints(minecraft, item);
                if (fingerprints == null) {
                    continue;
                }
                EXACT_MODELS.add(fingerprints.exact(), customId, item.copy());
                TEXTURE_MODELS.add(fingerprints.texture(), customId, item.copy());
            } catch (RuntimeException exception) {
                CraftEngineNeoForgeMod.instance().logger().warn("Unable to inspect the model for CraftEngine item " + customId, exception);
            }
        }
        indexed = true;
    }

    private static String customId(ItemStack item) {
        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains(CRAFTENGINE_ID)) {
            return null;
        }
        return customData.copyTag().getString(CRAFTENGINE_ID).orElse(null);
    }

    private static ModelFingerprints fingerprints(BlockState blockState) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockStateModel model = minecraft.getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockModelPart part : model.collectParts(RandomSource.create(0L))) {
            quads.addAll(part.getQuads(null));
            for (Direction direction : Direction.values()) {
                quads.addAll(part.getQuads(direction));
            }
        }
        return fingerprints(quads);
    }

    private static ModelFingerprints fingerprints(Minecraft minecraft, ItemStack item) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(
                renderState,
                item,
                ItemDisplayContext.GUI,
                minecraft.level,
                minecraft.player,
                0
        );
        ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) renderState;
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < accessor.ce$activeLayerCount(); i++) {
            quads.addAll(accessor.ce$layers()[i].prepareQuadList());
        }
        return fingerprints(quads);
    }

    private static ModelFingerprints fingerprints(List<BakedQuad> quads) {
        if (quads.isEmpty()) {
            return null;
        }

        List<String> exact = new ArrayList<>(quads.size());
        List<String> textures = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            String common = quad.sprite().contents().name() + "|" + quad.tintIndex() + "|" + quad.shade()
                    + "|" + quad.lightEmission() + "|" + quad.hasAmbientOcclusion();
            textures.add(common);
            exact.add(common + "|" + quad.direction() + "|" + Arrays.toString(quad.vertices()));
        }
        exact.sort(String::compareTo);
        textures.sort(String::compareTo);
        return new ModelFingerprints(new ModelFingerprint(exact), new ModelFingerprint(textures));
    }

    private record ModelFingerprints(ModelFingerprint exact, ModelFingerprint texture) {
    }

    private record ModelFingerprint(List<String> quads) {
        private ModelFingerprint {
            quads = List.copyOf(quads);
        }
    }

    static final class UniqueIndex<K, V> {
        private final Map<K, Candidate<V>> candidates = new HashMap<>();
        private final Set<K> ambiguous = new HashSet<>();

        void add(K key, String identity, V value) {
            if (ambiguous.contains(key)) {
                return;
            }
            Candidate<V> existing = candidates.get(key);
            if (existing == null) {
                candidates.put(key, new Candidate<>(identity, value));
            } else if (!existing.identity().equals(identity)) {
                candidates.remove(key);
                ambiguous.add(key);
            }
        }

        Optional<V> find(K key) {
            Candidate<V> candidate = candidates.get(key);
            return candidate == null ? Optional.empty() : Optional.of(candidate.value());
        }

        void clear() {
            candidates.clear();
            ambiguous.clear();
        }
    }

    static final class DirectIndex<K, V> {
        private final Map<K, V> values = new HashMap<>();

        void add(K key, V value) {
            values.putIfAbsent(key, value);
        }

        Optional<V> find(K key) {
            return Optional.ofNullable(values.get(key));
        }

        void clear() {
            values.clear();
        }
    }

    private record Candidate<V>(String identity, V value) {
    }
}
