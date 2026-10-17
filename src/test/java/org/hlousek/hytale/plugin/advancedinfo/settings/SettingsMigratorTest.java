package org.hlousek.hytale.plugin.advancedinfo.settings;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsMigratorTest {

    static class Sample {
        @Since(version = "1.0.0", defaultValue = "true")  boolean oldFlag = false;
        @Since(version = "1.0.1", defaultValue = "42")    int newNumber = 0;
        @Since(version = "1.1.0", defaultValue = "Europe/Prague") ZoneId futureZone = ZoneId.of("UTC");
        String untouched = "keep";
    }

    @Test
    void appliesDefaultsOnlyForFieldsNewerThanStoredVersion() {
        Sample s = new Sample();
        SettingsMigrator.migrate(s, "1.0.0", "1.0.1");
        assertFalse(s.oldFlag, "field introduced at or before stored version must not be reset");
        assertEquals(42, s.newNumber);
        assertEquals(ZoneId.of("UTC"), s.futureZone, "field beyond target version must not be touched");
        assertEquals("keep", s.untouched);
    }

    @Test
    void doesNothingWhenAlreadyCurrent() {
        Sample s = new Sample();
        SettingsMigrator.migrate(s, "1.0.1", "1.0.1");
        assertEquals(0, s.newNumber);
    }

    @Test
    void resetAppliesAllDefaults() {
        Sample s = new Sample();
        SettingsMigrator.resetToDefaults(s);
        assertTrue(s.oldFlag);
        assertEquals(42, s.newNumber);
        assertEquals(ZoneId.of("Europe/Prague"), s.futureZone);
        assertEquals("keep", s.untouched);
    }

    @Test
    void parseIgnoresSegmentsBeyondPatchAndTolerantOfGarbage() {
        assertArrayEquals(new int[] {1, 2, 3}, SettingsMigrator.parse("1.2.3.4"));
        assertArrayEquals(new int[] {1, 0, 0}, SettingsMigrator.parse("1"));
        assertArrayEquals(new int[] {0, 0, 0}, SettingsMigrator.parse("x.y"));
    }

    @Test
    void compareOrdersSemantically() {
        assertTrue(SettingsMigrator.compare(new int[] {1, 0, 1}, new int[] {1, 0, 0}) > 0);
        assertTrue(SettingsMigrator.compare(new int[] {0, 9, 9}, new int[] {1, 0, 0}) < 0);
        assertEquals(0, SettingsMigrator.compare(new int[] {2, 1, 0}, new int[] {2, 1, 0}));
    }
}
