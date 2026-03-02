package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfoManager {

    private static final String HUD_ID = "AdvancedInfo_Info";

    private final ConcurrentHashMap<UUID, InfoHud> huds = new ConcurrentHashMap<>();
    private final Set<UUID> mapVisible   = ConcurrentHashMap.newKeySet();
    private final Set<UUID> hudHidden    = ConcurrentHashMap.newKeySet();

    private boolean isMultipleHUDPresent = false;

    public void setMultipleHUDPresent(boolean multipleHUDPresent) {
        isMultipleHUDPresent = multipleHUDPresent;
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
     * Update the unified HUD panel. Pass null for a field to hide that row,
     * or call {@link #hideHud} when all sections are disabled.
     */
    public void updateHud(
        @Nonnull Player player,
        @Nonnull PlayerRef playerRef,
        @Nonnull HudContent content
    ) {
        UUID uuid = playerRef.getUuid();

        boolean isNew = !huds.containsKey(uuid);
        InfoHud hud = huds.get(uuid);

        // If font size changed on an existing HUD, tear it down so build() is called fresh.
        if (!isNew && hud != null && hud.getFontSize() != content.fontSize()) {
            huds.remove(uuid);
            if (isMultipleHUDPresent) {
                MultipleHUD.getInstance().hideCustomHud(player, HUD_ID);
            } else {
                hud.clear();
            }
            isNew = true;
            hud = null;
        }

        if (isNew) {
            hud = new InfoHud(playerRef);
            huds.put(uuid, hud);
            hud.applyContent(content);
            if (isMultipleHUDPresent) {
                MultipleHUD.getInstance().setCustomHud(player, playerRef, HUD_ID, hud);
            } else {
                player.getHudManager().setCustomHud(playerRef, hud);
            }
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
            && hud.getVerticalAlign()              == content.verticalAlign()) {
            return;
        }

        hud.applyContent(content);
        hud.update();
    }

    public void hideHud(@Nonnull Player player, @Nonnull PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        if (!huds.containsKey(uuid)) { return; }
        InfoHud hud = huds.remove(uuid);
        if (isMultipleHUDPresent) {
            MultipleHUD.getInstance().hideCustomHud(player, HUD_ID);
        } else {
            hud.clear();
        }
    }

    public void onPlayerLeave(@Nonnull PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        huds.remove(uuid);
        mapVisible.remove(uuid);
        hudHidden.remove(uuid);
    }
}
