package net.momirealms.craftengine.realblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryOccupancyTest {

    @Test
    void missingKeyIsNotOccupiedEvenWhenRegistryReturnsItsDefaultValue() {
        Object defaultBlock = new Object();
        Object canonicalBlock = new Object();

        assertFalse(RegistryOccupancy.isOccupied(false, defaultBlock, canonicalBlock));
    }

    @Test
    void existingDifferentBlockIsOccupied() {
        Object occupiedBlock = new Object();
        Object canonicalBlock = new Object();

        assertTrue(RegistryOccupancy.isOccupied(true, occupiedBlock, canonicalBlock));
    }

    @Test
    void existingCanonicalBlockSupportsIdempotentInstall() {
        Object canonicalBlock = new Object();

        assertFalse(RegistryOccupancy.isOccupied(true, canonicalBlock, canonicalBlock));
    }
}
