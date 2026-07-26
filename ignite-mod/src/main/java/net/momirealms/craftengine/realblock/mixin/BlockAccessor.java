package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor(value = "stateDefinition", remap = false)
    @Mutable
    void craftengine$setStateDefinition(StateDefinition<Block, BlockState> stateDefinition);

    @Accessor(value = "defaultBlockState", remap = false)
    void craftengine$setDefaultBlockState(BlockState state);
}
