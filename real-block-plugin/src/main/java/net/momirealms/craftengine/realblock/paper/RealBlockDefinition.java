package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.util.Key;

import java.util.List;

record RealBlockDefinition(Key id, List<RealBlockStateDefinition> states) {
    public RealBlockDefinition {
        states = List.copyOf(states);
    }

    public record RealBlockStateDefinition(int customIndex, RealBlockShape shape) {
    }
}
