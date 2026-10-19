package org.hlousek.hytale.plugin.advancedinfo.provider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageItemTest {

    @Test
    void sumsStacksOfTheSameItemAndSortsLargestFirst() {
        StorageItem.Summary summary = new StorageItem.Summary();
        summary.add("Wood", 64);
        summary.add("Stone", 100);
        summary.add("Wood", 64);
        summary.add("Apple", 3);

        assertEquals(List.of(
            new StorageItem("Wood", 128),
            new StorageItem("Stone", 100),
            new StorageItem("Apple", 3)
        ), summary.toList());
    }

    @Test
    void equalCountsAreOrderedById() {
        StorageItem.Summary summary = new StorageItem.Summary();
        summary.add("B", 5);
        summary.add("A", 5);

        assertEquals(List.of(new StorageItem("A", 5), new StorageItem("B", 5)), summary.toList());
    }

    @Test
    void ignoresEmptyQuantities() {
        StorageItem.Summary summary = new StorageItem.Summary();
        summary.add("Air", 0);
        assertTrue(summary.toList().isEmpty());
    }
}
