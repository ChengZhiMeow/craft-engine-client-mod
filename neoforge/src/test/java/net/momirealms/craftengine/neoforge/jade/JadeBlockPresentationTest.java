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
}
