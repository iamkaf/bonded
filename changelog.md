# Bonded Changelog

All notable changes to Bonded will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 5.2.0

### Added

- Added Critical Bonds: any experience gain has a small chance to triple.

  ![A Critical Bond on a Diamond Pickaxe after mining iron ore](https://i.kaf.sh/i/5d88f0ba-8780-4429-8021-550e3cf6fc23.png)

- Added a Modonomicon version of the Field Guide, crafted from a book and quill and Scrap.
- Added built-in Gear Rules for Weapons Expanded.

### Changed

- Redesigned the Tool Bench and Repair Bench.

  ![Two Tool Benches and a Repair Bench](https://i.kaf.sh/i/d5a4bc17-3a9a-42ff-9352-ada48f0d0055.png)

- The Tool Bench has a rainbow inlay as a nod to the original bench.
- Requires Konfig 0.10.1 or newer.
- Gear Rules now say which half of an upgrade is missing.

### Fixed

- Fixed client settings sometimes falling back to their defaults on Forge and NeoForge.
- Fixed Gear Rules upgrades that could not be entered with an older Konfig.
- Fixed a Gear Rule turning off entirely when its upgrade target is missing. Only the upgrade is skipped now.

## 5.1.0

### Added

- Gear Rules can now use an item or an item tag as an upgrade material. Enter an item ID directly, or prefix a tag ID with `#` to get tag suggestions and previews.

### Fixed

- Axes now gain Bonded experience from hitting mobs.

## 5.0.0

### Added

- BREAKING: Gear compatibility is now managed through the new Gear Rules config screen. Server owners can configure which items and tags work with Bonded, including their gear type, experience cap, repair material, and upgrade path. Changes apply immediately and sync to players.
  - Includes built-in support for Advanced Netherite, Arcane Armory, Basic Weapons, BetterEnd, BetterNether, and Immersive Armors.
- Added augments, a new progression system that gives individual pieces of gear their own trainable abilities.
- Added the Cake Destroyer augment. Train it by breaking cakes, then bonded tools and weapons can make enemies drop sugar.
- Added the Oceanic augment. Train it by wearing bonded leggings underwater, then move freely while swimming.
- Added `/bonded augment` commands and an addon API for creating and progressing custom augments.
- Added an optional Patchouli Field Guide covering Bonded recipes, progression, and augments. Craft it with a book and Scrap when Patchouli is installed.

### Changed

- Ported to Minecraft 26.3.
- Added Liteminer integration. Bonded tools gain reduced experience from extra blocks mined by Liteminer, and the HUD shows the total in yellow.

## 4.2.0

### Added

- Added Tempered Gold tools and armor, created by upgrading fully leveled gold gear at a tool bench.
- Nether Gold Ore can now drop Tempered Gold Nuggets when mined with the correct tool without Silk Touch.
- Added German, Brazilian Portuguese, and Simplified Chinese translations.

### Fixed

- Fixed Bonded gear not gaining the correct experience when mining ores.
- Fixed the bench HUD not recognizing tagged gear registered by addons.

## 4.1.0

### Added

- Repair benches can now repair bows and crossbows with string.

### Fixed

- Fixed a crash that could happen when monsters spawned with innate Bonded gear during threaded chunk loading.

## 4.0.1

### Changed

- Backported Bonded 4.0.1 to Minecraft 1.21.11, 26.1, 26.1.1, and 26.1.2.
- BREAKING: Bonded now requires Konfig on all supported Minecraft 1.21.11+ files.
  - Get it from [Modrinth](https://modrinth.com/mod/konfig) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/konfig)

### Fixed

- Fixed non-damageable body armor, such as horse armor, being treated as Bonded armor when a tag or datapack included it.
- Fixed stale Bonded durability data being preserved on items that no longer have valid durability.

## 3.1.1

### Added

- 1.21.11: Repair benches can now over-repair gear. Extra repair becomes temporary durability, shown as a pink bar on the item.
- 1.21.11: Added Scrap, a general repair material for the repair bench. Scrap can appear in chest loot, drop rarely from armored enemies, drop from broken bonded gear, and appear as a byproduct of upgrading gear.
- 1.21.11: Tool benches and repair benches can now use matching items from adjacent chests and barrels.
- 1.21.11: Gear found in chest loot or carried by monsters can now start with an innate bond level.
- 1.21.11: Added config options for innate loot bond.
- 1.21.11: Added public addon APIs for registering Bonded gear behavior and reading or changing Bonded item stack state.
- 1.21.11: Added advancements for bonding, upgrading, over-repairing, and finding or using Scrap.

### Changed

- 1.21.11: Backported Bonded 3.1.1.
- 1.21.11: Item level-ups no longer automatically repair gear. Use over-repairing instead.
- 1.21.11: The old `API` addon entry point is deprecated. Addons should use `BondedApi`.

### Fixed

- Fixed non-damageable body armor, such as horse armor, being treated as Bonded armor when a tag or datapack included it.
- Fixed stale Bonded durability data being preserved on items that no longer have valid durability.
- 1.21.11: Repair and upgrade addon events now fire from the benches.

## 4.0.0

### Added

- Added Konfig-powered configuration screens for Minecraft 26.2.
- Added client options for Bonded item tooltips, progression sounds, the repair or upgrade bench HUD, and the over-repair bar color.

### Changed

- Ported to Minecraft 26.2.
- BREAKING: Bonded now requires Konfig for configuration support.
  - Get it from [Modrinth](https://modrinth.com/mod/konfig) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/konfig)

### Fixed

- Fixed a crash that could happen when blocking damage with some shields.
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
