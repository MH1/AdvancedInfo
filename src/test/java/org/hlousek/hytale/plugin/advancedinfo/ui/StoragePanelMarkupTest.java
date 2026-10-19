package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoragePanelMarkupTest {

    @Test
    void rendersEveryCellAddressedFromJava() {
        HudStyle style = HudStyle.DEFAULT;
        int perRow = style.storageItemsPerRow();
        String markup = StoragePanelMarkup.render(style, 2, perRow);
        for (int i = 0; i < 2 * perRow; i++) {
            assertTrue(markup.contains(StoragePanelMarkup.cellId(i) + " "), "missing cell " + i);
            assertTrue(markup.contains(StoragePanelMarkup.iconId(i) + " "), "missing icon " + i);
            assertTrue(markup.contains(StoragePanelMarkup.countId(i) + " "), "missing count " + i);
        }
        assertFalse(markup.contains(StoragePanelMarkup.cellId(2 * perRow) + " "), "too many cells");
        assertTrue(markup.contains(StoragePanelMarkup.rowId(1) + " "));
        assertTrue(markup.contains(StoragePanelMarkup.MORE_ID + " "));
        assertTrue(markup.contains(StoragePanelMarkup.EMPTY_ID + " "));
    }

    @Test
    void bracesAreBalancedForEverySupportedSize() {
        for (int fs = HudStyle.MIN_FONT_SIZE; fs <= HudStyle.MAX_FONT_SIZE; fs++) {
            HudStyle style = new HudStyle(fs, HudStyle.DEFAULT_OPACITY);
            String markup = StoragePanelMarkup.render(style, 6, style.storageItemsPerRow());
            long open  = markup.chars().filter(c -> c == '{').count();
            long close = markup.chars().filter(c -> c == '}').count();
            assertEquals(open, close, "font size " + fs);
        }
    }

    @Test
    void inlineMarkupNeverReferencesFiles() {
        String markup = StoragePanelMarkup.render(HudStyle.DEFAULT, 2, HudStyle.DEFAULT.storageItemsPerRow());
        assertFalse(markup.contains("TexturePath"));
        assertFalse(markup.contains("$"));
        assertFalse(markup.contains(".png") || markup.contains(".ui"));
    }

    @Test
    void cellsFitIntoTheInfoPanelWidthForEveryFontAndItemSize() {
        for (int fs = HudStyle.MIN_FONT_SIZE; fs <= HudStyle.MAX_FONT_SIZE; fs++) {
            for (int is = HudStyle.MIN_STORAGE_ITEM_SIZE; is <= HudStyle.MAX_STORAGE_ITEM_SIZE; is++) {
                HudStyle s = new HudStyle(fs, HudStyle.DEFAULT_OPACITY, is);
                int n = s.storageItemsPerRow();
                int used = n * s.storageIconSize() + (n - 1) * s.storageCellGap() + 2 * s.panelHorizontalPadding();
                assertTrue(n == 1 || used <= s.panelWidth(),
                    "font " + fs + ", item size " + is + ": " + used + " > " + s.panelWidth());
            }
        }
    }

    @Test
    void itemSizeChangesItemsPerRowButNotPanelWidth() {
        HudStyle small = new HudStyle(18, HudStyle.DEFAULT_OPACITY, 10);
        HudStyle large = new HudStyle(18, HudStyle.DEFAULT_OPACITY, 32);
        assertEquals(small.panelWidth(), large.panelWidth());
        assertTrue(small.storageItemsPerRow() > large.storageItemsPerRow());
        assertTrue(small.storageCountFontSize() < large.storageCountFontSize());
    }

    @Test
    void outlineFollowsTheSetting() {
        HudStyle with    = new HudStyle(18, HudStyle.DEFAULT_OPACITY, 18, true);
        HudStyle without = new HudStyle(18, HudStyle.DEFAULT_OPACITY, 18, false);
        assertTrue(StoragePanelMarkup.render(with, 1, with.storageItemsPerRow()).contains("OutlineColor"));
        assertFalse(StoragePanelMarkup.render(without, 1, without.storageItemsPerRow()).contains("OutlineColor"));
    }

    @Test
    void itemSizeIsClampedToTheSmallestReadableSize() {
        assertEquals(HudStyle.MIN_STORAGE_ITEM_SIZE, new HudStyle(18, HudStyle.DEFAULT_OPACITY, 8).storageItemSize());
    }

    @Test
    void formatsCountsCompactly() {
        assertEquals("7", StoragePanelMarkup.formatCount(7));
        assertEquals("999", StoragePanelMarkup.formatCount(999));
        assertEquals("1k", StoragePanelMarkup.formatCount(1_000));
        assertEquals("1.2k", StoragePanelMarkup.formatCount(1_299));
        assertEquals("12k", StoragePanelMarkup.formatCount(12_345));
        assertEquals("1.5M", StoragePanelMarkup.formatCount(1_550_000));
    }
}
