package net.momirealms.craftengine.viacompat.realblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SixWayConnectionMaskTest {

    @Test
    void addsAndRemovesEachOfSixDirectionBits() {
        int connections = 0;
        for (int direction = 0; direction < 6; direction++) {
            connections = SixWayConnectionMask.update(connections, direction, true);
        }
        assertEquals(63, connections);

        connections = SixWayConnectionMask.update(connections, 2, false);
        assertEquals(59, connections);
    }

    @Test
    void rejectsNonDirectionBits() {
        assertThrows(IllegalArgumentException.class, () -> SixWayConnectionMask.update(0, 6, true));
        assertThrows(IllegalArgumentException.class, () -> SixWayConnectionMask.update(64, 0, true));
    }
}
