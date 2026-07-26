package net.momirealms.craftengine.realblock;

final class RegistryOccupancy {

    private RegistryOccupancy() {
    }

    static boolean isOccupied(boolean containsKey, Object value, Object canonicalValue) {
        return containsKey && value != canonicalValue;
    }
}
