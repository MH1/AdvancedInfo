package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.ProgressBarStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages a two-line progress bar widget in the HUD, which shows a first label,
 * an optional progress bar, and a second label.
 *
 * <p>The widget has four mutually exclusive display modes backed by
 * separate element groups in the {@code .ui} file:
 * <ul>
 *   <li><b>Below</b>   - first line, then 6 px bar below it, then second line</li>
 *   <li><b>Overlay</b> - 24 px bar with first line rendered on top, then second line</li>
 *   <li><b>None</b>    - first line, 6 px spacer, second line (bar enabled but no progress data)</li>
 *   <li><b>NoBar</b>   - first line, second line with no spacer (bar disabled entirely)</li>
 * </ul>
 *
 * <p>Call {@link #apply(UICommandBuilder)} every tick to push state to the UI.
 */
public class ProgressBarWidget {

    // -------------------------------------------------------------------------
    // UI selector prefixes - must match the groups in the .ui file exactly.
    // -------------------------------------------------------------------------
    private static final String GRP_BELOW   = "#AIHTargetBarBelow";
    private static final String GRP_OVERLAY = "#AIHTargetBarOverlay";
    private static final String GRP_NONE    = "#AIHTargetBarNone";
    private static final String GRP_NO_BAR  = "#AIHTargetBarNoBar";

    private static final String LBL_FIRST_BELOW   = "#AIHTargetTextBelow";
    private static final String LBL_FIRST_OVERLAY = "#AIHTargetTextOverlay";
    private static final String LBL_FIRST_NONE    = "#AIHTargetTextNone";
    private static final String LBL_FIRST_NO_BAR  = "#AIHTargetTextNoBar";

    private static final String LBL_SECOND_BELOW   = "#AIHTargetSourceTextBelow";
    private static final String LBL_SECOND_OVERLAY = "#AIHTargetSourceTextOverlay";
    private static final String LBL_SECOND_NONE    = "#AIHTargetSourceTextNone";
    private static final String LBL_SECOND_NO_BAR  = "#AIHTargetSourceTextNoBar";

    private static final String BAR_FILL_BELOW        = "#AIHHealthBarBelowFill";
    private static final String BAR_REMAINDER_BELOW   = "#AIHHealthBarBelowRemainder";
    private static final String BAR_FILL_OVERLAY      = "#AIHHealthBarOverlayFill";
    private static final String BAR_REMAINDER_OVERLAY = "#AIHHealthBarOverlayRemainder";

    // -------------------------------------------------------------------------
    // Public properties
    // -------------------------------------------------------------------------

    /** Text shown on the first line. Null renders as empty string. */
    @Nullable private String firstLine;

    /** Text shown on the second line. Null renders as empty string. */
    @Nullable private String secondLine;

    /** Progress bar display style. */
    @Nonnull private ProgressBarStyle mode = ProgressBarStyle.BELOW;

    /**
     * Whether the progress bar is enabled.
     * When {@code false} no bar or spacer is shown between the two lines.
     */
    private boolean barVisible = true;

    /**
     * Fill fraction [0.0, 1.0], or negative when there is no progress data.
     * A negative value hides the bar even if {@link #barVisible} is {@code true}.
     */
    private float progress = -1f;

    /** Pixel width of the full bar. Should match the {@code MinWidth} of the value group in the .ui. */
    private int width = 300;

    /**
     * Optional solid color override for the fill bar (e.g. {@code "#ff4400"}).
     * When non-null this bypasses the health-gradient logic.
     * When null the color is computed from {@link #progress} via the gradient.
     */
    @Nullable private String fillColor = null;

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setFirstLine(@Nullable String firstLine)   { this.firstLine  = firstLine; }
    public void setSecondLine(@Nullable String secondLine) { this.secondLine = secondLine; }
    public void setMode(@Nonnull ProgressBarStyle mode)    { this.mode       = mode; }
    public void setBarVisible(boolean barVisible)          { this.barVisible  = barVisible; }
    public void setProgress(float progress)                { this.progress    = progress; }
    public void setWidth(int width)                        { this.width       = width; }
    public void setFillColor(@Nullable String fillColor)   { this.fillColor   = fillColor; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Nullable public String           getFirstLine()  { return firstLine; }
    @Nullable public String           getSecondLine() { return secondLine; }
    @Nonnull  public ProgressBarStyle getMode()       { return mode; }
    public boolean isBarVisible()                     { return barVisible; }
    public float   getProgress()                      { return progress; }
    public int     getWidth()                         { return width; }
    @Nullable public String           getFillColor()  { return fillColor; }

    // -------------------------------------------------------------------------
    // State helpers
    // -------------------------------------------------------------------------

    public boolean stateEquals(@Nullable ProgressBarWidget other) {
        if (other == null) return false;
        return java.util.Objects.equals(firstLine,  other.firstLine)
            && java.util.Objects.equals(secondLine, other.secondLine)
            && java.util.Objects.equals(fillColor,  other.fillColor)
            && mode       == other.mode
            && barVisible == other.barVisible
            && progress   == other.progress
            && width      == other.width;
    }

    // -------------------------------------------------------------------------
    // Apply
    // -------------------------------------------------------------------------

    /**
     * Pushes the current state to the {@link UICommandBuilder}.
     * Call this every tick inside {@code _update()}.
     */
    public void apply(@Nonnull UICommandBuilder ui) {
        boolean showBar = barVisible && progress >= 0f;
        boolean overlay = showBar && mode == ProgressBarStyle.OVERLAY;
        boolean below   = showBar && !overlay;
        boolean none    = !showBar && barVisible && mode == ProgressBarStyle.BELOW;
        boolean noBar   = !showBar && !none;

        String t = firstLine  != null ? firstLine  : "";
        String s = secondLine != null ? secondLine : "";

        // Broadcast text to all copies so switching modes is instant.
        ui.set(LBL_FIRST_BELOW   + ".Text", t);
        ui.set(LBL_FIRST_OVERLAY + ".Text", t);
        ui.set(LBL_FIRST_NONE    + ".Text", t);
        ui.set(LBL_FIRST_NO_BAR  + ".Text", t);

        ui.set(LBL_SECOND_BELOW   + ".Text", s);
        ui.set(LBL_SECOND_OVERLAY + ".Text", s);
        ui.set(LBL_SECOND_NONE    + ".Text", s);
        ui.set(LBL_SECOND_NO_BAR  + ".Text", s);

        // Show exactly one group.
        ui.set(GRP_BELOW   + ".Visible", below);
        ui.set(GRP_OVERLAY + ".Visible", overlay);
        ui.set(GRP_NONE    + ".Visible", none);
        ui.set(GRP_NO_BAR  + ".Visible", noBar);

        if (below) {
            applyBar(ui, BAR_FILL_BELOW, BAR_REMAINDER_BELOW, false);
        } else if (overlay) {
            applyBar(ui, BAR_FILL_OVERLAY, BAR_REMAINDER_OVERLAY, true);
        }
    }

    private void applyBar(
        @Nonnull UICommandBuilder ui,
        @Nonnull String fillId,
        @Nonnull String remId,
        boolean darkFill
    ) {
        float pct     = Math.max(0f, Math.min(1f, progress));
        int fillWidth = Math.round(pct * width);
        int remWidth  = width - fillWidth;

        Anchor fillAnchor = new Anchor();
        fillAnchor.setWidth(Value.of(fillWidth));
        ui.setObject(fillId + ".Anchor", fillAnchor);

        Anchor remAnchor = new Anchor();
        remAnchor.setWidth(Value.of(remWidth));
        ui.setObject(remId + ".Anchor", remAnchor);

        PatchStyle fillColorStyle = new PatchStyle();
        fillColorStyle.setColor(Value.of(fillColor != null ? fillColor : healthBarColor(pct, darkFill)));
        ui.setObject(fillId + ".Background", fillColorStyle);
    }

    // -------------------------------------------------------------------------
    // Color
    // -------------------------------------------------------------------------

    /**
     * Color stops (normal): 0% = #aa2222 red, 25% = #cc6611 orange, 50% = #aaaa22 yellow, 100% = #44aa44 green.
     * Color stops (dark):   0% = #661111 red, 25% = #884400 orange, 50% = #777711 yellow, 100% = #227722 green.
     */
    private static String healthBarColor(float pct, boolean dark) {
        int r, g, b;
        if (pct <= 0.25f) {
            float t = pct / 0.25f;
            r = lerp(dark ? 0x66 : 0xaa, dark ? 0x88 : 0xcc, t);
            g = lerp(dark ? 0x11 : 0x22, dark ? 0x44 : 0x66, t);
            b = lerp(dark ? 0x11 : 0x22, dark ? 0x00 : 0x11, t);
        } else if (pct <= 0.5f) {
            float t = (pct - 0.25f) / 0.25f;
            r = lerp(dark ? 0x88 : 0xcc, dark ? 0x77 : 0xaa, t);
            g = lerp(dark ? 0x44 : 0x66, dark ? 0x77 : 0xaa, t);
            b = lerp(dark ? 0x00 : 0x11, dark ? 0x11 : 0x22, t);
        } else {
            float t = (pct - 0.5f) / 0.5f;
            r = lerp(dark ? 0x77 : 0xaa, dark ? 0x22 : 0x44, t);
            g = lerp(dark ? 0x77 : 0xaa, dark ? 0x77 : 0xaa, t);
            b = lerp(dark ? 0x11 : 0x22, dark ? 0x22 : 0x44, t);
        }
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }
}
