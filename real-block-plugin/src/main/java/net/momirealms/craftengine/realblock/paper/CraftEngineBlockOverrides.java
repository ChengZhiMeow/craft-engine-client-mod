package net.momirealms.craftengine.realblock.paper;

import java.lang.reflect.Field;
import java.util.Map;

final class CraftEngineBlockOverrides {
    private static final String FIELD_NAME = "blockStateOverrides";

    private CraftEngineBlockOverrides() {
    }

    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> mutable(Object blockManager) {
        Field field = findField(blockManager.getClass());
        if (!field.trySetAccessible()) {
            throw new IllegalStateException("cannot access CraftEngine " + FIELD_NAME + " field");
        }
        try {
            Object value = field.get(blockManager);
            if (!(value instanceof Map<?, ?> map)) {
                throw new IllegalStateException("CraftEngine " + FIELD_NAME + " field is not a Map");
            }
            return (Map<K, V>) map;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("cannot read CraftEngine " + FIELD_NAME + " field", exception);
        }
    }

    private static Field findField(Class<?> type) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(FIELD_NAME);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("CraftEngine " + FIELD_NAME + " field does not exist");
    }
}
