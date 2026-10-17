package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/**
 * Where the info panel is attached. Isolates the rest of the plugin from the
 * server's HUD slot API, which has already changed once (single slot -> keyed HUDs).
 */
public interface HudBackend {

    /** Human-readable name for log output. */
    @Nonnull
    String name();

    /** Attaches the panel; the backend is responsible for calling {@code build}. */
    void show(@Nonnull Player player, @Nonnull PlayerRef playerRef, @Nonnull InfoHud hud);

    /** Detaches the panel without affecting anything else on the player's screen. */
    void hide(@Nonnull Player player, @Nonnull PlayerRef playerRef, @Nonnull InfoHud hud);
}
