package org.hlousek.hytale.plugin.advancedinfo.ui;

import org.hlousek.hytale.plugin.advancedinfo.provider.StorageItem;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Content of the storage panel while the player looks at a storage block.
 *
 * @param items   item types with summed counts, largest first
 * @param maxRows maximum number of item rows; further items are summarised as "+N"
 */
public record StoragePanel(
    @Nonnull List<StorageItem> items,
    int maxRows,
    @Nonnull HorizontalAlign horizontalAlign,
    @Nonnull VerticalAlign verticalAlign
) {}
