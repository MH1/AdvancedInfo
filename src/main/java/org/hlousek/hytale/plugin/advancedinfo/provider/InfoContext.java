package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Everything a provider may need to compute its value for one player in one update.
 *
 * @param healthVisible whether the player may see target health ({@code AdvancedInfoPermissions.HEALTH})
 */
public record InfoContext(
    @Nonnull Player player,
    @Nonnull PlayerRef playerRef,
    @Nonnull Ref<EntityStore> entityRef,
    @Nonnull PlayerHudSettings settings,
    @Nonnull Store<EntityStore> store,
    @Nonnull CommandBuffer<EntityStore> commandBuffer,
    boolean healthVisible
) {
    public static final String DEFAULT_LANGUAGE = "en-US";

    @Nullable
    public World world() {
        return player.getWorld();
    }

    /** Language of the player's client, used for translation lookups. */
    @Nonnull
    public String language() {
        String language = playerRef.getLanguage();
        return language == null || language.isBlank() ? DEFAULT_LANGUAGE : language;
    }
}
