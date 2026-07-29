package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.block.AbstractBlockManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CraftEngineBlockOverridesTest {

    @Test
    void resolvesMutableBackingMapBehindCraftEngineReadOnlyView() {
        FakeBlockManager manager = new FakeBlockManager();
        assertThrows(UnsupportedOperationException.class, () -> manager.blockOverrides().put("zako:test_block", "model"));

        Map<String, String> mutable = CraftEngineBlockOverrides.mutable(manager);
        mutable.put("zako:test_block", "model");

        assertEquals("model", manager.blockOverrides().get("zako:test_block"));
    }

    @Test
    void craftEngineVersionStillExposesExpectedBackingField() throws ReflectiveOperationException {
        assertEquals(Map.class, AbstractBlockManager.class.getDeclaredField("blockStateOverrides").getType());
    }

    private static class FakeBlockManagerBase {
        protected final Map<String, String> blockStateOverrides = new LinkedHashMap<>();
    }

    private static final class FakeBlockManager extends FakeBlockManagerBase {
        Map<String, String> blockOverrides() {
            return Collections.unmodifiableMap(blockStateOverrides);
        }
    }
}
