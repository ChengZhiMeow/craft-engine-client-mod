package net.momirealms.craftengine.neoforge.block;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.mixin.HolderReferenceInvoker;
import net.momirealms.craftengine.neoforge.util.BlockStateUtils;

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
}
