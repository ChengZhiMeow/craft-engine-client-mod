package net.momirealms.craftengine.realblock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.IdentityHashMap;
import java.util.Map;

public final class RealBlockStateRuntime {
    private static final Map<BlockState, VoxelShape> OCCLUSION = new IdentityHashMap<>();
    private static final Map<BlockState, BlockState> ORIGINAL = new IdentityHashMap<>();

    private RealBlockStateRuntime() {
    }

    static synchronized void setOcclusion(BlockState state, VoxelShape shape) {
        OCCLUSION.put(state, shape);
    }

    static synchronized void setOriginal(BlockState state, BlockState original) {
        if (state != original) {
            ORIGINAL.put(state, original);
        }
    }

    public static synchronized VoxelShape occlusion(BlockState state) {
        return OCCLUSION.get(state);
    }

    public static synchronized BlockState original(BlockState state) {
        return ORIGINAL.get(state);
    }
}
