package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.block.BlockShape;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DispatchingBlockShapeTest {

    @Test
    void selectsShapeByTheRealStateInTheFirstArgument() {
        Object northState = new Object();
        Object upState = new Object();
        DispatchingBlockShape shape = new DispatchingBlockShape(List.of(
                new DispatchingBlockShape.Delegate(northState, new MarkerShape("north")),
                new DispatchingBlockShape.Delegate(upState, new MarkerShape("up"))
        ));

        assertEquals("north-outline", shape.getShape(null, new Object[]{northState}));
        assertEquals("up-collision", shape.getCollisionShape(null, new Object[]{upState}));
        assertEquals("up-support", shape.getSupportShape(null, new Object[]{upState}));
    }

    private record MarkerShape(String marker) implements BlockShape {
        @Override
        public Object getShape(Object block, Object[] arguments) {
            return marker + "-outline";
        }

        @Override
        public Object getCollisionShape(Object block, Object[] arguments) {
            return marker + "-collision";
        }

        @Override
        public Object getSupportShape(Object block, Object[] arguments) {
            return marker + "-support";
        }
    }
}
