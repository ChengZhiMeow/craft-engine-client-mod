package net.momirealms.craftengine.realblock.paper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBlockPluginDescriptorTest {

    @Test
    void persistedRegistryIsNotRestoredFromPaperBootstrap() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("paper-plugin.yml")) {
            assertNotNull(input);
            String descriptor = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(descriptor.contains("name: CraftEngineRealBlock"));
            assertTrue(descriptor.contains("prefix: \"默米-资源包引擎-真方块\""));
            assertTrue(descriptor.contains(
                    "main: net.momirealms.craftengine.realblock.paper.CraftEngineRealBlockPlugin"
            ));
            assertTrue(descriptor.contains("CraftEngine:"));
            assertFalse(descriptor.contains("ViaVersion:"));
            assertFalse(descriptor.contains("ViaBackwards:"));
            assertFalse(
                    descriptor.lines().anyMatch(line -> line.stripLeading().startsWith("bootstrapper:")),
                    "CraftEngine injects registry placeholders after plugin bootstrap during DATAPACK_DISCOVERY"
            );
        }
    }
}
