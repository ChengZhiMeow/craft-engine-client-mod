package net.momirealms.craftengine.neoforge.jade;

final class JadeBlockPresentation {
    private JadeBlockPresentation() {
    }

    static boolean useRegistryPresentation(boolean realBlock, boolean hasRegistryTranslation) {
        return realBlock && hasRegistryTranslation;
    }

    static boolean redirectToItemDisplay(boolean realBlock) {
        return !realBlock;
    }
}
