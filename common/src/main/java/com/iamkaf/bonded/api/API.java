package com.iamkaf.bonded.api;

import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Provides public methods to interact with the `TierMap` registry for adding
 * gear upgrades, repair materials, and experience caps.
 * <p>
 * This class serves as an abstraction layer for mod developers who wish to
 * extend or customize gear behavior in the `Bonded` mod.
 */
public class API {

    /**
     * Registers an upgrade path for a specific item, including the material
     * required for the upgrade.
     *
     * @param from     The item to be upgraded.
     * @param to       The item it upgrades into.
     * @param material The {@link Ingredient} required to perform the upgrade.
     */
    public static void addUpgrade(Item from, Item to, Ingredient material) {
        TierMap.addUpgrade(from, to, material);
    }

    /**
     * Sets the material required to repair a specific item.
     *
     * @param from     The item to be repaired.
     * @param material The {@link Ingredient} used for repairing the item.
     */
    public static void addRepairMaterial(Item from, Ingredient material) {
        TierMap.addRepairMaterial(from, material);
    }

    /**
     * Defines the maximum experience cap for a specific item.
     * <p>
     * Once the experience cap is reached, the item may no longer accumulate
     * experience unless overridden.
     *
     * @param gear          The item for which the experience cap is being set.
     * @param maxExperience The maximum experience value for the item.
     */
    public static void addExperienceCap(Item gear, Integer maxExperience) {
        TierMap.addExperienceCap(gear, maxExperience);
    }
}
