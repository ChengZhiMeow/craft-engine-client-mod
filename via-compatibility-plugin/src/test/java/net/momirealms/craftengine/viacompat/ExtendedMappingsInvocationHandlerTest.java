package net.momirealms.craftengine.viacompat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedMappingsInvocationHandlerTest {
    private static final int FIRST_EXTENDED_ID = 29_671;
    private static final int REGISTRY_SIZE = 35_671;

    @Test
    void preservesOnlyCraftEngineExtensionRange() {
        TestMappings mappings = wrap(new OffsetMappings(29_671, 27_946));

        assertEquals(-1, mappings.getNewId(-1));
        assertEquals(27_945, mappings.getNewId(29_670));
        assertEquals(29_671, mappings.getNewId(29_671));
        assertEquals(32_505, mappings.getNewId(32_505));
        assertEquals(35_670, mappings.getNewId(35_670));
        assertEquals(-1, mappings.getNewId(35_671));
    }

    @Test
    void expandsBothPaletteRegistrySizes() {
        TestMappings mappings = wrap(new OffsetMappings(29_671, 27_946));

        assertEquals(REGISTRY_SIZE, mappings.size());
        assertEquals(REGISTRY_SIZE, mappings.mappedSize());
    }

    @Test
    void containsAndInverseAlsoPreserveExtensionRange() {
        TestMappings mappings = wrap(new OffsetMappings(29_671, 27_946));

        assertFalse(mappings.contains(35_671));
        assertTrue(mappings.contains(32_505));
        assertEquals(32_505, mappings.inverse().getNewId(32_505));
    }

    private TestMappings wrap(TestMappings mappings) {
        return (TestMappings) ExtendedMappingsInvocationHandler.wrap(
                mappings,
                FIRST_EXTENDED_ID,
                REGISTRY_SIZE
        );
    }

    private interface TestMappings {
        int getNewId(int id);

        default int getNewIdOrDefault(int id, int fallback) {
            int mapped = getNewId(id);
            return mapped == -1 ? fallback : mapped;
        }

        default boolean contains(int id) {
            return getNewId(id) != -1;
        }

        void setNewId(int id, int mappedId);

        int size();

        int mappedSize();

        TestMappings inverse();
    }

    private record OffsetMappings(int size, int mappedSize) implements TestMappings {
        @Override
        public int getNewId(int id) {
            return id >= 0 && id < this.size ? Math.max(0, id - 1_725) : -1;
        }

        @Override
        public void setNewId(int id, int mappedId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestMappings inverse() {
            return this;
        }
    }
}
