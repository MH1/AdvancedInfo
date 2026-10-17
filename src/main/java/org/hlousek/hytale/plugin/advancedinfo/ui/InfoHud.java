package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfoHud extends CustomUIHud {

    /** Key under which the panel is registered in the player's HudManager. */
    public static final String KEY = "AdvancedInfo:Info";

    private static final String UI_PATH      = "AdvancedInfo/HUD/InfoHud.ui";
    private static final String CONTENT_SLOT = "#AIContent";

    @Nullable private String timeText;
    @Nullable private String biomeText;
    @Nullable private String zoneText;
    @Nullable private String positionText;
    @Nullable private String worldTimeText;
    private float targetHealthPct = -1f;
    private boolean targetEnabled = false;
    private boolean captionsEnabled = true;
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
        isBuilt = true;
        _update(ui);
    }

    public void update() {
        if (!isBuilt) { return; }
        UICommandBuilder ui = new UICommandBuilder();
        if (!style.equals(renderedStyle)) {
            renderContent(ui);
        }
        _update(ui);
        update(false, ui);
    }

    @Override
    protected void onRemove() {
        isBuilt = false;
        renderedStyle = null;
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
            offset.setBottom(Value.of(130));
        }
        ui.setObject("#AIOffset.Anchor", offset);
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
    public float   getTargetHealthPct()        { return targetHealthPct; }
    @Nonnull public HudStyle getStyle()        { return style; }
    @Nonnull public HorizontalAlign getHorizontalAlign() { return horizontalAlign; }
    @Nonnull public VerticalAlign   getVerticalAlign()   { return verticalAlign; }

    @Nonnull public ProgressBarWidget getTargetBar() { return targetBar; }
}
