package net.momirealms.craftengine.realblock.mixin;

import net.momirealms.craftengine.realblock.StateDefinitionFactoryAccess;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(StateDefinition.class)
public abstract class StateDefinitionMixin implements StateDefinitionFactoryAccess {
    @Unique
    private StateDefinition.Factory<?, ?> craftengine$factory;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private <O, S extends StateHolder<O, S>> void craftengine$captureFactory(
            Function<O, S> defaultStateGetter,
            O owner,
            StateDefinition.Factory<O, S> factory,
            Map<String, Property<?>> properties,
            CallbackInfo callback
    ) {
        this.craftengine$factory = factory;
    }

    @Override
    public StateDefinition.Factory<?, ?> craftengine$factory() {
        return this.craftengine$factory;
    }
}
