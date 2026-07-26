package net.momirealms.craftengine.viacompat.realblock;

import java.util.List;

public record RealBlockShape(
        List<ShapeBox> outline,
        List<ShapeBox> collision,
        List<ShapeBox> support,
        List<ShapeBox> occlusion
) {
    public RealBlockShape {
        outline = List.copyOf(outline);
        collision = List.copyOf(collision);
        support = List.copyOf(support);
        occlusion = List.copyOf(occlusion);
    }
}
