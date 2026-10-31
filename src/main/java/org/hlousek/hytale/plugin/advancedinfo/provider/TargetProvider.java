package org.hlousek.hytale.plugin.advancedinfo.provider;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.joml.Vector2d;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Resolves the entity or block the player is looking at.
 *
 * <p>Entities within {@link #RANGE} blocks are tested first by ray/AABB intersection;
 * when none is hit the first block whose hitbox the look ray crosses is used instead.
 * A targeted storage block also reports its contents.
 */
public final class TargetProvider {

    private static final double RANGE = 8.0;

    @Nonnull private final StorageAccess storageAccess;

    public TargetProvider(@Nonnull StorageAccess storageAccess) {
        this.storageAccess = storageAccess;
    }

    public boolean isEnabled(@Nonnull PlayerHudSettings settings) {
        return settings.isTargetEnabled();
    }

    /** Whether the storage panel needs the target even when the target row is hidden. */
    public boolean isStorageEnabled(@Nonnull PlayerHudSettings settings, boolean storagePermitted) {
        return storagePermitted && settings.isStorageEnabled();
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
        TargetInfo info = entity != null ? describeEntity(ctx, store, entity) : null;
        if (info == null) {
            info = describeTargetBlock(ctx, eyePos, lookDir, storageAccess);
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
            if (isMarker(store, candidate)) continue;
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

    /**
     * Invisible helper entities (spawn markers, spawn beacons, patrol path markers) have hitboxes
     * but nothing a player can see; they must never be reported as the target.
     */
    private static boolean isMarker(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> entity) {
        return store.getComponent(entity, SpawnMarkerEntity.getComponentType()) != null
            || store.getComponent(entity, LegacySpawnBeaconEntity.getComponentType()) != null
            || store.getComponent(entity, PatrolPathMarkerEntity.getComponentType()) != null;
    }

    /** Describes a living/named entity, or returns null when nothing meaningful can be shown for it. */
    @Nullable
    private static TargetInfo describeEntity(@Nonnull InfoContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> entity) {
        DisplayNameComponent displayName = store.getComponent(entity, DisplayNameComponent.getComponentType());
        PlayerRef targetPlayerRef = store.getComponent(entity, PlayerRef.getComponentType());
        NPCEntity npcEntity = store.getComponent(entity, NPCEntity.getComponentType());

        String text = null;
        if (displayName != null && displayName.getDisplayName() != null) {
            text = plainText(displayName.getDisplayName(), ctx.language());
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
        EntityStatMap statMap = ctx.healthVisible() ? store.getComponent(entity, EntityStatMap.getComponentType()) : null;
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

    /** A block whose hitbox the look ray crosses. */
    private record BlockHit(@Nonnull BlockType type, int x, int y, int z) {}

    @Nullable
    private static TargetInfo describeTargetBlock(
        @Nonnull InfoContext ctx, @Nonnull Vector3d eyePos, @Nonnull Vector3d lookDir, @Nonnull StorageAccess storageAccess
    ) {
        World world = ctx.world();
        if (world == null) { return null; }
        ChunkStore chunkStore = world.getChunkStore();

        BlockHit hit = firstBlockAlongRay(chunkStore, eyePos, lookDir);
        if (hit == null) { return null; }

        String text = blockDisplayName(ctx.language(), hit.type());
        Ref<ChunkStore> blockRef = blockEntityRef(chunkStore, hit);
        String detail = blockRef != null ? blockEntityDetail(chunkStore.getStore(), blockRef, hit.type()) : null;
        if (detail != null) {
            text = text + " [" + detail + "]";
        }
        StorageContents storage = null;
        if (blockRef != null && isStorage(chunkStore.getStore(), blockRef)
            && storageAccess.canView(ctx.playerRef(), world, hit.x(), hit.z(), hit.type().getId())) {
            storage = storageContents(chunkStore.getStore(), blockRef);
        }
        TargetInfo info = finish(text, packOwnerOfBlock(hit.type().getId()), -1f);
        return new TargetInfo(info.text(), info.sourceText(), info.healthPct(), storage);
    }

    /**
     * Walks the look ray cell by cell (Amanatides-Woo) and returns the first block whose hitbox
     * the ray crosses within {@link #RANGE}. Testing the hitbox instead of the whole cell lets
     * the ray pass thin blocks such as torches, the way the client's own targeting does.
     * TargetUtil's block targeting reports the placement position (the empty block before the
     * hit), which is not what we want to describe.
     */
    @Nullable
    private static BlockHit firstBlockAlongRay(@Nonnull ChunkStore chunkStore, @Nonnull Vector3d eyePos, @Nonnull Vector3d lookDir) {
        Vector3d dir = new Vector3d(lookDir);
        if (dir.lengthSquared() == 0) { return null; }
        dir.normalize();

        int x = (int) Math.floor(eyePos.x), y = (int) Math.floor(eyePos.y), z = (int) Math.floor(eyePos.z);
        int stepX = Double.compare(dir.x, 0.0), stepY = Double.compare(dir.y, 0.0), stepZ = Double.compare(dir.z, 0.0);
        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.x);
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.y);
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.z);
        double tMaxX = nextBoundary(eyePos.x, x, dir.x, stepX);
        double tMaxY = nextBoundary(eyePos.y, y, dir.y, stepY);
        double tMaxZ = nextBoundary(eyePos.z, z, dir.z, stepZ);

        for (double t = 0; t <= RANGE; ) {
            BlockHit hit = hitInCell(chunkStore, eyePos, dir, x, y, z);
            if (hit != null) { return hit; }
            if (tMaxX < tMaxY && tMaxX < tMaxZ) { t = tMaxX; tMaxX += tDeltaX; x += stepX; }
            else if (tMaxY < tMaxZ)            { t = tMaxY; tMaxY += tDeltaY; y += stepY; }
            else                               { t = tMaxZ; tMaxZ += tDeltaZ; z += stepZ; }
        }
        return null;
    }

    /** Ray parameter at which the ray leaves the current cell along one axis. */
    private static double nextBoundary(double origin, int cell, double dir, int step) {
        if (step == 0) { return Double.POSITIVE_INFINITY; }
        return step > 0 ? (cell + 1 - origin) / dir : (origin - cell) / -dir;
    }

    /**
     * The block in a cell when the ray crosses its hitbox within range; null for air, a miss or an
     * unloaded chunk. Multi-block structures keep their hitbox on the base block, so a filler cell is
     * tested against the base block's boxes at the base position.
     */
    @Nullable
    private static BlockHit hitInCell(
        @Nonnull ChunkStore chunkStore, @Nonnull Vector3d eyePos, @Nonnull Vector3d dir, int x, int y, int z
    ) {
        BlockSection section = blockSectionAt(chunkStore, x, y, z);
        if (section == null) { return null; }
        BlockType type = blockTypeIn(section, x, y, z);
        if (type == null) { return null; }

        int bx = x, by = y, bz = z;
        BlockSection baseSection = section;
        BlockType baseType = type;
        int filler = section.getFiller(x, y, z);
        if (filler != 0) {
            bx -= FillerBlockUtil.unpackX(filler);
            by -= FillerBlockUtil.unpackY(filler);
            bz -= FillerBlockUtil.unpackZ(filler);
            baseSection = blockSectionAt(chunkStore, bx, by, bz);
            BlockType resolved = baseSection != null ? blockTypeIn(baseSection, bx, by, bz) : null;
            if (resolved != null) { baseType = resolved; } else { baseSection = section; bx = x; by = y; bz = z; }
        }

        RotatedVariantBoxes boxes = hitbox(baseType, baseSection.getRotationIndex(bx, by, bz));
        if (boxes == null) { return new BlockHit(type, x, y, z); }
        double distance = rayDistance(eyePos, dir, bx, by, bz, boxes);
        return distance >= 0 && distance <= RANGE ? new BlockHit(type, x, y, z) : null;
    }

    /** The block's hitbox for its rotation; null when the hitbox asset cannot be resolved (treated as a full cell). */
    @Nullable
    private static RotatedVariantBoxes hitbox(@Nonnull BlockType type, int rotationIndex) {
        try {
            BlockBoundingBoxes boxes = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxTypeIndex());
            return boxes != null ? boxes.get(rotationIndex) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Distance along the ray to the nearest of the block's boxes placed at the block position,
     * or -1 when the ray misses them all. Boxes are in block-local coordinates (0..1 for a full cell).
     */
    private static double rayDistance(
        @Nonnull Vector3d eyePos, @Nonnull Vector3d dir, int x, int y, int z, @Nonnull RotatedVariantBoxes boxes
    ) {
        Box[] detail = boxes.getDetailBoxes();
        if (detail == null || detail.length == 0) {
            Box bounding = boxes.getBoundingBox();
            if (bounding == null) { return -1; }
            detail = new Box[] { bounding };
        }
        double best = -1;
        for (Box box : detail) {
            double d = CollisionMath.intersectRayAABB(eyePos, dir, x, y, z, box);
            if (d >= 0 && (best < 0 || d < best)) { best = d; }
        }
        return best;
    }

    @Nullable
    private static BlockSection blockSectionAt(@Nonnull ChunkStore chunkStore, int x, int y, int z) {
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) { return null; }
        return chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
    }

    /** The block type at a world position inside the section; null for air and unknown blocks. */
    @Nullable
    private static BlockType blockTypeIn(@Nonnull BlockSection section, int x, int y, int z) {
        int blockId = section.get(x, y, z);
        if (blockId == BlockType.EMPTY_ID) { return null; }
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        return blockType == null || blockType.isUnknown() ? null : blockType;
    }

    /**
     * The entity of the targeted block. Multi-block structures store the entity on their
     * base block, so filler offsets are resolved first.
     */
    @Nullable
    private static Ref<ChunkStore> blockEntityRef(@Nonnull ChunkStore chunkStore, @Nonnull BlockHit hit) {
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
        return blockRef != null && blockRef.isValid() ? blockRef : null;
    }

    /** Extra information carried by the block's entity (bench tier, growth stage, teleporter destination). */
    @Nullable
    private static String blockEntityDetail(
        @Nonnull Store<ChunkStore> store, @Nonnull Ref<ChunkStore> blockRef, @Nonnull BlockType type
    ) {
        BenchBlock bench = store.getComponent(blockRef, BenchBlock.getComponentType());
        if (bench != null) {
            return "Tier " + bench.getTierLevel();
        }
        FarmingBlock farming = store.getComponent(blockRef, FarmingBlock.getComponentType());
        if (farming != null) {
            GrowthStage stage = growthStage(farming, type);
            if (stage != null) { return stage.label(); }
        }
        Teleporter teleporter = store.getComponent(blockRef, Teleporter.getComponentType());
        if (teleporter != null) {
            if (!isBlank(teleporter.getWarp())) { return warpDisplayName(teleporter.getWarp()); }
            if (teleporter.hasOwnedWarp())      { return warpDisplayName(teleporter.getOwnedWarp()); }
        }
        return null;
    }

    /**
     * The stage a growing crop currently shows. The farming tick keeps the index of that stage in the
     * growth progress and removes the component once the last stage is done; a crop whose state block
     * carries no farming config inherits it from the block type it grew from.
     */
    @Nullable
    private static GrowthStage growthStage(@Nonnull FarmingBlock farming, @Nonnull BlockType type) {
        FarmingData data = type.getFarming();
        if (data == null && !isBlank(farming.getPreviousBlockType())) {
            BlockType previous = BlockType.getAssetMap().getAsset(farming.getPreviousBlockType());
            data = previous != null ? previous.getFarming() : null;
        }
        if (data == null || data.getStages() == null) { return null; }
        String stageSet = !isBlank(farming.getCurrentStageSet()) ? farming.getCurrentStageSet() : data.getStartingStageSet();
        FarmingStageData[] stages = stageSet != null ? data.getStages().get(stageSet) : null;
        if (stages == null) { return null; }
        return new GrowthStage((int) farming.getGrowthProgress(), stages.length);
    }

    /** The warp id as it was registered; teleporters keep only the lower-case lookup key. */
    @Nonnull
    private static String warpDisplayName(@Nonnull String warp) {
        TeleportPlugin plugin = TeleportPlugin.get();
        if (plugin == null || !plugin.isWarpsLoaded()) { return warp; }
        Warp registered = plugin.getWarps().get(warp.toLowerCase(Locale.ROOT));
        return registered != null && !isBlank(registered.getId()) ? registered.getId() : warp;
    }

    private static boolean isStorage(@Nonnull Store<ChunkStore> store, @Nonnull Ref<ChunkStore> blockRef) {
        return store.getComponent(blockRef, ItemContainerBlock.getComponentType()) != null;
    }

    /** Contents of a storage block (chest, barrel, ...); null when the block stores nothing. */
    @Nullable
    private static StorageContents storageContents(@Nonnull Store<ChunkStore> store, @Nonnull Ref<ChunkStore> blockRef) {
        ItemContainerBlock containerBlock = store.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (containerBlock == null) { return null; }
        ItemContainer container = containerBlock.getItemContainer();
        if (container == null) { return null; }

        StorageContents.Builder contents = new StorageContents.Builder(container.getCapacity());
        container.forEach((slot, stack) -> {
            if (ItemStack.isEmpty(stack)) { return; }
            Item item = stack.getItem();
            int maxStack = item != null ? item.getMaxStack() : stack.getQuantity();
            contents.addStack(stack.getItemId(), stack.getQuantity(), maxStack);
        });
        return contents.build();
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

    /**
     * Resolves a display-name message to plain text in the player's language.
     * Returns null for translation keys without a translation; Message.getAnsiMessage()
     * would otherwise fall back to the key plus the toString() of its parameter maps.
     */
    @Nullable
    private static String plainText(@Nonnull Message message, @Nonnull String language) {
        String raw = message.getRawText();
        if (raw != null) { return raw; }
        String key = message.getMessageId();
        if (isBlank(key)) { return null; }
        String translated = I18nModule.get().getMessage(language, key);
        if (translated == null && !InfoContext.DEFAULT_LANGUAGE.equals(language)) {
            translated = I18nModule.get().getMessage(InfoContext.DEFAULT_LANGUAGE, key);
        }
        return translated;
    }

    /** Normalizes blank text to a single space and hides the vanilla "Hytale" pack name. */
    @Nonnull
    private static TargetInfo finish(@Nullable String text, @Nullable String source, float healthPct) {
        if (isBlank(text)) { text = " "; }
        if (source == null || "Hytale".equals(source)) { source = " "; }
        return new TargetInfo(text, source, healthPct, null);
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
