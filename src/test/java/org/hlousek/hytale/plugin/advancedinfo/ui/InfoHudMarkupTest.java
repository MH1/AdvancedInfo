package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfoHudMarkupTest {

    @Test
    void allPlaceholdersAreResolvedForEverySupportedSize() {
        for (int fs = HudStyle.MIN_FONT_SIZE; fs <= HudStyle.MAX_FONT_SIZE; fs++) {
            String markup = InfoHudMarkup.render(new HudStyle(fs, HudStyle.DEFAULT_OPACITY));
            assertFalse(markup.contains("${"), "unresolved placeholder at font size " + fs);
            assertTrue(markup.contains("(FontSize: " + fs + ")"), "font size " + fs + " not applied");
        }
    }

    @Test
    void bracesAreBalanced() {
        String markup = InfoHudMarkup.render(HudStyle.DEFAULT);
        long open  = markup.chars().filter(c -> c == '{').count();
        long close = markup.chars().filter(c -> c == '}').count();
        assertEquals(open, close);
    }

    @Test
    void containsElementsAddressedFromJava() {
        String markup = InfoHudMarkup.render(HudStyle.DEFAULT);
        for (String id : new String[] {
            "#AIRows", "#AITimeRow", "#AITimeText", "#AITimeCaption",
            "#AIPositionRow", "#AIBiomeRow", "#AIZoneRow", "#AIWorldTimeRow", "#AITargetRow",
            "#AITargetBarBelow", "#AITargetBarOverlay", "#AITargetBarNone", "#AITargetBarNoBar",
            "#AIHealthBarBelowFill", "#AIHealthBarOverlayFill"
        }) {
            assertTrue(markup.contains(id + " "), "missing element " + id);
        }
        assertFalse(markup.contains("#AIH" + "Hud"), "legacy #AIH prefix must not be used");
    }

    @Test
    void inlineMarkupNeverReferencesFiles() {
        String markup = InfoHudMarkup.render(HudStyle.DEFAULT);
        assertFalse(markup.contains("TexturePath"), "textures must live in the static InfoHud.ui");
        assertFalse(markup.contains("$"), "document imports are not resolvable inline");
        assertFalse(markup.contains(".png") || markup.contains(".ui"), "no file references inline");
    }
}
