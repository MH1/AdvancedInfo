package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.hlousek.hytale.plugin.advancedinfo.AdvancedInfo;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.ProgressBarStyle;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SettingsPage extends InteractiveCustomUIPage<SettingsPage.EventData> {

    private static final String UI_PATH = "AdvancedInfo/Pages/SettingsPage.ui";

    /**
     * Curated list of common IANA timezone IDs shown in the dropdown.
     * "UTC" is always first and acts as the "no custom timezone" sentinel.
     */
    public static final List<String> COMMON_ZONES = List.of(
        "UTC",
        "Europe/London", "Europe/Paris", "Europe/Berlin", "Europe/Prague",
        "Europe/Warsaw", "Europe/Kyiv", "Europe/Moscow",
        "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles",
        "America/Sao_Paulo", "America/Argentina/Buenos_Aires",
        "America/Toronto", "America/Vancouver", "America/Mexico_City",
        "Asia/Tokyo", "Asia/Shanghai", "Asia/Seoul", "Asia/Singapore",
        "Asia/Kolkata", "Asia/Dubai", "Asia/Bangkok",
        "Australia/Sydney", "Pacific/Auckland", "Pacific/Honolulu",
        "Africa/Cairo", "Africa/Johannesburg"
    );

    private final ComponentType<EntityStore, PlayerHudSettings> settingsType;
    private final Runnable onSettingsChanged;

    public SettingsPage(
        @Nonnull PlayerRef playerRef,
        @Nonnull ComponentType<EntityStore, PlayerHudSettings> settingsType,
        @Nonnull Runnable onSettingsChanged
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, EventData.CODEC);
        this.settingsType     = settingsType;
        this.onSettingsChanged = onSettingsChanged;
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder ui,
        @Nonnull UIEventBuilder events,
        @Nonnull Store<EntityStore> store
    ) {
        ui.append(UI_PATH);

        PlayerHudSettings s = getOrCreateSettings(ref, store);
        applyState(ui, s);
        bindEvents(events);
    }

    private static void applyState(@Nonnull UICommandBuilder ui, @Nonnull PlayerHudSettings s) {
        // Checkboxes - use .Value, not .Checked
        ui.set("#ChkTime.Value",           s.isTimeEnabled());
        ui.set("#ChkBiome.Value",          s.isBiomeEnabled());
        ui.set("#ChkPosition.Value",       s.isPositionEnabled());
        ui.set("#ChkWorldTime.Value",      s.isWorldTimeEnabled());
        ui.set("#ChkZone.Value",           s.isZoneEnabled());
        ui.set("#ChkTarget.Value",         s.isTargetEnabled());
        ui.set("#ChkHealthBar.Value",      s.isHealthBarEnabled());
        ui.set("#ChkCaptions.Value",       s.isCaptionsEnabled());
        ui.set("#ChkTimezoneOffset.Value", s.isTimezoneOffsetVisible());

        // Alignment dropdowns
        ui.set("#DdHAlign.Entries", alignEntries("Left", "Center", "Right"));
        ui.set("#DdHAlign.Value",   capitalize(s.getHorizontalAlign().name()));
        ui.set("#DdVAlign.Entries", alignEntries("Top", "Bottom"));
        ui.set("#DdVAlign.Value",   capitalize(s.getVerticalAlign().name()));

        // Time format dropdown
        ui.set("#DdTimeFormat.Entries", alignEntries("24h", "12h"));
        ui.set("#DdTimeFormat.Value",   s.isTime24h() ? "24h" : "12h");

        // Timezone dropdown
        List<DropdownEntryInfo> tzEntries = new ArrayList<>();
        for (String zone : COMMON_ZONES) {
            tzEntries.add(new DropdownEntryInfo(LocalizableString.fromString(zone), zone));
        }
        String tzId = s.getTimezone().getId();
        ui.set("#DdTimezone.Entries", tzEntries);
        ui.set("#DdTimezone.Value",   COMMON_ZONES.contains(tzId) ? tzId : COMMON_ZONES.get(0));

        // Custom timezone text field
        ui.set("#TfTimezone.Value", "UTC".equals(tzId) ? "" : tzId);

        // Health bar style dropdown
        ui.set("#DdHealthBarStyle.Entries", alignEntries("Overlay", "Below"));
        ui.set("#DdHealthBarStyle.Value",   s.getHealthBarStyle() == ProgressBarStyle.OVERLAY ? "Overlay" : "Below");

        // Font size number field
        ui.set("#NfFontSize.Value", s.getFontSize());
    }

    private static List<DropdownEntryInfo> alignEntries(String... values) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        for (String v : values) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(v), v));
        }
        return entries;
    }

    // -------------------------------------------------------------------------
    // Event bindings
    // -------------------------------------------------------------------------

    private static void bindEvents(@Nonnull UIEventBuilder events) {
        bindBool  (events, "#ChkTime",           "toggle_time");
        bindBool  (events, "#ChkBiome",          "toggle_biome");
        bindBool  (events, "#ChkPosition",       "toggle_position");
        bindBool  (events, "#ChkWorldTime",      "toggle_worldtime");
        bindBool  (events, "#ChkZone",           "toggle_zone");
        bindBool  (events, "#ChkTarget",         "toggle_target");
        bindBool  (events, "#ChkHealthBar",      "toggle_healthbar");
        bindBool  (events, "#ChkCaptions",       "toggle_captions");
        bindBool  (events, "#ChkTimezoneOffset", "toggle_timezone_offset");
        bindString(events, "#DdHAlign",          "set_halign");
        bindString(events, "#DdVAlign",          "set_valign");
        bindString(events, "#DdTimeFormat",      "set_time_format");
        bindString(events, "#DdTimezone",        "set_timezone_dropdown");
        bindString(events, "#DdHealthBarStyle",  "set_healthbar_style");
        bindString(events, "#TfTimezone",        "set_timezone_text");
        bindNum   (events, "#NfFontSize",        "set_font_size");
        bindClick (events, "#BtnSave",           "save");
        bindClick (events, "#BtnReset",          "reset");
        bindClick (events, "#CloseBtn",          "close");
    }

    private static void bindBool(@Nonnull UIEventBuilder events, @Nonnull String sel, @Nonnull String action) {
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, sel,
            new com.hypixel.hytale.server.core.ui.builder.EventData()
                .put("Action", action)
                .put("@ValueBool", sel + ".Value"),
            false);
    }

    private static void bindString(@Nonnull UIEventBuilder events, @Nonnull String sel, @Nonnull String action) {
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, sel,
            new com.hypixel.hytale.server.core.ui.builder.EventData()
                .put("Action", action)
                .put("@Value", sel + ".Value"),
            false);
    }

    private static void bindNum(@Nonnull UIEventBuilder events, @Nonnull String sel, @Nonnull String action) {
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, sel,
            new com.hypixel.hytale.server.core.ui.builder.EventData()
                .put("Action", action)
                .put("@ValueNum", sel + ".Value"),
            false);
    }

    private static void bindClick(@Nonnull UIEventBuilder events, @Nonnull String sel, @Nonnull String action) {
        events.addEventBinding(CustomUIEventBindingType.Activating, sel,
            new com.hypixel.hytale.server.core.ui.builder.EventData()
                .put("Action", action),
            false);
    }

    // -------------------------------------------------------------------------
    // Event handling
    // -------------------------------------------------------------------------

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull EventData data
    ) {
        if (data.action == null) return;

        PlayerHudSettings settings = getOrCreateSettings(ref, store);
        String val = data.getEffectiveValue();
        boolean changed = true;

        switch (data.action) {
            case "toggle_time"            -> settings.setTimeEnabled(Boolean.parseBoolean(val));
            case "toggle_biome"           -> settings.setBiomeEnabled(Boolean.parseBoolean(val));
            case "toggle_position"        -> settings.setPositionEnabled(Boolean.parseBoolean(val));
            case "toggle_worldtime"       -> settings.setWorldTimeEnabled(Boolean.parseBoolean(val));
            case "toggle_zone"            -> settings.setZoneEnabled(Boolean.parseBoolean(val));
            case "toggle_target"          -> settings.setTargetEnabled(Boolean.parseBoolean(val));
            case "toggle_healthbar"       -> settings.setHealthBarEnabled(Boolean.parseBoolean(val));
            case "toggle_captions"        -> settings.setCaptionsEnabled(Boolean.parseBoolean(val));
            case "toggle_timezone_offset" -> settings.setTimezoneOffsetVisible(Boolean.parseBoolean(val));
            case "set_halign" -> {
                HorizontalAlign h = parseHAlign(val);
                if (h != null) settings.setHorizontalAlign(h);
            }
            case "set_valign" -> {
                VerticalAlign v = parseVAlign(val);
                if (v != null) settings.setVerticalAlign(v);
            }
            case "set_time_format"       -> settings.setTime24h("24h".equalsIgnoreCase(val));
            case "set_timezone_dropdown" -> {
                applyTimezone(settings, val);
                String display = "UTC".equals(val) ? "" : (val != null ? val : "");
                UICommandBuilder cmd = new UICommandBuilder();
                cmd.set("#TfTimezone.Value", display);
                sendUpdate(cmd);
            }
            case "set_timezone_text"     -> applyTimezone(settings, val);
            case "set_healthbar_style"   -> {
                ProgressBarStyle style = "Below".equalsIgnoreCase(val)
                    ? ProgressBarStyle.BELOW : ProgressBarStyle.OVERLAY;
                settings.setHealthBarStyle(style);
            }
            case "set_font_size" -> {
                try {
                    int size = (int) Double.parseDouble(val);
                    if (size >= 8 && size <= 32) settings.setFontSize(size);
                } catch (NumberFormatException ignored) {}
            }
            case "save" -> {
                onSettingsChanged.run();
                close();
                return;
            }
            case "reset" -> {
                settings.reset();
                onSettingsChanged.run();
                rebuild();
                return;
            }
            case "close" -> {
                close();
                return;
            }
            default -> changed = false;
        }

        if (changed) {
            onSettingsChanged.run();
        }
    }

    // -------------------------------------------------------------------------
    // Event data codec
    // -------------------------------------------------------------------------

    public static class EventData {
        public String  action;
        public String  value;
        public Double  valueNum;
        public Boolean valueBool;

        public EventData() {}

        @Nullable
        public String getEffectiveValue() {
            if (value     != null) return value;
            if (valueNum  != null) {
                if (valueNum % 1 == 0 && !Double.isInfinite(valueNum)) return String.valueOf(valueNum.longValue());
                return String.valueOf(valueNum);
            }
            if (valueBool != null) return String.valueOf(valueBool);
            return null;
        }

        @SuppressWarnings("deprecation")
        public static final BuilderCodec<EventData> CODEC = BuilderCodec
            .builder(EventData.class, EventData::new)
            .addField(new KeyedCodec<>("Action",     Codec.STRING),  (o, v) -> o.action    = v, o -> o.action)
            .addField(new KeyedCodec<>("@Value",     Codec.STRING),  (o, v) -> o.value     = v, o -> o.value)
            .addField(new KeyedCodec<>("@ValueNum",  Codec.DOUBLE),  (o, v) -> o.valueNum  = v, o -> o.valueNum)
            .addField(new KeyedCodec<>("@ValueBool", Codec.BOOLEAN), (o, v) -> o.valueBool = v, o -> o.valueBool)
            .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @Nonnull
    private PlayerHudSettings getOrCreateSettings(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        PlayerHudSettings s = store.getComponent(ref, settingsType);
        return s != null ? s : store.ensureAndGetComponent(ref, settingsType);
    }

    private static void applyTimezone(@Nonnull PlayerHudSettings settings, @Nullable String id) {
        if (id == null || id.isBlank()) return;
        try { settings.setTimezone(ZoneId.of(id.trim())); }
        catch (Exception e) { AdvancedInfo.LOGGER.atWarning().log("SettingsPage: unknown timezone \"%s\"", id); }
    }

    @Nullable
    private static HorizontalAlign parseHAlign(@Nullable String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "left"   -> HorizontalAlign.LEFT;
            case "center" -> HorizontalAlign.CENTER;
            case "right"  -> HorizontalAlign.RIGHT;
            default       -> null;
        };
    }

    @Nullable
    private static VerticalAlign parseVAlign(@Nullable String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "top"    -> VerticalAlign.TOP;
            case "bottom" -> VerticalAlign.BOTTOM;
            default       -> null;
        };
    }

    private static String capitalize(@Nonnull String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
