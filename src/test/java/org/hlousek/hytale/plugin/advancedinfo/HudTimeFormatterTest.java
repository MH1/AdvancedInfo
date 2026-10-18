package org.hlousek.hytale.plugin.advancedinfo;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.WorldDayFormat;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudTimeFormatterTest {

    private static final ZonedDateTime PRAGUE_AFTERNOON =
        ZonedDateTime.of(2026, 3, 1, 14, 5, 9, 0, ZoneId.of("Europe/Prague"));

    @Test
    void realTime24hWithoutOffset() {
        assertEquals("14:05:09", HudTimeFormatter.realTime(PRAGUE_AFTERNOON, true, false));
    }

    @Test
    void realTime12hWithoutOffset() {
        String text = HudTimeFormatter.realTime(PRAGUE_AFTERNOON, false, false);
        assertEquals("02:05:09", text.substring(0, 8));
    }

    @Test
    void realTimeAppendsPositiveOffset() {
        assertEquals("14:05:09 [+1]", HudTimeFormatter.realTime(PRAGUE_AFTERNOON, true, true));
    }

    @Test
    void realTimeAppendsNegativeOffset() {
        ZonedDateTime newYork = PRAGUE_AFTERNOON.withZoneSameInstant(ZoneId.of("America/New_York"));
        assertEquals("08:05:09 [-5]", HudTimeFormatter.realTime(newYork, true, true));
    }

    @Test
    void realTimeLabelsUtcOffsetAsUtc() {
        ZonedDateTime utc = PRAGUE_AFTERNOON.withZoneSameInstant(ZoneId.of("UTC"));
        assertEquals("13:05:09 [UTC]", HudTimeFormatter.realTime(utc, true, true));
    }

    private static final Instant EPOCH = Instant.parse("0001-01-01T00:00:00Z");

    @Test
    void worldTimeShowsDayOfYearAndShortTime() {
        LocalDateTime gameTime = LocalDateTime.of(2000, 1, 3, 18, 30);
        Instant instant = EPOCH.plusSeconds(400L * 86_400 + 18 * 3600 + 30 * 60);
        assertEquals("Day 3, 18:30", HudTimeFormatter.worldTime(gameTime, instant, EPOCH, WorldDayFormat.DAY_OF_YEAR, true));
    }

    @Test
    void worldTimeCanHideTheDay() {
        LocalDateTime gameTime = LocalDateTime.of(2000, 1, 3, 18, 30);
        assertEquals("18:30", HudTimeFormatter.worldTime(gameTime, EPOCH, EPOCH, WorldDayFormat.NONE, true));
        assertEquals("06:30 PM", HudTimeFormatter.worldTime(gameTime, EPOCH, EPOCH, WorldDayFormat.NONE, false).replace("\u202f", " ").replace("\u00a0", " "));
    }

    @Test
    void worldTimeCountsTotalDaysFromEpochStartingAtOne() {
        LocalDateTime gameTime = LocalDateTime.of(2000, 1, 3, 18, 30);
        assertEquals("Day 1, 18:30",   HudTimeFormatter.worldTime(gameTime, EPOCH.plusSeconds(18 * 3600), EPOCH, WorldDayFormat.TOTAL_DAYS, true));
        assertEquals("Day 401, 18:30", HudTimeFormatter.worldTime(gameTime, EPOCH.plusSeconds(400L * 86_400 + 60), EPOCH, WorldDayFormat.TOTAL_DAYS, true));
    }
}
