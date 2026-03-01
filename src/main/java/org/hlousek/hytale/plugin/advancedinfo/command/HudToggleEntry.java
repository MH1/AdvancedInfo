package org.hlousek.hytale.plugin.advancedinfo.command;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HudToggleEntry {

    @Nonnull  private final String name;
    @Nonnull  private final String description;
    @Nonnull  private final Consumer<PlayerHudSettings> enableAction;
    @Nonnull  private final Consumer<PlayerHudSettings> disableAction;
    @Nonnull  private final Predicate<PlayerHudSettings> visibilityGetter;
    @Nullable private final Predicate<PlayerHudSettings> guard;
    @Nullable private final String guardMessage;
    private final boolean excludeFromAll;

    public HudToggleEntry(
        @Nonnull String name,
        @Nonnull String description,
        @Nonnull Consumer<PlayerHudSettings> enableAction,
        @Nonnull Consumer<PlayerHudSettings> disableAction,
        @Nonnull Predicate<PlayerHudSettings> visibilityGetter
    ) {
        this(name, description, enableAction, disableAction, visibilityGetter, null, null, false);
    }

    public HudToggleEntry(
        @Nonnull String name,
        @Nonnull String description,
        @Nonnull Consumer<PlayerHudSettings> enableAction,
        @Nonnull Consumer<PlayerHudSettings> disableAction,
        @Nonnull Predicate<PlayerHudSettings> visibilityGetter,
        @Nullable Predicate<PlayerHudSettings> guard,
        @Nullable String guardMessage
    ) {
        this(name, description, enableAction, disableAction, visibilityGetter, guard, guardMessage, false);
    }

    public HudToggleEntry(
        @Nonnull String name,
        @Nonnull String description,
        @Nonnull Consumer<PlayerHudSettings> enableAction,
        @Nonnull Consumer<PlayerHudSettings> disableAction,
        @Nonnull Predicate<PlayerHudSettings> visibilityGetter,
        @Nullable Predicate<PlayerHudSettings> guard,
        @Nullable String guardMessage,
        boolean excludeFromAll
    ) {
        this.name             = name;
        this.description      = description;
        this.enableAction     = enableAction;
        this.disableAction    = disableAction;
        this.visibilityGetter = visibilityGetter;
        this.guard            = guard;
        this.guardMessage     = guardMessage;
        this.excludeFromAll   = excludeFromAll;
    }

    @Nonnull  public String getName()        { return name; }
    @Nonnull  public String getDescription() { return description; }

    /** Returns null if the guard passes, or the guard message if it blocks. */
    @Nullable
    public String checkGuard(@Nonnull PlayerHudSettings settings) {
        if (guard != null && !guard.test(settings)) {
            return guardMessage;
        }
        return null;
    }

    public void enable(@Nonnull PlayerHudSettings settings)  { enableAction.accept(settings); }
    public void disable(@Nonnull PlayerHudSettings settings) { disableAction.accept(settings); }
    public boolean isExcludedFromAll() { return excludeFromAll; }
    public boolean isVisible(@Nonnull PlayerHudSettings settings) { return visibilityGetter.test(settings); }
}
