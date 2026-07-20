package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.momirealms.craftengine.neoforge.block.CraftEngineBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public abstract class ItemBlockRenderTypesMixin {
    @Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
    private static void craftengine$getChunkRenderType(BlockState state, CallbackInfoReturnable<ChunkSectionLayer> cir) {
        if (!(state instanceof CraftEngineBlockState customState)) return;
        BlockState visualState = customState.visualBlockState();
        cir.setReturnValue(visualState.getBlock() instanceof LeavesBlock
                ? ChunkSectionLayer.CUTOUT_MIPPED
                : ItemBlockRenderTypes.getChunkRenderType(visualState));
    }
}
