package org.hlousek.hytale.plugin.advancedinfo.settings;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.time.ZoneId;

public class PlayerHudSettings implements Component<EntityStore> {

    private static final String CURRENT_VERSION = "1.0.0";

    public enum HorizontalAlign { LEFT, CENTER, RIGHT }
    public enum VerticalAlign   { TOP, BOTTOM }
    public enum ProgressBarStyle { BELOW, OVERLAY }

    private String version = "0.0.0";
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean timeEnabled = true;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean biomeEnabled = true;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean positionEnabled = true;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean worldTimeEnabled = true;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean zoneEnabled = false;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean time24h = true;
    @Since(version = "1.0.0", defaultValue = "UTC")
    private ZoneId timezone = ZoneId.of("UTC");
    @Since(version = "1.0.0", defaultValue = "false")
    private boolean timezoneOffsetVisible = false;
    @Since(version = "1.0.0", defaultValue = "LEFT")
    private String horizontalAlign = "LEFT";
    @Since(version = "1.0.0", defaultValue = "TOP")
    private String verticalAlign = "TOP";
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean targetEnabled = true;
    @Since(version = "1.0.0", defaultValue = "true")
    private boolean healthBarEnabled = true;
    @Since(version = "1.0.0", defaultValue = "OVERLAY")
    private String healthBarStyle = "OVERLAY";
    @Since(version = "1.0.0", defaultValue = "18")
    private int fontSize = 18;

    public PlayerHudSettings() {}

    public boolean isTimeEnabled()             { return timeEnabled; }
    public void setTimeEnabled(boolean v)      { this.timeEnabled = v; }

    public boolean isBiomeEnabled()            { return biomeEnabled; }
    public void setBiomeEnabled(boolean v)     { this.biomeEnabled = v; }

    public boolean isPositionEnabled()         { return positionEnabled; }
    public void setPositionEnabled(boolean v)  { this.positionEnabled = v; }

    public boolean isWorldTimeEnabled()        { return worldTimeEnabled; }
    public void setWorldTimeEnabled(boolean v) { this.worldTimeEnabled = v; }

    public boolean isZoneEnabled()             { return zoneEnabled; }
    public void setZoneEnabled(boolean v)      { this.zoneEnabled = v; }

    public boolean isTime24h()                 { return time24h; }
    public void setTime24h(boolean v)          { this.time24h = v; }

    public ZoneId getTimezone()                { return timezone; }
    public void setTimezone(ZoneId v)          { this.timezone = v; }

    public boolean isTimezoneOffsetVisible()        { return timezoneOffsetVisible; }
    public void setTimezoneOffsetVisible(boolean v) { this.timezoneOffsetVisible = v; }

    public boolean isTargetEnabled()            { return targetEnabled; }
    public void setTargetEnabled(boolean v)     { this.targetEnabled = v; }

    public boolean isHealthBarEnabled()         { return healthBarEnabled; }
    public void setHealthBarEnabled(boolean v)  { this.healthBarEnabled = v; }

    public ProgressBarStyle getHealthBarStyle() {
        try { return ProgressBarStyle.valueOf(healthBarStyle); } catch (Exception e) { return ProgressBarStyle.BELOW; }
    }
    public void setHealthBarStyle(ProgressBarStyle v) { this.healthBarStyle = v.name(); }

    public int getFontSize()              { return fontSize; }
    public void setFontSize(int v)        { this.fontSize = v; }

    public HorizontalAlign getHorizontalAlign() {
        try { return HorizontalAlign.valueOf(horizontalAlign); } catch (Exception e) { return HorizontalAlign.LEFT; }
    }
    public void setHorizontalAlign(HorizontalAlign v) { this.horizontalAlign = v.name(); }

    public VerticalAlign getVerticalAlign() {
        try { return VerticalAlign.valueOf(verticalAlign); } catch (Exception e) { return VerticalAlign.TOP; }
    }
    public void setVerticalAlign(VerticalAlign v) { this.verticalAlign = v.name(); }

    public boolean isTimezoneSet() {
        return !timezone.equals(ZoneId.of("UTC"));
    }

