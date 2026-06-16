package com.iamkaf.bonded.api;

import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Public helpers for Bonded item stack state.
 */
public final class BondedItemStacks {
    /**
     * Identifier used for Bonded's temporary over-repair durability modifier.
     */
    public static final Identifier OVER_REPAIR_ID = MaxDamageModifiers.OVER_REPAIR_ID;

    private BondedItemStacks() {
    }

    /**
     * Returns whether Bonded can safely write max-damage modifiers to this stack.
     *
     * @param stack the item stack to inspect
     * @return {@code true} when the stack is damageable and not unbreakable
     */
    public static boolean canModifyMaxDamage(ItemStack stack) {
        return MaxDamageModifiers.canModifyMaxDamage(stack);
    }

    /**
     * Returns the stack's max damage before temporary over-repair is applied.
     *
     * @param stack the item stack to inspect
     * @return the non-temporary max damage, or the stack's current max damage if no modifier data exists
     */
    public static int getBaseMaxDamage(ItemStack stack) {
        return MaxDamageModifiers.getBaseMaxDamage(stack);
    }

    /**
     * Returns an additive max-damage modifier amount.
     *
     * @param stack the item stack to inspect
     * @param id the modifier identifier
     * @return the additive amount, or {@code 0} when the modifier is absent
     */
    public static int getMaxDamageModifier(ItemStack stack, Identifier id) {
        return MaxDamageModifiers.getModifierAmount(stack, id);
    }

    /**
     * Adds or replaces an additive max-damage modifier.
     *
     * <p>Amounts less than or equal to zero remove the modifier.</p>
     *
     * @param stack the item stack to mutate
     * @param id the modifier identifier
     * @param amount the additive max-damage amount
     */
    public static void addOrReplaceMaxDamageModifier(ItemStack stack, Identifier id, int amount) {
        MaxDamageModifiers.addOrReplaceAdditive(stack, id, amount);
    }

    /**
     * Removes a max-damage modifier from the stack.
     *
     * @param stack the item stack to mutate
     * @param id the modifier identifier
     */
    public static void removeMaxDamageModifier(ItemStack stack, Identifier id) {
        MaxDamageModifiers.remove(stack, id);
    }

    /**
     * Returns Bonded's total temporary over-repair amount.
     *
     * @param stack the item stack to inspect
     * @return the over-repair amount, or {@code 0} when absent
     */
    public static int getOverRepairAmount(ItemStack stack) {
        return MaxDamageModifiers.getOverRepairAmount(stack);
    }

    /**
     * Returns whether the stack currently has any temporary over-repair.
     *
     * @param stack the item stack to inspect
     * @return {@code true} when over-repair is present
     */
    public static boolean hasOverRepair(ItemStack stack) {
        return MaxDamageModifiers.hasOverRepair(stack);
    }

    /**
     * Sets Bonded's temporary over-repair amount.
     *
     * <p>Amounts less than or equal to zero clear over-repair.</p>
     *
     * @param stack the item stack to mutate
     * @param amount the over-repair amount
     */
    public static void setOverRepairAmount(ItemStack stack, int amount) {
        MaxDamageModifiers.setOverRepairAmount(stack, amount);
    }

    /**
     * Returns the remaining over-repair after current item damage is considered.
     *
     * @param stack the item stack to inspect
     * @return the usable over-repair still protecting the stack
     */
    public static int getRemainingOverRepair(ItemStack stack) {
        return MaxDamageModifiers.getRemainingOverRepair(stack);
    }

    /**
     * Applies repair using Bonded's over-repair rules.
     *
     * <p>Repair first removes normal item damage. Any excess repair becomes temporary
     * over-repair.</p>
     *
     * @param stack the item stack to mutate
     * @param repairAmount the repair amount in durability points
     * @return {@code true} when the stack's damage or over-repair state changed
     */
    public static boolean repairWithOverRepair(ItemStack stack, int repairAmount) {
        return MaxDamageModifiers.repairWithOverRepair(stack, repairAmount);
    }
}
