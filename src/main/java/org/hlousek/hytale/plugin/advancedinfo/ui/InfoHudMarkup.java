package org.hlousek.hytale.plugin.advancedinfo.ui;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Renders the content of the info panel as inline UI markup for a given {@link HudStyle}.
 *
 * <p>The result is appended under the {@code #AIContent} slot of {@code InfoHud.ui} with
 * {@code UICommandBuilder.appendInline}. Element ids must stay in sync with
 * {@link InfoHud} and {@link ProgressBarWidget}.
 *
 * <p>Inline documents have no file location, so nothing in here may reference a file
 * (textures, imports); such properties live in the static {@code InfoHud.ui}. Styles use
 * the anonymous {@code (…)} form, which is what the server's own inline HUDs use.
 */
final class InfoHudMarkup {

    private static final String TEMPLATE = """
          Group #AIRows {
            LayoutMode: Top;
            Padding: (Horizontal: ${panelHPad}, Vertical: ${panelVPad});
            Group #AITopSpacer { Anchor: (Height: ${rowBottomPad}); }
            Group #AITimeRow {
              LayoutMode: Left;
              Padding: (Bottom: ${rowBottomPad});
              Label #AITimeCaption { Style: ${captionStyle}; Text: "Time"; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Label #AITimeText { Style: ${valueStyle}; Anchor: (MinWidth: ${minWTime}); }
            }
            Group #AIPositionRow {
              LayoutMode: Left;
              Padding: (Bottom: ${rowBottomPad});
              Label #AIPositionCaption { Style: ${captionStyle}; Text: "XYZ"; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Label #AIPositionText { Style: ${valueStyle}; Anchor: (MinWidth: ${minWPosition}); }
            }
            Group #AIBiomeRow {
              LayoutMode: Left;
              Padding: (Bottom: ${rowBottomPad});
              Label #AIBiomeCaption { Style: ${captionStyle}; Text: "Biome"; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Label #AIBiomeText { Style: ${valueStyle}; Anchor: (MinWidth: ${minWBiome}); }
            }
            Group #AIZoneRow {
              LayoutMode: Left;
              Padding: (Bottom: ${rowBottomPad});
              Label #AIZoneCaption { Style: ${captionStyle}; Text: " "; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Label #AIZoneText { Style: ${valueStyle}; Anchor: (MinWidth: ${minWZone}); }
            }
            Group #AIWorldTimeRow {
              LayoutMode: Left;
              Label #AIWorldTimeCaption { Style: ${captionStyle}; Text: "World"; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Label #AIWorldTimeText { Style: ${valueStyle}; Anchor: (MinWidth: ${minWWorldTime}); }
            }
            Group #AITargetRow {
              LayoutMode: Left;
              Padding: (Bottom: ${rowBottomPad});
              Label #AITargetCaption { Style: ${captionStyle}; Text: "Target"; Anchor: (MinWidth: ${minWCaption}); Padding: (${captionPad}); }
              Group #AITargetValueGroup {
                LayoutMode: Top;
                Anchor: (MinWidth: ${minWTarget});
                Group #AITargetBarBelow {
                  LayoutMode: Top;
                  Label #AITargetTextBelow { Style: ${valueStyle}; }
                  Group #AIHealthBarBelow {
                    LayoutMode: Left;
                    Anchor: (Height: ${barHeight}, Left: 0, Right: 0);
                    Group #AIHealthBarBelowFill { Background: (Color: #44aa44); }
                    Group #AIHealthBarBelowRemainder { Background: (Color: #333333); }
                  }
                  Label #AITargetSourceTextBelow { Style: ${sourceStyle}; }
                }
                Group #AITargetBarOverlay {
                  LayoutMode: Top;
                  Group #AIHealthBarOverlay {
                    LayoutMode: Left;
                    Anchor: (Height: ${overlayBarH}, Left: 0, Right: 0);
                    Group #AIHealthBarOverlayFill { Background: (Color: #44aa44); }
                    Group #AIHealthBarOverlayRemainder { Background: (Color: #333333); }
                  }
                  Label #AITargetTextOverlay { Style: ${valueStyle}; Anchor: (Top: -${overlayBarH}); Padding: (Left: ${overlayTextPad}); }
                  Label #AITargetSourceTextOverlay { Style: ${sourceStyle}; }
                }
                Group #AITargetBarNone {
                  LayoutMode: Top;
                  Label #AITargetTextNone { Style: ${valueStyle}; }
                  Group #AITargetBarNoneSpacer { Anchor: (Height: ${barHeight}); }
                  Label #AITargetSourceTextNone { Style: ${sourceStyle}; }
                }
                Group #AITargetBarNoBar {
                  LayoutMode: Top;
                  Label #AITargetTextNoBar { Style: ${valueStyle}; }
                  Label #AITargetSourceTextNoBar { Style: ${sourceStyle}; }
                }
              }
            }
          }
        """;

    private InfoHudMarkup() {}

    @Nonnull
    static String render(@Nonnull HudStyle style) {
        Map<String, String> values = Map.ofEntries(
            Map.entry("valueStyle",     "(FontSize: " + style.fontSize() + ")"),
            Map.entry("captionStyle",   "(FontSize: " + style.captionFontSize() + ", TextColor: #aaaaaa)"),
            Map.entry("sourceStyle",    "(FontSize: " + style.sourceFontSize() + ", RenderBold: true, TextColor: #6699cc)"),
            Map.entry("captionPad",     "Right: " + style.captionRightPadding() + ", Top: " + style.captionTopPadding()),
            Map.entry("panelHPad",      String.valueOf(style.panelHorizontalPadding())),
            Map.entry("panelVPad",      String.valueOf(style.panelVerticalPadding())),
            Map.entry("overlayTextPad", String.valueOf(style.overlayTextPadding())),
            Map.entry("rowBottomPad",   String.valueOf(style.rowBottomPadding())),
            Map.entry("barHeight",      String.valueOf(style.barHeight())),
            Map.entry("overlayBarH",    String.valueOf(style.overlayBarHeight())),
            Map.entry("minWCaption",    String.valueOf(style.captionMinWidth())),
            Map.entry("minWTime",       String.valueOf(style.timeMinWidth())),
            Map.entry("minWPosition",   String.valueOf(style.positionMinWidth())),
            Map.entry("minWBiome",      String.valueOf(style.biomeMinWidth())),
            Map.entry("minWZone",       String.valueOf(style.zoneMinWidth())),
            Map.entry("minWWorldTime",  String.valueOf(style.worldTimeMinWidth())),
            Map.entry("minWTarget",     String.valueOf(style.targetWidth()))
        );

        String out = TEMPLATE;
        for (Map.Entry<String, String> e : values.entrySet()) {
            out = out.replace("${" + e.getKey() + "}", e.getValue());
        }
        if (out.contains("${")) {
            throw new IllegalStateException("Unresolved placeholder in InfoHud markup: " + out);
        }
        return out;
    }
}
