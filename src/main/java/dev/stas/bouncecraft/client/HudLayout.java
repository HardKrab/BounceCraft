package dev.stas.bouncecraft.client;

/**
 * Top-right HUD placement matching vanilla status-effect icon rows
 * ({@code Gui.renderEffects}: 24×24, y=1 beneficial / y=27 harmful, demo +15).
 */
public final class HudLayout {
    public static final int MARGIN = 8;
    public static final int EFFECT_ICON = 24;
    public static final int EFFECT_TOP = 1;
    public static final int HARMFUL_ROW_OFFSET = 26;
    public static final int DEMO_OFFSET = 15;
    public static final int EFFECT_GAP = 4;

    private HudLayout() {
    }

    public static int overlayX(int guiWidth, int contentWidth) {
        return guiWidth - MARGIN - contentWidth;
    }

    /**
     * Baseline Y for the overlay. Sits below vanilla potion icons when they occupy
     * the top-right; otherwise a small top margin.
     */
    public static int overlayY(boolean demo, boolean beneficialIcons, boolean harmfulIcons) {
        if (!beneficialIcons && !harmfulIcons) {
            return MARGIN;
        }
        int top = EFFECT_TOP + (demo ? DEMO_OFFSET : 0);
        int bottom = harmfulIcons ? top + HARMFUL_ROW_OFFSET + EFFECT_ICON : top + EFFECT_ICON;
        return bottom + EFFECT_GAP;
    }
}
