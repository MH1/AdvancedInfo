# Changelog

## Unreleased Changes

- Added `/ai ui` command to open the settings UI page.
- Added `/ai show captions` and `/ai hide captions` commands to toggle the visibility of all captions.
- Bugfix: Resolved an issue where the HUD would not resolve the name of some objects.
- Several minor bugfixes

## v1.0.0-SNAPSHOT (2026-03-01)

- HUD panel with real-time clock, biome, XYZ position, world time, zone/tier, and target info
- Target health bar with `below` and `overlay` display styles and a health-gradient color
- `/advanced-info` (`/ai`) command with `show`, `hide`, `align`, `time`, `timezone`, `healthbar`, `font`, `status`, and `reset` subcommands
- Per-player settings persisted across sessions with automatic migration support
- Optional [MultipleHUD](https://www.curseforge.com/hytale/mods/multiplehud) integration
- HUD auto-hides when the world map is open
