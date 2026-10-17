package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;

/**
 * Visual parameters of the info panel that are baked into the rendered markup.
 *
 * <p>All pixel metrics are derived from {@link #fontSize()} relative to the
 * reference size of 18 px, which is the size the layout was designed at.
 * Adding a new style parameter means adding a record component here and using
 * it in {@link InfoHudMarkup} or {@link InfoHud#renderContent}; no templates
 * need to be generated.
 */
public record HudStyle(int fontSize, int backgroundOpacity) {

    public static final int MIN_FONT_SIZE     = 8;
    public static final int MAX_FONT_SIZE     = 32;
    public static final int DEFAULT_FONT_SIZE = 18;

    /** Panel background opacity in percent; 0 is fully transparent. */
    public static final int MIN_OPACITY     = 0;
    public static final int MAX_OPACITY     = 100;
    public static final int DEFAULT_OPACITY = 60;

    public static final String BACKGROUND_COLOR = "#000000";

    public static final HudStyle DEFAULT = new HudStyle(DEFAULT_FONT_SIZE, DEFAULT_OPACITY);

    public HudStyle {
        fontSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, fontSize));
        backgroundOpacity = Math.max(MIN_OPACITY, Math.min(MAX_OPACITY, backgroundOpacity));
    }

    @Nonnull
    public static HudStyle of(@Nonnull PlayerHudSettings settings) {
        return new HudStyle(settings.getFontSize(), settings.getBackgroundOpacity());
    }

    /**
     * Background color as {@code #RRGGBBAA}. The runtime color converter only accepts
     * hex notation; the {@code #rrggbb(alpha)} form is document-parser syntax.
     */
    @Nonnull
    public String backgroundColor() {
        int alpha = Math.round(backgroundOpacity * 255f / 100f);
        return String.format("%s%02x", BACKGROUND_COLOR, alpha);
    }

    public float scale() {
        return fontSize / (float) DEFAULT_FONT_SIZE;
    }

    public int scaled(float px) {
        return Math.round(px * scale());
    }

    public int captionFontSize()      { return Math.round(fontSize * 13f / DEFAULT_FONT_SIZE); }
    public int sourceFontSize()       { return Math.round(fontSize * 12f / DEFAULT_FONT_SIZE); }
    public int captionTopPadding()    { return Math.max(0, (fontSize - captionFontSize()) / 2); }
    public int captionRightPadding()  { return Math.max(2, scaled(6)); }
    public int overlayBarHeight()     { return fontSize + 6; }
    public int overlayTextPadding()   { return Math.max(1, scaled(4)); }
    public int rowBottomPadding()     { return scaled(4); }
    public int barHeight()            { return Math.max(1, scaled(6)); }

    /** Inner padding of the panel; the design value plus the former 3 px row-group padding. */
    public int panelHorizontalPadding() { return Math.max(4, scaled(16)); }
    public int panelVerticalPadding()   { return Math.max(3, scaled(11)); }

    public int captionMinWidth()      { return scaled(50); }
    public int timeMinWidth()         { return scaled(220); }
    public int positionMinWidth()     { return scaled(180); }
    public int biomeMinWidth()        { return scaled(190); }
    public int zoneMinWidth()         { return scaled(120); }
    public int worldTimeMinWidth()    { return scaled(80); }

    /** Width of the target value column; also the full width of the health bar. */
    public int targetWidth()          { return scaled(300); }
}
