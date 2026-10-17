package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Player block coordinates. */
public final class PositionRowProvider implements InfoRowProvider {

    @Override
    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isPositionEnabled();
    }

    @Override
    @Nullable
    public String text(@Nonnull InfoContext ctx) {
        Transform transform = ctx.playerRef().getTransform();
        if (transform == null) { return null; }
        Vector3d pos = transform.getPosition();
        return String.format("%d %d %d", (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
    }
}
