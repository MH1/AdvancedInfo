package org.hlousek.hytale.plugin.advancedinfo.settings;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerHudSettingsTest {

    @Test
    void migrationFrom120AddsStorageDefaultsAndKeepsExistingValues() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setFontSize(24);
        s.setStorageEnabled(false);
        s.setStorageMaxRows(5);
        setVersion(s, "1.2.0");

        s.applyMigration();

        assertEquals(24, s.getFontSize(), "settings from older versions must be kept");
        assertTrue(s.isStorageEnabled());
        assertEquals(HorizontalAlign.LEFT, s.getStorageHorizontalAlign());
        assertEquals(VerticalAlign.TOP, s.getStorageVerticalAlign());
        assertEquals(2, s.getStorageMaxRows());
        assertEquals(18, s.getStorageItemSize());
        assertTrue(s.isStorageCountOutline());
    }

    @Test
    void migrationFrom120PlacesStoragePanelLikeInfoPanel() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setHorizontalAlign(HorizontalAlign.RIGHT);
        s.setVerticalAlign(VerticalAlign.BOTTOM);
        setVersion(s, "1.2.0");

        s.applyMigration();

        assertEquals(HorizontalAlign.RIGHT, s.getStorageHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, s.getStorageVerticalAlign());
    }

    @Test
    void migrationFrom130KeepsStorageAlignment() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setHorizontalAlign(HorizontalAlign.RIGHT);
        s.setStorageHorizontalAlign(HorizontalAlign.CENTER);
        setVersion(s, "1.3.0");

        s.applyMigration();

        assertEquals(HorizontalAlign.CENTER, s.getStorageHorizontalAlign());
    }

    @Test
    void migrationFrom130AddsFullLabelDefault() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setStorageFullLabel(false);
        setVersion(s, "1.3.0");

        s.applyMigration();

        assertTrue(s.isStorageFullLabel());
    }

    @Test
    void migrationFrom130GivesTheStoragePanelTheInfoPanelOpacity() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setBackgroundOpacity(25);
        setVersion(s, "1.3.0");

        s.applyMigration();

        assertEquals(25, s.getStorageBackgroundOpacity());
    }

    @Test
    void migrationFrom140KeepsStorageOpacity() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setBackgroundOpacity(25);
        s.setStorageBackgroundOpacity(80);
        setVersion(s, "1.4.0");

        s.applyMigration();

        assertEquals(80, s.getStorageBackgroundOpacity());
    }

    @Test
    void migrationFrom140KeepsFullLabel() throws Exception {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setStorageFullLabel(false);
        setVersion(s, "1.4.0");

        s.applyMigration();

        assertFalse(s.isStorageFullLabel());
    }

    @Test
    void middleIsOnlyOfferedToTheStoragePanel() {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setStorageVerticalAlign(VerticalAlign.MIDDLE);
        s.setVerticalAlign(VerticalAlign.MIDDLE);

        assertEquals(VerticalAlign.MIDDLE, s.getStorageVerticalAlign());
        assertEquals(VerticalAlign.TOP, s.getVerticalAlign(), "the info panel never sits in the middle");
    }

    @Test
    void cloneCopiesStorageSettings() {
        PlayerHudSettings s = new PlayerHudSettings();
        s.setStorageEnabled(false);
        s.setStorageHorizontalAlign(HorizontalAlign.RIGHT);
        s.setStorageVerticalAlign(VerticalAlign.BOTTOM);
        s.setStorageMaxRows(4);
        s.setStorageFullLabel(false);
        s.setStorageBackgroundOpacity(15);

        PlayerHudSettings copy = (PlayerHudSettings) s.clone();

        assertFalse(copy.isStorageEnabled());
        assertEquals(HorizontalAlign.RIGHT, copy.getStorageHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, copy.getStorageVerticalAlign());
        assertEquals(4, copy.getStorageMaxRows());
        assertFalse(copy.isStorageFullLabel());
        assertEquals(15, copy.getStorageBackgroundOpacity());
    }

    private static void setVersion(PlayerHudSettings s, String version) throws Exception {
        Field f = PlayerHudSettings.class.getDeclaredField("version");
        f.setAccessible(true);
        f.set(s, version);
    }
}
