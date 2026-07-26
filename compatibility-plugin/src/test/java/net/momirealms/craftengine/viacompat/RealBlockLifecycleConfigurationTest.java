package net.momirealms.craftengine.viacompat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RealBlockLifecycleConfigurationTest {

    @Test
    void persistedRegistryIsNotRestoredFromPaperBootstrap() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("paper-plugin.yml")) {
            assertNotNull(input);
            String descriptor = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertFalse(
                    descriptor.lines().anyMatch(line -> line.stripLeading().startsWith("bootstrapper:")),
                    "CraftEngine injects registry placeholders after plugin bootstrap during DATAPACK_DISCOVERY"
            );
        }
    }
}
