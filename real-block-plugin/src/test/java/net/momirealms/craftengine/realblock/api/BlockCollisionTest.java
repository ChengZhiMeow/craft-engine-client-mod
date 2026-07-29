package net.momirealms.craftengine.realblock.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlockCollisionTest {

    @Test
    void distinguishesMissingStatesFromStatesWithoutCollision() {
        BlockCollision collision = new BlockCollision(
                "zako:test_block",
                List.of(new BlockCollision.State(0, List.of()))
        );

        assertTrue(collision.collisionBoxes(0).isPresent());
        assertTrue(collision.collisionBoxes(0).orElseThrow().isEmpty());
        assertTrue(collision.collisionBoxes(1).isEmpty());
    }

    @Test
    void copiesStateAndBoxLists() {
        List<CollisionBox> boxes = new ArrayList<>();
        boxes.add(new CollisionBox(0, 0, 0, 16, 16, 16));
        BlockCollision.State state = new BlockCollision.State(0, boxes);
        List<BlockCollision.State> states = new ArrayList<>(List.of(state));
        BlockCollision collision = new BlockCollision("zako:test_block", states);

        boxes.clear();
        states.clear();

        assertEquals(1, collision.states().size());
        assertEquals(1, collision.states().getFirst().boxes().size());
        assertThrows(UnsupportedOperationException.class, () -> collision.states().clear());
        assertThrows(UnsupportedOperationException.class, () -> collision.states().getFirst().boxes().clear());
    }
}
