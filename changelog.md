# Bonded Changelog

All notable changes to Bonded will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 4.0.0

### Added

- Added Konfig-powered configuration screens for Minecraft 26.2.
- Added client options for Bonded item tooltips, progression sounds, the repair or upgrade bench HUD, and the over-repair bar color.

### Changed

- Ported to Minecraft 26.2.
- BREAKING: Bonded now requires Konfig for configuration support.
  - Get it from [Modrinth](https://modrinth.com/mod/konfig) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/konfig)

### Fixed

- Fixed Bonded's mod icon.
- Fixed level-up and max-level sounds playing for nearby players.

## 3.1.0

### Added
- Repair benches can now over-repair gear. Extra repair becomes temporary durability, shown as a pink bar on the item.
- Added Scrap, a general repair material for the repair bench. Scrap can appear in chest loot, drop rarely from armored enemies, drop from broken bonded gear, and appear as a byproduct of upgrading gear.
- Tool benches and repair benches can now use matching items from adjacent chests and barrels.
- Gear found in chest loot or carried by monsters can now start with an innate bond level.
- Added config options for innate loot bond.
- Added public addon APIs for registering Bonded gear behavior and reading or changing Bonded item stack state.
- Added advancements for bonding, upgrading, over-repairing, and finding or using Scrap.

### Changed
- The old `API` addon entry point is deprecated. Addons should use `BondedApi`.

### Removed
- Item level-ups no longer automatically repair gear. Use over-repairing instead.

### Fixed
- Repair and upgrade addon events now fire from the benches.

## 3.0.0

### Added
- Minecraft 26.1 support

## 2.0.0+1.21.11

### Added
- Port to Minecraft 1.21.11
- Added `/bonded experience` and `/bonded xp` commands
- Added automatic migration for old Bonded attribute modifiers

### Changed
- Moved to the new multiloader monorepo and template
- Added Forge support
- Cleaned up how Bonded attribute bonuses stack on gear

### Fixed
- Fixed missing bench models, items, and sound subtitles
- Fixed duplicate Bonded bonus modifiers on items
- Fixed silent repair and upgrade bench failures when those features are disabled
- Fixed shovels not gaining XP when turning grass blocks into path blocks

## 1.4.1+1.21.7

### Fixed
- Fixed a crash when trying to upgrade an item but the target upgrade doesn't have an upgrade material
- Added a missing mod to the dependency list

## 1.4.0+1.21.7

### Added
- Added a HUD for the tool and repair benches
- Added custom sounds for item level ups
- Improved some feedback messages

### Fixed
- Fixed a bug that prevented shears from getting experience
- Fixed a bug where the tool bench would drop the repair bench when broken
- Fixed hand motion on item pickup
- Added missing block translations

### Changed
- Balancing changes
- Improved mod compatibility
- Some repair bench adjustments
- Fixed inconsistent durability bonuses

## 1.3.0+1.21.4

### Added
- Port to Minecraft 1.21.4

## Types of changes
- `Added` for new features.
- `Changed` for changes in existing functionality.
- `Deprecated` for soon-to-be removed features.
- `Removed` for now removed features.
- `Fixed` for any bug fixes.
- `Security` in case of vulnerabilities.
