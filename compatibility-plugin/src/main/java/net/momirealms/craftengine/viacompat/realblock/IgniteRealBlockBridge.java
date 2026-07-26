package net.momirealms.craftengine.viacompat.realblock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Paper plugins are isolated from Ignite mod classes even though both can see
 * Minecraft. Resolve the bridge through Minecraft's Ember class loader and
 * keep every Ignite-owned type behind this JDK-only facade.
 */
public final class IgniteRealBlockBridge {
    private static final String BRIDGE_NAME =
            "net.momirealms.craftengine.realblock.RealBlockRegistryBridge";
    private static final Class<?> BRIDGE = loadBridgeClass(BRIDGE_NAME);
    private static final Class<?> STATE_INPUT = loadBridgeClass(BRIDGE_NAME + "$StateInput");
    private static final Class<?> STATE_COPY = loadBridgeClass(BRIDGE_NAME + "$StateCopy");
    private static final Constructor<?> STATE_INPUT_CONSTRUCTOR =
            constructor(STATE_INPUT, Object.class, int.class);
    private static final Constructor<?> STATE_COPY_CONSTRUCTOR =
            constructor(STATE_COPY, Object.class, Object.class);

    private IgniteRealBlockBridge() {
    }

    public static String runtimeVersion() {
        return (String) invokeStatic("runtimeVersion", new Class<?>[0]);
    }

    public static void installPersisted() {
        invokeStatic("installPersisted", new Class<?>[0]);
    }

    public static void persist(String id, List<Integer> rawStateIds) {
        invokeStatic("persist", new Class<?>[]{String.class, List.class}, id, rawStateIds);
    }

    public static void configureOcclusion(List<Object> states, Object shape) {
        invokeStatic("configureOcclusion", new Class<?>[]{List.class, Object.class}, states, shape);
    }

    public static void configureCollision(Object block, boolean hasCollision) {
        invokeStatic("configureCollision", new Class<?>[]{Object.class, boolean.class}, block, hasCollision);
    }

    public static void refreshStateCaches(List<Object> states) {
        invokeStatic("refreshStateCaches", new Class<?>[]{List.class}, states);
    }

    public static void copyStateSettings(List<StateCopy> states) {
        List<Object> bridgeStates = new ArrayList<>(states.size());
        for (StateCopy state : states) {
            bridgeStates.add(construct(STATE_COPY_CONSTRUCTOR, state.sourceState(), state.targetState()));
        }
        invokeStatic("copyStateSettings", new Class<?>[]{List.class}, bridgeStates);
    }

    public static Installation install(String id, List<StateInput> inputs) {
        List<Object> bridgeInputs = new ArrayList<>(inputs.size());
        for (StateInput input : inputs) {
            bridgeInputs.add(construct(STATE_INPUT_CONSTRUCTOR, input.minecraftState(), input.rawStateId()));
        }
        Object installation = invokeStatic(
                "install",
                new Class<?>[]{String.class, List.class},
                id,
                bridgeInputs
        );
        return convertInstallation(installation);
    }

    private static Installation convertInstallation(Object installation) {
        @SuppressWarnings("unchecked")
        List<Object> bridgeStates = (List<Object>) invoke(installation, "states");
        List<StateMapping> states = new ArrayList<>(bridgeStates.size());
        for (Object bridgeState : bridgeStates) {
            states.add(new StateMapping(
                    (int) invoke(bridgeState, "rawStateId"),
                    invoke(bridgeState, "originalState"),
                    invoke(bridgeState, "realState"),
                    invoke(bridgeState, "originalBlock")
            ));
        }
        return new Installation(
                (String) invoke(installation, "id"),
                (String) invoke(installation, "replacedId"),
                invoke(installation, "realBlock"),
                states
        );
    }

    private static Class<?> loadBridgeClass(String name) {
        try {
            Class<?> minecraftBootstrap = Class.forName(
                    "net.minecraft.server.Bootstrap",
                    false,
                    IgniteRealBlockBridge.class.getClassLoader()
            );
            return Class.forName(name, true, minecraftBootstrap.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "CraftEngine real-block Ignite mod is missing or was not loaded through Ignite",
                    exception
            );
        }
    }

    private static Constructor<?> constructor(Class<?> owner, Class<?>... parameterTypes) {
        try {
            return owner.getConstructor(parameterTypes);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Incompatible Ignite bridge constructor: " + owner.getName(), exception);
        }
    }

    private static Object construct(Constructor<?> constructor, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException exception) {
            throw bridgeFailure("Unable to construct Ignite bridge value", exception);
        }
    }

    private static Object invokeStatic(String name, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Method method = BRIDGE.getMethod(name, parameterTypes);
            return method.invoke(null, arguments);
        } catch (ReflectiveOperationException exception) {
            throw bridgeFailure("Unable to invoke Ignite bridge method " + name, exception);
        }
    }

    private static Object invoke(Object receiver, String name) {
        try {
            return receiver.getClass().getMethod(name).invoke(receiver);
        } catch (ReflectiveOperationException exception) {
            throw bridgeFailure("Unable to read Ignite bridge value " + name, exception);
        }
    }

    private static RuntimeException bridgeFailure(String message, ReflectiveOperationException exception) {
        Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                ? invocation.getCause()
                : exception;
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new IllegalStateException(message, cause);
    }

    public record StateInput(Object minecraftState, int rawStateId) {
    }

    public record StateCopy(Object sourceState, Object targetState) {
    }

    public record StateMapping(int rawStateId, Object originalState, Object realState, Object originalBlock) {
    }

    public record Installation(String id, String replacedId, Object realBlock, List<StateMapping> states) {
        public Installation {
            states = List.copyOf(states);
        }
    }
}
