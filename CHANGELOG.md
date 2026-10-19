# Changelog

## v1.3.0 (2026-10-19)

- New storage panel: while looking at a storage block (chest, barrel, …) it lists the contents as item icons with total counts, largest first. It has its own alignment (shown below the info panel when both use the same one), is shown by default where the info panel is, and can be toggled with `/ai show storage` / `/ai hide storage`.
- With SimpleClaims installed, the storage panel only shows contents of storage blocks the player may open in claimed chunks.
- Added `/ai storage align <h> <v>`, `/ai storage rows <n>`, `/ai storage size <n>` (size of the item icons and counts, independent of the font size) and `/ai storage outline <on|off>` (outline around the counts).
- Added the permission `advancedinfo.storage`, granted by default.
- The settings UI is split into the tabs Info, Storage and Display; SAVE and RESET DEFAULTS stay visible on every tab.

## v1.2.0 (2026-10-18)

- Added permissions `advancedinfo.command` (with `advancedinfo.command.<subcommand>`) and `advancedinfo.health`, both granted by default. Server admins can deny `advancedinfo.health` per player or for everyone; the health numbers, the health bar and its settings are then hidden.
- The world time row can show the day of the year (default), the total number of days since the world started, or no day at all (`/ai day <none|year|total>`, dropdown in the settings UI).
- Bugfix: Players without operator rights can use the `/ai` command again; it is now granted to every player by default and admins can revoke it per player or group.
- Bugfix: Invisible spawn markers, spawn beacons and patrol path markers are no longer picked up as the target (they showed up as raw object data, e.g. in the temple); the block behind them is described instead.
- Bugfix: Entities whose name is an untranslated message key no longer show raw object data as the target name; the NPC role or model name is used instead.

## v1.1.0 (2026-10-17)

- Requires Hytale server 0.7.x.
- The panel is registered through the server's native multi-HUD support (available since Hytale 0.5), so it works alongside other mods' HUDs out of the box. The optional [MultipleHUD](https://www.curseforge.com/hytale/mods/multiplehud) integration is therefore no longer needed and was removed.
- HUD layout is rendered at runtime from the player's settings; font size and padding scale together and changing them no longer rebuilds the HUD.
- Added configurable panel background opacity (`/ai opacity <percent>`, slider in the settings UI). The panel background is now a flat dark color instead of the tooltip texture.
- Settings UI: sliders for font size and background opacity.
- Added `/ai ui` command to open the settings UI page.
- Added `/ai show captions` and `/ai hide captions` commands to toggle the visibility of all captions.
- Targeted crafting benches show their tier and teleporters their destination warp in brackets, e.g. `Workbench [Tier 2]`.
- Block targeting works for all block types again, including plants and crops.
- HUD data is refreshed 5 times per second instead of every server tick, reducing server load with many players.
- Zone and block names are translated using the player's client language.
- Bugfix: Resolved an issue where the HUD would not resolve the name of some objects.
- Bugfix: An unknown health bar style now falls back to `overlay` (the default) instead of `below`.

## v1.0.0-SNAPSHOT (2026-03-01)

- HUD panel with real-time clock, biome, XYZ position, world time, zone/tier, and target info
- Target health bar with `below` and `overlay` display styles and a health-gradient color
- `/advanced-info` (`/ai`) command with `show`, `hide`, `align`, `time`, `timezone`, `healthbar`, `font`, `status`, and `reset` subcommands
- Per-player settings persisted across sessions with automatic migration support
- Optional [MultipleHUD](https://www.curseforge.com/hytale/mods/multiplehud) integration
- HUD auto-hides when the world map is open
