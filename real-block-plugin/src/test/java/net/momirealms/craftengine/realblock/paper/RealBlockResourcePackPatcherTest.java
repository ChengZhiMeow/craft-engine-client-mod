package net.momirealms.craftengine.realblock.paper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.core.util.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBlockResourcePackPatcherTest {
    @TempDir
    Path tempDirectory;

    @Test
    void writesRealBlockstateIntoFinalZipAndRemovesStaleManagedEntries() throws Exception {
        Path zip = tempDirectory.resolve("resource_pack.zip");
        createZip(zip);

        RealBlockResourcePackPatcher.patch(
                zip,
                Map.of(
                        Key.of("zako:test_block"),
                        Map.of(
                                "ce_state=0", model("zako:block/test_block"),
                                "ce_state=1", model("zako:block/test_block_on")
                        )
                ),
                Set.of(3335, 3336)
        );

        try (FileSystem fileSystem = FileSystems.newFileSystem(zip)) {
            Path blockState = fileSystem.getPath("/assets/zako/blockstates/test_block.json");
            assertTrue(Files.exists(blockState));
            try (Reader reader = Files.newBufferedReader(blockState, StandardCharsets.UTF_8)) {
                JsonObject variants = JsonParser.parseReader(reader)
                        .getAsJsonObject()
                        .getAsJsonObject("variants");
                assertEquals(
                        "zako:block/test_block",
                        variants.getAsJsonObject("ce_state=0").get("model").getAsString()
                );
                assertEquals(
                        "zako:block/test_block_on",
                        variants.getAsJsonObject("ce_state=1").get("model").getAsString()
                );
            }
            assertFalse(Files.exists(
                    fileSystem.getPath("/assets/craftengine/blockstates/custom_3335.json")
            ));
            assertFalse(Files.exists(
                    fileSystem.getPath("/assets/craftengine/blockstates/custom_3336.json")
            ));
        }

        RealBlockResourcePackPatcher.patch(zip, Map.of(), Set.of());

        try (FileSystem fileSystem = FileSystems.newFileSystem(zip)) {
            assertFalse(Files.exists(fileSystem.getPath("/assets/zako/blockstates/test_block.json")));
            assertTrue(Files.exists(
                    fileSystem.getPath("/assets/craftengine/blockstates/custom_3335.json")
            ));
            assertTrue(Files.exists(
                    fileSystem.getPath("/assets/craftengine/blockstates/custom_3336.json")
            ));
        }
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
            Path mcmeta = fileSystem.getPath("/pack.mcmeta");
            try (Writer writer = Files.newBufferedWriter(mcmeta, StandardCharsets.UTF_8)) {
                writer.write("{}");
            }
            Path blockStates = fileSystem.getPath("/assets/craftengine/blockstates");
            Files.createDirectories(blockStates);
            Files.writeString(
                    blockStates.resolve("custom_3335.json"),
                    "{\"variants\":{\"\":{\"model\":\"zako:block/test_block\"}}}",
                    StandardCharsets.UTF_8
            );
            Files.writeString(
                    blockStates.resolve("custom_3336.json"),
                    "{\"variants\":{\"\":{\"model\":\"zako:block/test_block_on\"}}}",
                    StandardCharsets.UTF_8
            );
        }
    }
}
