package net.momirealms.craftengine.realblock.mixin;

import net.momirealms.craftengine.realblock.StateHolderMixinSupport;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CraftEngine's generated block-state class can bypass StateHolder#getValue.
 * Intercepting Property#value(StateHolder) covers the codec entry point itself,
 * so a synthetic ce_state always encodes as a valid property value.
 */
@Mixin(Property.class)
public abstract class PropertyMixin<T extends Comparable<T>> {
    @Inject(
            method = "value(Lnet/minecraft/world/level/block/state/StateHolder;)"
                    + "Lnet/minecraft/world/level/block/state/properties/Property$Value;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    @SuppressWarnings("unchecked")
    private void craftengine$realBlockStateValue(
            StateHolder<?, ?> state,
            CallbackInfoReturnable<Property.Value<T>> callback
    ) {
        Property<T> property = (Property<T>) (Object) this;
        if (!"ce_state".equals(property.getName())) {
            return;
        }
        T value = StateHolderMixinSupport.valueOrDefault(property, state.getValues());
        callback.setReturnValue(property.value(value));
    }
}
