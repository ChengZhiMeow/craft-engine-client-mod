package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldAccessor;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class DispatchingBlockBehavior extends BlockBehavior {
    private final Map<Object, Delegate> delegates = new IdentityHashMap<>();
    private final Delegate fallback;

    DispatchingBlockBehavior(List<Delegate> delegates) {
        super(delegates.getFirst().behavior().block());
        this.fallback = delegates.getFirst();
        for (Delegate delegate : delegates) {
            this.delegates.put(delegate.realState(), delegate);
        }
    }

    private Delegate select(Object[] arguments) {
        if (arguments.length == 0) {
            return fallback;
        }
        return delegates.getOrDefault(arguments[0], fallback);
    }

    @Override
    public Object rotate(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().rotate(delegate.originalBlock(), arguments);
    }

    @Override
    public Object mirror(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().mirror(delegate.originalBlock(), arguments);
    }

    @Override
    public Object updateShape(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().updateShape(delegate.originalBlock(), arguments);
    }

    @Override
    public void neighborChanged(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().neighborChanged(delegate.originalBlock(), arguments);
    }

    @Override
    public void tick(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().tick(delegate.originalBlock(), arguments);
    }

    @Override
    public void randomTick(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().randomTick(delegate.originalBlock(), arguments);
    }

    @Override
    public void onPlace(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().onPlace(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean canSurvive(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().canSurvive(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean isPathFindable(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().isPathFindable(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean hasAnalogOutputSignal(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().hasAnalogOutputSignal(delegate.originalBlock(), arguments);
    }

    @Override
    public int getAnalogOutputSignal(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().getAnalogOutputSignal(delegate.originalBlock(), arguments);
    }

    @Override
    public void preExplosionHit(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().preExplosionHit(delegate.originalBlock(), arguments);
    }

    @Override
    public void postExplosionHit(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().postExplosionHit(delegate.originalBlock(), arguments);
    }

    @Override
    public void entityInside(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().entityInside(delegate.originalBlock(), arguments);
    }

    @Override
    public void affectNeighborsAfterRemoval(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().affectNeighborsAfterRemoval(delegate.originalBlock(), arguments);
    }

    @Override
    public int getSignal(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().getSignal(delegate.originalBlock(), arguments);
    }

    @Override
    public int getDirectSignal(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().getDirectSignal(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean isSignalSource(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().isSignalSource(delegate.originalBlock(), arguments);
    }

    @Override
    public Object playerWillDestroy(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().playerWillDestroy(delegate.originalBlock(), arguments);
    }

    @Override
    public void spawnAfterBreak(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().spawnAfterBreak(delegate.originalBlock(), arguments);
    }

    @Override
    public void stepOn(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().stepOn(delegate.originalBlock(), arguments);
    }

    @Override
    public void onProjectileHit(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().onProjectileHit(delegate.originalBlock(), arguments);
    }

    @Override
    public void placeMultiState(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().placeMultiState(delegate.originalBlock(), arguments);
    }

    @Override
    public void fallOn(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().fallOn(delegate.originalBlock(), arguments);
    }

    @Override
    public void updateEntityMovementAfterFallOn(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().updateEntityMovementAfterFallOn(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean triggerEvent(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        return delegate.behavior().triggerEvent(delegate.originalBlock(), arguments);
    }

    @Override
    public void attack(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().attack(delegate.originalBlock(), arguments);
    }

    @Override
    public void handlePrecipitation(Object block, Object[] arguments) {
        Delegate delegate = select(arguments);
        delegate.behavior().handlePrecipitation(delegate.originalBlock(), arguments);
    }

    @Override
    public boolean canPlaceMultiState(WorldAccessor world, BlockPos pos, ImmutableBlockState state) {
        return state.behavior().canPlaceMultiState(world, pos, state);
    }

    @Override
    public boolean hasMultiState(ImmutableBlockState state) {
        return state.behavior().hasMultiState(state);
    }

    @Override
    public Item itemToPickup(World world, BlockPos pos, ImmutableBlockState state, Player player) {
        return state.behavior().itemToPickup(world, pos, state, player);
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state.behavior().updateStateForPlacement(context, state);
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context, ImmutableBlockState state) {
        return state.behavior().canBeReplaced(context, state);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        return state.behavior().useOnBlock(context, state);
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        return state.behavior().useWithoutItem(context, state);
    }

    record Delegate(Object realState, Object originalBlock, BlockBehavior behavior) {
    }
}
