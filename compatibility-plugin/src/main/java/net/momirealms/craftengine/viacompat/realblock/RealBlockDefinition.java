package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public record RealBlockDefinition(Key id, List<RealBlockStateDefinition> states) {
    public RealBlockDefinition {
        states = List.copyOf(states);
    }

    public record RealBlockStateDefinition(int customIndex, RealBlockShape shape) {
    }
}
