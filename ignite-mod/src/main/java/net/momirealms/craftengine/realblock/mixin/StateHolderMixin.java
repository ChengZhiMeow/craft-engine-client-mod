package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A CraftEngine placeholder can already be present in a loaded chunk when its
 * canonical block is upgraded to a multi-state real block. Its old state map
 * predates the synthetic ce_state property, so vanilla's codec would otherwise
 * fail while saving that chunk. Encoding it as state zero migrates the stale
 * in-memory value to the canonical real state on the next load.
 */
@Mixin(StateHolder.class)
public abstract class StateHolderMixin {
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends Comparable<T>> void craftengine$legacyRealBlockStateValue(
            Property<T> property,
            CallbackInfoReturnable<T> callback
    ) {
        StateHolder<?, ?> state = (StateHolder<?, ?>) (Object) this;
        if ("ce_state".equals(property.getName()) && !state.getValues().containsKey(property)) {
            callback.setReturnValue(property.getPossibleValues().getFirst());
        }
    }
}
