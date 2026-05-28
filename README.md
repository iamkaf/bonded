# Bonded

A gear leveling mod for modern Minecraft.

## About

Bonded adds progression and item growth systems for tools, weapons, and armor.
It uses a multiloader setup built on Amber and supports Fabric, Forge, and NeoForge from the same codebase.

## Monorepo Structure

This repository contains Bonded's Minecraft-versioned projects:

```text
bonded/
├── 26.1.2/           # Active Minecraft 26.1.2 project
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

- 26.1.2 - Active

## Building

Use `just` from the repo root.

```bash
just build 26.1.2
just run 26.1.2 :fabric:runClient
just run 26.1.2 :forge:runClient
just run 26.1.2 :neoforge:runClient
```

Build outputs are written to `<version>/<loader>/build/libs/`.

## Development

- Java 21
- Git
- just

Open the version directory in your IDE:

```bash
idea 26.1.2
```

## Addon API

Bonded exposes a public addon API under `com.iamkaf.bonded.api` on the active `26.1.2` line.
The API is intended for mods that need to register gear compatibility, inspect or mutate Bonded item
state, react to Bonded progression events, or award Bonded experience from custom gameplay.

### Gear Compatibility

Use `BondedApi` to register upgrade paths, repair materials, and experience caps:

```java
import com.iamkaf.bonded.api.BondedApi;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

TagKey<Item> upgradeMaterial = MyTags.Items.STEEL_INGOTS;

BondedApi.addUpgrade(MyItems.STEEL_PICKAXE.get(), MyItems.MYTHRIL_PICKAXE.get(), upgradeMaterial);
BondedApi.addRepairMaterial(MyItems.STEEL_PICKAXE.get(), Items.IRON_INGOT);
BondedApi.addExperienceCap(MyItems.STEEL_PICKAXE.get(), 250);
```

Available methods:

- `addUpgrade(Item from, Item to, TagKey<Item> material)`: lets the Tool Bench upgrade one item into another once the gear meets Bonded's level requirement. The material is an item tag because the bench accepts any matching stack.
- `addRepairMaterial(Item from, Item material)`: lets the Repair Bench repair the item with that material. Bonded also writes the vanilla `REPAIRABLE` component for these entries.
- `addExperienceCap(Item gear, Integer maxExperience)`: sets the gear's starting max experience. Unknown gear uses Bonded's configured default cap.

The old `com.iamkaf.bonded.api.API` class still delegates to `BondedApi`, but it is deprecated.
New addons should import `BondedApi` directly.

### Item Stack State

Use `BondedItemStacks` when an addon needs to read or change Bonded's max-damage and over-repair state:

```java
import com.iamkaf.bonded.api.BondedItemStacks;
import net.minecraft.resources.Identifier;

if (BondedItemStacks.canModifyMaxDamage(stack)) {
    int baseMaxDamage = BondedItemStacks.getBaseMaxDamage(stack);
    int overRepair = BondedItemStacks.getOverRepairAmount(stack);

    BondedItemStacks.addOrReplaceMaxDamageModifier(
            stack,
            Identifier.fromNamespaceAndPath("examplemod", "reinforced"),
            40
    );
}
```

Available helpers:

- `canModifyMaxDamage(ItemStack stack)`: returns `true` when the stack is damageable and not unbreakable.
- `getBaseMaxDamage(ItemStack stack)`: returns max damage before temporary over-repair is applied.
- `getMaxDamageModifier(ItemStack stack, Identifier id)`: reads an additive max-damage modifier by id.
- `addOrReplaceMaxDamageModifier(ItemStack stack, Identifier id, int amount)`: adds or replaces an additive max-damage modifier. Amounts less than or equal to zero remove it.
- `removeMaxDamageModifier(ItemStack stack, Identifier id)`: removes a modifier by id.
- `getOverRepairAmount(ItemStack stack)`: returns Bonded's total temporary over-repair amount.
- `hasOverRepair(ItemStack stack)`: returns whether the stack currently has over-repair.
- `setOverRepairAmount(ItemStack stack, int amount)`: sets Bonded's temporary over-repair. Amounts less than or equal to zero clear it.
- `getRemainingOverRepair(ItemStack stack)`: returns the over-repair still protecting the stack after current item damage is considered.
- `repairWithOverRepair(ItemStack stack, int repairAmount)`: repairs normal damage first, then stores excess repair as temporary over-repair.

`BondedItemStacks.OVER_REPAIR_ID` is the modifier id Bonded uses for temporary over-repair.
Use your own namespaced id for permanent or addon-owned max-damage modifiers.

### Bond Events

Server-side progression events live in `com.iamkaf.bonded.api.event.BondEvent`.

Available events:

- `ITEM_EXPERIENCE_GAINED`: fired before Bonded applies item experience. Return anything other than `InteractionResult.PASS` to stop Bonded's default experience handling for that gain.
- `ITEM_LEVELED_UP`: fired after an item levels up.
- `MODIFY_REPAIR_AMOUNT`: fired before the Repair Bench applies durability. Return the amount that should be passed to the next listener.
- `ITEM_REPAIRED`: fired after the Repair Bench successfully repairs or over-repairs an item.
- `ITEM_UPGRADED`: fired after the Tool Bench upgrades an item.

Example:

```java
import com.iamkaf.bonded.api.BondedItemStacks;
import com.iamkaf.bonded.api.event.BondEvent;

BondEvent.MODIFY_REPAIR_AMOUNT.register((gear, player, component, material, repairAmount) -> {
    if (material.is(MyTags.Items.PREMIUM_REPAIR_MATERIALS)) {
        return repairAmount * 2;
    }

    return repairAmount;
});

BondEvent.ITEM_REPAIRED.register((gear, player, component, material) -> {
    if (BondedItemStacks.hasOverRepair(gear)) {
        // React to over-repaired gear.
    }
});
```

### Game Events

General hooks live in `com.iamkaf.bonded.api.event.GameEvents`.

Available events:

- `AWARD_ITEM_EXPERIENCE`: lets addons award Bonded experience from custom gameplay.
- `MODIFY_SMITHING_RESULT`: lets addons preserve or modify Bonded state on smithing results.

Example:

```java
import com.iamkaf.bonded.api.event.GameEvents;

GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 5);
```
