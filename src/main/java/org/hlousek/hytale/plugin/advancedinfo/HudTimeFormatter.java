package org.hlousek.hytale.plugin.advancedinfo;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.WorldDayFormat;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class HudTimeFormatter {

    private static final DateTimeFormatter FORMATTER_24H       = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_12H       = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FORMATTER_SHORT_24H = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATTER_SHORT_12H = DateTimeFormatter.ofPattern("hh:mm a");

    private HudTimeFormatter() {}

    @Nonnull
    public static String realTime(@Nonnull ZonedDateTime now, boolean use24h, boolean useOffset) {
        String time = now.format(use24h ? FORMATTER_24H : FORMATTER_12H);
        if (!useOffset) return time;
        int offsetHours = now.getOffset().getTotalSeconds() / 3600;
        String offsetLabel = offsetHours == 0 ? "UTC"
            : (offsetHours > 0 ? "+" + offsetHours : String.valueOf(offsetHours));
        return time + " [" + offsetLabel + "]";
    }

    /**
     * Formats the in-game time with an optional day prefix.
     *
     * @param gameDateTime calendar view of the game time (day of year, time of day)
     * @param gameTime     absolute game time
     * @param epoch        start of the game calendar (year zero); total days are counted from here
     */
    @Nonnull
    public static String worldTime(
        @Nonnull LocalDateTime gameDateTime,
        @Nonnull Instant gameTime,
        @Nonnull Instant epoch,
        @Nonnull WorldDayFormat dayFormat,
        boolean use24h
    ) {
        String timeOfDay = gameDateTime.format(use24h ? FORMATTER_SHORT_24H : FORMATTER_SHORT_12H);
        return switch (dayFormat) {
            case NONE        -> timeOfDay;
            case DAY_OF_YEAR -> "Day " + gameDateTime.getDayOfYear() + ", " + timeOfDay;
            case TOTAL_DAYS  -> "Day " + (Duration.between(epoch, gameTime).toDays() + 1) + ", " + timeOfDay;
        };
    }
}
