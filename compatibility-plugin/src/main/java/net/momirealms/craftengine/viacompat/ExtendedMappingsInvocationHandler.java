package net.momirealms.craftengine.viacompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class ExtendedMappingsInvocationHandler implements InvocationHandler {
    private final Object delegate;
    private final int firstExtendedId;
    private final int extendedRegistrySize;

    private ExtendedMappingsInvocationHandler(Object delegate, int firstExtendedId, int extendedRegistrySize) {
        this.delegate = delegate;
        this.firstExtendedId = firstExtendedId;
        this.extendedRegistrySize = extendedRegistrySize;
    }

    static Object wrap(Object delegate, int firstExtendedId, int extendedRegistrySize) {
        if (firstExtendedId < 0 || extendedRegistrySize <= firstExtendedId) {
            throw new IllegalArgumentException(
                    "Invalid extended block state range [" + firstExtendedId + ", " + extendedRegistrySize + ")"
            );
        }
        Class<?> mappingsType = findMappingsInterface(delegate.getClass());
        return Proxy.newProxyInstance(
                mappingsType.getClassLoader(),
                new Class<?>[]{mappingsType},
                new ExtendedMappingsInvocationHandler(delegate, firstExtendedId, extendedRegistrySize)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (("getNewId".equals(name) || "getNewIdOrDefault".equals(name) || "contains".equals(name))
                && args != null && args.length >= 1 && args[0] instanceof Integer id && isExtended(id)) {
            return "contains".equals(name) ? true : id;
        }
        if (("size".equals(name) || "mappedSize".equals(name)) && method.getParameterCount() == 0) {
            return Math.max((int) invokeDelegate(method, args), this.extendedRegistrySize);
        }
        if ("inverse".equals(name) && method.getParameterCount() == 0) {
            return wrap(invokeDelegate(method, args), this.firstExtendedId, this.extendedRegistrySize);
        }
        return invokeDelegate(method, args);
    }

    private boolean isExtended(int id) {
        return id >= this.firstExtendedId && id < this.extendedRegistrySize;
    }

    private Object invokeDelegate(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(this.delegate, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private static Class<?> findMappingsInterface(Class<?> type) {
        Class<?> compatibleInterface = null;
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Class<?> interfaceType : current.getInterfaces()) {
                if ("com.viaversion.viaversion.api.data.Mappings".equals(interfaceType.getName())) {
                    return interfaceType;
                }
                if (hasMappingsContract(interfaceType)) {
                    compatibleInterface = interfaceType;
                }
            }
        }
        if (compatibleInterface != null) {
            return compatibleInterface;
        }
        throw new IllegalArgumentException(type.getName() + " does not implement ViaVersion Mappings");
    }

    private static boolean hasMappingsContract(Class<?> type) {
        try {
            type.getMethod("getNewId", int.class);
            type.getMethod("size");
            type.getMethod("mappedSize");
            type.getMethod("inverse");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
