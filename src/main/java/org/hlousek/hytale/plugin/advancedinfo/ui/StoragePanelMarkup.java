package org.hlousek.hytale.plugin.advancedinfo.ui;

import javax.annotation.Nonnull;

/**
 * Renders the storage panel as inline UI markup: {@code rows} rows of {@code perRow} fixed-size
 * item cells. A cell holds the icon and its count, so an item never splits across rows.
 * Cells are filled and hidden by {@link InfoHud}; the last cell also carries the "+N" label.
 * Counts can get a black outline so they stay readable on light icons.
 *
 * <p>Same inline rules as {@link InfoHudMarkup}: no file references, anonymous styles only.
 */
final class StoragePanelMarkup {

    private StoragePanelMarkup() {}

    static String cellId(int index)  { return "#AISCell" + index; }
    static String iconId(int index)  { return "#AISIcon" + index; }
    static String countId(int index) { return "#AISCount" + index; }
    static String rowId(int row)     { return "#AISRow" + row; }
    static final String MORE_ID  = "#AISMore";
    static final String EMPTY_ID = "#AISEmpty";

    @Nonnull
    static String render(@Nonnull HudStyle style, int rows, int perRow) {
        int size = style.storageIconSize();
        int gap  = style.storageCellGap();
        String countStyle = "(FontSize: " + style.storageCountFontSize()
            + ", RenderBold: true, TextColor: #ffffff"
            + (style.storageCountOutline() ? ", OutlineColor: #000000" : "")
            + ", HorizontalAlignment: End, VerticalAlignment: End)";
        String moreStyle = "(FontSize: " + style.storageLabelFontSize()
            + ", RenderBold: true, TextColor: #aaaaaa, HorizontalAlignment: Center, VerticalAlignment: Center)";
        int last = rows * perRow - 1;

        StringBuilder sb = new StringBuilder();
        sb.append("Group #AISRows {\n")
          .append("  LayoutMode: Top;\n")
          .append("  Padding: (Horizontal: ").append(style.panelHorizontalPadding())
          .append(", Vertical: ").append(style.panelVerticalPadding()).append(");\n")
          .append("  Label ").append(EMPTY_ID).append(" { Style: (FontSize: ").append(style.storageLabelFontSize())
          .append(", TextColor: #aaaaaa); Text: \"Empty\"; }\n");
        for (int r = 0; r < rows; r++) {
            sb.append("  Group ").append(rowId(r)).append(" {\n")
              .append("    LayoutMode: Left;\n")
              .append("    Anchor: (Height: ").append(size).append(r > 0 ? ", Top: " + gap : "").append(");\n");
            for (int c = 0; c < perRow; c++) {
                int i = r * perRow + c;
                sb.append("    Group ").append(cellId(i)).append(" {\n")
                  .append("      Anchor: (Width: ").append(size).append(", Height: ").append(size)
                  .append(c > 0 ? ", Left: " + gap : "").append(");\n")
                  .append("      ItemIcon ").append(iconId(i)).append(" { Anchor: (Left: 0, Top: 0, Width: ")
                  .append(size).append(", Height: ").append(size).append("); }\n")
                  .append("      Label ").append(countId(i)).append(" { Anchor: (Right: 0, Bottom: 0); Style: ")
                  .append(countStyle).append("; }\n");
                if (i == last) {
                    sb.append("      Label ").append(MORE_ID).append(" { Style: ").append(moreStyle).append("; }\n");
                }
                sb.append("    }\n");
            }
            sb.append("  }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /** Compact count for the corner of an icon: 999, 1.2k, 12k, 1.2M. */
    @Nonnull
    static String formatCount(int count) {
        if (count < 1_000) { return String.valueOf(count); }
        if (count < 10_000) { return trimZero(count / 100 / 10.0) + "k"; }
        if (count < 1_000_000) { return (count / 1_000) + "k"; }
        return trimZero(count / 100_000 / 10.0) + "M";
    }

    @Nonnull
    private static String trimZero(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
