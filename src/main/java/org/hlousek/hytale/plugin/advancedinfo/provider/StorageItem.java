package org.hlousek.hytale.plugin.advancedinfo.provider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One item type in a storage block with the summed quantity of all its stacks.
 *
 * @param itemId item asset id, used as the {@code ItemId} of the HUD icon
 * @param count  total quantity over all stacks
 */
public record StorageItem(@Nonnull String itemId, int count) {

    private static final Comparator<StorageItem> LARGEST_FIRST =
        Comparator.comparingInt(StorageItem::count).reversed().thenComparing(StorageItem::itemId);

    /** Accumulates stacks and returns one entry per item type, largest count first. */
    public static final class Summary {
        private final Map<String, Integer> counts = new LinkedHashMap<>();

        public void add(@Nonnull String itemId, int quantity) {
            if (quantity <= 0) { return; }
            counts.merge(itemId, quantity, Integer::sum);
        }

        @Nonnull
        public List<StorageItem> toList() {
            List<StorageItem> items = new ArrayList<>(counts.size());
            counts.forEach((id, count) -> items.add(new StorageItem(id, count)));
            items.sort(LARGEST_FIRST);
            return List.copyOf(items);
        }
    }
}
