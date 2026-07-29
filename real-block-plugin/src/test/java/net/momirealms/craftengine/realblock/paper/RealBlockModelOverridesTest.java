package net.momirealms.craftengine.realblock.paper;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RealBlockModelOverridesTest {
    @Test
    void createsRealIdVariantsFromVisualStatesWithoutCustomNamespaceKeys() {
        JsonObject off = model("zako:block/test_block");
        JsonObject on = model("zako:block/test_block_on");
        Map<Key, Map<String, com.google.gson.JsonElement>> source = Map.of(
                Key.of("minecraft:tripwire"),
                Map.of(
                        "attached=false,powered=false", off,
                        "attached=false,powered=true", on
                )
        );

        Map<String, com.google.gson.JsonElement> result = RealBlockModelOverrides.create(
                source,
                List.of(
                        "minecraft:tripwire[attached=false,powered=false]",
                        "minecraft:tripwire[attached=false,powered=true]"
                )
        );

        assertEquals(List.of("ce_state=0", "ce_state=1"), result.keySet().stream().toList());
        assertSame(off, result.get("ce_state=0"));
        assertSame(on, result.get("ce_state=1"));
    }

    @Test
    void singleStateUsesTheDefaultVariantKey() {
        JsonObject model = model("zako:block/test_block");
        Map<Key, Map<String, com.google.gson.JsonElement>> source = Map.of(
                Key.of("minecraft:tripwire"),
                Map.of("powered=false", model)
        );

        Map<String, com.google.gson.JsonElement> result = RealBlockModelOverrides.create(
                source,
                List.of("minecraft:tripwire[powered=false]")
        );

        assertEquals(List.of(""), result.keySet().stream().toList());
        assertSame(model, result.get(""));
    }

    private static JsonObject model(String path) {
        JsonObject model = new JsonObject();
        model.addProperty("model", path);
        return model;
    }
}
