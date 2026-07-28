package net.momirealms.craftengine.realblock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.realblock.mixin.BlockAccessor;
import net.momirealms.craftengine.realblock.mixin.BlockBehaviourAccessor;
import net.momirealms.craftengine.realblock.mixin.HolderReferenceAccessor;
import net.momirealms.craftengine.realblock.mixin.IdMapperAccessor;
import net.momirealms.craftengine.realblock.mixin.MappedRegistryAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Called by the Paper companion plugin after CraftEngine has produced its
 * configured states, but before Paper loads worlds.
 */
public final class RealBlockRegistryBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("CraftEngineRealBlock");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path MANIFEST = Path.of("plugins", "CraftEngine", "real_blocks.registry.json");
    private static final String STATE_PROPERTY = "ce_state";
    private static final Map<String, Installation> INSTALLATIONS = new LinkedHashMap<>();
    private static boolean persistedInstalled;

    private RealBlockRegistryBridge() {
    }

    public static String runtimeVersion() {
        return "1.21.11";
    }

    public static synchronized void installPersisted() {
        if (persistedInstalled) {
            return;
        }
        if (!Files.isRegularFile(MANIFEST)) {
            persistedInstalled = true;
            LOGGER.info("No persisted CraftEngine real-block registry manifest yet");
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(MANIFEST, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null || root.get("format") == null || root.get("format").getAsInt() != 1) {
                throw new IllegalStateException("unsupported real-block registry manifest format");
            }
            String minecraft = root.get("minecraft").getAsString();
            if (!runtimeVersion().equals(minecraft)) {
                throw new IllegalStateException(
                        "real-block manifest targets Minecraft " + minecraft + ", expected " + runtimeVersion()
                );
            }
            JsonObject blocks = root.getAsJsonObject("blocks");
            if (blocks == null) {
                throw new IllegalStateException("real-block registry manifest has no blocks object");
            }
            for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                List<StateInput> inputs = new ArrayList<>();
                for (JsonElement rawIdElement : entry.getValue().getAsJsonArray()) {
                    int rawId = rawIdElement.getAsInt();
                    BlockState state = Block.BLOCK_STATE_REGISTRY.byId(rawId);
                    if (state == null) {
                        throw new IllegalStateException(
                                "persisted raw block state id is unavailable for " + entry.getKey() + ": " + rawId
                        );
                    }
                    inputs.add(new StateInput(state, rawId));
                }
                install(entry.getKey(), inputs);
                LOGGER.info("Installed persisted real block {} before world loading", entry.getKey());
            }
            persistedInstalled = true;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("unable to install persisted CraftEngine real blocks", exception);
        }
    }

    public static synchronized void persist(String id, List<Integer> rawStateIds) {
        try {
            JsonObject root;
            if (Files.isRegularFile(MANIFEST)) {
                root = GSON.fromJson(Files.readString(MANIFEST, StandardCharsets.UTF_8), JsonObject.class);
            } else {
                root = new JsonObject();
            }
            root.addProperty("format", 1);
            root.addProperty("minecraft", runtimeVersion());
            JsonObject blocks = root.has("blocks") && root.get("blocks").isJsonObject()
                    ? root.getAsJsonObject("blocks")
                    : new JsonObject();
            root.add("blocks", blocks);
            blocks.add(id, GSON.toJsonTree(rawStateIds));
            Files.createDirectories(MANIFEST.getParent());
            Files.writeString(MANIFEST, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("unable to persist real-block registry manifest", exception);
        }
    }

    public static synchronized void configureOcclusion(List<Object> states, Object shape) {
        if (!(shape instanceof VoxelShape voxelShape)) {
            throw new IllegalArgumentException("occlusion shape is not a Minecraft VoxelShape");
        }
        for (Object state : states) {
            if (!(state instanceof BlockState blockState)) {
                throw new IllegalArgumentException("real state is not a Minecraft BlockState");
            }
            RealBlockStateRuntime.setOcclusion(blockState, voxelShape);
        }
    }

    public static synchronized void configureCollision(Object block, boolean hasCollision) {
        if (!(block instanceof Block realBlock)) {
            throw new IllegalArgumentException("real block is not a Minecraft Block");
        }
        ((BlockBehaviourAccessor) (Object) realBlock).craftengine$setHasCollision(hasCollision);
    }

    public static synchronized void refreshStateCaches(List<Object> states) {
        for (Object state : states) {
            if (!(state instanceof BlockState blockState)) {
                throw new IllegalArgumentException("real state is not a Minecraft BlockState");
            }
            blockState.initCache();
        }
    }

    public static synchronized void copyStateSettings(List<StateCopy> states) {
        for (StateCopy state : states) {
            if (!(state.sourceState() instanceof BlockState source)) {
                throw new IllegalArgumentException("source state is not a Minecraft BlockState");
            }
            if (!(state.targetState() instanceof BlockState target)) {
                throw new IllegalArgumentException("target state is not a Minecraft BlockState");
            }
            if (source != target) {
                copyStateSettings(source, target);
            }
        }
    }

    public static synchronized Installation install(String id, List<StateInput> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("real block must contain at least one state");
        }
        Installation existing = INSTALLATIONS.get(id);
        if (existing != null) {
            List<Integer> requestedIds = inputs.stream().map(StateInput::rawStateId).toList();
            List<Integer> installedIds = existing.states().stream().map(StateMapping::rawStateId).toList();
            if (!requestedIds.equals(installedIds)) {
                persist(id, requestedIds);
                throw new IllegalStateException(
                        "real block state topology changed for " + id
                                + "; the new registry manifest was written, restart the server once more"
                );
            }
            return existing;
        }

        Identifier realId = Identifier.parse(id);
        if ("minecraft".equals(realId.getNamespace())) {
            throw new IllegalArgumentException("real_block cannot replace a minecraft namespace id: " + id);
        }

        List<BlockState> originalStates = new ArrayList<>(inputs.size());
        Set<Integer> rawIds = new HashSet<>();
        for (StateInput input : inputs) {
            if (!(input.minecraftState() instanceof BlockState state)) {
                throw new IllegalArgumentException("state " + input.rawStateId() + " is not a Minecraft BlockState");
            }
            if (!rawIds.add(input.rawStateId())) {
                throw new IllegalArgumentException("duplicate raw block state id " + input.rawStateId());
            }
            if (Block.BLOCK_STATE_REGISTRY.byId(input.rawStateId()) != state) {
                throw new IllegalStateException("raw block state id does not match CraftEngine state " + input.rawStateId());
            }
            Identifier sourceId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (sourceId == null
                    || !"craftengine".equals(sourceId.getNamespace())
                    || !sourceId.getPath().startsWith("custom_")) {
                throw new IllegalStateException(
                        "raw block state id " + input.rawStateId() + " is not a CraftEngine injected state"
                );
            }
            originalStates.add(state);
        }

        Block canonicalBlock = originalStates.getFirst().getBlock();
        MappedRegistry<Block> blockRegistry = castRegistry(BuiltInRegistries.BLOCK);
        Identifier originalId = blockRegistry.getKey(canonicalBlock);
        if (originalId == null) {
            throw new IllegalStateException("canonical CraftEngine block is not registered");
        }
        Block occupied = blockRegistry.getValue(realId);
        if (RegistryOccupancy.isOccupied(blockRegistry.containsKey(realId), occupied, canonicalBlock)) {
            throw new IllegalArgumentException("block id is already registered: " + realId);
        }

        List<BlockState> realStates;
        if (originalStates.size() == 1) {
            realStates = List.of(originalStates.getFirst());
        } else {
            StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(canonicalBlock);
            builder.add(IntegerProperty.create(STATE_PROPERTY, 0, originalStates.size() - 1));
            StateDefinition<Block, BlockState> definition = builder.create(
                    Block::defaultBlockState,
                    CraftEngineBlockStateFactory.from(originalStates.getFirst())
            );
            ((BlockAccessor) canonicalBlock).craftengine$setStateDefinition(definition);
            ((BlockAccessor) canonicalBlock).craftengine$setDefaultBlockState(definition.any());
            realStates = List.copyOf(definition.getPossibleStates());
            for (int index = 0; index < realStates.size(); index++) {
                copyStateSettings(originalStates.get(index), realStates.get(index));
                realStates.get(index).initCache();
            }
        }

        renameBlock(blockRegistry, canonicalBlock, originalId, realId);
        replaceStateMappings(inputs, originalStates, realStates);

        List<StateMapping> mappings = new ArrayList<>(inputs.size());
        for (int index = 0; index < inputs.size(); index++) {
            RealBlockStateRuntime.setOriginal(realStates.get(index), originalStates.get(index));
            mappings.add(new StateMapping(
                    inputs.get(index).rawStateId(),
                    originalStates.get(index),
                    realStates.get(index),
                    originalStates.get(index).getBlock()
            ));
        }
        Installation installation = new Installation(
                id,
                originalId.toString(),
                canonicalBlock,
                List.copyOf(mappings)
        );
        INSTALLATIONS.put(id, installation);
        return installation;
    }

    @SuppressWarnings("unchecked")
    private static MappedRegistry<Block> castRegistry(Object registry) {
        return (MappedRegistry<Block>) registry;
    }

    @SuppressWarnings("unchecked")
    private static void renameBlock(
            MappedRegistry<Block> registry,
            Block block,
            Identifier from,
            Identifier to
    ) {
        MappedRegistryAccessor<Block> accessor = (MappedRegistryAccessor<Block>) (Object) registry;
        Holder.Reference<Block> holder = accessor.craftengine$byValue().get(block);
        if (holder == null) {
            throw new IllegalStateException("missing holder for CraftEngine block " + from);
        }
        ResourceKey<Block> fromKey = ResourceKey.create(Registries.BLOCK, from);
        ResourceKey<Block> toKey = ResourceKey.create(Registries.BLOCK, to);
        RegistrationInfo info = accessor.craftengine$registrationInfos().remove(fromKey);
        accessor.craftengine$byLocation().remove(from);
        accessor.craftengine$byKey().remove(fromKey);
        ((HolderReferenceAccessor<Block>) (Object) holder).craftengine$setKey(toKey);
        accessor.craftengine$byLocation().put(to, holder);
        accessor.craftengine$byKey().put(toKey, holder);
        if (info != null) {
            accessor.craftengine$registrationInfos().put(toKey, info);
        }
        block.properties().setId(toKey);
    }

    @SuppressWarnings("unchecked")
    private static void replaceStateMappings(
            List<StateInput> inputs,
            List<BlockState> originalStates,
            List<BlockState> realStates
    ) {
        IdMapper<BlockState> registry = Block.BLOCK_STATE_REGISTRY;
        IdMapperAccessor<BlockState> accessor = (IdMapperAccessor<BlockState>) (Object) registry;
        Reference2IntMap<BlockState> toId = accessor.craftengine$toId();
        for (int index = 0; index < inputs.size(); index++) {
            int rawId = inputs.get(index).rawStateId();
            BlockState oldState = originalStates.get(index);
            BlockState realState = realStates.get(index);
            toId.removeInt(oldState);
            registry.addMapping(realState, rawId);
        }
    }

    private static void copyStateSettings(BlockState source, BlockState target) {
        ((BlockStateSettingsBridge) (Object) target).craftengine$copySettingsFrom(source);
    }

    public record StateInput(Object minecraftState, int rawStateId) {
    }

    public record StateCopy(Object sourceState, Object targetState) {
    }

    public record StateMapping(int rawStateId, Object originalState, Object realState, Object originalBlock) {
    }

    public record Installation(String id, String replacedId, Object realBlock, List<StateMapping> states) {
        public Installation {
            states = List.copyOf(states);
        }
    }
}
