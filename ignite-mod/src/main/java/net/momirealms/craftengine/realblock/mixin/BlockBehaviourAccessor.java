package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {

    @Mutable
    @Accessor("hasCollision")
    void craftengine$setHasCollision(boolean hasCollision);
}
