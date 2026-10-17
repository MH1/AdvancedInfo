# Changelog

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
