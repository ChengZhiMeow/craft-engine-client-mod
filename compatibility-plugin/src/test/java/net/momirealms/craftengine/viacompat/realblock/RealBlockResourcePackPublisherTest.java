package net.momirealms.craftengine.viacompat.realblock;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBlockResourcePackPublisherTest {
    @TempDir
    Path tempDirectory;

    @Test
    void refreshesHostedSnapshotAfterAddingRealBlockstate() throws Exception {
        Path generatedPack = tempDirectory.resolve("resource_pack.zip");
        Path staleHostedPack = tempDirectory.resolve("stale-hosted.zip");
        Path refreshedHostedPack = tempDirectory.resolve("refreshed-hosted.zip");
        createZip(generatedPack);
        Files.copy(generatedPack, staleHostedPack);

        RealBlockResourcePackPublisher.patchAndPublish(
                generatedPack,
                Map.of(
                        Key.of("zako:test_block"),
                        Map.of("ce_state=0", model("zako:block/test_block_0"))
                ),
                Set.of(3349),
                () -> {
                    try {
                        Files.copy(
                                generatedPack,
                                refreshedHostedPack,
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (java.io.IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                }
        );

        assertFalse(hasEntry(staleHostedPack, "/assets/zako/blockstates/test_block.json"));
        assertTrue(hasEntry(refreshedHostedPack, "/assets/zako/blockstates/test_block.json"));
    }

    private static JsonObject model(String path) {
        JsonObject model = new JsonObject();
        model.addProperty("model", path);
        return model;
    }

    private static void createZip(Path zip) throws Exception {
        try (FileSystem fileSystem = FileSystems.newFileSystem(
                java.net.URI.create("jar:" + zip.toUri()),
                Map.of("create", "true")
        )) {
            Files.writeString(fileSystem.getPath("/pack.mcmeta"), "{}");
        }
    }

    private static boolean hasEntry(Path zip, String entry) throws Exception {
        try (FileSystem fileSystem = FileSystems.newFileSystem(zip)) {
            return Files.exists(fileSystem.getPath(entry));
        }
    }
}
