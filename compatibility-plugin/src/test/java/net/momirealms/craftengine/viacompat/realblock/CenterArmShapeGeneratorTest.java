package net.momirealms.craftengine.viacompat.realblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CenterArmShapeGeneratorTest {

    @Test
    void createsCenterPlusEveryConnectedArm() {
        RealBlockShape shape = CenterArmShapeGenerator.create(2, 63);

        assertEquals(7, shape.collision().size());
        assertTrue(shape.collision().contains(new ShapeBox(6, 6, 6, 10, 10, 10)));
        assertTrue(shape.collision().contains(new ShapeBox(6, 0, 6, 10, 6, 10)));
        assertTrue(shape.collision().contains(new ShapeBox(10, 6, 6, 16, 10, 10)));
        assertEquals(shape.collision(), shape.outline());
        assertEquals(shape.collision(), shape.support());
    }

    @Test
    void disconnectedBlockUsesOnlyItsCenter() {
        assertEquals(
                java.util.List.of(new ShapeBox(6, 6, 6, 10, 10, 10)),
                CenterArmShapeGenerator.create(2, 0).collision()
        );
    }

    @Test
    void rejectsMasksOutsideSixBits() {
        assertThrows(IllegalArgumentException.class, () -> CenterArmShapeGenerator.create(2, 64));
    }
}
