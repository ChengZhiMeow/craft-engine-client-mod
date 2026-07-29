package net.momirealms.craftengine.realblock.api;

/**
 * A block-local collision box using CraftEngine model coordinates ({@code 0..16}).
 */
public record CollisionBox(
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
) {

    public CollisionBox {
        float[] values = {minX, minY, minZ, maxX, maxY, maxZ};
        for (float value : values) {
            if (!Float.isFinite(value) || value < 0 || value > 16) {
                throw new IllegalArgumentException("Collision coordinates must be finite and between 0 and 16");
            }
        }
        if (minX >= maxX || minY >= maxY || minZ >= maxZ) {
            throw new IllegalArgumentException("Collision minimum must be lower than maximum");
        }
    }
}
