package org.hlousek.hytale.plugin.advancedinfo.provider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Contents of a storage block.
 *
 * @param items one entry per item type with the summed count, largest first
 * @param full  whether a single item type fills the block: every slot holds a maximum stack of it
 */
public record StorageContents(@Nonnull List<StorageItem> items, boolean full) {

    /** Collects the occupied slots of a container with {@code capacity} slots. */
    public static final class Builder {
        private final int capacity;
        private int usedSlots;
        private boolean allStacksFull = true;
        private final StorageItem.Summary summary = new StorageItem.Summary();

        public Builder(int capacity) {
            this.capacity = capacity;
        }

        /** Adds one occupied slot; {@code maxStack} is the largest quantity the slot can hold for this item. */
        public void addStack(@Nonnull String itemId, int quantity, int maxStack) {
            if (quantity <= 0) { return; }
            usedSlots++;
            if (quantity < maxStack) { allStacksFull = false; }
            summary.add(itemId, quantity);
        }

        @Nonnull
        public StorageContents build() {
            List<StorageItem> items = summary.toList();
            return new StorageContents(items, items.size() == 1 && capacity > 0 && usedSlots >= capacity && allStacksFull);
        }
    }
}
