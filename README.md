# Bonded

A gear leveling mod for Fabric, Forge, and NeoForge.

## About

Bonded makes tools, weapons, and armor grow with use. Gear earns Bond from normal play, gains bonuses as it
levels, and can be repaired, over-repaired, or upgraded at dedicated benches.

Standard gear from other mods works through Minecraft's item tags. Bonded also ships detailed Gear Rule
profiles for mods with nonstandard equipment or upgrade paths, and server owners can add their own rules
without making an addon.

## Requirements

- [Amber](https://modrinth.com/mod/amber)
- [Konfig](https://modrinth.com/mod/konfig)
- Fabric API on Fabric

Fabric players can optionally install [Mod Menu](https://modrinth.com/mod/modmenu) to open Bonded's
configuration from the Mods screen.

## Gear rules

Gear Rules decide how Bonded treats an item or item tag:

- gear type and experience cap
- repair behavior and repair material
- Tool Bench upgrade target and ingredient
- whether the rule is enabled

Bonded includes profiles for vanilla and Bonded gear, Basic Weapons, Advanced Netherite, Immersive Armors,
BetterEnd, BetterNether, and Arcane Armory. Shipped rules are read-only. Copy one into User Overrides to
change it, or add a rule for another item or item tag. Valid changes save automatically and apply live.

If a configured item belongs to a mod that is later removed, Bonded keeps the rule dormant instead of
deleting it. The rule becomes active again when the item returns.

In multiplayer, the server owns common settings and Gear Rules. Connected players see the effective server
rules, and operators can edit them when the server permits remote config changes.

Configuration lives in `bonded-common.toml`, `bonded-client.toml`, and `gear-rules.toml`.

## Optional integrations

- Liteminer: vein-mined blocks award diminishing Bonded experience and the Liteminer HUD previews the total gain.
- Patchouli: adds a craftable Bonded Field Guide covering progression, workstation recipes, and augments. Its recipe is absent when Patchouli is not installed.

## Repository structure

This repository contains Bonded's Minecraft-versioned projects:

```text
bonded/
├── common/           # Shared code and resources
├── fabric/           # Fabric loader code
├── forge/            # Forge loader code
├── neoforge/         # NeoForge loader code
├── versions/         # Per-Minecraft-version configuration and overlays
├── changelog.md      # Shared changelog used by publishing tasks
├── LICENSE
└── README.md
```

## Building

Use `just` from the repo root.

```bash
just build 26.2
just run 26.2 :fabric:runClient
just run 26.2 :forge:runClient
just run 26.2 :neoforge:runClient
```

Build outputs are written to `<version>/<loader>/build/libs/`.

## Development

- Java 21
- Git
- just

Open the version directory in your IDE:

```bash
idea 26.2
```

## Addon API

Bonded exposes a public addon API under `com.iamkaf.bonded.api`.
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

Register addon defaults during mod initialization. Server-owned Gear Rule overrides take precedence when Bonded
resolves rules for a world or reload.

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

### Augments

Augments are independently progressing abilities stored on item stacks. Register an `Augment` during mod
initialization, then award progress from the gameplay hooks that belong to your addon:

```java
import com.iamkaf.bonded.api.augment.Augment;
import com.iamkaf.bonded.api.augment.AugmentApi;
import net.minecraft.resources.Identifier;

Augment echoing = AugmentApi.register(new Augment(
        Identifier.fromNamespaceAndPath("examplemod", "echoing"),
        "augment.examplemod.echoing",
        250,
        stack -> stack.is(MyTags.Items.ECHOING_GEAR)
));

AugmentApi.addProgress(player, stack, echoing, 1);
if (AugmentApi.isActive(stack, echoing)) {
    // Apply the active augment's behavior.
}
```

Progress is clamped between zero and the augment's activation requirement. Unknown augment ids remain stored
when an addon is temporarily absent. `AugmentEvents` exposes ordered hooks for modifying gains, observing progress,
and reacting to activation or deactivation. Mutations are server-only and return an `AugmentProgressResult` with the
exact previous value, current value, and transition status.
