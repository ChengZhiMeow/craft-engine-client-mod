package net.momirealms.craftengine.realblock;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class CraftEngineBlockStateFactory {

    private CraftEngineBlockStateFactory() {
    }

    static StateDefinition.Factory<Block, BlockState> from(BlockState template) {
        Class<? extends BlockState> stateClass = template.getClass();
        if (stateClass == BlockState.class) {
            throw new IllegalArgumentException("CraftEngine block state template is not runtime-generated");
        }

        Constructor<? extends BlockState> constructor;
        try {
            constructor = stateClass.getDeclaredConstructor(
                    Block.class,
                    Reference2ObjectArrayMap.class,
                    MapCodec.class
            );
        } catch (NoSuchMethodException exception) {
            throw new IllegalArgumentException(
                    "CraftEngine block state class has no compatible constructor: " + stateClass.getName(),
                    exception
            );
        }
        if (!constructor.trySetAccessible()) {
            throw new IllegalStateException(
                    "cannot access CraftEngine block state constructor: " + stateClass.getName()
            );
        }

        return (owner, values, codec) -> instantiate(constructor, owner, values, codec);
    }

    private static BlockState instantiate(
            Constructor<? extends BlockState> constructor,
            Block owner,
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> values,
            MapCodec<BlockState> codec
    ) {
        try {
            return constructor.newInstance(owner, values, codec);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(
                    "cannot create CraftEngine block state " + constructor.getDeclaringClass().getName(),
                    exception
            );
        }
    }
}
