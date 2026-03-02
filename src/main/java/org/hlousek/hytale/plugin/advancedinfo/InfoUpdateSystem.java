package org.hlousek.hytale.plugin.advancedinfo;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.HorizontalAlign;
import org.hlousek.hytale.plugin.advancedinfo.settings.PlayerHudSettings.VerticalAlign;
import org.hlousek.hytale.plugin.advancedinfo.ui.HudContent;
import org.hlousek.hytale.plugin.advancedinfo.ui.InfoManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public class InfoUpdateSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull private final InfoManager infoManager;
    @Nonnull private final ComponentType<EntityStore, PlayerHudSettings> settingsComponentType;
    @Nonnull private final Query<EntityStore> query;

    public InfoUpdateSystem(
        @Nonnull InfoManager infoManager,
        @Nonnull ComponentType<EntityStore, PlayerHudSettings> settingsComponentType
    ) {
        this.infoManager = infoManager;
        this.settingsComponentType = settingsComponentType;
        this.query = Query.and(Player.getComponentType(), PlayerRef.getComponentType(), HeadRotation.getComponentType());
    }

    @Override
    public void tick(
        float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
        @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        final Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);

        Player player = holder.getComponent(Player.getComponentType());
        if (player == null) { return; }

        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (playerRef == null) { return; }

        PlayerHudSettings settings = holder.getComponent(settingsComponentType);
        if (settings == null) { return; }

        // If everything is disabled, hide the whole panel and return.
        if (!settings.isTimeEnabled() && !settings.isBiomeEnabled() && !settings.isPositionEnabled()
                && !settings.isWorldTimeEnabled() && !settings.isZoneEnabled() && !settings.isTargetEnabled()) {
            this.infoManager.hideHud(player, playerRef);
            return;
        }

        // Hide the HUD while the world map or a menu/inventory window is open.
        if (this.infoManager.isHudSuppressed(playerRef.getUuid())) {
            this.infoManager.hideHud(player, playerRef);
            return;
        }

        // --- Time ---
        @Nullable String timeText = null;
        if (settings.isTimeEnabled()) {
            ZonedDateTime now = ZonedDateTime.now(settings.getTimezone());
            boolean useOffset = !settings.isTimezoneSet() || settings.isTimezoneOffsetVisible();
            timeText = HudTimeFormatter.realTime(now, settings.isTime24h(), useOffset);
        }

        // --- Biome ---
        @Nullable String biomeText = null;
        if (settings.isBiomeEnabled()) {
            WorldMapTracker tracker = player.getWorldMapTracker();
            String biomeName = tracker != null ? tracker.getCurrentBiomeName() : null;
            biomeText = biomeName != null ? biomeName : "Unknown";
        }

        // --- Position ---
        @Nullable String positionText = null;
        if (settings.isPositionEnabled()) {
            Transform transform = playerRef.getTransform();
            if (transform != null) {
                Vector3d pos = transform.getPosition();
                positionText = String.format("%d %d %d",
                    (int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
            }
        }

        // --- World Time ---
        @Nullable String worldTimeText = null;
        if (settings.isWorldTimeEnabled()) {
            World world = player.getWorld();
            if (world != null) {
                WorldTimeResource worldTime = (WorldTimeResource) store.getResource(WorldTimeResource.getResourceType());
                if (worldTime != null && !world.getWorldConfig().isGameTimePaused()) {
                    worldTimeText = HudTimeFormatter.worldTime(worldTime.getGameDateTime(), settings.isTime24h());
                }
            }
        }

        // --- Zone & Tier ---
        @Nullable String zoneText = null;
        if (settings.isZoneEnabled()) {
            World world = player.getWorld();
            if (world != null && world.getChunkStore().getGenerator() instanceof ChunkGenerator generator) {
                Vector3d pos = playerRef.getTransform() != null ? playerRef.getTransform().getPosition() : null;
                if (pos != null) {
                    int seed = (int) world.getWorldConfig().getSeed();
                    ZoneBiomeResult result = generator.getZoneBiomeResultAt(seed, (int) pos.getX(), (int) pos.getZ());
                    Zone zone = result.getZoneResult().getZone();

                    // Only show zones that have Display: true in their Zone.json.
                    // Shore/shallow ocean sub-zones have Display: false and no translation entry.
                    if (zone.discoveryConfig().display()) {
                        String tierKey = "server.map.tier." + zone.name();
                        String resolved = I18nModule.get().getMessage("en-US", tierKey);
                        zoneText = resolved != null ? resolved : zone.name();
                    }
                }
            }
        }

        // --- Target ---
        @Nullable String targetText = null;
        @Nullable String targetSourceText = null;
        float targetHealthPct = -1f;
        if (settings.isTargetEnabled()) {
            Ref<EntityStore> playerEntityRef = archetypeChunk.getReferenceTo(index);
            HeadRotation headRotation = commandBuffer.getComponent(playerEntityRef, HeadRotation.getComponentType());
            Transform playerTransform = playerRef.getTransform();

            if (headRotation != null && playerTransform != null) {
                float eyeHeight = 0.0f;
                ModelComponent modelComponent = store.getComponent(playerEntityRef, ModelComponent.getComponentType());
                if (modelComponent != null) {
                    eyeHeight = modelComponent.getModel().getEyeHeight(playerEntityRef, store);
                }
                Vector3d eyePos = new Vector3d(
                    playerTransform.getPosition().getX(),
                    playerTransform.getPosition().getY() + eyeHeight,
                    playerTransform.getPosition().getZ()
                );
                Transform lookTransform = new Transform(
                    eyePos.getX(), eyePos.getY(), eyePos.getZ(),
                    headRotation.getRotation().getPitch(),
                    headRotation.getRotation().getYaw(),
                    headRotation.getRotation().getRoll()
                );
                Vector3d lookDir = lookTransform.getDirection();

                List<Ref<EntityStore>> nearby = TargetUtil.getAllEntitiesInSphere(eyePos, 8.0, store);

                Ref<EntityStore> closest = null;
                double minDist2 = Double.MAX_VALUE;
                for (Ref<EntityStore> candidate : nearby) {
                    if (candidate == null || !candidate.isValid() || candidate.equals(playerEntityRef)) continue;
                    TransformComponent tc = store.getComponent(candidate, TransformComponent.getComponentType());
                    if (tc == null) continue;
                    BoundingBox bb = store.getComponent(candidate, BoundingBox.getComponentType());
                    boolean hit;
                    if (bb != null) {
                        Vector2d minMax = new Vector2d();
                        hit = CollisionMath.intersectRayAABB(eyePos, lookDir,
                            tc.getPosition().getX(), tc.getPosition().getY(), tc.getPosition().getZ(),
                            bb.getBoundingBox(), minMax);
                    } else {
                        Vector3d toEntity = new Vector3d(
                            tc.getPosition().getX() - eyePos.getX(),
                            tc.getPosition().getY() - eyePos.getY(),
                            tc.getPosition().getZ() - eyePos.getZ()
                        );
                        double dot = toEntity.dot(lookDir);
                        hit = dot > 0;
                    }
                    if (!hit) continue;
                    double dist2 = eyePos.distanceSquaredTo(tc.getPosition());
                    if (dist2 < minDist2) {
                        minDist2 = dist2;
                        closest = candidate;
                    }
                }

                if (closest != null) {
                    DisplayNameComponent displayName = store.getComponent(closest, DisplayNameComponent.getComponentType());
                    PlayerRef targetPlayerRef = store.getComponent(closest, PlayerRef.getComponentType());
                    NPCEntity npcEntity = store.getComponent(closest, NPCEntity.getComponentType());
                    if (displayName != null && displayName.getDisplayName() != null) {
                        targetText = displayName.getDisplayName().getAnsiMessage();
                    }
                    if (targetText == null || targetText.isBlank()) {
                        if (targetPlayerRef != null) {
                            targetText = targetPlayerRef.getUsername();
                        }
                    }
                    if (targetText == null || targetText.isBlank()) {
                        if (npcEntity != null && npcEntity.getRoleName() != null) {
                            String roleName = npcEntity.getRoleName();
                            int colon = roleName.indexOf(':');
                            targetText = colon >= 0 ? roleName.substring(colon + 1) : roleName;
                        }
                    }
                    if (targetText == null || targetText.isBlank()) {
                        ModelComponent targetModel = store.getComponent(closest, ModelComponent.getComponentType());
                        if (targetModel != null) {
                            String modelId = targetModel.getModel().getModelAssetId();
                            int colon = modelId.indexOf(':');
                            targetText = colon >= 0 ? modelId.substring(colon + 1) : modelId;
                        }
                    }
                    EntityStatMap statMap = store.getComponent(closest, EntityStatMap.getComponentType());
                    if (statMap != null) {
                        EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
                        if (health != null) {
                            targetText = targetText + " (" + (int) health.get() + "/" + (int) health.getMax() + ")";
                            if (health.getMax() > 0f) {
                                targetHealthPct = health.get() / health.getMax();
                            }
                        }
                    }
                    if (npcEntity != null) {
                        targetSourceText = packOwnerOfNpc(npcEntity.getNPCTypeId());
                    }
                } else {
                    // No entity in view - fall back to block targeting.
                    World world = player.getWorld();
                    if (world != null) {
                        Vector3i blockPos = TargetUtil.getTargetBlock(playerEntityRef, 8.0, store);
                        if (blockPos != null) {
                            int bx = blockPos.getX(), by = blockPos.getY(), bz = blockPos.getZ();
                            long chunkIndex = ChunkUtil.indexChunkFromBlock(bx, bz);
                            WorldChunk targetChunk = world.getChunkIfLoaded(chunkIndex);
                            if (targetChunk != null) {
                                // Resolve the filler offset to find the true base block position.
                                BlockChunk blockChunkForFiller = targetChunk.getBlockChunk();
                                int filler = blockChunkForFiller != null
                                    ? blockChunkForFiller.getSectionAtBlockY(by).getFiller(bx, by, bz)
                                    : 0;

                                bx -= FillerBlockUtil.unpackX(filler);
                                by -= FillerBlockUtil.unpackY(filler);
                                bz -= FillerBlockUtil.unpackZ(filler);

                                // The base block may have crossed into an adjacent chunk.
                                long baseChunkIndex = ChunkUtil.indexChunkFromBlock(bx, bz);
                                WorldChunk worldChunk = baseChunkIndex == chunkIndex
                                    ? targetChunk
                                    : world.getChunkIfLoaded(baseChunkIndex);

                                if (worldChunk != null) {
                                    com.hypixel.hytale.component.Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(baseChunkIndex);
                                    if (chunkRef != null && chunkRef.isValid()) {
                                        com.hypixel.hytale.component.ComponentAccessor<ChunkStore> chunkAccessor =
                                            (com.hypixel.hytale.component.ComponentAccessor<ChunkStore>) world.getChunkStore().getStore();
                                        BlockChunk blockChunk = chunkAccessor.getComponent(chunkRef, BlockChunk.getComponentType());
                                        if (blockChunk != null) {
                                            BlockSection section = blockChunk.getSectionAtBlockY(by);
                                            int blockId = section.get(bx, by, bz);
                                            if (blockId != BlockType.EMPTY_ID) {
                                                BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                                                if (blockType != null && !blockType.isUnknown()) {
                                                    BenchState benchState = null;
                                                    Teleporter teleporter = null;
                                                    var blockState = worldChunk.getState(bx, by, bz);
                                                    if (blockState instanceof BenchState bs) {
                                                        benchState = bs;
                                                    }
                                                    Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(bx, by, bz);
                                                    if (blockRef != null && blockRef.isValid()) {
                                                        teleporter = world.getChunkStore().getStore().getComponent(blockRef, Teleporter.getComponentType());
                                                    }
                                                    targetText = blockDisplayName(blockType, benchState, teleporter);
                                                    targetSourceText = packOwnerOfBlock(blockType.getId());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (targetText == null || targetText.isBlank()) {
                targetText = " ";
            };
            if ("Hytale".equals(targetSourceText)) {
                targetSourceText = null;
            }
            if (targetSourceText == null) {
                targetSourceText = " ";
            };
        }

        this.infoManager.updateHud(player, playerRef, new HudContent(
            timeText, biomeText, zoneText, positionText, worldTimeText, targetText, targetSourceText,
            targetHealthPct,
            settings.isTargetEnabled(),
            settings.isHealthBarEnabled(),
            settings.isCaptionsEnabled(),
            settings.getHealthBarStyle(),
            settings.getHorizontalAlign(),
            settings.getVerticalAlign(),
            settings.getFontSize(),
            Math.round(300 * (settings.getFontSize() / 18f))
        ));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Nullable
    private static String blockDisplayName(@Nonnull BlockType blockType, @Nullable BenchState benchState, @Nullable Teleporter teleporter) {
        Item item = blockType.getItem();
        String name = null;
        if (item != null) {
            String key = item.getTranslationKey();
            if (key != null) {
                name = I18nModule.get().getMessage("en-US", key);
            }
        }
        if (name == null) {
            String id = blockType.getId();
            int colon = id.indexOf(':');
            name = colon >= 0 ? id.substring(colon + 1) : id;
        }
        if (teleporter != null && teleporter.getOwnedWarp() != null) {
            name = name + " (" + teleporter.getOwnedWarp() + ")";
        } else if (benchState != null) {
            name = name + " (Tier " + benchState.getTierLevel() + ")";
        }
        return name;
    }

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
}
