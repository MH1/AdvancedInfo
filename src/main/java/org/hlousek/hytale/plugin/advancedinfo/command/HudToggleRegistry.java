package org.hlousek.hytale.plugin.advancedinfo.command;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class HudToggleRegistry {

    private static final Map<String, HudToggleEntry> ENTRIES;

    static {
        Map<String, HudToggleEntry> map = new LinkedHashMap<>();

        register(map, new HudToggleEntry(
            "time",
            "the time display",
            s -> s.setTimeEnabled(true),
            s -> s.setTimeEnabled(false),
            PlayerHudSettings::isTimeEnabled
        ));

        register(map, new HudToggleEntry(
            "biome",
            "the biome display",
            s -> s.setBiomeEnabled(true),
            s -> s.setBiomeEnabled(false),
            PlayerHudSettings::isBiomeEnabled
        ));

        register(map, new HudToggleEntry(
            "position",
            "the position display",
            s -> s.setPositionEnabled(true),
            s -> s.setPositionEnabled(false),
            PlayerHudSettings::isPositionEnabled
        ));

        register(map, new HudToggleEntry(
            "worldtime",
            "the world time display",
            s -> s.setWorldTimeEnabled(true),
            s -> s.setWorldTimeEnabled(false),
            PlayerHudSettings::isWorldTimeEnabled
        ));

        register(map, new HudToggleEntry(
            "zone",
            "the zone and tier display",
            s -> s.setZoneEnabled(true),
            s -> s.setZoneEnabled(false),
            PlayerHudSettings::isZoneEnabled
        ));

        register(map, new HudToggleEntry(
            "target",
            "the target display",
            s -> s.setTargetEnabled(true),
            s -> s.setTargetEnabled(false),
            PlayerHudSettings::isTargetEnabled
        ));

        register(map, new HudToggleEntry(
            "healthbar",
            "the target health bar",
            s -> s.setHealthBarEnabled(true),
            s -> s.setHealthBarEnabled(false),
            PlayerHudSettings::isHealthBarEnabled,
            PlayerHudSettings::isTargetEnabled,
            "Target display is disabled. Enable it first with /ai show target.",
            true
        ));

        register(map, new HudToggleEntry(
            "timezone",
            "the timezone offset label on the time display",
            s -> s.setTimezoneOffsetVisible(true),
            s -> s.setTimezoneOffsetVisible(false),
            PlayerHudSettings::isTimezoneOffsetVisible,
            PlayerHudSettings::isTimezoneSet,
            "No timezone set. The offset [UTC] is always shown. Use /ai config timezone to set one.",
            true
        ));

        ENTRIES = Collections.unmodifiableMap(map);
    }

    private static void register(@Nonnull Map<String, HudToggleEntry> map, @Nonnull HudToggleEntry entry) {
        map.put(entry.getName(), entry);
    }

    @Nonnull
    public static Map<String, HudToggleEntry> getEntries() {
        return ENTRIES;
    }
}
