package net.momirealms.craftengine.realblock;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftEngineBlockStateFactoryTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void rejectsVanillaBlockStateTemplates() {
        BlockState template = new BlockState(
                Blocks.STONE,
                new Reference2ObjectArrayMap<>(),
                BlockState.CODEC.fieldOf("state")
        );

        assertThrows(IllegalArgumentException.class, () -> CraftEngineBlockStateFactory.from(template));
    }

    @Test
    void reusesTheFactoryCapturedByTheStateDefinitionMixin() {
        GeneratedFactory generatedFactory = new GeneratedFactory();

        StateDefinition.Factory<Block, BlockState> captured =
                CraftEngineBlockStateFactory.from(() -> generatedFactory);
        BlockState state = captured.create(
                Blocks.STONE,
                new Reference2ObjectArrayMap<>(),
                BlockState.CODEC.fieldOf("state")
        );

        assertSame(generatedFactory, captured);
        assertEquals(GeneratedBlockState.class, state.getClass());
    }

    private static final class GeneratedBlockState extends BlockState {
        private GeneratedBlockState(
                Block owner,
                Reference2ObjectArrayMap<Property<?>, Comparable<?>> values,
                MapCodec<BlockState> codec
        ) {
            super(owner, values, codec);
        }
    }

    private static final class GeneratedFactory
            implements StateDefinition.Factory<Block, BlockState> {
        @Override
        public BlockState create(
                Block owner,
                Reference2ObjectArrayMap<Property<?>, Comparable<?>> values,
                MapCodec<BlockState> codec
        ) {
            return new GeneratedBlockState(owner, values, codec);
        }
    }
}
