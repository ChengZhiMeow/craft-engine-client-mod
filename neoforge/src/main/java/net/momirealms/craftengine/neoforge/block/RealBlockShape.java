package net.momirealms.craftengine.neoforge.block;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public record RealBlockShape(
        List<Box> outline,
        List<Box> collision,
        List<Box> support,
        List<Box> occlusion
) {
    public VoxelShape outlineShape() {
        return combine(outline);
    }

    public VoxelShape collisionShape() {
        return combine(collision);
    }

    public VoxelShape supportShape() {
        return combine(support);
    }

    public VoxelShape occlusionShape() {
        return combine(occlusion);
    }

    private static VoxelShape combine(List<Box> boxes) {
        VoxelShape shape = Shapes.empty();
        for (Box box : boxes) {
            shape = Shapes.or(shape, Shapes.box(
                    box.minX / 16.0,
                    box.minY / 16.0,
                    box.minZ / 16.0,
                    box.maxX / 16.0,
                    box.maxY / 16.0,
                    box.maxZ / 16.0
            ));
        }
        return shape.optimize();
    }

    public record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }
}
