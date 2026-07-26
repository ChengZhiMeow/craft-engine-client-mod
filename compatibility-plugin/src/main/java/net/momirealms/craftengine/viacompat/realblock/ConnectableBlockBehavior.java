package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;

import java.util.Optional;

final class ConnectableBlockBehavior extends BukkitBlockBehavior {
    private static final Key TYPE = Key.ce("connectable_block");
    private static final Key LEGACY_CABLE_TYPE = Key.ce("cable_block");
    static final BlockBehaviorFactory<ConnectableBlockBehavior> FACTORY = new Factory();
    private final Property<Integer> connectionsProperty;

    private ConnectableBlockBehavior(BlockDefinition block, Property<Integer> connectionsProperty) {
        super(block);
        this.connectionsProperty = connectionsProperty;
    }

    static void register() {
        if (!BuiltInRegistries.BLOCK_BEHAVIOR_TYPE.containsKey(TYPE)) {
            BlockBehaviors.register(TYPE, FACTORY);
        }
        if (!BuiltInRegistries.BLOCK_BEHAVIOR_TYPE.containsKey(LEGACY_CABLE_TYPE)) {
            BlockBehaviors.register(LEGACY_CABLE_TYPE, FACTORY);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        int connections = 0;
        for (Direction direction : Direction.values()) {
            if (world.getBlock(pos.relative(direction)).is(blockDefinition.id())) {
                connections |= 1 << direction.data3d();
            }
        }
        return state.with(connectionsProperty, connections);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args) {
        Optional<ImmutableBlockState> optionalState = BlockStateUtils.getOptionalCustomBlockState(args[0]);
        if (optionalState.isEmpty()) {
            return super.updateShape(thisBlock, args);
        }
        ImmutableBlockState state = optionalState.get();
        Direction direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]);
        int connections = state.get(connectionsProperty);
        boolean connected = BlockStateUtils.getOptionalCustomBlockState(args[updateShape$neighborState])
                .map(neighbor -> neighbor.owner().value().id().equals(blockDefinition.id()))
                .orElse(false);
        int updated = SixWayConnectionMask.update(connections, direction.data3d(), connected);
        if (updated == connections) {
            return args[0];
        }
        return state.with(connectionsProperty, updated).customBlockState().minecraftState();
    }

    private static final class Factory implements BlockBehaviorFactory<ConnectableBlockBehavior> {
        @Override
        public ConnectableBlockBehavior create(BlockDefinition block, ConfigSection section) {
            String legacyProperty = section.getString("connections-property", "connections");
            String property = section.getString("property", legacyProperty);
            return new ConnectableBlockBehavior(
                    block,
                    BlockBehaviorFactory.getProperty(section.path(), block, property, Integer.class)
            );
        }
    }
}
