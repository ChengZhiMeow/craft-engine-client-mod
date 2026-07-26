package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.ResourceException;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertSame;

class RealBlockNativeParserLookupTest {
    @Test
    void findsNativeParserWhenSectionIdsContainBothPluralAndSingularAliases() {
        ConfigParser expected = new StubParser("blocks", "block");
        ConfigParser unrelated = new StubParser("block_state_mappings", "block-state-mappings");

        assertSame(
                expected,
                NativeBlockParserLookup.find(new ConfigParser[]{expected, unrelated})
        );
    }

    private record StubParser(String... sectionId) implements ConfigParser {
        @Override
        public LoadingStage loadingStage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addConfig(CachedConfigSection config) {
        }

        @Override
        public void loadAll() {
        }

        @Override
        public void clearConfigs() {
        }

        @Override
        public void setErrorHandler(Consumer<ResourceException> errorHandler) {
        }
    }
}
