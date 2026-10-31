package org.hlousek.hytale.plugin.advancedinfo.provider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageContentsTest {

    @Test
    void fullWhenOneItemTypeFillsEverySlotWithMaximumStacks() {
        StorageContents.Builder b = new StorageContents.Builder(3);
        b.addStack("Wood", 64, 64);
        b.addStack("Wood", 64, 64);
        b.addStack("Wood", 64, 64);

        StorageContents contents = b.build();

        assertTrue(contents.full());
        assertEquals(List.of(new StorageItem("Wood", 192)), contents.items());
    }

    @Test
    void unstackableItemsFillTheirSlots() {
        StorageContents.Builder b = new StorageContents.Builder(2);
        b.addStack("Sword", 1, 1);
        b.addStack("Sword", 1, 1);
        assertTrue(b.build().full());
    }

    @Test
    void notFullWithMixedContents() {
        StorageContents.Builder b = new StorageContents.Builder(2);
        b.addStack("Wood", 64, 64);
        b.addStack("Stone", 64, 64);
        assertFalse(b.build().full());
    }

    @Test
    void notFullWithAFreeSlot() {
        StorageContents.Builder b = new StorageContents.Builder(3);
        b.addStack("Wood", 64, 64);
        b.addStack("Wood", 64, 64);
        assertFalse(b.build().full());
    }

    @Test
    void notFullWithAPartialStack() {
        StorageContents.Builder b = new StorageContents.Builder(2);
        b.addStack("Wood", 64, 64);
        b.addStack("Wood", 30, 64);
        assertFalse(b.build().full());
    }

    @Test
    void emptyContainerIsNeverFull() {
        assertFalse(new StorageContents.Builder(0).build().full());
        assertFalse(new StorageContents.Builder(5).build().full());
    }

    @Test
    void emptyStacksDoNotOccupySlots() {
        StorageContents.Builder b = new StorageContents.Builder(1);
        b.addStack("Air", 0, 64);
        StorageContents contents = b.build();
        assertFalse(contents.full());
        assertTrue(contents.items().isEmpty());
    }
}
