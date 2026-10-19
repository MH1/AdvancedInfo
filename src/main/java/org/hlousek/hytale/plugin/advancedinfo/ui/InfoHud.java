package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import org.hlousek.hytale.plugin.advancedinfo.provider.StorageItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class InfoHud extends CustomUIHud {

    /** Key under which the panel is registered in the player's HudManager. */
    public static final String KEY = "AdvancedInfo:Info";

    private static final String UI_PATH      = "AdvancedInfo/HUD/InfoHud.ui";
    private static final String CONTENT_SLOT = "#AIContent";
    private static final String STORAGE_STACKED_SLOT = "#AIStorageStacked";
    private static final String STORAGE_SOLO_SLOT    = "#AIStorageSolo";

    /** Distance of a bottom-aligned panel from the screen edge, keeps it clear of the hotbar. */
    private static final int BOTTOM_OFFSET = 130;

    /** What the storage markup was rendered for; a change requires a re-render. */
    private record StorageLayout(@Nonnull HudStyle style, int rows, boolean stacked) {}

    @Nullable private String timeText;
    @Nullable private String biomeText;
    @Nullable private String zoneText;
    @Nullable private String positionText;
    @Nullable private String worldTimeText;
    private float targetHealthPct = -1f;
    private boolean targetEnabled = false;
    private boolean captionsEnabled = true;
    private boolean infoVisible = true;
    @Nullable private StoragePanel storage;
    @Nullable private StorageLayout renderedStorage;
    @Nonnull private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    @Nonnull private VerticalAlign   verticalAlign   = VerticalAlign.TOP;

    private final ProgressBarWidget targetBar = new ProgressBarWidget();

    private boolean isBuilt = false;
    @Nonnull  private HudStyle style = HudStyle.DEFAULT;
    @Nullable private HudStyle renderedStyle;

    public InfoHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append(UI_PATH);
        renderContent(ui);
        renderStorage(ui);
        isBuilt = true;
        _update(ui);
    }

    public void update() {
        if (!isBuilt) { return; }
        UICommandBuilder ui = new UICommandBuilder();
        if (!style.equals(renderedStyle)) {
            renderContent(ui);
        }
        renderStorage(ui);
        _update(ui);
        update(false, ui);
    }

    @Override
    protected void onRemove() {
        isBuilt = false;
        renderedStyle = null;
        renderedStorage = null;
    }

    /**
     * (Re)creates the panel content and background for the current style inside the
     * static skeleton. Only the content slot is touched, so the HUD itself is never torn down.
     */
    private void renderContent(@Nonnull UICommandBuilder ui) {
        if (renderedStyle != null) {
            ui.clear(CONTENT_SLOT);
        }
        ui.appendInline(CONTENT_SLOT, InfoHudMarkup.render(style));

        PatchStyle background = new PatchStyle();
        background.setColor(Value.of(style.backgroundColor()));
        ui.setObject(CONTENT_SLOT + ".Background", background);

        renderedStyle = style;
    }

    /** Stacked below the info panel when both share the alignment, otherwise placed on its own. */
    private boolean isStorageStacked() {
        return storage != null && infoVisible
            && storage.horizontalAlign() == horizontalAlign && storage.verticalAlign() == verticalAlign;
    }

    /**
     * (Re)creates the storage cells when the style, the row limit or the placement changed.
     * While no storage is targeted the rendered cells are kept and only hidden.
     */
    private void renderStorage(@Nonnull UICommandBuilder ui) {
        if (storage == null) { return; }
        StorageLayout wanted = new StorageLayout(style, storage.maxRows(), isStorageStacked());
        if (wanted.equals(renderedStorage)) { return; }

        if (renderedStorage != null) {
            ui.clear(renderedStorage.stacked() ? STORAGE_STACKED_SLOT : STORAGE_SOLO_SLOT);
        }
        String slot = wanted.stacked() ? STORAGE_STACKED_SLOT : STORAGE_SOLO_SLOT;
        ui.appendInline(slot, StoragePanelMarkup.render(style, wanted.rows(), style.storageItemsPerRow()));

        PatchStyle background = new PatchStyle();
        background.setColor(Value.of(style.backgroundColor()));
        ui.setObject(slot + ".Background", background);

        renderedStorage = wanted;
    }

    private void _update(@Nonnull UICommandBuilder ui) {
        ui.set("#AITimeText.Text",      timeText      != null ? timeText      : "");
        ui.set("#AIBiomeText.Text",     biomeText     != null ? biomeText     : "");
        ui.set("#AIZoneText.Text",      zoneText      != null ? zoneText      : "");
        ui.set("#AIPositionText.Text",  positionText  != null ? positionText  : "");
        ui.set("#AIWorldTimeText.Text", worldTimeText != null ? worldTimeText : "");

        ui.set("#AITimeRow.Visible",      timeText      != null);
        ui.set("#AIPositionRow.Visible",  positionText  != null);
        ui.set("#AIBiomeRow.Visible",     biomeText     != null);
        ui.set("#AIZoneRow.Visible",      zoneText      != null);
        ui.set("#AIWorldTimeRow.Visible", worldTimeText != null);
        ui.set("#AITargetRow.Visible",    targetEnabled);

        ui.set("#AITimeCaption.Visible",      captionsEnabled);
        ui.set("#AIPositionCaption.Visible",  captionsEnabled);
        ui.set("#AIBiomeCaption.Visible",     captionsEnabled);
        ui.set("#AIZoneCaption.Visible",      captionsEnabled);
        ui.set("#AIWorldTimeCaption.Visible", captionsEnabled);
        ui.set("#AITargetCaption.Visible",    captionsEnabled);

        targetBar.apply(ui);

        ui.set("#AIHud.LayoutMode", verticalAlign == VerticalAlign.BOTTOM ? "Bottom" : "Top");
        ui.set("#AIInner.LayoutMode", switch (horizontalAlign) {
            case LEFT   -> "Left";
            case CENTER -> "Center";
            case RIGHT  -> "Right";
        });

        Anchor offset = new Anchor();
        if (verticalAlign == VerticalAlign.BOTTOM) {
            offset.setBottom(Value.of(BOTTOM_OFFSET));
        }
        ui.setObject("#AIOffset.Anchor", offset);
        ui.set("#AIContent.Visible", infoVisible);

        updateStorage(ui);
    }

    private void updateStorage(@Nonnull UICommandBuilder ui) {
        boolean stacked = isStorageStacked();
        ui.set(STORAGE_STACKED_SLOT + ".Visible", storage != null && stacked);
        ui.set(STORAGE_SOLO_SLOT + ".Visible",    storage != null && !stacked);
        if (storage == null || renderedStorage == null) { return; }

        if (stacked) {
            Anchor gap = new Anchor();
            gap.setTop(Value.of(style.storagePanelGap()));
            ui.setObject(STORAGE_STACKED_SLOT + ".Anchor", gap);
        } else {
            ui.set("#AISHud.LayoutMode", storage.verticalAlign() == VerticalAlign.BOTTOM ? "Bottom" : "Top");
            ui.set("#AISInner.LayoutMode", switch (storage.horizontalAlign()) {
                case LEFT   -> "Left";
                case CENTER -> "Center";
                case RIGHT  -> "Right";
            });
            Anchor offset = new Anchor();
            if (storage.verticalAlign() == VerticalAlign.BOTTOM) {
                offset.setBottom(Value.of(BOTTOM_OFFSET));
            }
            ui.setObject("#AISOffset.Anchor", offset);
            Anchor width = new Anchor();
            width.setWidth(Value.of(style.panelWidth()));
            ui.setObject(STORAGE_SOLO_SLOT + ".Anchor", width);
        }

        List<StorageItem> items = storage.items();
        int perRow   = style.storageItemsPerRow();
        int capacity = renderedStorage.rows() * perRow;
        boolean overflow = items.size() > capacity;
        int shown = overflow ? capacity - 1 : items.size();
        int usedCells = overflow ? capacity : shown;

        ui.set(StoragePanelMarkup.EMPTY_ID + ".Visible", items.isEmpty());
        for (int r = 0; r < renderedStorage.rows(); r++) {
            ui.set(StoragePanelMarkup.rowId(r) + ".Visible", usedCells > r * perRow);
        }
        for (int i = 0; i < capacity; i++) {
            ui.set(StoragePanelMarkup.cellId(i) + ".Visible", i < usedCells);
            if (i < shown) {
                StorageItem item = items.get(i);
                ui.set(StoragePanelMarkup.iconId(i) + ".ItemId", item.itemId());
                ui.set(StoragePanelMarkup.countId(i) + ".Text", StoragePanelMarkup.formatCount(item.count()));
            }
        }
        int last = capacity - 1;
        ui.set(StoragePanelMarkup.iconId(last) + ".Visible",  !overflow);
        ui.set(StoragePanelMarkup.countId(last) + ".Visible", !overflow);
        ui.set(StoragePanelMarkup.MORE_ID + ".Visible", overflow);
        ui.set(StoragePanelMarkup.MORE_ID + ".Text", overflow ? "+" + (items.size() - shown) : "");
    }

    public void applyContent(@Nonnull HudContent content) {
        this.timeText         = content.timeText();
        this.biomeText        = content.biomeText();
        this.zoneText         = content.zoneText();
        this.positionText     = content.positionText();
        this.worldTimeText    = content.worldTimeText();
        this.targetHealthPct  = content.targetHealthPct();
        this.targetEnabled    = content.targetEnabled();
        this.captionsEnabled  = content.captionsEnabled();
        this.horizontalAlign  = content.horizontalAlign();
        this.verticalAlign    = content.verticalAlign();
        this.style            = content.style();
        this.infoVisible      = content.infoVisible();
        this.storage          = content.storage();

        targetBar.setFirstLine(content.targetText());
        targetBar.setSecondLine(content.targetSourceText());
        targetBar.setMode(content.healthBarStyle());
        targetBar.setBarVisible(content.healthBarEnabled());
        targetBar.setProgress(content.targetHealthPct());
        targetBar.setWidth(content.style().targetWidth());
    }

    public void setTimeText(@Nullable String timeText)           { this.timeText = timeText; }
    public void setBiomeText(@Nullable String biomeText)         { this.biomeText = biomeText; }
    public void setZoneText(@Nullable String zoneText)           { this.zoneText = zoneText; }
    public void setPositionText(@Nullable String positionText)   { this.positionText = positionText; }
    public void setWorldTimeText(@Nullable String worldTimeText) { this.worldTimeText = worldTimeText; }
    public void setHorizontalAlign(@Nonnull HorizontalAlign v)   { this.horizontalAlign = v; }
    public void setVerticalAlign(@Nonnull VerticalAlign v)       { this.verticalAlign = v; }

    @Nullable public String getTimeText()      { return timeText; }
    @Nullable public String getBiomeText()     { return biomeText; }
    @Nullable public String getZoneText()      { return zoneText; }
    @Nullable public String getPositionText()  { return positionText; }
    @Nullable public String getWorldTimeText() { return worldTimeText; }
    public boolean isTargetEnabled()           { return targetEnabled; }
    public boolean isCaptionsEnabled()         { return captionsEnabled; }
    public boolean isInfoVisible()             { return infoVisible; }
    @Nullable public StoragePanel getStorage() { return storage; }
    public float   getTargetHealthPct()        { return targetHealthPct; }
    @Nonnull public HudStyle getStyle()        { return style; }
    @Nonnull public HorizontalAlign getHorizontalAlign() { return horizontalAlign; }
    @Nonnull public VerticalAlign   getVerticalAlign()   { return verticalAlign; }

    @Nonnull public ProgressBarWidget getTargetBar() { return targetBar; }
}
