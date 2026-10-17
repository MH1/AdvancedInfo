package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.joml.Vector2d;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Resolves the entity or block the player is looking at.
 *
 * <p>Entities within {@link #RANGE} blocks are tested first by ray/AABB intersection;
 * when none is hit the targeted block is used instead.
 */
public final class TargetProvider {

    private static final double RANGE = 8.0;

    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isTargetEnabled();
    }

    @Nonnull
    public TargetInfo resolve(@Nonnull InfoContext ctx) {
        Store<EntityStore> store = ctx.store();
        Ref<EntityStore> playerEntityRef = ctx.entityRef();

        HeadRotation headRotation = ctx.commandBuffer().getComponent(playerEntityRef, HeadRotation.getComponentType());
        TransformComponent transform = store.getComponent(playerEntityRef, TransformComponent.getComponentType());
        if (headRotation == null || transform == null) { return TargetInfo.NONE; }

        float eyeHeight = 0.0f;
        ModelComponent modelComponent = store.getComponent(playerEntityRef, ModelComponent.getComponentType());
        if (modelComponent != null) {
            eyeHeight = modelComponent.getModel().getEyeHeight(playerEntityRef, store);
        }
        Vector3d eyePos = new Vector3d(transform.getPosition()).add(0, eyeHeight, 0);
        Vector3d lookDir = headRotation.getDirection();

        // Prefer a named entity under the crosshair; anything we cannot describe
        // (block entities, markers, ...) must not shadow the block behind it.
        Ref<EntityStore> entity = findLookedAtEntity(store, playerEntityRef, eyePos, lookDir);
        TargetInfo info = entity != null ? describeEntity(store, entity) : null;
        if (info == null) {
            info = describeTargetBlock(ctx, eyePos, lookDir);
        }
        return info != null ? info : TargetInfo.NONE;
    }

    // -------------------------------------------------------------------------
    // Entities
    // -------------------------------------------------------------------------

    @Nullable
    private static Ref<EntityStore> findLookedAtEntity(
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> self,
        @Nonnull Vector3d eyePos,
        @Nonnull Vector3d lookDir
    ) {
        List<Ref<EntityStore>> nearby = TargetUtil.getAllEntitiesInSphere(eyePos, RANGE, store);

        Ref<EntityStore> closest = null;
        double minDist2 = Double.MAX_VALUE;
        for (Ref<EntityStore> candidate : nearby) {
            if (candidate == null || !candidate.isValid() || candidate.equals(self)) continue;
            TransformComponent tc = store.getComponent(candidate, TransformComponent.getComponentType());
            BoundingBox bb = store.getComponent(candidate, BoundingBox.getComponentType());
            // Only entities with a real hitbox can be aimed at.
            if (tc == null || bb == null) continue;
            Vector3d p = tc.getPosition();
            if (!CollisionMath.intersectRayAABB(eyePos, lookDir, p.x, p.y, p.z, bb.getBoundingBox(), new Vector2d())) continue;
            double dist2 = eyePos.distanceSquared(tc.getPosition());
            if (dist2 < minDist2) {
                minDist2 = dist2;
                closest = candidate;
            }
        }
        return closest;
    }

    /** Describes a living/named entity, or returns null when nothing meaningful can be shown for it. */
    @Nullable
    private static TargetInfo describeEntity(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> entity) {
        DisplayNameComponent displayName = store.getComponent(entity, DisplayNameComponent.getComponentType());
        PlayerRef targetPlayerRef = store.getComponent(entity, PlayerRef.getComponentType());
        NPCEntity npcEntity = store.getComponent(entity, NPCEntity.getComponentType());

        String text = null;
        if (displayName != null && displayName.getDisplayName() != null) {
            text = displayName.getDisplayName().getAnsiMessage();
        }
        if (isBlank(text) && targetPlayerRef != null) {
            text = targetPlayerRef.getUsername();
        }
        if (isBlank(text) && npcEntity != null && npcEntity.getRoleName() != null) {
            text = stripNamespace(npcEntity.getRoleName());
        }
        if (isBlank(text)) {
            ModelComponent targetModel = store.getComponent(entity, ModelComponent.getComponentType());
            if (targetModel != null) {
                text = stripNamespace(targetModel.getModel().getModelAssetId());
            }
        }
        if (isBlank(text)) { return null; }

        float healthPct = -1f;
        EntityStatMap statMap = store.getComponent(entity, EntityStatMap.getComponentType());
        if (statMap != null) {
            EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
            if (health != null) {
                text = text + " (" + (int) health.get() + "/" + (int) health.getMax() + ")";
                if (health.getMax() > 0f) {
                    healthPct = health.get() / health.getMax();
                }
            }
        }

        String source = npcEntity != null ? packOwnerOfNpc(npcEntity.getNPCTypeId()) : null;
        return finish(text, source, healthPct);
    }

    // -------------------------------------------------------------------------
    // Blocks
    // -------------------------------------------------------------------------

    private static final double RAY_STEP = 0.1;

    /** A non-empty block found along the look ray. */
    private record BlockHit(@Nonnull BlockType type, int x, int y, int z) {}

    @Nullable
    private static TargetInfo describeTargetBlock(@Nonnull InfoContext ctx, @Nonnull Vector3d eyePos, @Nonnull Vector3d lookDir) {
        World world = ctx.world();
        if (world == null) { return null; }
        ChunkStore chunkStore = world.getChunkStore();

        BlockHit hit = firstBlockAlongRay(chunkStore, eyePos, lookDir);
        if (hit == null) { return null; }

        String text = blockDisplayName(ctx.language(), hit.type());
        String detail = blockEntityDetail(chunkStore, hit);
        if (detail != null) {
            text = text + " [" + detail + "]";
        }
        return finish(text, packOwnerOfBlock(hit.type().getId()), -1f);
    }

    /**
     * Marches from the eye along the look direction and returns the first non-empty block
     * within {@link #RANGE}. TargetUtil's block targeting reports the placement position
     * (the empty block before the hit), which is not what we want to describe.
     */
    @Nullable
    private static BlockHit firstBlockAlongRay(@Nonnull ChunkStore chunkStore, @Nonnull Vector3d eyePos, @Nonnull Vector3d lookDir) {
        Vector3d dir = new Vector3d(lookDir);
        if (dir.lengthSquared() == 0) { return null; }
        dir.normalize();

        int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE, lastZ = Integer.MIN_VALUE;
        for (double t = 0; t <= RANGE; t += RAY_STEP) {
            int x = (int) Math.floor(eyePos.x + dir.x * t);
            int y = (int) Math.floor(eyePos.y + dir.y * t);
            int z = (int) Math.floor(eyePos.z + dir.z * t);
            if (x == lastX && y == lastY && z == lastZ) { continue; }
            lastX = x; lastY = y; lastZ = z;

            BlockType blockType = blockTypeAt(chunkStore, x, y, z);
            if (blockType != null) { return new BlockHit(blockType, x, y, z); }
        }
        return null;
    }

    @Nullable
    private static BlockSection blockSectionAt(@Nonnull ChunkStore chunkStore, int x, int y, int z) {
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) { return null; }
        return chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
    }

    /** Reads the block type at a world position from the loaded chunk section; null for air/unknown/unloaded. */
    @Nullable
    private static BlockType blockTypeAt(@Nonnull ChunkStore chunkStore, int x, int y, int z) {
        BlockSection section = blockSectionAt(chunkStore, x, y, z);
        if (section == null) { return null; }

        int blockId = section.get(x, y, z);
        if (blockId == BlockType.EMPTY_ID) { return null; }
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        return blockType == null || blockType.isUnknown() ? null : blockType;
    }

    /**
     * Extra information carried by the block's entity (bench tier, teleporter destination).
     * Multi-block structures store the entity on their base block, so filler offsets are
     * resolved first.
     */
    @Nullable
    private static String blockEntityDetail(@Nonnull ChunkStore chunkStore, @Nonnull BlockHit hit) {
        int x = hit.x(), y = hit.y(), z = hit.z();
        BlockSection section = blockSectionAt(chunkStore, x, y, z);
        if (section == null) { return null; }

        int filler = section.getFiller(x, y, z);
        if (filler != 0) {
            x -= FillerBlockUtil.unpackX(filler);
            y -= FillerBlockUtil.unpackY(filler);
            z -= FillerBlockUtil.unpackZ(filler);
        }

        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) { return null; }
        Store<ChunkStore> store = chunkStore.getStore();
        BlockComponentSection components = store.getComponent(sectionRef, BlockComponentSection.getComponentType());
        if (components == null) { return null; }
        Ref<ChunkStore> blockRef = components.getBlockReference(ChunkUtil.indexBlock(x, y, z));
        if (blockRef == null || !blockRef.isValid()) { return null; }

        BenchBlock bench = store.getComponent(blockRef, BenchBlock.getComponentType());
        if (bench != null) {
            return "Tier " + bench.getTierLevel();
        }
        Teleporter teleporter = store.getComponent(blockRef, Teleporter.getComponentType());
        if (teleporter != null) {
            if (!isBlank(teleporter.getWarp())) { return teleporter.getWarp(); }
            if (teleporter.hasOwnedWarp())      { return teleporter.getOwnedWarp(); }
        }
        return null;
    }

    @Nonnull
    private static String blockDisplayName(@Nonnull String language, @Nonnull BlockType blockType) {
        Item item = blockType.getItem();
        if (item != null) {
            String key = item.getTranslationKey();
            if (key != null) {
                String name = I18nModule.get().getMessage(language, key);
                if (name != null) { return name; }
            }
        }
        return stripNamespace(blockType.getId());
    }

    // -------------------------------------------------------------------------
    // Asset pack ownership
    // -------------------------------------------------------------------------

    @Nullable
    private static String packOwnerOfBlock(@Nullable String blockId) {
        if (blockId == null) return null;
        for (var pack : AssetModule.get().getAssetPacks()) {
            var keys = BlockType.getAssetMap().getKeysForPack(pack.getName());
            if (keys != null && keys.contains(blockId)) {
                return pack.getManifest().getName();
            }
        }
        return null;
    }

    @Nullable
    private static String packOwnerOfNpc(@Nullable String npcTypeId) {
        if (npcTypeId == null) return null;
        for (var pack : AssetModule.get().getAssetPacks()) {
            if (npcTypeId.startsWith(pack.getManifest().getGroup() + ":") ||
                npcTypeId.startsWith(pack.getManifest().getName() + ":")) {
                return pack.getManifest().getName();
            }
        }
        // Fallback: strip the namespace prefix from the NPC type ID.
        int colon = npcTypeId.indexOf(':');
        return colon > 0 ? npcTypeId.substring(0, colon) : null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Normalizes blank text to a single space and hides the vanilla "Hytale" pack name. */
    @Nonnull
    private static TargetInfo finish(@Nullable String text, @Nullable String source, float healthPct) {
        if (isBlank(text)) { text = " "; }
        if (source == null || "Hytale".equals(source)) { source = " "; }
        return new TargetInfo(text, source, healthPct);
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }

    @Nonnull
    private static String stripNamespace(@Nonnull String id) {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }
}
