package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudStyleTest {

    @Test
    void fontSizeIsClampedToSupportedRange() {
        assertEquals(HudStyle.MIN_FONT_SIZE, new HudStyle(1, 80).fontSize());
        assertEquals(HudStyle.MAX_FONT_SIZE, new HudStyle(100, 80).fontSize());
        assertEquals(20, new HudStyle(20, 80).fontSize());
    }

    @Test
    void referenceSizeKeepsDesignMetrics() {
        HudStyle s = HudStyle.DEFAULT;
        assertEquals(1f, s.scale());
        assertEquals(13, s.captionFontSize());
        assertEquals(12, s.sourceFontSize());
        assertEquals(2, s.captionTopPadding());
        assertEquals(24, s.overlayBarHeight());
        assertEquals(4, s.rowBottomPadding());
        assertEquals(6, s.barHeight());
        assertEquals(16, s.panelHorizontalPadding());
        assertEquals(11, s.panelVerticalPadding());
        assertEquals(6, s.captionRightPadding());
        assertEquals(4, s.overlayTextPadding());
        assertEquals(300, s.targetWidth());
    }

    @Test
    void metricsScaleWithFontSize() {
        HudStyle s = new HudStyle(9, 80);
        assertEquals(0.5f, s.scale());
        assertEquals(150, s.targetWidth());
        assertEquals(3, s.barHeight());
        assertEquals(2, s.rowBottomPadding());
        assertEquals(8, s.panelHorizontalPadding());
        assertEquals(6, s.panelVerticalPadding());
    }

    @Test
    void paddingsScaleWithoutCollapsing() {
        HudStyle small = new HudStyle(HudStyle.MIN_FONT_SIZE, 60);
        HudStyle large = new HudStyle(HudStyle.MAX_FONT_SIZE, 60);
        assertTrue(small.panelHorizontalPadding() >= 4 && small.panelHorizontalPadding() < large.panelHorizontalPadding());
        assertTrue(small.captionRightPadding() >= 2 && small.captionRightPadding() < large.captionRightPadding());
        assertTrue(small.overlayTextPadding() >= 1 && small.overlayTextPadding() < large.overlayTextPadding());
    }

    @Test
    void opacityIsClampedAndRenderedAsUiColor() {
        assertEquals(0, new HudStyle(18, -5).backgroundOpacity());
        assertEquals(100, new HudStyle(18, 250).backgroundOpacity());
        assertEquals("#00000099", HudStyle.DEFAULT.backgroundColor());
        assertEquals("#00000000", new HudStyle(18, 0).backgroundColor());
        assertEquals("#000000ff", new HudStyle(18, 100).backgroundColor());
        assertEquals("#00000059", new HudStyle(18, 35).backgroundColor());
    }

    @Test
    void storageOpacityIsIndependentAndDefaultsToTheInfoPanel() {
        assertEquals("#00000099", HudStyle.DEFAULT.storageBackgroundColor());
        assertEquals("#00000099", new HudStyle(18, 60, 18, true).storageBackgroundColor());
        HudStyle s = new HudStyle(18, 60, 18, true, 20);
        assertEquals("#00000099", s.backgroundColor());
        assertEquals("#00000033", s.storageBackgroundColor());
        assertEquals(100, new HudStyle(18, 60, 18, true, 150).storageBackgroundOpacity());
    }

    @Test
    void middlePanelGrowsOnlyAsNeededAndAtMostOneAndAHalfTimes() {
        HudStyle s = HudStyle.DEFAULT;
        int regular = s.storageItemsPerRow();
        int widest  = s.storageItemsPerRow(Math.round(s.panelWidth() * HudStyle.STORAGE_MIDDLE_MAX_WIDTH));
        assertTrue(widest > regular);

        assertEquals(regular, s.storageMiddleColumns(0, 2));
        assertEquals(regular, s.storageMiddleColumns(2 * regular, 2));
        assertEquals(regular + 1, s.storageMiddleColumns(2 * regular + 1, 2));
        assertEquals(widest, s.storageMiddleColumns(1000, 2));
        assertEquals(regular, s.storageMiddleColumns(1000, 0));

        assertEquals(s.panelWidth(), s.storagePanelWidth(regular));
        assertEquals(s.panelWidth(), s.storagePanelWidth(1));
        int grown = s.storagePanelWidth(regular + 1);
        assertTrue(grown > s.panelWidth());
        assertTrue(s.storagePanelWidth(widest) <= Math.round(s.panelWidth() * HudStyle.STORAGE_MIDDLE_MAX_WIDTH));
    }

    @Test
    void smallSizesNeverProduceZeroHeightBar() {
        assertTrue(new HudStyle(HudStyle.MIN_FONT_SIZE, 80).barHeight() >= 1);
    }
}
