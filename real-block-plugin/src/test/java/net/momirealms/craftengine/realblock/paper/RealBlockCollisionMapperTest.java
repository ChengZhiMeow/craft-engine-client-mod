package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.realblock.api.BlockCollision;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RealBlockCollisionMapperTest {

    @Test
    void exposesEveryStateCollisionInModelCoordinates() {
        Key id = Key.of("zako:test_block");
        RealBlockDefinition definition = new RealBlockDefinition(
                id,
                List.of(
                        state(3, List.of(new ShapeBox(0, 0, 0, 16, 8, 16))),
                        state(7, List.of())
                )
        );

        Map<String, BlockCollision> snapshot =
                RealBlockCollisionMapper.createSnapshot(Map.of(id, definition));
        BlockCollision collision = snapshot.get("zako:test_block");

        assertEquals(2, collision.states().size());
        assertEquals(8, collision.collisionBoxes(0).orElseThrow().getFirst().maxY());
        assertEquals(List.of(), collision.collisionBoxes(1).orElseThrow());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
    }

    private static RealBlockDefinition.RealBlockStateDefinition state(
            int customIndex,
            List<ShapeBox> collision
    ) {
        RealBlockShape shape = new RealBlockShape(collision, collision, collision, List.of());
        return new RealBlockDefinition.RealBlockStateDefinition(customIndex, shape);
    }
}
