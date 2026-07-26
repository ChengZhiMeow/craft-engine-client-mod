package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.block.BlockShape;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class DispatchingBlockShape implements BlockShape {
    private final Map<Object, Delegate> delegates = new IdentityHashMap<>();
    private final Delegate fallback;

    DispatchingBlockShape(List<Delegate> delegates) {
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("at least one block shape delegate is required");
        }
        this.fallback = delegates.getFirst();
        for (Delegate delegate : delegates) {
            this.delegates.put(delegate.realState(), delegate);
        }
    }

    private BlockShape select(Object[] arguments) {
        if (arguments.length == 0) {
            return fallback.shape();
        }
        return delegates.getOrDefault(arguments[0], fallback).shape();
    }

    @Override
    public Object getShape(Object block, Object[] arguments) {
        return select(arguments).getShape(block, arguments);
    }

    @Override
    public Object getCollisionShape(Object block, Object[] arguments) {
        return select(arguments).getCollisionShape(block, arguments);
    }

    @Override
    public Object getSupportShape(Object block, Object[] arguments) {
        return select(arguments).getSupportShape(block, arguments);
    }

    record Delegate(Object realState, BlockShape shape) {
    }
}
