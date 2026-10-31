package org.hlousek.hytale.plugin.advancedinfo.provider;

import javax.annotation.Nullable;

/**
 * Growth stage of a crop.
 *
 * @param index zero-based index of the stage the block currently shows
 * @param count number of stages in the active stage set, the last one being the fully grown form
 */
public record GrowthStage(int index, int count) {

    /** "Stage i/N" while the crop is still growing; null for a fully grown crop or a degenerate stage set. */
    @Nullable
    public String label() {
        if (count < 2 || index < 0 || index >= count - 1) { return null; }
        return "Stage " + (index + 1) + "/" + count;
    }
}
