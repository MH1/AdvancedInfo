package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import org.hlousek.hytale.plugin.advancedinfo.HudTimeFormatter;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** In-game day and time of day; hidden while game time is paused. */
public final class WorldTimeRowProvider implements InfoRowProvider {

    @Override
    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isWorldTimeEnabled();
    }

    @Override
    @Nullable
    public String text(@Nonnull InfoContext ctx) {
        World world = ctx.world();
        if (world == null) { return null; }
        WorldTimeResource worldTime = (WorldTimeResource) ctx.store().getResource(WorldTimeResource.getResourceType());
        if (worldTime == null || world.getWorldConfig().isGameTimePaused()) { return null; }
        return HudTimeFormatter.worldTime(worldTime.getGameDateTime(), ctx.settings().isTime24h());
    }
}
