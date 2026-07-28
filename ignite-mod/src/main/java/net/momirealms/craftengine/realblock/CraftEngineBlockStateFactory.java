package net.momirealms.craftengine.realblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

final class CraftEngineBlockStateFactory {
    private CraftEngineBlockStateFactory() {
    }

    static StateDefinition.Factory<Block, BlockState> from(BlockState template) {
        if (template.getClass() == BlockState.class) {
            throw new IllegalArgumentException("CraftEngine block state template is not runtime-generated");
        }
        Object definition = template.getBlock().getStateDefinition();
        if (!(definition instanceof StateDefinitionFactoryAccess access)) {
            throw new IllegalStateException("StateDefinition factory Mixin is unavailable");
        }
        return from(access);
    }

    static StateDefinition.Factory<Block, BlockState> from(StateDefinitionFactoryAccess access) {
        return cast(access.craftengine$factory());
    }

    @SuppressWarnings("unchecked")
    private static StateDefinition.Factory<Block, BlockState> cast(
            StateDefinition.Factory<?, ?> factory
    ) {
        return (StateDefinition.Factory<Block, BlockState>) factory;
    }
}
