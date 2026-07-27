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
}
