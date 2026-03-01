package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.ProgressBarStyle;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record HudContent(
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
    @Nonnull ProgressBarStyle healthBarStyle,
    @Nonnull HorizontalAlign horizontalAlign,
    @Nonnull VerticalAlign verticalAlign,
    int fontSize,
    int barWidth
) {}
