package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.ProgressBarStyle;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @param infoVisible whether any row of the info panel is enabled
 * @param storage     storage panel content; null while no storage block is targeted or the panel is off
 */
public record HudContent(
    boolean infoVisible,
    @Nullable String timeText,
    @Nullable String biomeText,
    @Nullable String zoneText,
    @Nullable String positionText,
    @Nullable String worldTimeText,
    @Nullable String targetText,
    @Nullable String targetSourceText,
    float targetHealthPct,
    boolean targetEnabled,
    boolean healthBarEnabled,
    boolean captionsEnabled,
    @Nonnull ProgressBarStyle healthBarStyle,
    @Nonnull HorizontalAlign horizontalAlign,
    @Nonnull VerticalAlign verticalAlign,
    @Nonnull HudStyle style,
    @Nullable StoragePanel storage
) {}
