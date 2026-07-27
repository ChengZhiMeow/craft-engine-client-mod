package net.momirealms.craftengine.realblock.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.momirealms.craftengine.realblock.StateHolderMixinSupport;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StateHolderMixinTest {

    @Test
    void missingCeStateUsesDefaultValue() {
        IntegerProperty property = IntegerProperty.create("ce_state", 0, 63);

        assertTrue(StateHolderMixinSupport.shouldUseDefaultValue(property, Map.of()));
    }

    @Test
    void moonriseMapViewWithNullValueUsesDefaultValue() {
        IntegerProperty property = IntegerProperty.create("ce_state", 0, 63);
        Reference2ObjectArrayMap<Property<?>, Comparable<?>> values = new Reference2ObjectArrayMap<>();
        values.put(property, null);

        assertTrue(StateHolderMixinSupport.shouldUseDefaultValue(property, values));
    }
}
