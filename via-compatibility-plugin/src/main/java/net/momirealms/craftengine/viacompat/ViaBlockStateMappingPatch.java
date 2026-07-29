package net.momirealms.craftengine.viacompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class ViaBlockStateMappingPatch {
    private final ClassLoader viaBackwardsClassLoader;
    private final List<String> protocolClassNames;
    private final int firstExtendedId;
    private final int extendedRegistrySize;
    private final List<PatchedField> patchedFields = new ArrayList<>();

    ViaBlockStateMappingPatch(ClassLoader viaBackwardsClassLoader,
                              List<String> protocolClassNames,
                              int firstExtendedId,
                              int extendedRegistrySize) {
        this.viaBackwardsClassLoader = viaBackwardsClassLoader;
        this.protocolClassNames = List.copyOf(protocolClassNames);
        this.firstExtendedId = firstExtendedId;
        this.extendedRegistrySize = extendedRegistrySize;
    }

    int apply() throws ReflectiveOperationException {
        if (!this.patchedFields.isEmpty()) {
            return this.patchedFields.size();
        }

        List<PatchedField> applied = new ArrayList<>();
        try {
            for (String protocolClassName : this.protocolClassNames) {
                Object mappingData = readStaticField(protocolClassName, "MAPPINGS");
                Field blockStateMappingsField = findField(mappingData.getClass(), "blockStateMappings");
                blockStateMappingsField.setAccessible(true);
                Object originalMappings = blockStateMappingsField.get(mappingData);
                if (originalMappings == null) {
                    throw new MappingsNotReadyException();
                }
                Object extendedMappings = ExtendedMappingsInvocationHandler.wrap(
                        originalMappings,
                        this.firstExtendedId,
                        this.extendedRegistrySize
                );
                blockStateMappingsField.set(mappingData, extendedMappings);
                applied.add(new PatchedField(mappingData, blockStateMappingsField, originalMappings, extendedMappings));
                validateExtendedMappings(extendedMappings);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            restore(applied);
            throw exception;
        }

        this.patchedFields.addAll(applied);
        return this.patchedFields.size();
    }

    void restore() {
        restore(this.patchedFields);
        this.patchedFields.clear();
    }

    int firstExtendedId() {
        return this.firstExtendedId;
    }

    int extendedRegistrySize() {
        return this.extendedRegistrySize;
    }

    private Object readStaticField(String className, String fieldName) throws ReflectiveOperationException {
        Class<?> protocolClass = Class.forName(className, true, this.viaBackwardsClassLoader);
        Field field = protocolClass.getField(fieldName);
        return field.get(null);
    }

    private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + fieldName);
    }

    private void validateExtendedMappings(Object mappings) throws ReflectiveOperationException {
        Method getNewId = mappings.getClass().getMethod("getNewId", int.class);
        Method size = mappings.getClass().getMethod("size");
        Method mappedSize = mappings.getClass().getMethod("mappedSize");
        int firstMappedId = (int) getNewId.invoke(mappings, this.firstExtendedId);
        int lastMappedId = (int) getNewId.invoke(mappings, this.extendedRegistrySize - 1);
        int sourceSize = (int) size.invoke(mappings);
        int targetSize = (int) mappedSize.invoke(mappings);
        if (firstMappedId != this.firstExtendedId
                || lastMappedId != this.extendedRegistrySize - 1
                || sourceSize < this.extendedRegistrySize
                || targetSize < this.extendedRegistrySize) {
            throw new IllegalStateException(
                    "ViaBackwards mapping verification failed: first=" + firstMappedId
                            + ", last=" + lastMappedId
                            + ", size=" + sourceSize
                            + ", mappedSize=" + targetSize
            );
        }
    }

    private static void restore(List<PatchedField> fields) {
        for (int index = fields.size() - 1; index >= 0; index--) {
            PatchedField patched = fields.get(index);
            try {
                if (patched.field().get(patched.target()) == patched.replacement()) {
                    patched.field().set(patched.target(), patched.original());
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private record PatchedField(Object target, Field field, Object original, Object replacement) {
    }

    static final class MappingsNotReadyException extends RuntimeException {
    }
}
