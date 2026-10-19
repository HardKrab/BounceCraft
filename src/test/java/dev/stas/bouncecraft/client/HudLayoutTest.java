package dev.stas.bouncecraft.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HudLayoutTest {
    @Test
    void topRightWithoutEffectsUsesMargin() {
        assertEquals(8, HudLayout.overlayY(false, false, false));
        assertEquals(1920 - 8 - 100, HudLayout.overlayX(1920, 100));
    }

    @Test
    void sitsBelowBeneficialIcons() {
        int y = HudLayout.overlayY(false, true, false);
        assertEquals(1 + 24 + 4, y);
        assertTrue(y >= 25);
    }

    @Test
    void sitsBelowHarmfulIconRow() {
        assertEquals(1 + 26 + 24 + 4, HudLayout.overlayY(false, false, true));
        assertEquals(1 + 26 + 24 + 4, HudLayout.overlayY(false, true, true));
    }

    @Test
    void demoModeShiftsIconsDown() {
        assertEquals(1 + 15 + 24 + 4, HudLayout.overlayY(true, true, false));
        assertEquals(1 + 15 + 26 + 24 + 4, HudLayout.overlayY(true, false, true));
    }
}
