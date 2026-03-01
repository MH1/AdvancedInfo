package org.hlousek.hytale.plugin.advancedinfo.settings;

import org.hlousek.hytale.plugin.advancedinfo.AdvancedInfo;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Applies migration defaults to a settings object by inspecting {@link Since}-annotated fields.
 * For every field whose {@link Since#version()} is greater than {@code currentVersion} and at most
 * {@code targetVersion}, the field is set to the parsed {@link Since#defaultValue()}.
 *
 * <p>Versions are compared as {@code major.minor.patch} - the 4th segment and beyond are ignored.
 */
public class SettingsMigrator {

    private SettingsMigrator() {}

    /**
     * Scans all {@link Since}-annotated fields on {@code target}, and for any field introduced
     * in a version newer than {@code currentVersion} and at most {@code targetVersion},
     * applies the annotated default value.
     *
     * @param target         the settings object to migrate
     * @param currentVersion the version string stored in the saved data (e.g. {@code "0.0.0"})
     * @param targetVersion  the current plugin version to migrate up to (e.g. {@code "1.0.5"})
     */
    public static <T> void migrate(@Nonnull T target, @Nonnull String currentVersion, @Nonnull String targetVersion) {
        int[] current = parse(currentVersion);
        int[] target_ = parse(targetVersion);

        if (compare(current, target_) >= 0) return;

        for (Field field : target.getClass().getDeclaredFields()) {
            Since since = field.getAnnotation(Since.class);
            if (since == null) continue;

            int[] sinceVer = parse(since.version());
            if (compare(sinceVer, current) <= 0) continue;   // already applied
            if (compare(sinceVer, target_) > 0) continue;    // beyond target

            field.setAccessible(true);
            try {
                field.set(target, parseValue(field.getType(), since.defaultValue()));
            } catch (Exception e) {
                AdvancedInfo.LOGGER.atSevere().withCause(e)
                    .log("Failed to apply migration default for field '%s' (%s -> %s)",
                        field.getName(), currentVersion, since.version());
            }
        }
    }

    /**
     * Applies the {@link Since#defaultValue()} of every {@link Since}-annotated field on
     * {@code target}, regardless of version. Used by {@link PlayerHudSettings#reset()}.
     */
    public static <T> void resetToDefaults(@Nonnull T target) {
        for (Field field : target.getClass().getDeclaredFields()) {
            Since since = field.getAnnotation(Since.class);
            if (since == null) continue;

            field.setAccessible(true);
            try {
                field.set(target, parseValue(field.getType(), since.defaultValue()));
            } catch (Exception e) {
                AdvancedInfo.LOGGER.atSevere().withCause(e)
                    .log("Failed to apply reset default for field '%s'", field.getName());
            }
        }
    }

    /**
     * Parses a version string into a 3-element {@code int[]} of {@code [major, minor, patch]}.
     * Any segments beyond the third are ignored. Missing segments default to {@code 0}.
     */
    @Nonnull
    static int[] parse(@Nonnull String version) {
        String[] parts = version.split("\\.", -1);
        int[] result = new int[3];
        for (int i = 0; i < 3 && i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Compares two {@code [major, minor, patch]} arrays.
     *
     * @return negative if {@code a < b}, zero if equal, positive if {@code a > b}
     */
    static int compare(@Nonnull int[] a, @Nonnull int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        }
        return 0;
    }

    private static Object parseValue(@Nonnull Class<?> type, @Nonnull String value) {
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == int.class     || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class    || type == Long.class)    return Long.parseLong(value);
        if (type == double.class  || type == Double.class)  return Double.parseDouble(value);
        if (type == float.class   || type == Float.class)   return Float.parseFloat(value);
        if (type == String.class)                           return value;
        if (type == java.time.ZoneId.class)                 return java.time.ZoneId.of(value);
        throw new IllegalArgumentException("Unsupported field type for @Since migration: " + type.getName());
    }
}
