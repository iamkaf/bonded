# Architectury Migration Report

This report analyzes all `dev.architectury` imports in the Bonded mod and their usage patterns to prepare for migration away from Architectury.

## Summary of Architectury Usage

The codebase uses Architectury primarily for:
- **Event handling** (6 imports)
- **Registry management** (7 imports)
- **Creative tab registration** (1 import)

## Detailed Analysis

### 1. Event System Usage

**Files affected:**
- `GameplayHooks.java` (lines 16-21)
- `GearManager.java` (line 8)
- `BondedClient.java` (line 4)
- `BondEvent.java` (lines 4-6)
- `GameEvents.java` (lines 3-4)

**Imports and functionality:**
- `CompoundEventResult` - Used for event results that can carry data and interrupt evaluation
- `EventResult` - Used for simple event pass/interrupt results
- `Event<T>` - Event interface for creating custom events
- `EventFactory` - Factory for creating events of different types
- `BlockEvent.BREAK` - Hook for block breaking events
- `EntityEvent.LIVING_HURT` - Hook for entity damage events
- `PlayerEvent.CRAFT_ITEM`, `PlayerEvent.PICKUP_ITEM_POST` - Player interaction events
- `LifecycleEvent.SERVER_LEVEL_LOAD` - Server lifecycle events
- `ClientGuiEvent.RENDER_HUD` - Client-side HUD rendering

**Key usage patterns:**
- Compound events for modifying experience values (`BondEvent.ITEM_EXPERIENCE_GAINED`)
- Loop events for multiple listeners (`BondEvent.ITEM_LEVELED_UP`, `GameEvents.SHIELD_BLOCK`)
- Standard Minecraft event hooks for gameplay integration

### 2. Registry System Usage

**Files affected:**
- `Sounds.java` (line 4)
- `Blocks.java` (lines 6-7)
- `DataComponents.java` (lines 6-7)
- `CreativeModeTabs.java` (lines 4-6)

**Imports and functionality:**
- `DeferredRegister<T>` - Deferred registration system for Minecraft registries
- `DeferredSupplier<T>` - Supplier wrapper for deferred registry entries
- `RegistrySupplier<T>` - Supplier for registered objects
- `CreativeTabRegistry` - Helper for creative mode tab creation

**Registry registrations:**
- Sound events (2 sounds: `ITEM_LEVEL`, `ITEM_MAX_LEVEL`)
- Blocks (2 blocks: `TOOL_BENCH`, `REPAIR_BENCH`)
- Data components (2 components: `ITEM_LEVEL_CONTAINER`, `APPLIED_BONUSES_CONTAINER`)
- Creative mode tabs (1 tab: `BONDED`)

## Migration Strategy Recommendations

### 1. Event System Migration
**Replace with:** NeoForge's event system or Fabric's event system

**Approach:**
- `CompoundEventResult` → Use event cancellation and data modification patterns specific to the target mod loader
- `EventFactory.createCompoundEventResult()` → Create custom event classes that extend appropriate mod loader event base
- `EventFactory.createLoop()` → Use standard event listener registration patterns
- Built-in events → Use mod loader-specific equivalents (NeoForge: `@SubscribeEvent`, Fabric: `EventCallback`)

### 2. Registry System Migration
**Replace with:** Mod loader-specific registration systems

**For NeoForge:**
- `DeferredRegister` → Use `@RegisterEvent` annotations or `DeferredRegister` from NeoForge
- Registry suppliers → Use `RegistryObject<T>` or `Holder<T>`

**For Fabric:**
- `DeferredRegister` → Use direct registration in entrypoints or `RegistryHelper`
- Suppliers → Use `Identifier`-based lookup or custom holder system

### 3. Creative Tab Migration
**NeoForge:** Use `BuildCreativeModeTabContentsEvent` or `DeferredRegister<CreativeModeTab>`
**Fabric:** Use `FabricCreativeModeTabRegistry.register()`

## Critical Considerations

1. **Platform-specific code** - Will need conditional loading or separate implementations
2. **Event compatibility** - Custom events (`BondEvent`, `GameEvents`) will need complete reimplementation
3. **Timing differences** - Registration and event firing timing may differ between mod loaders
4. **API changes** - Some event parameters or return types may differ

## Files Requiring Major Changes

1. **`GameplayHooks.java`** - Heavy event usage, will need complete event system migration
2. **`BondEvent.java`** - Custom event system, requires full rewrite for target mod loader
3. **`GameEvents.java`** - Custom events, requires full rewrite
4. **Registry files** (`Sounds.java`, `Blocks.java`, `DataComponents.java`, `CreativeModeTabs.java`) - Registry system migration

## Migration Priority

**High Priority:**
- Event system (core gameplay functionality)
- Registry system (mod initialization)

**Medium Priority:**
- Creative tab registration (UI/feature)

**Low Priority:**
- Utility classes and helper methods