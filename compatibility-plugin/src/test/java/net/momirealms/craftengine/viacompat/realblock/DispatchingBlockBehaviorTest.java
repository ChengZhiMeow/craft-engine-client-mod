package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchingBlockBehaviorTest {
    private static final Set<String> NON_DISPATCH_METHODS = Set.of(
            "block",
            "let",
            "getFirst"
    );

    @Test
    void overridesEveryPublicBehaviorEntryPoint() {
        Set<String> overrides = Set.of(DispatchingBlockBehavior.class.getDeclaredMethods()).stream()
                .map(DispatchingBlockBehaviorTest::signature)
                .collect(Collectors.toSet());

        for (Method method : BlockBehavior.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())
                    || Modifier.isStatic(method.getModifiers())
                    || NON_DISPATCH_METHODS.contains(method.getName())
                    || method.isSynthetic()) {
                continue;
            }
            assertTrue(
                    overrides.contains(signature(method)),
                    () -> "missing dispatch override for " + method
            );
        }
    }

    private static String signature(Method method) {
        return method.getName() + java.util.Arrays.toString(method.getParameterTypes());
    }
}
