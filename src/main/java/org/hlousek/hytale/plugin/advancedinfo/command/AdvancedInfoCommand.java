package org.hlousek.hytale.plugin.advancedinfo.command;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.ProgressBarStyle;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.util.function.Consumer;

public class AdvancedInfoCommand extends AbstractCommandCollection {

    public AdvancedInfoCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> settingsComponentType) {
        super("advanced-info", "Manage the Advanced Info HUD.");
        addAliases("ai");

        addSubCommand(new ShowSubCommands(settingsComponentType));
        addSubCommand(new HideSubCommands(settingsComponentType));
        addSubCommand(new ConfigTimeSubCommand(settingsComponentType));
        addSubCommand(new ConfigTimezoneSubCommand(settingsComponentType));
        addSubCommand(new AlignSubCommand(settingsComponentType));
        addSubCommand(new HAlignSubCommand(settingsComponentType));
        addSubCommand(new VAlignSubCommand(settingsComponentType));
        addSubCommand(new ResetSubCommand(settingsComponentType));
        addSubCommand(new StatusSubCommand(settingsComponentType));
        addSubCommand(new HealthBarSubCommand(settingsComponentType));
        addSubCommand(new FontSubCommand(settingsComponentType));
    }

    private static PlayerHudSettings getSettings(
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull ComponentType<EntityStore, PlayerHudSettings> type
    ) {
        PlayerHudSettings settings = store.getComponent(ref, type);
        if (settings == null) {
            settings = store.ensureAndGetComponent(ref, type);
        }
        return settings;
    }

    // -------------------------------------------------------------------------
    // Generic toggle subcommand base
    // -------------------------------------------------------------------------

    private static class ToggleSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final HudToggleEntry entry;
        private final Consumer<PlayerHudSettings> action;
        private final String feedbackMessage;

        public ToggleSubCommand(
            @Nonnull ComponentType<EntityStore, PlayerHudSettings> type,
            @Nonnull HudToggleEntry entry,
            @Nonnull Consumer<PlayerHudSettings> action,
            @Nonnull String feedbackMessage
        ) {
            super(entry.getName(), (feedbackMessage.contains("Show") ? "Show " : "Hide ") + entry.getDescription() + ".");
            this.type            = type;
            this.entry           = entry;
            this.action          = action;
            this.feedbackMessage = feedbackMessage;
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            PlayerHudSettings settings = getSettings(store, ref, type);
            @Nullable String guardMessage = entry.checkGuard(settings);
            if (guardMessage != null) {
                playerRef.sendMessage(Message.raw(guardMessage));
                return;
            }
            action.accept(settings);
            playerRef.sendMessage(Message.raw(feedbackMessage));
        }
    }

    // -------------------------------------------------------------------------
    // /ai show ...
    // -------------------------------------------------------------------------

    private static class ShowSubCommands extends AbstractCommandCollection {

        public ShowSubCommands(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("show", "Show a HUD element.");
            HudToggleRegistry.getEntries().forEach((name, entry) ->
                addSubCommand(new ToggleSubCommand(
                    type, entry, entry::enable,
                    "Showing " + entry.getDescription() + "."
                ))
            );
            addSubCommand(new ShowAllSubCommand(type));
        }
    }

    // -------------------------------------------------------------------------
    // /ai hide ...
    // -------------------------------------------------------------------------

    private static class HideSubCommands extends AbstractCommandCollection {

        public HideSubCommands(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("hide", "Hide a HUD element.");
            HudToggleRegistry.getEntries().forEach((name, entry) ->
                addSubCommand(new ToggleSubCommand(
                    type, entry, entry::disable,
                    "Hiding " + entry.getDescription() + "."
                ))
            );
            addSubCommand(new HideAllSubCommand(type));
        }
    }

    private static class ShowAllSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;

        public ShowAllSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("all", "Show all HUD elements.");
            this.type = type;
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            PlayerHudSettings settings = getSettings(store, ref, type);
            HudToggleRegistry.getEntries().values().stream()
                .filter(e -> !e.isExcludedFromAll())
                .forEach(e -> e.enable(settings));
            playerRef.sendMessage(Message.raw("Showing all HUD elements."));
        }
    }

    private static class HideAllSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;

        public HideAllSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("all", "Hide all HUD elements.");
            this.type = type;
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            PlayerHudSettings settings = getSettings(store, ref, type);
            HudToggleRegistry.getEntries().values().stream()
                .filter(e -> !e.isExcludedFromAll())
                .forEach(e -> e.disable(settings));
            playerRef.sendMessage(Message.raw("Hiding all HUD elements."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai time <12|24>
    // -------------------------------------------------------------------------

    private static class ConfigTimeSubCommand extends AbstractPlayerCommand {
        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> formatArg;

        public ConfigTimeSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("time", "Set the time display format.");
            this.type = type;
            this.formatArg = withRequiredArg(
                "format", "Time format",
                new SingleArgumentType<String>("Time Format", "Use \"12\" for 12-hour or \"24\" for 24-hour format", "\"12\"", "\"24\"") {
                    @Override public String parse(String input, ParseResult parseResult) { return input; }
                }
            );
        }

        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String format = this.formatArg.get(ctx);
            if (!format.equals("12") && !format.equals("24")) {
                playerRef.sendMessage(Message.raw("Invalid format. Use \"12\" or \"24\"."));
                return;
            }
            getSettings(store, ref, type).setTime24h(format.equals("24"));
            playerRef.sendMessage(Message.raw("Time format set to " + format + "-hour."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai timezone <zone>
    // -------------------------------------------------------------------------

    private static class ConfigTimezoneSubCommand extends AbstractPlayerCommand {
        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> zoneArg;

        public ConfigTimezoneSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("timezone", "Set your timezone for the time display (e.g. Europe/Prague, America/New_York, UTC).");
            this.type = type;
            this.zoneArg = withRequiredArg(
                "zone", "Timezone ID",
                new SingleArgumentType<String>("Timezone ID", "A IANA timezone ID, e.g. Europe/Prague, America/New_York, UTC+2", "UTC", "Europe/Prague", "America/New_York") {
                    @Override public String parse(String input, ParseResult parseResult) { return input; }
                }
            );
        }

        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String input = this.zoneArg.get(ctx);
            ZoneId zoneId;
            try {
                zoneId = ZoneId.of(input);
            } catch (Exception e) {
                playerRef.sendMessage(Message.raw("Unknown timezone \"" + input + "\". Use an IANA zone ID, e.g. Europe/Prague, America/New_York, UTC."));
                return;
            }
            getSettings(store, ref, type).setTimezone(zoneId);
            playerRef.sendMessage(Message.raw("Timezone set to " + zoneId.getId() + "."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai align <horizontal> <vertical>
    // -------------------------------------------------------------------------

    private static class AlignSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> hArg;
        private final RequiredArg<String> vArg;

        public AlignSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("align", "Set both HUD alignments at once: /ai align <left|center|right> <top|bottom>.");
            this.type = type;
            this.hArg = withRequiredArg(
                "horizontal", "Horizontal alignment",
                new SingleArgumentType<String>("Horizontal", "left|center|right", "left", "center", "right") {
                    @Override public String parse(String input, ParseResult parseResult) { return input.toLowerCase(); }
                }
            );
            this.vArg = withRequiredArg(
                "vertical", "Vertical alignment",
                new SingleArgumentType<String>("Vertical", "top|bottom", "top", "bottom") {
                    @Override public String parse(String input, ParseResult parseResult) { return input.toLowerCase(); }
                }
            );
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            HorizontalAlign h = parseHorizontal(hArg.get(ctx));
            VerticalAlign   v = parseVertical(vArg.get(ctx));
            if (h == null) { playerRef.sendMessage(Message.raw("Invalid horizontal alignment. Use: left, center, right.")); return; }
            if (v == null) { playerRef.sendMessage(Message.raw("Invalid vertical alignment. Use: top, bottom.")); return; }
            PlayerHudSettings settings = getSettings(store, ref, type);
            settings.setHorizontalAlign(h);
            settings.setVerticalAlign(v);
            playerRef.sendMessage(Message.raw("Alignment set to " + h.name().toLowerCase() + " " + v.name().toLowerCase() + "."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai halign <horizontal>
    // -------------------------------------------------------------------------

    private static class HAlignSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> hArg;

        public HAlignSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("halign", "Set the horizontal HUD alignment: left, center, or right.");
            this.type = type;
            this.hArg = withRequiredArg(
                "horizontal", "Horizontal alignment",
                new SingleArgumentType<String>("Horizontal", "left|center|right", "left", "center", "right") {
                    @Override public String parse(String input, ParseResult parseResult) { return input.toLowerCase(); }
                }
            );
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            HorizontalAlign h = parseHorizontal(hArg.get(ctx));
            if (h == null) { playerRef.sendMessage(Message.raw("Invalid horizontal alignment. Use: left, center, right.")); return; }
            getSettings(store, ref, type).setHorizontalAlign(h);
            playerRef.sendMessage(Message.raw("Horizontal alignment set to " + h.name().toLowerCase() + "."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai valign <vertical>
    // -------------------------------------------------------------------------

    private static class VAlignSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> vArg;

        public VAlignSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("valign", "Set the vertical HUD alignment: top or bottom.");
            this.type = type;
            this.vArg = withRequiredArg(
                "vertical", "Vertical alignment",
                new SingleArgumentType<String>("Vertical", "top|bottom", "top", "bottom") {
                    @Override public String parse(String input, ParseResult parseResult) { return input.toLowerCase(); }
                }
            );
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            VerticalAlign v = parseVertical(vArg.get(ctx));
            if (v == null) { playerRef.sendMessage(Message.raw("Invalid vertical alignment. Use: top, bottom.")); return; }
            getSettings(store, ref, type).setVerticalAlign(v);
            playerRef.sendMessage(Message.raw("Vertical alignment set to " + v.name().toLowerCase() + "."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai reset
    // -------------------------------------------------------------------------

    private static class ResetSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;

        public ResetSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("reset", "Reset all HUD settings to their default values.");
            this.type = type;
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            getSettings(store, ref, type).reset();
            playerRef.sendMessage(Message.raw("HUD settings reset to defaults."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai status
    // -------------------------------------------------------------------------

    private static class StatusSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;

        public StatusSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("status", "Show the visibility status of all HUD components.");
            this.type = type;
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            PlayerHudSettings settings = getSettings(store, ref, type);
            StringBuilder sb = new StringBuilder("HUD component status:");
            for (HudToggleEntry entry : HudToggleRegistry.getEntries().values()) {
                sb.append("\n  ").append(entry.getName())
                  .append(": ").append(entry.isVisible(settings) ? "visible" : "hidden");
            }
            sb.append("\n  alignment: ")
              .append(settings.getHorizontalAlign().name().toLowerCase())
              .append(" ").append(settings.getVerticalAlign().name().toLowerCase());
            sb.append("\n  time format: ").append(settings.isTime24h() ? "24h" : "12h");
            sb.append("\n  timezone: ").append(settings.getTimezone().getId());
            sb.append("\n  healthbar: ").append(settings.getHealthBarStyle().name().toLowerCase());
            sb.append("\n  font size: ").append(settings.getFontSize());
            playerRef.sendMessage(Message.raw(sb.toString()));
        }
    }

    // -------------------------------------------------------------------------
    // /ai healthbar <below|overlay>
    // -------------------------------------------------------------------------

    private static class HealthBarSubCommand extends AbstractPlayerCommand {

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> styleArg;

        public HealthBarSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("healthbar", "Set the health bar style: \"below\" places it under the name, \"overlay\" renders the name on top.");
            this.type = type;
            this.styleArg = withRequiredArg(
                "style", "Health bar style",
                new SingleArgumentType<String>("Style", "below|overlay", "below", "overlay") {
                    @Override public String parse(String input, ParseResult parseResult) { return input.toLowerCase(); }
                }
            );
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            String input = styleArg.get(ctx);
            ProgressBarStyle style = switch (input) {
                case "below"   -> ProgressBarStyle.BELOW;
                case "overlay" -> ProgressBarStyle.OVERLAY;
                default -> null;
            };
            if (style == null) {
                playerRef.sendMessage(Message.raw("Unknown style \"" + input + "\". Use: below, overlay."));
                return;
            }
            getSettings(store, ref, type).setHealthBarStyle(style);
            playerRef.sendMessage(Message.raw("Health bar style set to " + input + "."));
        }
    }

    // -------------------------------------------------------------------------
    // /ai font <size>
    // -------------------------------------------------------------------------

    private static class FontSubCommand extends AbstractPlayerCommand {

        private static final int MIN_SIZE = 8;
        private static final int MAX_SIZE = 32;

        private final ComponentType<EntityStore, PlayerHudSettings> type;
        private final RequiredArg<String> sizeArg;

        public FontSubCommand(@Nonnull ComponentType<EntityStore, PlayerHudSettings> type) {
            super("font", "Set the font size of the HUD value labels (range: " + MIN_SIZE + "-" + MAX_SIZE + ").");
            this.type = type;
            this.sizeArg = withRequiredArg(
                "size", "Font size",
                new SingleArgumentType<String>("Font Size", "Integer between " + MIN_SIZE + " and " + MAX_SIZE, "18") {
                    @Override public String parse(String input, ParseResult parseResult) { return input; }
                }
            );
        }

        @Override
        protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
        ) {
            int size;
            try {
                size = Integer.parseInt(sizeArg.get(ctx));
            } catch (NumberFormatException e) {
                playerRef.sendMessage(Message.raw("Invalid font size. Use a number between " + MIN_SIZE + " and " + MAX_SIZE + "."));
                return;
            }
            if (size < MIN_SIZE || size > MAX_SIZE) {
                playerRef.sendMessage(Message.raw("Font size must be between " + MIN_SIZE + " and " + MAX_SIZE + "."));
                return;
            }
            getSettings(store, ref, type).setFontSize(size);
            playerRef.sendMessage(Message.raw("Font size set to " + size + "."));
        }
    }

    @Nullable
    private static HorizontalAlign parseHorizontal(@Nonnull String s) {
        return switch (s) {
            case "left"   -> HorizontalAlign.LEFT;
            case "center" -> HorizontalAlign.CENTER;
            case "right"  -> HorizontalAlign.RIGHT;
            default       -> null;
        };
    }

    @Nullable
    private static VerticalAlign parseVertical(@Nonnull String s) {
        return switch (s) {
            case "top"    -> VerticalAlign.TOP;
            case "bottom" -> VerticalAlign.BOTTOM;
            default       -> null;
        };
    }
}
