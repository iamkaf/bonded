# Bonded

A gear leveling mod for modern Minecraft.

## About

Bonded adds progression and item growth systems for tools, weapons, and armor.
It uses a multiloader setup built on Amber and supports Fabric, Forge, and NeoForge from the same codebase.

## Monorepo Structure

This repository contains Bonded's Minecraft-versioned projects:

```text
bonded/
├── 1.21.11/          # Active Minecraft 1.21.11 project
├── changelog.md      # Shared changelog used by publishing tasks
├── LICENSE
└── README.md
```

Each version directory follows the same layout:

- `common/` - shared code across loaders
- `fabric/` - Fabric implementation
- `forge/` - Forge implementation
- `neoforge/` - NeoForge implementation

## Supported Versions

- 1.21.11 - Active

## Building

Use `just` from the repo root.

```bash
just build 1.21.11
just run 1.21.11 :fabric:runClient
just run 1.21.11 :forge:runClient
just run 1.21.11 :neoforge:runClient
```

Build outputs are written to `<version>/<loader>/build/libs/`.

## Development

- Java 21
- Git
- just

Open the version directory in your IDE:

```bash
idea 1.21.11
```

## Notes

Selected design notes and Amber API request notes are kept at the repo root for reference.
