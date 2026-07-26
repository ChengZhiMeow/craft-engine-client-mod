package net.momirealms.craftengine.viacompat.realblock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBlockCollisionBindingTest {

    @Test
    void configuresBlockCollisionBeforeRefreshingStateCaches() throws IOException {
        String source = java.nio.file.Files.readString(
                java.nio.file.Path.of(
                        "src/main/java/net/momirealms/craftengine/viacompat/realblock/RealBlockManager.java"
                ),
                StandardCharsets.UTF_8
        );

        int configure = source.indexOf("IgniteRealBlockBridge.configureCollision(");
        int refresh = source.indexOf("IgniteRealBlockBridge.refreshStateCaches(");
        assertTrue(configure >= 0, "real block must copy the configured collision flag to NMS");
        assertTrue(refresh > configure, "collision must be configured before BlockState caches are rebuilt");
    }
}
