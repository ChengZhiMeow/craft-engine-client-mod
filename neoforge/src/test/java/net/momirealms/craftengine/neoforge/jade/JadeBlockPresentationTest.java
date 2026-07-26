package net.momirealms.craftengine.neoforge.jade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JadeBlockPresentationTest {

    @Test
    void translatedRealBlockKeepsRegistryPresentationWithoutEntityRedirect() {
        assertTrue(JadeBlockPresentation.useRegistryPresentation(true, true));
        assertFalse(JadeBlockPresentation.redirectToItemDisplay(true));
    }

    @Test
    void untranslatedRealBlockUsesCraftEngineItemFallbackWithoutEntityRedirect() {
        assertFalse(JadeBlockPresentation.useRegistryPresentation(true, false));
        assertFalse(JadeBlockPresentation.redirectToItemDisplay(true));
    }

    @Test
    void SyntheticCraftEngineBlockRetainsExistingModelAndEntityBehavior() {
        assertFalse(JadeBlockPresentation.useRegistryPresentation(false, true));
        assertTrue(JadeBlockPresentation.redirectToItemDisplay(false));
    }

    @Test
    void namedNonEmptyItemDisplayIsUsable() {
        assertTrue(JadeBlockPresentation.isUsableItemDisplay(false, "真实线缆方块"));
    }

    @Test
    void unnamedItemDisplayIsIgnored() {
        assertFalse(JadeBlockPresentation.isUsableItemDisplay(false, null));
        assertFalse(JadeBlockPresentation.isUsableItemDisplay(false, "  "));
    }

    @Test
    void emptyNamedItemDisplayIsIgnored() {
        assertFalse(JadeBlockPresentation.isUsableItemDisplay(true, "真实线缆方块"));
    }

    @Test
    void directlyTargetedUnnamedItemDisplayIsIgnored() {
        assertTrue(JadeBlockPresentation.ignoreDirectItemDisplay(true, null));
        assertTrue(JadeBlockPresentation.ignoreDirectItemDisplay(true, " "));
        assertFalse(JadeBlockPresentation.ignoreDirectItemDisplay(true, "展示名称"));
        assertFalse(JadeBlockPresentation.ignoreDirectItemDisplay(false, null));
    }
}
