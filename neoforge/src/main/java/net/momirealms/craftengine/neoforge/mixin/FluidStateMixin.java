package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidState.class)
public class FluidStateMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void ce$cancelScheduledTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (!ModConfig.INSTANCE.enableCancelBlockUpdate() || !NetworkManager.instance().serverInstalled()) return;
        ci.cancel();
    }
}
