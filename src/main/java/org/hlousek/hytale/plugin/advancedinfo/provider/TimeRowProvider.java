package org.hlousek.hytale.plugin.advancedinfo.provider;

import org.hlousek.hytale.plugin.advancedinfo.HudTimeFormatter;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;

/** Wall-clock time in the player's configured timezone. */
public final class TimeRowProvider implements InfoRowProvider {

    @Override
    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isTimeEnabled();
    }

    @Override
    @Nonnull
    public String text(@Nonnull InfoContext ctx) {
        PlayerHudSettings settings = ctx.settings();
        ZonedDateTime now = ZonedDateTime.now(settings.getTimezone());
        boolean useOffset = !settings.isTimezoneSet() || settings.isTimezoneOffsetVisible();
        return HudTimeFormatter.realTime(now, settings.isTime24h(), useOffset);
    }
}
