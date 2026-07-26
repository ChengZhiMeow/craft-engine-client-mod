package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.realblock.RealBlockStateRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true, remap = false)
    private void craftengine$realOcclusion(CallbackInfoReturnable<VoxelShape> callback) {
        VoxelShape shape = RealBlockStateRuntime.occlusion((BlockState) (Object) this);
        if (shape != null) {
            callback.setReturnValue(shape);
        }
    }

    @Inject(method = "getFaceOcclusionShape", at = @At("HEAD"), cancellable = true, remap = false)
    private void craftengine$realFaceOcclusion(
            Direction direction,
            CallbackInfoReturnable<VoxelShape> callback
    ) {
        VoxelShape shape = RealBlockStateRuntime.occlusion((BlockState) (Object) this);
        if (shape != null) {
            callback.setReturnValue(shape.getFaceShape(direction));
        }
    }

    @Inject(method = "getSoundType", at = @At("HEAD"), cancellable = true, remap = false)
    private void craftengine$realSound(CallbackInfoReturnable<SoundType> callback) {
        BlockState original = RealBlockStateRuntime.original((BlockState) (Object) this);
        if (original != null) {
            callback.setReturnValue(original.getSoundType());
        }
    }

    @Inject(
            method = "is(Lnet/minecraft/tags/TagKey;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void craftengine$realTags(TagKey<Block> tag, CallbackInfoReturnable<Boolean> callback) {
        BlockState original = RealBlockStateRuntime.original((BlockState) (Object) this);
        if (original != null) {
            callback.setReturnValue(original.is(tag));
        }
    }

    @Inject(method = "getTags", at = @At("HEAD"), cancellable = true, remap = false)
    private void craftengine$realTagStream(CallbackInfoReturnable<Stream<TagKey<Block>>> callback) {
        BlockState original = RealBlockStateRuntime.original((BlockState) (Object) this);
        if (original != null) {
            callback.setReturnValue(original.getTags());
        }
    }
}
