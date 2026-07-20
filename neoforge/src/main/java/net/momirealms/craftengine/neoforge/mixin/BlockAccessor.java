package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessor {

    @Mutable
    @Accessor("stateDefinition")
    void ce$setStateDefinition(StateDefinition<Block, BlockState> stateDefinition);

    @Accessor("defaultBlockState")
    void ce$setDefaultBlockState(BlockState defaultBlockState);
}