    /** Called after deserialization to apply defaults for fields added in newer versions. */
    public void applyMigration() {
        SettingsMigrator.migrate(this, version, CURRENT_VERSION);
        version = CURRENT_VERSION;
    }

    public void reset() {
        SettingsMigrator.resetToDefaults(this);
        version = CURRENT_VERSION;
    }

    @Override
    public Component<EntityStore> clone() {
        PlayerHudSettings copy = new PlayerHudSettings();
        copy.version               = this.version;
        copy.timeEnabled           = this.timeEnabled;
        copy.biomeEnabled          = this.biomeEnabled;
        copy.positionEnabled       = this.positionEnabled;
        copy.worldTimeEnabled      = this.worldTimeEnabled;
        copy.zoneEnabled           = this.zoneEnabled;
        copy.targetEnabled         = this.targetEnabled;
        copy.healthBarEnabled      = this.healthBarEnabled;
        copy.healthBarStyle        = this.healthBarStyle;
        copy.fontSize              = this.fontSize;
        copy.time24h               = this.time24h;
        copy.timezone              = this.timezone;
        copy.timezoneOffsetVisible = this.timezoneOffsetVisible;
        copy.horizontalAlign       = this.horizontalAlign;
        copy.verticalAlign         = this.verticalAlign;
        return copy;
    }

    public static final BuilderCodec<PlayerHudSettings> CODEC =
        BuilderCodec.builder(PlayerHudSettings.class, PlayerHudSettings::new)
            .append(new KeyedCodec<>("Version",              Codec.STRING),
                (c, v) -> c.version = v, c -> c.version).add()
            .append(new KeyedCodec<>("TimeEnabled",          Codec.BOOLEAN),
                (c, v) -> c.timeEnabled = v, c -> c.timeEnabled).add()
            .append(new KeyedCodec<>("BiomeEnabled",         Codec.BOOLEAN),
                (c, v) -> c.biomeEnabled = v, c -> c.biomeEnabled).add()
            .append(new KeyedCodec<>("PositionEnabled",      Codec.BOOLEAN),
                (c, v) -> c.positionEnabled = v, c -> c.positionEnabled).add()
            .append(new KeyedCodec<>("WorldTimeEnabled",     Codec.BOOLEAN),
                (c, v) -> c.worldTimeEnabled = v, c -> c.worldTimeEnabled).add()
            .append(new KeyedCodec<>("ZoneEnabled",          Codec.BOOLEAN),
                (c, v) -> c.zoneEnabled = v, c -> c.zoneEnabled).add()
            .append(new KeyedCodec<>("TargetEnabled",        Codec.BOOLEAN),
                (c, v) -> c.targetEnabled = v, c -> c.targetEnabled).add()
            .append(new KeyedCodec<>("HealthBarEnabled",     Codec.BOOLEAN),
                (c, v) -> c.healthBarEnabled = v, c -> c.healthBarEnabled).add()
            .append(new KeyedCodec<>("HealthBarStyle",       Codec.STRING),
                (c, v) -> c.healthBarStyle = v, c -> c.healthBarStyle).add()
            .append(new KeyedCodec<>("FontSize",             Codec.INTEGER),
                (c, v) -> c.fontSize = v, c -> c.fontSize).add()
            .append(new KeyedCodec<>("Time24h",              Codec.BOOLEAN),
                (c, v) -> c.time24h = v, c -> c.time24h).add()
            .append(new KeyedCodec<>("Timezone",             Codec.STRING),
                (c, v) -> { try { c.timezone = ZoneId.of(v); } catch (Exception ignored) {} },
                c -> c.timezone.getId()).add()
            .append(new KeyedCodec<>("TimezoneOffsetVisible", Codec.BOOLEAN),
                (c, v) -> c.timezoneOffsetVisible = v, c -> c.timezoneOffsetVisible).add()
            .append(new KeyedCodec<>("HorizontalAlign",      Codec.STRING),
                (c, v) -> c.horizontalAlign = v, c -> c.horizontalAlign).add()
            .append(new KeyedCodec<>("VerticalAlign",        Codec.STRING),
                (c, v) -> c.verticalAlign = v, c -> c.verticalAlign).add()
            .build();
}
