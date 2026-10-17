package org.hlousek.hytale.plugin.advancedinfo.provider;

import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Supplies the text of one simple HUD row.
 *
 * <p>A row is shown when {@link #isEnabled} is true and {@link #text} returns non-null.
 */
public interface InfoRowProvider {

    boolean isEnabled(@Nonnull PlayerHudSettings settings);

    @Nullable
    String text(@Nonnull InfoContext ctx);

    /** Convenience: null when disabled, otherwise the provider's text. */
    @Nullable
    default String textIfEnabled(@Nonnull InfoContext ctx) {
        return isEnabled(ctx.settings()) ? text(ctx) : null;
    }
}
