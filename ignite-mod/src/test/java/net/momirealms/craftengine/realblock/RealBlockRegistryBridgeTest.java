package net.momirealms.craftengine.realblock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBlockRegistryBridgeTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void migratesLegacyManifestIntoCraftEngineCache() throws Exception {
        Path legacyManifest = temporaryDirectory.resolve("CraftEngine").resolve("real_blocks.registry.json");
        Path cacheManifest = temporaryDirectory.resolve("CraftEngine")
                .resolve("cache")
                .resolve("real_blocks.registry.json");
        Files.createDirectories(legacyManifest.getParent());
        Files.writeString(legacyManifest, "{\"format\":1}");

        RealBlockRegistryBridge.migrateLegacyManifest(legacyManifest, cacheManifest);

        assertFalse(Files.exists(legacyManifest));
        assertTrue(Files.isRegularFile(cacheManifest));
        assertEquals("{\"format\":1}", Files.readString(cacheManifest));
    }

    @Test
    void keepsExistingCacheManifestAndLeavesLegacyFileUntouched() throws Exception {
        Path legacyManifest = temporaryDirectory.resolve("CraftEngine").resolve("real_blocks.registry.json");
        Path cacheManifest = temporaryDirectory.resolve("CraftEngine")
                .resolve("cache")
                .resolve("real_blocks.registry.json");
        Files.createDirectories(cacheManifest.getParent());
        Files.writeString(legacyManifest, "legacy");
        Files.writeString(cacheManifest, "cache");

        RealBlockRegistryBridge.migrateLegacyManifest(legacyManifest, cacheManifest);

        assertEquals("legacy", Files.readString(legacyManifest));
        assertEquals("cache", Files.readString(cacheManifest));
    }
}
