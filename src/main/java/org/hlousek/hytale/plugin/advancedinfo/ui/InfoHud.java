package org.hlousek.hytale.plugin.advancedinfo.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfoHud extends CustomUIHud {

    private static final String UI_PATH_TEMPLATE = "AdvancedInfo/HUD/generated/InfoHud_%d.ui";

    @Nullable private String timeText;
    @Nullable private String biomeText;
    @Nullable private String zoneText;
    @Nullable private String positionText;
    @Nullable private String worldTimeText;
    private float targetHealthPct = -1f;
    private boolean targetEnabled = false;
    @Nonnull private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    @Nonnull private VerticalAlign   verticalAlign   = VerticalAlign.TOP;

    private final ProgressBarWidget targetBar = new ProgressBarWidget();

    private boolean isBuilt = false;
    private int fontSize = 18;

    public InfoHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append(String.format(UI_PATH_TEMPLATE, fontSize));
        isBuilt = true;
        _update(ui);
    }

    public void update() {
        if (!isBuilt) { return; }
        UICommandBuilder ui = new UICommandBuilder();
        _update(ui);
        update(false, ui);
    }

    public void clear() {
        this.update(true, new UICommandBuilder());
        isBuilt = false;
    }

    private void _update(@Nonnull UICommandBuilder ui) {
        ui.set("#AIHTimeText.Text",      timeText      != null ? timeText      : "");
        ui.set("#AIHBiomeText.Text",     biomeText     != null ? biomeText     : "");
        ui.set("#AIHZoneText.Text",      zoneText      != null ? zoneText      : "");
        ui.set("#AIHPositionText.Text",  positionText  != null ? positionText  : "");
        ui.set("#AIHWorldTimeText.Text", worldTimeText != null ? worldTimeText : "");

        ui.set("#AIHTimeRow.Visible",      timeText      != null);
        ui.set("#AIHPositionRow.Visible",  positionText  != null);
        ui.set("#AIHBiomeRow.Visible",     biomeText     != null);
        ui.set("#AIHZoneRow.Visible",      zoneText      != null);
        ui.set("#AIHWorldTimeRow.Visible", worldTimeText != null);
        ui.set("#AIHTargetRow.Visible",    targetEnabled);

        targetBar.apply(ui);

        ui.set("#AIHHud.LayoutMode", verticalAlign == VerticalAlign.BOTTOM ? "Bottom" : "Top");
        ui.set("#AIHInner.LayoutMode", switch (horizontalAlign) {
            case LEFT   -> "Left";
            case CENTER -> "Center";
            case RIGHT  -> "Right";
        });

        Anchor offset = new Anchor();
        if (verticalAlign == VerticalAlign.BOTTOM) {
            offset.setBottom(Value.of(130));
        }
        ui.setObject("#AIHOffset.Anchor", offset);
    }

    public void applyContent(@Nonnull HudContent content) {
        this.timeText         = content.timeText();
        this.biomeText        = content.biomeText();
        this.zoneText         = content.zoneText();
        this.positionText     = content.positionText();
        this.worldTimeText    = content.worldTimeText();
        this.targetHealthPct  = content.targetHealthPct();
        this.targetEnabled    = content.targetEnabled();
        this.horizontalAlign  = content.horizontalAlign();
        this.verticalAlign    = content.verticalAlign();
        this.fontSize         = content.fontSize();

        targetBar.setFirstLine(content.targetText());
        targetBar.setSecondLine(content.targetSourceText());
        targetBar.setMode(content.healthBarStyle());
        targetBar.setBarVisible(content.healthBarEnabled());
        targetBar.setProgress(content.targetHealthPct());
        targetBar.setWidth(content.barWidth());
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
    public float   getTargetHealthPct()        { return targetHealthPct; }
    public int     getFontSize()               { return fontSize; }
    @Nonnull public HorizontalAlign getHorizontalAlign() { return horizontalAlign; }
    @Nonnull public VerticalAlign   getVerticalAlign()   { return verticalAlign; }

    @Nonnull public ProgressBarWidget getTargetBar() { return targetBar; }
}
