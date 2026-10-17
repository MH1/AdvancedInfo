package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;

/** Name of the biome the player is standing in. */
public final class BiomeRowProvider implements InfoRowProvider {

    @Override
    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isBiomeEnabled();
    }

    @Override
    @Nonnull
    public String text(@Nonnull InfoContext ctx) {
        WorldMapTracker tracker = ctx.player().getWorldMapTracker();
        String biomeName = tracker != null ? tracker.getCurrentBiomeName() : null;
        return biomeName != null ? biomeName : "Unknown";
    }
}
