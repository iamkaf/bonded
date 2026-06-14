package com.iamkaf.bonded.api;

import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Public entry point for registering Bonded gear behavior.
 */
public final class BondedApi {
    private BondedApi() {
    }

    /**
     * Registers an upgrade path for a specific item.
     *
     * @param from the item to be upgraded
     * @param to the item it upgrades into
     * @param material the item tag required to perform the upgrade
     */
    public static void addUpgrade(Item from, Item to, TagKey<Item> material) {
        TierMap.addUpgrade(from, to, material);
    }

    /**
     * Registers an item that can repair the given gear item on the repair bench.
     *
     * @param from the item to be repaired
     * @param material the item used to repair it
     */
    public static void addRepairMaterial(Item from, Item material) {
        TierMap.addRepairMaterial(from, material);
    }

    /**
     * Sets the maximum experience cap for a gear item.
     *
     * @param gear the gear item
     * @param maxExperience the maximum experience value for the item
     */
    public static void addExperienceCap(Item gear, Integer maxExperience) {
        TierMap.addExperienceCap(gear, maxExperience);
    }
}
