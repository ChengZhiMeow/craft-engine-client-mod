package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.block.BlockShape;

import java.lang.reflect.Method;
import java.util.List;

final class ConfiguredBlockShape implements BlockShape {
    private static final Method EMPTY;
    private static final Method BOX;
    private static final Method OR;

    static {
        try {
            Class<?> shapes = Class.forName("net.minecraft.world.phys.shapes.Shapes");
            Class<?> voxelShape = Class.forName("net.minecraft.world.phys.shapes.VoxelShape");
            EMPTY = shapes.getMethod("empty");
            BOX = shapes.getMethod("box", double.class, double.class, double.class, double.class, double.class, double.class);
            OR = shapes.getMethod("or", voxelShape, voxelShape);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private final Object outline;
    private final Object collision;
    private final Object support;
    private final Object occlusion;

    ConfiguredBlockShape(RealBlockShape shape) {
        this.outline = createShape(shape.outline());
        this.collision = createShape(shape.collision());
        this.support = createShape(shape.support());
        this.occlusion = createShape(shape.occlusion());
    }

    @Override
    public Object getShape(Object block, Object[] arguments) {
        return outline;
    }

    @Override
    public Object getCollisionShape(Object block, Object[] arguments) {
        return collision;
    }

    @Override
    public Object getSupportShape(Object block, Object[] arguments) {
        return support;
    }

    Object occlusion() {
        return occlusion;
    }

    private static Object createShape(List<ShapeBox> boxes) {
        try {
            Object result = EMPTY.invoke(null);
            for (ShapeBox box : boxes) {
                Object part = BOX.invoke(
                        null,
                        box.minX() / 16.0,
                        box.minY() / 16.0,
                        box.minZ() / 16.0,
                        box.maxX() / 16.0,
                        box.maxY() / 16.0,
                        box.maxZ() / 16.0
                );
                result = OR.invoke(null, result, part);
            }
            return result;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to construct a Minecraft voxel shape", exception);
        }
    }
}
