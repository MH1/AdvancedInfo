package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/** Registers the panel as a keyed custom HUD in the player's HudManager (server 0.7+). */
public final class NativeHudBackend implements HudBackend {

    @Override
    @Nonnull
    public String name() {
        return "HudManager";
    }

    @Override
    public void show(@Nonnull Player player, @Nonnull PlayerRef playerRef, @Nonnull InfoHud hud) {
        player.getHudManager().addCustomHud(playerRef, hud);
    }

    @Override
    public void hide(@Nonnull Player player, @Nonnull PlayerRef playerRef, @Nonnull InfoHud hud) {
        player.getHudManager().removeCustomHud(playerRef, hud.getKey());
    }
}
