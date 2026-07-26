package net.momirealms.craftengine.neoforge.jade;

import org.jetbrains.annotations.Nullable;

final class JadeBlockPresentation {
    private JadeBlockPresentation() {
    }

    static boolean useRegistryPresentation(boolean realBlock, boolean hasRegistryTranslation) {
        return realBlock && hasRegistryTranslation;
    }

    static boolean redirectToItemDisplay(boolean realBlock) {
        return !realBlock;
    }

    static boolean isUsableItemDisplay(boolean itemEmpty, @Nullable String customName) {
        return !itemEmpty && customName != null && !customName.isBlank();
    }

    static boolean ignoreDirectItemDisplay(boolean itemDisplay, @Nullable String customName) {
        return itemDisplay && (customName == null || customName.isBlank());
    }
}
