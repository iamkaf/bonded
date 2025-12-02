# Bonded

A gear leveling mod for modern Minecraft.

This repository provides a starting point for creating Minecraft mods that target Fabric, Forge and NeoForge from the same codebase.
It is adapted from [jaredlll08's MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) and stripped down to a minimal starting point.

## Directory layout

- `common/` contains code shared between all loaders.
- `fabric/`, `forge/` and `neoforge/` contain loader specific entry points and build logic.
- `buildSrc/` holds the Gradle scripts that wire everything together.

Feel free to expand upon this structure to suit the needs of your own mods.
