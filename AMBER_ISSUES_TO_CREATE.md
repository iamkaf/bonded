# Amber API Requests

This document consolidates the Amber issue drafts that were previously split across `amber-issues/`.
They were written while porting Bonded and describe missing or desirable cross-loader hooks in Amber.

## Requests

1. `ItemEvents.MODIFY_DEFAULT_COMPONENTS`
2. `PlayerEvents.SHIELD_BLOCK`
3. `BlockEvents.IGNITE_BLOCK` and `EntityEvents.IGNITE_ENTITY`
4. Fishing lifecycle events under `PlayerEvents`
5. `EntityEvents.SHEAR`
6. `ItemEvents.SMITHING`

## 1. ItemEvents.MODIFY_DEFAULT_COMPONENTS

### Summary

Add a cross-platform event for modifying default item data components during registration, matching the role of Forge's `ModifyDefaultComponentsEvent` and Fabric's `DefaultItemComponentEvents.MODIFY`.

### Bonded use case

Bonded needs to add the `Repairable` data component to vanilla and modded items so they work with Bonded's repair system.
Without a common hook, this requires loader-specific implementations.

### Proposed API

```java
public interface ItemEvents {
    Event<ModifyDefaultComponents> MODIFY_DEFAULT_COMPONENTS = EventFactory.createArrayBacked(
        ModifyDefaultComponents.class, callbacks -> (context) -> {
            for (ModifyDefaultComponents callback : callbacks) {
                callback.modify(context);
            }
        }
    );

    interface ModifyDefaultComponents {
        void modify(ComponentModificationContext context);
    }
}

public interface ComponentModificationContext {
    void modify(Item item, Consumer<DataComponentMap.Builder> builder);
    Item getItem();
    boolean hasComponent(DataComponentType<?> componentType);
}
```

### Example

```java
ItemEvents.MODIFY_DEFAULT_COMPONENTS.register((context) -> {
    TierMap.getRepairMaterialMap().forEach((item, material) -> {
        context.modify(item, builder -> {
            builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(material.builtInRegistryHolder()))
            );
        });
    });
});
```

### Notes

- NeoForge target: `ModifyDefaultComponentsEvent`
- Fabric target: `DefaultItemComponentEvents.MODIFY`
- Fires during item bootstrapping, not at runtime

## 2. PlayerEvents.SHIELD_BLOCK

### Summary

Add a cross-platform event for successful shield blocks with the player, the shield used, the blocked damage, and the damage source.

### Bonded use case

Bonded levels shields when they block damage.
Today that requires less-accurate Fabric workarounds and separate Forge-style handling.

### Proposed API

```java
public interface PlayerEvents {
    Event<ShieldBlock> SHIELD_BLOCK = EventFactory.createArrayBacked(
        ShieldBlock.class, callbacks -> (player, shield, blockedDamage, source) -> {
            for (ShieldBlock callback : callbacks) {
                callback.block(player, shield, blockedDamage, source);
            }
        }
    );

    interface ShieldBlock {
        void block(ServerPlayer player, ItemStack shield, float blockedDamage, DamageSource source);
    }
}
```

### Example

```java
PlayerEvents.SHIELD_BLOCK.register((player, shield, blockedDamage, source) -> {
    int experience = (int) ((blockedDamage + 1)
        * Bonded.CONFIG.armorDamageTakenExperienceGainedMultiplier.get());

    if (source.isExplosion()) {
        experience *= 2;
    }

    emitProgressEvents(shield, player, experience);
});
```

### Notes

- NeoForge target: `LivingShieldBlockEvent`
- Fabric fallback: post-damage hooks plus manual shield detection
- Should fire server-side after a successful block is determined

## 3. Block and Entity Ignite Events

### Summary

Add one event for block ignition and one for entity ignition, covering flint and steel, fire charges, lightning, lava, and related sources.

### Bonded use case

Bonded awards flint-and-steel experience when igniting blocks or entities.
Right now that is implemented through multiple mixins.

### Proposed API

```java
public interface BlockEvents {
    Event<IgniteBlock> IGNITE_BLOCK = EventFactory.createArrayBacked(
        IgniteBlock.class, callbacks -> (context) -> {
            for (IgniteBlock callback : callbacks) {
                callback.ignite(context);
            }
        }
    );

    interface IgniteBlock {
        void ignite(BlockIgnitionContext context);
    }
}

public interface EntityEvents {
    Event<IgniteEntity> IGNITE_ENTITY = EventFactory.createArrayBacked(
        IgniteEntity.class, callbacks -> (context) -> {
            for (IgniteEntity callback : callbacks) {
                callback.ignite(context);
            }
        }
    );

    interface IgniteEntity {
        void ignite(EntityIgnitionContext context);
    }
}
```

### Contexts and source enums

```java
public interface BlockIgnitionContext {
    @Nullable Player getPlayer();
    ItemStack getIgnitionItem();
    BlockPos getIgnitedPosition();
    BlockState getIgnitedBlockState();
    Level getLevel();
    BlockIgnitionSource getSource();
}

public interface EntityIgnitionContext {
    @Nullable Player getPlayer();
    ItemStack getIgnitionItem();
    Entity getIgnitedEntity();
    Level getLevel();
    EntityIgnitionSource getSource();
}

public enum BlockIgnitionSource {
    FLINT_AND_STEEL,
    FIRE_CHARGE,
    FIRE_ARROW,
    LAVA_SPREAD,
    LIGHTNING,
    ENVIRONMENTAL
}

public enum EntityIgnitionSource {
    FLINT_AND_STEEL,
    FIRE_CHARGE,
    FIRE_ARROW,
    LAVA,
    LIGHTNING,
    ENVIRONMENTAL,
    FIRE_SPREAD
}
```

