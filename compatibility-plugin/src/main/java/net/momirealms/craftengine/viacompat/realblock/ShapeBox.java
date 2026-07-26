package net.momirealms.craftengine.viacompat.realblock;

public record ShapeBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

    public ShapeBox {
        float[] values = {minX, minY, minZ, maxX, maxY, maxZ};
        for (float value : values) {
            if (!Float.isFinite(value) || value < 0 || value > 16) {
                throw new IllegalArgumentException("Shape coordinates must be finite and between 0 and 16");
            }
        }
        if (minX >= maxX || minY >= maxY || minZ >= maxZ) {
            throw new IllegalArgumentException("Shape minimum must be lower than maximum");
        }
    }
}
