package net.momirealms.craftengine.viacompat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ViaPluginDescriptorTest {

    @Test
    void declaresOnlyCrossVersionDependenciesAndVisiblePrefix() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("paper-plugin.yml")) {
            assertNotNull(input);
            String descriptor = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(descriptor.contains("name: CraftEngineViaCompatibility"));
            assertTrue(descriptor.contains("prefix: \"跨版本-CE支持\""));
            assertTrue(descriptor.contains("CraftEngine:"));
            assertTrue(descriptor.contains("ViaVersion:"));
            assertTrue(descriptor.contains("ViaBackwards:"));
            assertFalse(descriptor.contains("CraftEngineRealBlock"));
            assertFalse(descriptor.contains("real block registry"));
        }
    }
}
