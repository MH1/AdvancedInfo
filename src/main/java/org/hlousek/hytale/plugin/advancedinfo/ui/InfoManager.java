package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfoManager {

    /** Minimum time between two panel refreshes for one player. */
    public static final float UPDATE_INTERVAL_SECONDS = 0.2f;

    private final ConcurrentHashMap<UUID, InfoHud> huds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, float[]> updateAccumulators = new ConcurrentHashMap<>();
    private final Set<UUID> mapVisible   = ConcurrentHashMap.newKeySet();
    private final Set<UUID> hudHidden    = ConcurrentHashMap.newKeySet();

    @Nonnull private volatile HudBackend backend = new NativeHudBackend();

    public void setBackend(@Nonnull HudBackend backend) {
        this.backend = backend;
    }

    @Nonnull
    public HudBackend getBackend() {
        return backend;
    }

    public void onMapVisibilityChanged(@Nonnull UUID uuid, boolean visible) {
        if (visible) { mapVisible.add(uuid); } else { mapVisible.remove(uuid); }
    }

    public void onHudVisibilityChanged(@Nonnull UUID uuid, boolean visible) {
        if (visible) { hudHidden.remove(uuid); } else { hudHidden.add(uuid); }
    }

    public boolean isHudSuppressed(@Nonnull UUID uuid) {
        return mapVisible.contains(uuid) || hudHidden.contains(uuid);
    }

    /**
     * Accumulates {@code dt} (seconds) for the player and returns true once
     * {@link #UPDATE_INTERVAL_SECONDS} has elapsed since the last accepted update.
     */
    public boolean shouldUpdate(@Nonnull UUID uuid, float dt) {
        float[] acc = updateAccumulators.computeIfAbsent(uuid, k -> new float[] { UPDATE_INTERVAL_SECONDS });
        acc[0] += dt;
        if (acc[0] < UPDATE_INTERVAL_SECONDS) { return false; }
        acc[0] = 0f;
        return true;
    }

    /**
     * Update the unified HUD panel. Pass null for a field to hide that row,
     * or call {@link #hideHud} when all sections are disabled.
     */
    public void updateHud(
        @Nonnull Player player,
        @Nonnull PlayerRef playerRef,
        @Nonnull HudContent content
    ) {
        UUID uuid = playerRef.getUuid();

        InfoHud hud = huds.get(uuid);

        if (hud == null) {
            hud = new InfoHud(playerRef);
            huds.put(uuid, hud);
            hud.applyContent(content);
            backend.show(player, playerRef, hud);
            return;
        }

        if (Objects.equals(hud.getTimeText(),        content.timeText())
            && Objects.equals(hud.getBiomeText(),       content.biomeText())
            && Objects.equals(hud.getZoneText(),        content.zoneText())
            && Objects.equals(hud.getPositionText(),    content.positionText())
            && Objects.equals(hud.getWorldTimeText(),   content.worldTimeText())
            && Objects.equals(hud.getTargetBar().getFirstLine(),  content.targetText())
            && Objects.equals(hud.getTargetBar().getSecondLine(), content.targetSourceText())
            && hud.getTargetBar().getProgress()   == content.targetHealthPct()
            && hud.isTargetEnabled()               == content.targetEnabled()
            && hud.isCaptionsEnabled()             == content.captionsEnabled()
            && hud.getTargetBar().isBarVisible()   == content.healthBarEnabled()
            && hud.getTargetBar().getMode()        == content.healthBarStyle()
            && hud.getHorizontalAlign()            == content.horizontalAlign()
            && hud.getVerticalAlign()              == content.verticalAlign()
            && hud.getStyle().equals(content.style())) {
            return;
        }

        hud.applyContent(content);
        hud.update();
    }

    public void hideHud(@Nonnull Player player, @Nonnull PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        if (!huds.containsKey(uuid)) { return; }
        InfoHud hud = huds.remove(uuid);
        backend.hide(player, playerRef, hud);
    }

    public void onPlayerLeave(@Nonnull PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        huds.remove(uuid);
        updateAccumulators.remove(uuid);
        mapVisible.remove(uuid);
        hudHidden.remove(uuid);
    }
}
