package net.momirealms.craftengine.realblock.paper;

import java.util.ArrayList;
import java.util.List;

final class CenterArmShapeGenerator {
    static final int DOWN = 1;
    static final int UP = 1 << 1;
    static final int NORTH = 1 << 2;
    static final int SOUTH = 1 << 3;
    static final int WEST = 1 << 4;
    static final int EAST = 1 << 5;

    private CenterArmShapeGenerator() {
    }

    static RealBlockShape create(float radius, int connections) {
        if (connections < 0 || connections > 63) {
            throw new IllegalArgumentException("six-way connection mask must be between 0 and 63");
        }
        float min = 8.0f - radius;
        float max = 8.0f + radius;
        List<ShapeBox> boxes = new ArrayList<>(7);
        boxes.add(new ShapeBox(min, min, min, max, max, max));
        addIf(boxes, connections, DOWN, new ShapeBox(min, 0, min, max, min, max));
        addIf(boxes, connections, UP, new ShapeBox(min, max, min, max, 16, max));
        addIf(boxes, connections, NORTH, new ShapeBox(min, min, 0, max, max, min));
        addIf(boxes, connections, SOUTH, new ShapeBox(min, min, max, max, max, 16));
        addIf(boxes, connections, WEST, new ShapeBox(0, min, min, min, max, max));
        addIf(boxes, connections, EAST, new ShapeBox(max, min, min, 16, max, max));
        List<ShapeBox> shape = List.copyOf(boxes);
        return new RealBlockShape(shape, shape, shape, List.of());
    }

    private static void addIf(List<ShapeBox> boxes, int connections, int direction, ShapeBox box) {
        if ((connections & direction) != 0) {
            boxes.add(box);
        }
    }
}
