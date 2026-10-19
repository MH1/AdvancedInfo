package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;

/** Decides whether a player may see the contents of a storage block, e.g. because of land claims. */
@FunctionalInterface
public interface StorageAccess {

    StorageAccess ALLOW_ALL = (player, world, x, z, blockId) -> true;

    /**
     * @param x       block x of the targeted block
     * @param z       block z of the targeted block
     * @param blockId block type id of the targeted block
     */
    boolean canView(@Nonnull PlayerRef player, @Nonnull World world, int x, int z, @Nonnull String blockId);
}
