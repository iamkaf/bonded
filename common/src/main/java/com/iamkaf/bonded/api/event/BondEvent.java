package com.iamkaf.bonded.api.event;

import com.iamkaf.bonded.component.ItemLevelContainer;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Defines events related to item progression, including experience gain,
 * leveling up, repairing, and upgrading.
 * <p>
 * These events allow mod developers to listen and respond to specific
 * actions that affect item progression in the `Bonded` mod.
 */
public interface BondEvent {

    /**
     * Event triggered when an item gains experience.
     * Listeners can modify or cancel the experience gained.
     */
    Event<ExperienceGained> ITEM_EXPERIENCE_GAINED = EventFactory.createCompoundEventResult();

    /**
     * Event triggered when an item levels up.
     * Listeners can execute additional logic when an item reaches a new level.
     */
    Event<LeveledUp> ITEM_LEVELED_UP = EventFactory.createLoop();

    /**
     * Event triggered when an item is repaired.
     * Listeners can modify the resulting ItemStack.
     */
    Event<Repaired> ITEM_REPAIRED = EventFactory.createLoop();

    /**
     * Event triggered when an item is upgraded.
     * Listeners can modify the resulting ItemStack.
     */
    Event<Upgraded> ITEM_UPGRADED = EventFactory.createLoop();

    /**
     * Functional interface for handling experience gain events.
     */
    interface ExperienceGained {
        /**
         * Handles experience gain for an item.
         *
         * @param gear             The item gaining experience.
         * @param player           The player interacting with the item.
         * @param component        The item level container associated with the item.
         * @param experienceAmount The amount of experience to be gained.
         * @return A {@link CompoundEventResult} containing the modified or final experience amount.
         */
        CompoundEventResult<Integer> experience(ItemStack gear, Player player, ItemLevelContainer component
                , Integer experienceAmount);
    }

    /**
     * Functional interface for handling item leveling up events.
     */
    interface LeveledUp {
        /**
         * Handles logic when an item levels up.
         *
         * @param gear      The item leveling up.
         * @param player    The player interacting with the item.
         * @param component The item level container associated with the item.
         * @param newLevel  The new level of the item.
         */
        void level(ItemStack gear, Player player, ItemLevelContainer component, Integer newLevel);
    }

    /**
     * Functional interface for handling item repair events.
     */
    interface Repaired {
        /**
         * Handles logic when an item is repaired.
         *
         * @param gear      The item being repaired.
         * @param player    The player repairing the item.
         * @param component The item level container associated with the item.
         * @param material  The {@link ItemStack} used as the repair material.
         */
        void repair(ItemStack gear, Player player, ItemLevelContainer component, ItemStack material);
    }

    /**
     * Functional interface for handling item upgrade events.
     */
    interface Upgraded {
        /**
         * Handles logic when an item is upgraded to a new item.
         *
         * @param oldGear   The original item being upgraded.
         * @param newGear   The resulting upgraded item.
         * @param player    The player performing the upgrade.
         * @param component The item level container associated with the item.
         * @param material  The {@link ItemStack} used as the upgrade material.
         */
        void upgrade(ItemStack oldGear, ItemStack newGear, Player player, ItemLevelContainer component,
                ItemStack material);
    }
}
