package org.hlousek.hytale.plugin.advancedinfo.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GrowthStageTest {

    @Test
    void countsStagesFromOne() {
        assertEquals("Stage 1/4", new GrowthStage(0, 4).label());
        assertEquals("Stage 3/4", new GrowthStage(2, 4).label());
    }

    @Test
    void fullyGrownCropShowsNoStage() {
        assertNull(new GrowthStage(3, 4).label());
        assertNull(new GrowthStage(7, 4).label());
    }

    @Test
    void degenerateStageSetsShowNothing() {
        assertNull(new GrowthStage(0, 1).label());
        assertNull(new GrowthStage(0, 0).label());
        assertNull(new GrowthStage(-1, 4).label());
    }
}
