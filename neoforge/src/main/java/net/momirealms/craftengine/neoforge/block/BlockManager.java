package net.momirealms.craftengine.neoforge.block;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.mixin.HolderReferenceInvoker;
import net.momirealms.craftengine.neoforge.mixin.IdMapperAccessor;
import net.momirealms.craftengine.neoforge.mixin.MappedRegistryAccessor;
import net.momirealms.craftengine.neoforge.mixin.PropertiesAccessor;
import net.momirealms.craftengine.neoforge.mixin.BlockAccessor;
import net.momirealms.craftengine.neoforge.mixin.BlockBehaviourAccessor;
import net.momirealms.craftengine.neoforge.util.BlockStateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockManager {
    private static BlockManager instance;
    private final CraftEngineNeoForgeMod mod;
    private final CraftEngineBlock[] customBlocks;
    private final CraftEngineBlockState[] customBlockStates;
    private final Holder.Reference<Block>[] customBlockHolders;
    private int[] stateMappings;
    private final AtomicBoolean handleVisualBlockStateBatchFinished = new AtomicBoolean(true);
    private VisualBlockStatesData visualBlockStatesData;
    private final List<RealBlockInstallation> realBlockInstallations = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public BlockManager(CraftEngineNeoForgeMod mod) {
        instance = this;
        this.mod = mod;
        this.customBlocks = new CraftEngineBlock[ModConfig.INSTANCE.serverSideBlocks()];
        this.customBlockStates = new CraftEngineBlockState[ModConfig.INSTANCE.serverSideBlocks()];
        this.customBlockHolders = new Holder.Reference[ModConfig.INSTANCE.serverSideBlocks()];
        this.registerServerSideCustomBlocks(ModConfig.INSTANCE.serverSideBlocks());
    }

    public static BlockManager instance() {
        return instance;
    }

    public void completeRegistration() {
        if (customBlockStates.length == 0) {
            throw new IllegalStateException("server-side-blocks must be greater than zero");
        }
        int vanillaStateCount = Block.BLOCK_STATE_REGISTRY.getId(customBlockStates[0]);
        if (vanillaStateCount < 0) {
            throw new IllegalStateException("CraftEngine custom block states were not added to the global state registry");
        }
        this.mod.logger().info("Vanilla block count: " + vanillaStateCount);
        BlockStateUtils.init(vanillaStateCount);
        this.stateMappings = new int[Block.BLOCK_STATE_REGISTRY.size()];
        for (int i = 0; i < this.stateMappings.length; i++) {
            this.stateMappings[i] = i;
        }
        for (int i = 0; i < customBlockStates.length; i++) {
            int expected = vanillaStateCount + i;
            int actual = Block.BLOCK_STATE_REGISTRY.getId(customBlockStates[i]);
            if (expected != actual) {
                throw new IllegalStateException("BlockState ID mismatch for " + customBlockStates[i]
                        + " (expected " + expected + ", got " + actual + ")");
            }
        }
        this.mod.logger().info("Registered " + customBlockStates.length + " custom blocks.");
    }

    private void registerServerSideCustomBlocks(int count) {
        for (int i = 0; i < count; i++) {
            ResourceLocation customBlockId = ResourceLocation.fromNamespaceAndPath("craftengine", "custom_" + i);
            CraftEngineBlock customBlock = CraftEngineBlock.generateBlock(customBlockId);
            this.customBlocks[i] = customBlock;
            Holder.Reference<Block> blockHolder = Registry.registerForHolder(BuiltInRegistries.BLOCK, customBlockId, customBlock);
            this.customBlockHolders[i] = blockHolder;
            @SuppressWarnings("unchecked")
            HolderReferenceInvoker<Block> holderReferenceInvoker = (HolderReferenceInvoker<Block>) blockHolder;
            holderReferenceInvoker.ce$callBindValue(customBlock);
            holderReferenceInvoker.ce$tags(Set.of());
            CraftEngineBlockState newBlockState = (CraftEngineBlockState) customBlock.defaultBlockState();
            this.customBlockStates[i] = newBlockState;
        }
    }

    public CraftEngineBlock[] customBlocks() {
        return this.customBlocks;
    }

    public CraftEngineBlockState[] customBlockStates() {
        return this.customBlockStates;
    }

    public Holder.Reference<Block>[] customBlockHolders() {
        return this.customBlockHolders;
    }

    public int remapState(int state) {
        return this.stateMappings[state];
    }

    public void remapState(int state, int newState) {
        this.stateMappings[state] = newState;
    }

    public void handleVisualBlockStateBatchStart(int size) {
        if (!this.handleVisualBlockStateBatchFinished.compareAndSet(true, false)) return;
        this.visualBlockStatesData = new VisualBlockStatesData(size);
    }

    public void handleVisualBlockStates(int startIndex, int[] data) {
        if (this.visualBlockStatesData == null || this.visualBlockStatesData.isReceived()) return;
        this.visualBlockStatesData.receiveDataChunk(startIndex, data);
    }

    public void handleVisualBlockStateBatchFinished() {
        if (this.visualBlockStatesData == null || this.visualBlockStatesData.isReceived()) return;
        this.visualBlockStatesData.setReceived();
        int[] data = this.visualBlockStatesData.data;
        for (int i = 0; i < data.length; i++) {
            int customId = i + BlockStateUtils.vanillaStateSize();
            int vanillaId = data[i];
            if (vanillaId == -1) continue;
            BlockStateUtils.handleRemap(customId, vanillaId);
        }
        this.handleVisualBlockStateBatchFinished.set(true);
    }

    public void handleTags() {
        if (this.visualBlockStatesData == null || !this.visualBlockStatesData.isReceived()) return;
        int[] data = this.visualBlockStatesData.data;
        for (int i = 0; i < data.length; i++) {
            int customId = i + BlockStateUtils.vanillaStateSize();
            int vanillaId = data[i];
            if (vanillaId == -1) continue;
            BlockStateUtils.handleRemapTag(customId, vanillaId);
        }
    }

    public synchronized void installRealBlocks(List<RealBlockDefinition> definitions) {
        clearRealBlocks();
        if (definitions.isEmpty()) {
            return;
        }

        Set<ResourceLocation> ids = new HashSet<>();
        Set<Integer> usedIndices = new HashSet<>();
        for (RealBlockDefinition definition : definitions) {
            if (!ids.add(definition.id())) {
                throw new IllegalArgumentException("Duplicate real block id " + definition.id());
            }
            if (BuiltInRegistries.BLOCK.containsKey(definition.id())) {
                throw new IllegalArgumentException("Block id is already registered: " + definition.id());
            }
            if (definition.states().isEmpty()) {
                throw new IllegalArgumentException("Real block has no states: " + definition.id());
            }
            for (RealBlockStateDefinition state : definition.states()) {
                if (state.customIndex() < 0 || state.customIndex() >= customBlockStates.length) {
                    throw new IllegalArgumentException("Custom state index out of range for " + definition.id() + ": " + state.customIndex());
                }
                if (!usedIndices.add(state.customIndex())) {
                    throw new IllegalArgumentException("Custom state index is used more than once: " + state.customIndex());
                }
            }
        }

        for (RealBlockDefinition definition : definitions) {
            installRealBlock(definition);
        }
        this.mod.logger().info("Registered " + definitions.size() + " CraftEngine real block IDs.");
    }

    private void installRealBlock(RealBlockDefinition definition) {
        int stateCount = definition.states().size();
        int firstIndex = definition.states().getFirst().customIndex();
        CraftEngineBlock realBlock = customBlocks[firstIndex];
        ResourceLocation originalId = BuiltInRegistries.BLOCK.getKey(realBlock);
        if (originalId == null) {
            throw new IllegalStateException("Missing registry key for CraftEngine custom block " + firstIndex);
        }

        StateDefinition<Block, BlockState> oldStateDefinition = realBlock.getStateDefinition();
        BlockState oldDefaultState = realBlock.defaultBlockState();
        CraftEngineBlock[] oldBlocks = new CraftEngineBlock[stateCount];
        CraftEngineBlockState[] oldStates = new CraftEngineBlockState[stateCount];
        for (int i = 0; i < stateCount; i++) {
            int customIndex = definition.states().get(i).customIndex();
            oldBlocks[i] = customBlocks[customIndex];
            oldStates[i] = customBlockStates[customIndex];
        }

        StateDefinition<Block, BlockState> newStateDefinition = CraftEngineBlock.rebuildStateDefinition(realBlock, stateCount);
        List<BlockState> newStates = newStateDefinition.getPossibleStates();
        IdMapperAccessor<BlockState> stateRegistry = (IdMapperAccessor<BlockState>) (Object) Block.BLOCK_STATE_REGISTRY;
        boolean hasCollision = false;
        for (int i = 0; i < stateCount; i++) {
            RealBlockStateDefinition stateDefinition = definition.states().get(i);
            int customIndex = stateDefinition.customIndex();
            int rawStateId = BlockStateUtils.vanillaStateSize() + customIndex;
            CraftEngineBlockState realState = (CraftEngineBlockState) newStates.get(i);
            realState.setRealShape(stateDefinition.shape());
            hasCollision |= !stateDefinition.shape().collisionShape().isEmpty();

            stateRegistry.ce$toId().removeInt(oldStates[i]);
            Block.BLOCK_STATE_REGISTRY.addMapping(realState, rawStateId);
            customBlocks[customIndex] = realBlock;
            customBlockStates[customIndex] = realState;

            int visualId = visualStateId(customIndex);
            if (visualId >= 0) {
                BlockStateUtils.handleRemap(rawStateId, visualId);
            }
        }
        ((BlockBehaviourAccessor) realBlock).ce$hasCollision(hasCollision);
        renameBlock(realBlock, customBlockHolders[firstIndex], originalId, definition.id());
        realBlockInstallations.add(new RealBlockInstallation(
                definition.id(),
                originalId,
                firstIndex,
                definition.states().stream().mapToInt(RealBlockStateDefinition::customIndex).toArray(),
                realBlock,
                oldStateDefinition,
                oldDefaultState,
                oldBlocks,
                oldStates
        ));
    }

    public synchronized void clearRealBlocks() {
        IdMapperAccessor<BlockState> stateRegistry = (IdMapperAccessor<BlockState>) (Object) Block.BLOCK_STATE_REGISTRY;
        for (int installationIndex = realBlockInstallations.size() - 1; installationIndex >= 0; installationIndex--) {
            RealBlockInstallation installation = realBlockInstallations.get(installationIndex);
            renameBlock(
                    installation.realBlock(),
                    customBlockHolders[installation.firstIndex()],
                    installation.realId(),
                    installation.originalId()
            );
            BlockAccessor blockAccessor = (BlockAccessor) installation.realBlock();
            blockAccessor.ce$setStateDefinition(installation.oldStateDefinition());
            blockAccessor.ce$setDefaultBlockState(installation.oldDefaultState());

            for (int i = 0; i < installation.customIndices().length; i++) {
                int customIndex = installation.customIndices()[i];
                int rawStateId = BlockStateUtils.vanillaStateSize() + customIndex;
                BlockState current = customBlockStates[customIndex];
                stateRegistry.ce$toId().removeInt(current);
                Block.BLOCK_STATE_REGISTRY.addMapping(installation.oldStates()[i], rawStateId);
                customBlocks[customIndex] = installation.oldBlocks()[i];
                customBlockStates[customIndex] = installation.oldStates()[i];
                int visualId = visualStateId(customIndex);
                if (visualId >= 0) {
                    BlockStateUtils.handleRemap(rawStateId, visualId);
                }
            }
        }
        realBlockInstallations.clear();
    }

    public synchronized Optional<ResourceLocation> realBlockId(Block block) {
        return realBlockInstallations.stream()
                .filter(installation -> installation.realBlock() == block)
                .map(RealBlockInstallation::realId)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private void renameBlock(CraftEngineBlock block, Holder.Reference<Block> holder, ResourceLocation from, ResourceLocation to) {
        MappedRegistry<Block> registry = (MappedRegistry<Block>) BuiltInRegistries.BLOCK;
        MappedRegistryAccessor<Block> accessor = (MappedRegistryAccessor<Block>) (Object) registry;
        ResourceKey<Block> fromKey = ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, from);
        ResourceKey<Block> toKey = ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, to);
        RegistrationInfo registrationInfo = accessor.ce$registrationInfos().remove(fromKey);
        accessor.ce$byLocation().remove(from);
        accessor.ce$byKey().remove(fromKey);
        ((HolderReferenceInvoker<Block>) holder).ce$key(toKey);
        accessor.ce$byLocation().put(to, holder);
        accessor.ce$byKey().put(toKey, holder);
        if (registrationInfo != null) {
            accessor.ce$registrationInfos().put(toKey, registrationInfo);
        }
        ((PropertiesAccessor) ((BlockBehaviourAccessor) block).ce$properties()).ce$setId(toKey);
        ((BlockBehaviourAccessor) block).ce$descriptionId(Util.makeDescriptionId("block", to));
    }

    private int visualStateId(int customIndex) {
        if (visualBlockStatesData == null || !visualBlockStatesData.isReceived() || customIndex >= visualBlockStatesData.data.length) {
            return -1;
        }
        return visualBlockStatesData.data[customIndex];
    }

    public record RealBlockDefinition(ResourceLocation id, List<RealBlockStateDefinition> states) {
        public RealBlockDefinition {
            states = List.copyOf(states);
        }
    }

    public record RealBlockStateDefinition(int customIndex, RealBlockShape shape) {
    }

    private record RealBlockInstallation(
            ResourceLocation realId,
            ResourceLocation originalId,
            int firstIndex,
            int[] customIndices,
            CraftEngineBlock realBlock,
            StateDefinition<Block, BlockState> oldStateDefinition,
            BlockState oldDefaultState,
            CraftEngineBlock[] oldBlocks,
            CraftEngineBlockState[] oldStates
    ) {
    }
}
