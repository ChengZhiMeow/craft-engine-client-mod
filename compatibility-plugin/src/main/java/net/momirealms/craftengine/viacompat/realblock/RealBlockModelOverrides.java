package net.momirealms.craftengine.viacompat.realblock;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.util.Key;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RealBlockModelOverrides {
    private RealBlockModelOverrides() {
    }

    static Map<String, JsonElement> create(
            Map<Key, Map<String, JsonElement>> sourceOverrides,
            List<String> visualStates
    ) {
        Map<String, JsonElement> result = new LinkedHashMap<>();
        for (int ordinal = 0; ordinal < visualStates.size(); ordinal++) {
            String visualState = visualStates.get(ordinal);
            JsonElement model = resolve(sourceOverrides, visualState);
            result.put(visualStates.size() == 1 ? "" : "ce_state=" + ordinal, model);
        }
        return result;
    }

    private static JsonElement resolve(
            Map<Key, Map<String, JsonElement>> sourceOverrides,
            String visualState
    ) {
        int propertiesStart = visualState.indexOf('[');
        Key visualBlockId = Key.of(
                propertiesStart < 0 ? visualState : visualState.substring(0, propertiesStart)
        );
        String properties = propertiesStart < 0
                ? ""
                : visualState.substring(propertiesStart + 1, visualState.lastIndexOf(']'));
        Map<String, JsonElement> blockModels = sourceOverrides.get(visualBlockId);
        if (blockModels == null) {
            throw new IllegalArgumentException(
                    "no generated client model table exists for visual block " + visualBlockId
            );
        }
        JsonElement model = blockModels.get(properties);
        if (model == null) {
            throw new IllegalArgumentException(
                    "no generated client model exists for visual state " + visualState
            );
        }
        return model;
    }
}
