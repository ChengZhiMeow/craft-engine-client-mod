package net.momirealms.craftengine.realblock.paper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ShapeBoxTest {

    @Test
    void acceptsIc2StyleCableBounds() {
        assertDoesNotThrow(() -> new ShapeBox(6, 0, 6, 10, 16, 10));
    }

    @Test
    void rejectsInvertedOrOutOfBlockBounds() {
        assertThrows(IllegalArgumentException.class, () -> new ShapeBox(10, 0, 6, 6, 16, 10));
        assertThrows(IllegalArgumentException.class, () -> new ShapeBox(-1, 0, 6, 10, 16, 10));
        assertThrows(IllegalArgumentException.class, () -> new ShapeBox(6, 0, 6, 17, 16, 10));
    }
}
