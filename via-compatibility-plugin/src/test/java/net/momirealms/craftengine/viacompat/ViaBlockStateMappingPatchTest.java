package net.momirealms.craftengine.viacompat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViaBlockStateMappingPatchTest {
    private static final int FIRST_EXTENDED_ID = 29_671;
    private static final int EXTENDED_SIZE = 30_671;

    @AfterEach
    void resetMappings() {
        ValidProtocol.MAPPINGS.blockStateMappings = ValidProtocol.ORIGINAL;
    }

    @Test
    void appliesAndRestoresExtendedMapping() throws ReflectiveOperationException {
        ViaBlockStateMappingPatch patch = patchFor(List.of(ValidProtocol.class.getName()));

        assertEquals(1, patch.apply());
        assertEquals(FIRST_EXTENDED_ID, ValidProtocol.MAPPINGS.blockStateMappings.getNewId(FIRST_EXTENDED_ID));
        assertEquals(EXTENDED_SIZE, ValidProtocol.MAPPINGS.blockStateMappings.size());

        patch.restore();
        assertSame(ValidProtocol.ORIGINAL, ValidProtocol.MAPPINGS.blockStateMappings);
    }

    @Test
    void rollsBackEarlierFieldsWhenALaterProtocolFails() {
        ViaBlockStateMappingPatch patch = patchFor(List.of(
                ValidProtocol.class.getName(),
                InvalidProtocol.class.getName()
        ));

        assertThrows(NoSuchFieldException.class, patch::apply);
        assertSame(ValidProtocol.ORIGINAL, ValidProtocol.MAPPINGS.blockStateMappings);
    }

    private ViaBlockStateMappingPatch patchFor(List<String> protocols) {
        return new ViaBlockStateMappingPatch(
                getClass().getClassLoader(),
                protocols,
                FIRST_EXTENDED_ID,
                EXTENDED_SIZE
        );
    }

    public interface TestMappings {
        int getNewId(int id);

        default int getNewIdOrDefault(int id, int fallback) {
            int mapped = getNewId(id);
            return mapped == -1 ? fallback : mapped;
        }

        default boolean contains(int id) {
            return getNewId(id) != -1;
        }

        int size();

        int mappedSize();

        TestMappings inverse();
    }

    private record IdentityMappings(int size) implements TestMappings {
        @Override
        public int getNewId(int id) {
            return id >= 0 && id < this.size ? id : -1;
        }

        @Override
        public int mappedSize() {
            return this.size;
        }

        @Override
        public TestMappings inverse() {
            return this;
        }
    }

    public static final class MappingData {
        private TestMappings blockStateMappings;

        private MappingData(TestMappings blockStateMappings) {
            this.blockStateMappings = blockStateMappings;
        }
    }

    public static final class ValidProtocol {
        private static final TestMappings ORIGINAL = new IdentityMappings(FIRST_EXTENDED_ID);
        public static final MappingData MAPPINGS = new MappingData(ORIGINAL);
    }

    public static final class InvalidProtocol {
        public static final Object MAPPINGS = new Object();
    }
}
