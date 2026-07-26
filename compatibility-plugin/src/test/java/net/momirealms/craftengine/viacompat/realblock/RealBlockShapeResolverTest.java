package net.momirealms.craftengine.viacompat.realblock;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RealBlockShapeResolverTest {

    @Test
    void selectorTreatsUnspecifiedStatePropertiesAsWildcards() {
        RealBlockShape north = shape(0);
        RealBlockShape up = shape(1);
        RealBlockShapeResolver resolver = new RealBlockShapeResolver(null, List.of(
                new RealBlockShapeResolver.Rule(Map.of("facing", "north"), north),
                new RealBlockShapeResolver.Rule(Map.of("facing", "up"), up)
        ));

        assertSame(north, resolver.resolve(Map.of("facing", "north", "lit", "false")));
        assertSame(north, resolver.resolve(Map.of("facing", "north", "lit", "true")));
        assertSame(up, resolver.resolve(Map.of("facing", "up", "lit", "false")));
    }

    @Test
    void parsesCompoundSelectorsAndRejectsMalformedSelectors() {
        assertEquals(
                Map.of("facing", "south", "lit", "true"),
                RealBlockShapeResolver.parseSelector("facing=south, lit=true")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> RealBlockShapeResolver.parseSelector("facing")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> RealBlockShapeResolver.parseSelector("facing=north,facing=south")
        );
    }

    @Test
    void validatesSelectorPropertiesAndRequiresEveryStateToMatch() {
        RealBlockShapeResolver resolver = new RealBlockShapeResolver(null, List.of(
                new RealBlockShapeResolver.Rule(Map.of("direction", "north"), shape(0))
        ));

        assertThrows(IllegalArgumentException.class, () -> resolver.validateProperties(Set.of("facing", "lit")));
        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(Map.of("direction", "south"))
        );
    }

    private static RealBlockShape shape(float offset) {
        ShapeBox box = new ShapeBox(offset, 0, 0, offset + 1, 1, 1);
        return new RealBlockShape(List.of(box), List.of(box), List.of(box), List.of());
    }
}
