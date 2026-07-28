package net.momirealms.craftengine.realblock.mixin;

import net.momirealms.craftengine.realblock.StateHolderMixinSupport;
import net.minecraft.world.level.block.state.BlockState;
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
public abstract class StateHolderMixin<O, S extends StateHolder<O, S>> {
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends Comparable<T>> void craftengine$legacyRealBlockStateValue(
            Property<T> property,
            CallbackInfoReturnable<T> callback
    ) {
        StateHolder<?, ?> state = (StateHolder<?, ?>) (Object) this;
        if (StateHolderMixinSupport.shouldUseDefaultValue(property, state.getValues())) {
            callback.setReturnValue(property.getPossibleValues().getFirst());
        }
    }

    @Inject(method = "trySetValue", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends Comparable<T>, V extends T> void craftengine$setRealBlockStateValue(
            Property<T> property,
            V value,
            CallbackInfoReturnable<S> callback
    ) {
        if (!"ce_state".equals(property.getName())
                || !((Object) this instanceof BlockState state)
                || state.getBlock().getStateDefinition().getProperties().size() != 1
                || state.getBlock().getStateDefinition().getProperty("ce_state") != property) {
            return;
        }
        int index = property.getInternalIndex(value);
        if (index < 0) {
            return;
        }
        BlockState target = state.getBlock().getStateDefinition().getPossibleStates().get(index);
        @SuppressWarnings("unchecked")
        S result = (S) target;
        callback.setReturnValue(result);
    }

}
