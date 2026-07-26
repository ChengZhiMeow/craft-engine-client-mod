package net.momirealms.craftengine.viacompat.realblock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.core.util.Key;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class RealBlockResourcePackPatcher {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String MARKER_PATH = "/assets/craftengine/real_block_blockstates.json";
    private static final Object ZIP_LOCK = new Object();

    private RealBlockResourcePackPatcher() {
    }

    static void patch(
            Path resourcePackZip,
            Map<Key, Map<String, JsonElement>> blockStates,
            Set<Integer> internalStateIndices
    ) throws IOException {
        if (resourcePackZip == null || Files.notExists(resourcePackZip)) {
            return;
        }
        Map<Key, Map<String, JsonElement>> snapshot = snapshot(blockStates);
        Set<Integer> internalStateSnapshot = Set.copyOf(internalStateIndices);
        synchronized (ZIP_LOCK) {
            Path absoluteZip = resourcePackZip.toAbsolutePath().normalize();
            Path parent = absoluteZip.getParent();
            if (parent == null) {
                throw new IOException("Resource pack has no parent directory: " + absoluteZip);
            }
            Path temporaryZip = Files.createTempFile(parent, absoluteZip.getFileName().toString(), ".realblock.tmp");
            try {
                Files.copy(absoluteZip, temporaryZip, StandardCopyOption.REPLACE_EXISTING);
                patchCopy(temporaryZip, snapshot, internalStateSnapshot);
                replace(temporaryZip, absoluteZip);
            } finally {
                Files.deleteIfExists(temporaryZip);
            }
        }
    }

    private static Map<Key, Map<String, JsonElement>> snapshot(
            Map<Key, Map<String, JsonElement>> blockStates
    ) {
        Map<Key, Map<String, JsonElement>> result = new LinkedHashMap<>();
        blockStates.forEach((id, variants) -> {
            Map<String, JsonElement> variantCopies = new LinkedHashMap<>();
            variants.forEach((variant, model) -> variantCopies.put(variant, model.deepCopy()));
            result.put(id, variantCopies);
        });
        return result;
    }

    private static void patchCopy(
            Path zip,
            Map<Key, Map<String, JsonElement>> blockStates,
            Set<Integer> internalStateIndices
    ) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(zip)) {
            Path marker = fileSystem.getPath(MARKER_PATH);
            MarkerData previous = readMarker(marker);
            for (String oldPath : previous.generatedPaths()) {
                Files.deleteIfExists(fileSystem.getPath(oldPath));
            }
            for (Map.Entry<String, byte[]> removed : previous.removedFiles().entrySet()) {
                Path restored = fileSystem.getPath(removed.getKey());
                Files.createDirectories(restored.getParent());
                Files.write(restored, removed.getValue());
            }

            Set<String> managedPaths = new LinkedHashSet<>();
            for (Map.Entry<Key, Map<String, JsonElement>> entry : blockStates.entrySet()) {
                Key id = entry.getKey();
                String path = "/assets/" + id.namespace() + "/blockstates/" + id.value() + ".json";
                Path blockStateFile = fileSystem.getPath(path);
                Files.createDirectories(blockStateFile.getParent());
                JsonObject root = new JsonObject();
                JsonObject variants = new JsonObject();
                entry.getValue().forEach(variants::add);
                root.add("variants", variants);
                try (Writer writer = Files.newBufferedWriter(blockStateFile, StandardCharsets.UTF_8)) {
                    GSON.toJson(root, writer);
                }
                managedPaths.add(path);
            }

            Map<String, byte[]> removedFiles = new LinkedHashMap<>();
            for (int internalStateIndex : internalStateIndices) {
                String path = "/assets/craftengine/blockstates/custom_" + internalStateIndex + ".json";
                Path internalBlockState = fileSystem.getPath(path);
                if (Files.exists(internalBlockState)) {
                    removedFiles.put(path, Files.readAllBytes(internalBlockState));
                    Files.delete(internalBlockState);
                }
            }

            Files.createDirectories(marker.getParent());
            JsonObject markerRoot = new JsonObject();
            JsonArray paths = new JsonArray();
            managedPaths.forEach(paths::add);
            markerRoot.add("paths", paths);
            JsonObject removed = new JsonObject();
            removedFiles.forEach((path, content) ->
                    removed.addProperty(path, Base64.getEncoder().encodeToString(content))
            );
            markerRoot.add("removed", removed);
            try (Writer writer = Files.newBufferedWriter(marker, StandardCharsets.UTF_8)) {
                GSON.toJson(markerRoot, writer);
            }
        }
    }

    private static MarkerData readMarker(Path marker) throws IOException {
        if (Files.notExists(marker)) {
            return MarkerData.EMPTY;
        }
        try (Reader reader = Files.newBufferedReader(marker, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray paths = root.getAsJsonArray("paths");
            Set<String> generatedPaths = new LinkedHashSet<>();
            if (paths != null) {
                paths.forEach(path -> generatedPaths.add(path.getAsString()));
            }
            Map<String, byte[]> removedFiles = new LinkedHashMap<>();
            JsonObject removed = root.getAsJsonObject("removed");
            if (removed != null) {
                removed.entrySet().forEach(entry -> removedFiles.put(
                        entry.getKey(),
                        Base64.getDecoder().decode(entry.getValue().getAsString())
                ));
            }
            return new MarkerData(generatedPaths, removedFiles);
        }
    }

    private static void replace(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record MarkerData(Set<String> generatedPaths, Map<String, byte[]> removedFiles) {
        private static final MarkerData EMPTY = new MarkerData(Set.of(), Map.of());
    }
}
