package org.hlousek.hytale.plugin.advancedinfo;

/**
 * Permission nodes of the plugin. Subcommand nodes are derived by the server from {@link #COMMAND}
 * ({@code advancedinfo.command.ui}, {@code advancedinfo.command.show.healthbar} …).
 *
 * <p>Every node here is granted to the {@code hytale:Adventurer} group by default, so all players have
 * it until an admin denies it with a {@code -<node>} entry for a user or a group.
 */
public final class AdvancedInfoPermissions {

    /**
     * The {@code /ai} command. Set explicitly because the generated node would repeat the manifest
     * group and name ({@code advancedinfo.advancedinfo.command.advanced-info}).
     */
    public static final String COMMAND = "advancedinfo.command";

    /** Seeing the target's health: the numbers next to its name and the health bar. */
    public static final String HEALTH = "advancedinfo.health";

    private AdvancedInfoPermissions() {}
}
