package net.momirealms.craftengine.realblock;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public final class StateHolderMixinSupport {
    private StateHolderMixinSupport() {
    }

    public static boolean shouldUseDefaultValue(
            Property<?> property,
            Map<Property<?>, Comparable<?>> values
    ) {
        return "ce_state".equals(property.getName()) && values.get(property) == null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> T valueOrDefault(
            Property<T> property,
            Map<Property<?>, Comparable<?>> values
    ) {
        Comparable<?> value = values.get(property);
        return value == null ? property.getPossibleValues().getFirst() : (T) value;
    }
}
