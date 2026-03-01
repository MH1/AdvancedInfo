package org.hlousek.hytale.plugin.advancedinfo;

import javax.annotation.Nonnull;
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

    @Nonnull
    public static String worldTime(@Nonnull LocalDateTime gameDateTime, boolean use24h) {
        int day = gameDateTime.getDayOfYear();
        String timeOfDay = gameDateTime.format(use24h ? FORMATTER_SHORT_24H : FORMATTER_SHORT_12H);
        return "Day " + day + ", " + timeOfDay;
    }
}
