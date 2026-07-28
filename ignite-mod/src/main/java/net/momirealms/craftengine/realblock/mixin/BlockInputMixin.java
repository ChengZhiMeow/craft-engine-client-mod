package net.momirealms.craftengine.realblock.mixin;

import net.momirealms.craftengine.realblock.StateHolderMixinSupport;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockInput.class)
public abstract class BlockInputMixin {
    @Redirect(
            method = {
                    "copyProperty",
                    "test(Lnet/minecraft/world/level/block/state/pattern/BlockInWorld;)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;"
                            + "getValue(Lnet/minecraft/world/level/block/state/properties/Property;)"
                            + "Ljava/lang/Comparable;"
            ),
            remap = false
    )
    private static <T extends Comparable<T>> T craftengine$realBlockStateValue(
            BlockState state,
            Property<T> property
    ) {
        if ("ce_state".equals(property.getName())) {
            return StateHolderMixinSupport.valueOrDefault(property, state.getValues());
        }
        return state.getValue(property);
    }
}
