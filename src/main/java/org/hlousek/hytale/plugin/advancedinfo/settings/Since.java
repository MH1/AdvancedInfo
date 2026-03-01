package org.hlousek.hytale.plugin.advancedinfo.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a settings field as having been introduced in a specific settings version.
 * When an existing player's saved data pre-dates this version, {@link SettingsMigrator}
 * will apply the given {@link #defaultValue()} instead of leaving the field at its
 * Java field initializer value.
 *
 * <p>Version format: {@code "major.minor.patch"} - the 4th segment and beyond are ignored.
 * Examples: {@code "1.0.0"}, {@code "1.2.5"}.
 *
 * <p>Supported field types: {@code boolean}, {@code int}, {@code long},
 * {@code double}, {@code float}, {@code String}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Since {

    /** The settings version in which this field was introduced, e.g. {@code "1.0.5"}. */
    String version();

    /** String representation of the default value for existing players upgrading past this version. */
    String defaultValue();
}
