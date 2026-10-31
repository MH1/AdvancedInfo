package org.hlousek.hytale.plugin.advancedinfo.provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * What the player is looking at.
 *
 * @param text       primary line (name, optionally with health numbers); never blank
 * @param sourceText asset pack that owns the target, or a single space when unknown/vanilla
 * @param healthPct  remaining health in [0, 1], or negative when the target has no health
 * @param storage    contents of the targeted storage block; null when the target is no storage
 */
public record TargetInfo(@Nonnull String text, @Nonnull String sourceText, float healthPct, @Nullable StorageContents storage) {

    public static final TargetInfo NONE = new TargetInfo(" ", " ", -1f, null);
}
