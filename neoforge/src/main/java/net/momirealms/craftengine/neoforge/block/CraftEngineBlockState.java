package net.momirealms.craftengine.neoforge.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CraftEngineBlockState extends BlockState {
    private BlockState visualBlockState = Blocks.STONE.defaultBlockState();
    private VoxelShape realOutlineShape;
    private VoxelShape realCollisionShape;
    private VoxelShape realSupportShape;
    private VoxelShape realOcclusionShape;

    public CraftEngineBlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }

    public void setVisualBlockState(BlockState visualBlockState) {
        this.visualBlockState = visualBlockState;
    }

    public BlockState visualBlockState() {
        return this.visualBlockState;
    }

    public void setRealShape(RealBlockShape shape) {
        this.realOutlineShape = shape.outlineShape();
        this.realCollisionShape = shape.collisionShape();
        this.realSupportShape = shape.supportShape();
        this.realOcclusionShape = shape.occlusionShape();
    }

    public void clearRealShape() {
        this.realOutlineShape = null;
        this.realCollisionShape = null;
        this.realSupportShape = null;
        this.realOcclusionShape = null;
    }

    @Override
    public @NotNull VoxelShape getFaceOcclusionShape(Direction direction) {
        if (realOcclusionShape != null) return realOcclusionShape.getFaceShape(direction);
        return visualBlockState.getFaceOcclusionShape(direction);
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape() {
        if (realOcclusionShape != null) return realOcclusionShape;
        return visualBlockState.getOcclusionShape();
    }

    @Override
    public @NotNull VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
        if (realOutlineShape != null) return realOutlineShape;
        return visualBlockState.getShape(blockGetter, blockPos);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (realOutlineShape != null) return realOutlineShape;
        return visualBlockState.getShape(blockGetter, blockPos, collisionContext);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
        if (realCollisionShape != null) return realCollisionShape;
        return visualBlockState.getCollisionShape(blockGetter, blockPos);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (realCollisionShape != null) return realCollisionShape;
        return visualBlockState.getCollisionShape(blockGetter, blockPos, collisionContext);
    }

    @Override
    public @NotNull VoxelShape getEntityInsideCollisionShape(BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
        if (realCollisionShape != null) return realCollisionShape;
        return visualBlockState.getEntityInsideCollisionShape(blockGetter, blockPos, entity);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(BlockGetter blockGetter, BlockPos blockPos) {
        if (realSupportShape != null) return realSupportShape;
        return visualBlockState.getBlockSupportShape(blockGetter, blockPos);
    }

    @Override
    public @NotNull VoxelShape getVisualShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (realOutlineShape != null) return realOutlineShape;
        return visualBlockState.getVisualShape(blockGetter, blockPos, collisionContext);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(BlockGetter blockGetter, BlockPos blockPos) {
        if (realOutlineShape != null) return realOutlineShape;
        return visualBlockState.getInteractionShape(blockGetter, blockPos);
    }

    @Override
    public @NotNull InteractionResult useItemOn(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return visualBlockState.useItemOn(itemStack, level, player, interactionHand, blockHitResult);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(Level level, Player player, BlockHitResult blockHitResult) {
        return visualBlockState.useWithoutItem(level, player, blockHitResult);
    }

    @Override
    public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
        return visualBlockState.isValidSpawn(blockGetter, blockPos, entityType);
    }

    @Override
    public @NotNull Vec3 getOffset(BlockPos blockPos) {
        return visualBlockState.getOffset(blockPos);
    }

    @Override
    public <T extends Comparable<T>> @NotNull T getValue(Property<@NotNull T> property) {
        try {
            return super.getValue(property);
        } catch (IllegalArgumentException ignored) {
            try {
                return visualBlockState.getValue(property);
            } catch (IllegalArgumentException ignoredAgain) {
                return property.getPossibleValues().getFirst();
            }
        }
    }

    @Override
    public <T extends Comparable<T>, V extends T> @NotNull BlockState setValue(Property<@NotNull T> property, V comparable) {
        try {
            return super.setValue(property, comparable);
        } catch (IllegalArgumentException ignored) {
            return this;
        }
    }
}
