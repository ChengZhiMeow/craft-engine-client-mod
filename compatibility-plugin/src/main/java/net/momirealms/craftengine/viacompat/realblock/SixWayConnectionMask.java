package net.momirealms.craftengine.viacompat.realblock;

final class SixWayConnectionMask {

    private SixWayConnectionMask() {
    }

    static int update(int connections, int directionIndex, boolean connected) {
        if (connections < 0 || connections > 63) {
            throw new IllegalArgumentException("six-way connection mask must be between 0 and 63");
        }
        if (directionIndex < 0 || directionIndex >= 6) {
            throw new IllegalArgumentException("six-way direction index must be between 0 and 5");
        }
        int directionBit = 1 << directionIndex;
        return connected ? connections | directionBit : connections & ~directionBit;
    }
}
