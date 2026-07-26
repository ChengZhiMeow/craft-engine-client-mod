package net.momirealms.craftengine.viacompat.realblock;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.util.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class RealBlockResourcePackPublisher {
    private RealBlockResourcePackPublisher() {
    }

    static boolean patchAndPublish(
            Path resourcePackZip,
            Map<Key, Map<String, JsonElement>> blockStates,
            Set<Integer> internalStateIndices,
            Runnable publisher
    ) throws IOException {
        if (resourcePackZip == null || Files.notExists(resourcePackZip)) {
            return false;
        }
        RealBlockResourcePackPatcher.patch(resourcePackZip, blockStates, internalStateIndices);
        Objects.requireNonNull(publisher, "publisher").run();
        return true;
    }
}
