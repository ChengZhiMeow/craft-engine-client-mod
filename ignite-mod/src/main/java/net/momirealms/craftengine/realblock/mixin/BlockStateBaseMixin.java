package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.realblock.BlockStateSettingsBridge;
import net.momirealms.craftengine.realblock.RealBlockStateRuntime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements BlockStateSettingsBridge {
    @Shadow(remap = false)
    @Final
    @Mutable
    private int lightEmission;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean useShapeForLightOcclusion;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean isAir;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean ignitedByLava;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean liquid;
    @Shadow(remap = false)
    private boolean legacySolid;
    @Shadow(remap = false)
    @Final
    @Mutable
    private PushReaction pushReaction;
    @Shadow(remap = false)
    @Final
    @Mutable
    private MapColor mapColor;
    @Shadow(remap = false)
    @Final
    @Mutable
    public float destroySpeed;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean requiresCorrectToolForDrops;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean canOcclude;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.StatePredicate isRedstoneConductor;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.StatePredicate isSuffocating;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.StatePredicate isViewBlocking;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.StatePredicate hasPostProcess;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.StatePredicate emissiveRendering;
    @Shadow(remap = false)
    @Final
    @Mutable
    private BlockBehaviour.OffsetFunction offsetFunction;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean spawnTerrainParticles;
    @Shadow(remap = false)
    @Final
    @Mutable
    private NoteBlockInstrument instrument;
    @Shadow(remap = false)
    @Final
    @Mutable
    private boolean replaceable;
    @Shadow(remap = false)
    private boolean isRandomlyTicking;

    @Override
    public Settings craftengine$settings() {
        return new Settings(
                this.lightEmission,
                this.useShapeForLightOcclusion,
                this.isAir,
                this.ignitedByLava,
                this.liquid,
                this.legacySolid,
                this.pushReaction,
                this.mapColor,
                this.destroySpeed,
                this.requiresCorrectToolForDrops,
                this.canOcclude,
                this.isRedstoneConductor,
                this.isSuffocating,
                this.isViewBlocking,
                this.hasPostProcess,
                this.emissiveRendering,
                this.offsetFunction,
                this.spawnTerrainParticles,
                this.instrument,
                this.replaceable,
                this.isRandomlyTicking
        );
    }

    @Override
    public void craftengine$copySettingsFrom(BlockState source) {
        Settings settings = ((BlockStateSettingsBridge) (Object) source).craftengine$settings();
        this.lightEmission = settings.lightEmission();
        this.useShapeForLightOcclusion = settings.useShapeForLightOcclusion();
        this.isAir = settings.air();
        this.ignitedByLava = settings.ignitedByLava();
        this.liquid = settings.liquid();
        this.legacySolid = settings.legacySolid();
        this.pushReaction = settings.pushReaction();
        this.mapColor = settings.mapColor();
        this.destroySpeed = settings.destroySpeed();
        this.requiresCorrectToolForDrops = settings.requiresCorrectToolForDrops();
        this.canOcclude = settings.canOcclude();
        this.isRedstoneConductor = settings.redstoneConductor();
        this.isSuffocating = settings.suffocating();
        this.isViewBlocking = settings.viewBlocking();
        this.hasPostProcess = settings.postProcess();
        this.emissiveRendering = settings.emissiveRendering();
        this.offsetFunction = settings.offsetFunction();
        this.spawnTerrainParticles = settings.spawnTerrainParticles();
        this.instrument = settings.instrument();
        this.replaceable = settings.replaceable();
        this.isRandomlyTicking = settings.randomlyTicking();
    }
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
