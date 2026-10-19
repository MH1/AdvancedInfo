package org.hlousek.hytale.plugin.advancedinfo;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.hlousek.hytale.plugin.advancedinfo.provider.BiomeRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.InfoContext;
import org.hlousek.hytale.plugin.advancedinfo.provider.InfoRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.PositionRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.SimpleClaimsStorageAccess;
import org.hlousek.hytale.plugin.advancedinfo.provider.TargetInfo;
import org.hlousek.hytale.plugin.advancedinfo.provider.TargetProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.TimeRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.WorldTimeRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.provider.ZoneRowProvider;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.hlousek.hytale.plugin.advancedinfo.ui.HudContent;
import org.hlousek.hytale.plugin.advancedinfo.ui.HudStyle;
import org.hlousek.hytale.plugin.advancedinfo.ui.InfoManager;
import org.hlousek.hytale.plugin.advancedinfo.ui.StoragePanel;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Collects the HUD rows for every player and pushes them to {@link InfoManager}.
 *
 * <p>Runs as an entity ticking system but only recomputes a player's panel every
 * {@link InfoManager#UPDATE_INTERVAL_SECONDS}; the per-tick cost is a single map lookup.
 */
public class InfoUpdateSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull private final InfoManager infoManager;
    @Nonnull private final ComponentType<EntityStore, PlayerHudSettings> settingsComponentType;
    @Nonnull private final Query<EntityStore> query;

    private final InfoRowProvider timeRow      = new TimeRowProvider();
    private final InfoRowProvider biomeRow     = new BiomeRowProvider();
    private final InfoRowProvider positionRow  = new PositionRowProvider();
    private final InfoRowProvider worldTimeRow = new WorldTimeRowProvider();
    private final InfoRowProvider zoneRow      = new ZoneRowProvider();
    private final TargetProvider  target       = new TargetProvider(new SimpleClaimsStorageAccess());
    private final List<InfoRowProvider> rows   = List.of(timeRow, biomeRow, positionRow, worldTimeRow, zoneRow);

    public InfoUpdateSystem(
        @Nonnull InfoManager infoManager,
        @Nonnull ComponentType<EntityStore, PlayerHudSettings> settingsComponentType
    ) {
        this.infoManager = infoManager;
        this.settingsComponentType = settingsComponentType;
        this.query = Query.and(Player.getComponentType(), PlayerRef.getComponentType(), HeadRotation.getComponentType());
    }

    @Override
    public void tick(
        float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
        @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) { return; }

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) { return; }

        if (!this.infoManager.shouldUpdate(playerRef.getUuid(), dt)) { return; }

        PlayerHudSettings settings = archetypeChunk.getComponent(index, settingsComponentType);
        if (settings == null) { return; }

        boolean anyRowEnabled  = target.isEnabled(settings) || rows.stream().anyMatch(r -> r.isEnabled(settings));
        boolean storageEnabled = target.isStorageEnabled(settings, playerRef.hasPermission(AdvancedInfoPermissions.STORAGE));
        if ((!anyRowEnabled && !storageEnabled) || this.infoManager.isHudSuppressed(playerRef.getUuid())) {
            this.infoManager.hideHud(player, playerRef);
            return;
        }

        boolean healthVisible = playerRef.hasPermission(AdvancedInfoPermissions.HEALTH);
        InfoContext ctx = new InfoContext(
            player, playerRef, archetypeChunk.getReferenceTo(index), settings, store, commandBuffer, healthVisible);

        TargetInfo resolved   = target.isEnabled(settings) || storageEnabled ? target.resolve(ctx) : TargetInfo.NONE;
        TargetInfo targetInfo = target.isEnabled(settings) ? resolved : TargetInfo.NONE;
        StoragePanel storage  = storageEnabled && resolved.storage() != null
            ? new StoragePanel(resolved.storage(), settings.getStorageMaxRows(),
                settings.getStorageHorizontalAlign(), settings.getStorageVerticalAlign())
            : null;

        if (!anyRowEnabled && storage == null) {
            this.infoManager.hideHud(player, playerRef);
            return;
        }

        this.infoManager.updateHud(player, playerRef, new HudContent(
            anyRowEnabled,
            timeRow.textIfEnabled(ctx),
            biomeRow.textIfEnabled(ctx),
            zoneRow.textIfEnabled(ctx),
            positionRow.textIfEnabled(ctx),
            worldTimeRow.textIfEnabled(ctx),
            targetInfo.text(),
            targetInfo.sourceText(),
            targetInfo.healthPct(),
            settings.isTargetEnabled(),
            settings.isHealthBarEnabled() && healthVisible,
            settings.isCaptionsEnabled(),
            settings.getHealthBarStyle(),
            settings.getHorizontalAlign(),
            settings.getVerticalAlign(),
            HudStyle.of(settings),
            storage
        ));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }
}
