package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** World-generation zone (tier) at the player's position. */
public final class ZoneRowProvider implements InfoRowProvider {

    @Override
    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isZoneEnabled();
    }

    @Override
    @Nullable
    public String text(@Nonnull InfoContext ctx) {
        World world = ctx.world();
        if (world == null || !(world.getChunkStore().getGenerator() instanceof ChunkGenerator generator)) { return null; }
        Transform transform = ctx.playerRef().getTransform();
        if (transform == null) { return null; }

        Vector3d pos = transform.getPosition();
        int seed = (int) world.getWorldConfig().getSeed();
        ZoneBiomeResult result = generator.getZoneBiomeResultAt(seed, (int) Math.floor(pos.x), (int) Math.floor(pos.z));
        Zone zone = result.getZoneResult().getZone();

        // Only show zones that have Display: true in their Zone.json.
        // Shore/shallow ocean sub-zones have Display: false and no translation entry.
        if (!zone.discoveryConfig().display()) { return null; }

        String tierKey = "server.map.tier." + zone.name();
        String resolved = I18nModule.get().getMessage(ctx.language(), tierKey);
        return resolved != null ? resolved : zone.name();
    }
}
