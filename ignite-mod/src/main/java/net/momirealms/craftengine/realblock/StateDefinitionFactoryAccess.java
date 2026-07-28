package net.momirealms.craftengine.realblock;

import net.minecraft.world.level.block.state.StateDefinition;

public interface StateDefinitionFactoryAccess {
    StateDefinition.Factory<?, ?> craftengine$factory();
}