### Notes

- Block ignition needs block position/state context
- Entity ignition needs entity context
- Should fire before final ignition so mods can intercept or react consistently

## 4. Fishing Lifecycle Events

### Summary

Add a complete fishing lifecycle API covering cast, bite, success, and stop, with typed result enums instead of raw integer return codes.

### Bonded use case

Bonded awards fishing rod experience on successful catches.
The current approach depends on a `FishingHook.retrieve` mixin and magic numeric result codes.

### Proposed API

```java
public interface PlayerEvents {
    Event<FishingStart> FISHING_START = EventFactory.createArrayBacked(
        FishingStart.class, callbacks -> (context) -> {
            for (FishingStart callback : callbacks) {
                callback.start(context);
            }
        }
    );

    Event<FishingBite> FISHING_BITE = EventFactory.createArrayBacked(
        FishingBite.class, callbacks -> (context) -> {
            for (FishingBite callback : callbacks) {
                callback.bite(context);
            }
        }
    );

    Event<FishingSuccess> FISHING_SUCCESS = EventFactory.createArrayBacked(
        FishingSuccess.class, callbacks -> (context) -> {
            for (FishingSuccess callback : callbacks) {
                callback.success(context);
            }
        }
    );

    Event<FishingStop> FISHING_STOP = EventFactory.createArrayBacked(
        FishingStop.class, callbacks -> (context) -> {
            for (FishingStop callback : callbacks) {
                callback.stop(context);
            }
        }
    );
}
```

### Contexts and enums

```java
public interface FishingContext {
    ServerPlayer getPlayer();
    ItemStack getFishingRod();
    FishingHook getFishingHook();
    Level getLevel();
}

public interface FishingSuccessContext extends FishingContext {
    FishingResult getResult();
    ItemStack getCaughtItem();
    @Nullable Entity getCaughtEntity();
    boolean isTreasure();
    int getExperienceValue();
}

public interface FishingStopContext extends FishingContext {
    FishingStopReason getReason();
    boolean wasSuccessful();
}

public enum FishingResult {
    FISH,
    TREASURE,
    JUNK,
    ENTITY,
    NOTHING
}

public enum FishingStopReason {
    REELED_IN,
    ROD_BROKE,
    HOOK_ESCAPED,
    TIME_OUT,
    PLAYER_LOGOUT
}
```

### Notes

- Eliminates magic-number switch logic
- Covers the full server-side fishing lifecycle
- Gives mods a typed way to react to catches, misses, and interruptions

## 5. EntityEvents.SHEAR

### Summary

Add a cross-platform entity shearing event for sheep, mooshrooms, snow golems, and similar targets.

### Bonded use case

Bonded awards experience to shears when players shear entities.
That is currently implemented with loader-specific mixins.

### Proposed API

```java
public interface EntityEvents {
    Event<Shear> SHEAR = EventFactory.createArrayBacked(
        Shear.class, callbacks -> (context) -> {
            for (Shear callback : callbacks) {
                callback.shear(context);
            }
        }
    );

    interface Shear {
        void shear(ShearingContext context);
    }
}

public interface ShearingContext {
    @Nullable ServerPlayer getPlayer();
    ItemStack getShears();
    Entity getTargetEntity();
    Level getLevel();
    ShearTarget getTargetType();
    List<ItemStack> getDrops();
    boolean wasSuccessful();
    ShearSource getSource();
}
```

### Target/source enums

```java
public enum ShearTarget {
    SHEEP,
    MUSHROOM_COW,
    SNOW_GOLEM,
    BEE_NEST,
    BEEHIVE,
    OTHER
}

public enum ShearSource {
    PLAYER,
    DISPENSER,
    AUTOMATION,
    UNKNOWN
}
```

### Notes

- Focused on entity shearing, not block shearing
- Exposes drops before spawn so mods can adjust rewards
- Gives a stable hook for progression systems like Bonded

## 6. ItemEvents.SMITHING

### Summary

Add smithing lifecycle events covering both valid smithing setup and result completion.

### Bonded use case

Bonded currently uses a custom smithing-result event and a mixin into `SmithingMenu`.
A richer Amber API would replace that with a shared cross-loader hook.

### Proposed API

```java
public interface ItemEvents {
    Event<SmithingStart> SMITHING_START = EventFactory.createArrayBacked(
        SmithingStart.class, callbacks -> (context) -> {
            for (SmithingStart callback : callbacks) {
                callback.start(context);
            }
        }
    );

    Event<SmithingComplete> SMITHING_COMPLETE = EventFactory.createArrayBacked(
        SmithingComplete.class, callbacks -> (context) -> {
            for (SmithingComplete callback : callbacks) {
                callback.complete(context);
            }
        }
    );
}

public interface SmithingContext {
    ServerPlayer getPlayer();
    SmithingMenu getSmithingMenu();
    ItemStack getTemplate();
    ItemStack getBaseItem();
    ItemStack getAddition();
    Level getLevel();
}

public interface SmithingCompleteContext extends SmithingContext {
    ItemStack getResult();
    List<ItemStack> getConsumedItems();
    boolean wasSuccessful();
    SmithingType getSmithingType();
    Recipe<?> getRecipe();
}
```

### Smithing type enum

```java
public enum SmithingType {
    TRIMMING,
    NETHERITE_UPGRADE,
    TRANSFORMATION,
    CUSTOM
}
```

### Notes

- `SMITHING_START`: when valid inputs are present
- `SMITHING_COMPLETE`: when the player takes the result
- Gives mods full access to inputs, output, recipe, and smithing category
