package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import org.hlousek.hytale.plugin.advancedinfo.AdvancedInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Hides storage contents in chunks claimed with the optional SimpleClaims plugin when the player may
 * not interact there. Uses the same check SimpleClaims applies when a block is used:
 * {@code ClaimManager.isAllowedToInteract} with the chest flag for blocks whose id contains "chest"
 * and the generic block flag otherwise.
 *
 * <p>SimpleClaims is bound by reflection on first use (all plugins are started by then), so it is
 * no build dependency. When it is installed but its API cannot be bound or a check fails, contents
 * are hidden (fail closed) and a single warning is logged.
 */
public final class SimpleClaimsStorageAccess implements StorageAccess {

    private static final PluginIdentifier PLUGIN_ID = new PluginIdentifier("Buuz135", "SimpleClaims");
    private static final String CLAIM_MANAGER = "com.buuz135.simpleclaims.claim.ClaimManager";
    private static final String PARTY_INFO    = "com.buuz135.simpleclaims.claim.party.PartyInfo";
    private static final String CHEST_NODE    = "simpleclaims.party.protection.interact.chest";
    private static final String BLOCK_NODE    = "simpleclaims.party.protection.interact";

    private record Binding(
        @Nonnull Method getInstance,
        @Nonnull Method isAllowedToInteract,
        @Nonnull Predicate<Object> chestFlag,
        @Nonnull Predicate<Object> blockFlag
    ) {}

    private enum State { UNRESOLVED, ABSENT, BOUND, BROKEN }

    private volatile State state = State.UNRESOLVED;
    @Nullable private volatile Binding binding;

    @Override
    public boolean canView(@Nonnull PlayerRef player, @Nonnull World world, int x, int z, @Nonnull String blockId) {
        if (state == State.UNRESOLVED) { resolve(); }
        return switch (state) {
            case ABSENT -> true;
            case BROKEN -> false;
            default     -> check(player.getUuid(), world.getName(), x, z, blockId);
        };
    }

    private boolean check(@Nonnull UUID player, @Nonnull String world, int x, int z, @Nonnull String blockId) {
        Binding b = binding;
        if (b == null) { return false; }
        boolean chest = blockId.toLowerCase(Locale.ROOT).contains("chest");
        try {
            Object manager = b.getInstance().invoke(null);
            Object allowed = b.isAllowedToInteract().invoke(manager, player, world, x, z,
                chest ? b.chestFlag() : b.blockFlag(), chest ? CHEST_NODE : BLOCK_NODE);
            return Boolean.TRUE.equals(allowed);
        } catch (Throwable t) {
            fail("SimpleClaims claim check failed, storage contents stay hidden: %s", t);
            return false;
        }
    }

    private synchronized void resolve() {
        if (state != State.UNRESOLVED) { return; }
        PluginBase plugin = PluginManager.get().getPlugin(PLUGIN_ID);
        if (plugin == null || !plugin.isEnabled()) {
            state = State.ABSENT;
            return;
        }
        try {
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> managerClass = Class.forName(CLAIM_MANAGER, true, loader);
            Class<?> partyClass   = Class.forName(PARTY_INFO, true, loader);
            binding = new Binding(
                managerClass.getMethod("getInstance"),
                managerClass.getMethod("isAllowedToInteract",
                    UUID.class, String.class, int.class, int.class, Predicate.class, String.class),
                flag(partyClass.getMethod("isChestInteractEnabled")),
                flag(partyClass.getMethod("isBlockInteractEnabled"))
            );
            state = State.BOUND;
            AdvancedInfo.LOGGER.atInfo().log("SimpleClaims found: storage contents respect claim permissions.");
        } catch (Throwable t) {
            fail("SimpleClaims is installed but its API is not supported, storage contents stay hidden: %s", t);
        }
    }

    @Nonnull
    private Predicate<Object> flag(@Nonnull Method partyGetter) {
        return party -> {
            try {
                return Boolean.TRUE.equals(partyGetter.invoke(party));
            } catch (Throwable t) {
                fail("SimpleClaims party check failed, storage contents stay hidden: %s", t);
                return false;
            }
        };
    }

    private void fail(@Nonnull String message, @Nonnull Throwable t) {
        if (state != State.BROKEN) {
            state = State.BROKEN;
            AdvancedInfo.LOGGER.atWarning().log(message, t.toString());
        }
    }
}
