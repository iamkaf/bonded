package com.iamkaf.bonded.util;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.MaxDamageModifiersComponent;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public final class MaxDamageModifiers {
    public static final Identifier OVER_REPAIR_ID = Bonded.resource("over_repair");

    private MaxDamageModifiers() {
    }

    public static MaxDamageModifiersComponent getOrCreate(ItemStack stack) {
        MaxDamageModifiersComponent component = stack.get(DataComponents.MAX_DAMAGE_MODIFIERS.get());
        if (component != null) {
            return component;
        }

        return MaxDamageModifiersComponent.make(stack.getMaxDamage());
    }

    public static int getBaseMaxDamage(ItemStack stack) {
        MaxDamageModifiersComponent component = stack.get(DataComponents.MAX_DAMAGE_MODIFIERS.get());
        if (component == null) {
            return stack.getMaxDamage();
        }

        return component.baseMaxDamage() + component.additiveTotalExcept(OVER_REPAIR_ID);
    }

    public static int getIntrinsicMaxDamage(ItemStack stack) {
        return getOrCreate(stack).baseMaxDamage();
    }

    public static int getModifierAmount(ItemStack stack, Identifier id) {
        return getOrCreate(stack)
                .get(id)
                .filter(modifier -> modifier.operation() == AttributeModifier.Operation.ADD_VALUE)
                .map(modifier -> Math.max(0, (int) Math.round(modifier.amount())))
                .orElse(0);
    }

    public static int getOverRepairAmount(ItemStack stack) {
        return getModifierAmount(stack, OVER_REPAIR_ID);
    }

    public static boolean hasOverRepair(ItemStack stack) {
        return getOverRepairAmount(stack) > 0;
    }

    public static int getMaxOverRepairAmount(ItemStack stack) {
        return getBaseMaxDamage(stack);
    }

    public static boolean canRepairWithOverRepair(ItemStack stack) {
        return canModifyMaxDamage(stack)
                && (getStoredDamage(stack) > 0 || getOverRepairAmount(stack) < getMaxOverRepairAmount(stack));
    }

    public static void addOrReplaceAdditive(ItemStack stack, Identifier id, int amount) {
        if (!canModifyMaxDamage(stack)) {
            return;
        }

        MaxDamageModifiersComponent component = getOrCreate(stack);
        if (amount <= 0) {
            remove(stack, id);
            return;
        }

        setAndMaterialize(
                stack,
                component.withModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE))
        );
    }

    public static void remove(ItemStack stack, Identifier id) {
        MaxDamageModifiersComponent component = stack.get(DataComponents.MAX_DAMAGE_MODIFIERS.get());
        if (component == null) {
            return;
        }

        setAndMaterialize(stack, component.withoutModifier(id));
    }

    public static void setOverRepairAmount(ItemStack stack, int amount) {
        addOrReplaceAdditive(stack, OVER_REPAIR_ID, amount);
    }

    public static boolean clearOverRepair(ItemStack stack) {
        int overRepairAmount = getOverRepairAmount(stack);
        if (overRepairAmount <= 0) {
            return false;
        }

        int baseDamage = Math.max(0, getStoredDamage(stack) - overRepairAmount);
        remove(stack, OVER_REPAIR_ID);
        stack.setDamageValue(baseDamage);
        return true;
    }

    public static void rebaseExisting(ItemStack stack, int baseMaxDamage) {
        MaxDamageModifiersComponent component = stack.get(DataComponents.MAX_DAMAGE_MODIFIERS.get());
        if (component == null) {
            return;
        }

        if (baseMaxDamage <= 0) {
            stack.remove(DataComponents.MAX_DAMAGE_MODIFIERS.get());
            writeMaxDamage(stack, 0);
            return;
        }

        MaxDamageModifiersComponent rebased = new MaxDamageModifiersComponent(
                baseMaxDamage,
                component.modifiers()
        );
        setAndMaterialize(stack, rebased);
    }

    public static boolean repairWithOverRepair(ItemStack stack, float percent) {
        if (!canModifyMaxDamage(stack)) {
            return false;
        }

        int repairAmount = Math.round((float) getBaseMaxDamage(stack) * percent);
        return repairWithOverRepair(stack, repairAmount);
    }

    public static boolean repairWithOverRepair(ItemStack stack, int repairAmount) {
        if (!canModifyMaxDamage(stack)) {
            return false;
        }

        if (repairAmount <= 0) {
            return false;
        }

        int damageBefore = getStoredDamage(stack);
        int overRepairBefore = getOverRepairAmount(stack);
        int repairedDamage = Math.max(0, damageBefore - repairAmount);
        int excessRepair = Math.max(0, repairAmount - damageBefore);
        int cappedOverRepair = Math.min(getMaxOverRepairAmount(stack), overRepairBefore + excessRepair);

        stack.setDamageValue(repairedDamage);
        if (cappedOverRepair != overRepairBefore) {
            setOverRepairAmount(stack, cappedOverRepair);
        }

        return damageBefore != getStoredDamage(stack) || overRepairBefore != getOverRepairAmount(stack);
    }

    public static boolean consumeOverRepairIfThresholdReached(ItemStack stack) {
        int overRepairAmount = getOverRepairAmount(stack);
        int damage = getStoredDamage(stack);
        if (overRepairAmount <= 0 || damage < overRepairAmount) {
            return false;
        }

        int shiftedDamage = damage - overRepairAmount;
        remove(stack, OVER_REPAIR_ID);
        stack.setDamageValue(shiftedDamage);
        return true;
    }

    public static int getRemainingOverRepair(ItemStack stack) {
        return Math.max(0, getOverRepairAmount(stack) - getStoredDamage(stack));
    }

    public static int getBaseDurabilityBarWidth(ItemStack stack, int totalBarWidth) {
        int baseMaxDamage = getBaseMaxDamage(stack);
        if (baseMaxDamage <= 0) {
            return 0;
        }

        int baseDamage = Math.max(0, getStoredDamage(stack) - getOverRepairAmount(stack));
        return Mth.clamp(Math.round((float) totalBarWidth - (float) baseDamage * totalBarWidth / (float) baseMaxDamage),
                0,
                totalBarWidth);
    }

    public static int getBaseDurabilityBarColor(ItemStack stack) {
        int baseMaxDamage = getBaseMaxDamage(stack);
        if (baseMaxDamage <= 0) {
            return stack.getBarColor();
        }

        int baseDamage = Math.max(0, getStoredDamage(stack) - getOverRepairAmount(stack));
        float healthPercentage = Math.max(0.0F, ((float) baseMaxDamage - (float) baseDamage) / (float) baseMaxDamage);
        return Mth.hsvToRgb(healthPercentage / 3.0F, 1.0F, 1.0F);
    }

    public static int getOverRepairBarWidth(ItemStack stack, int totalBarWidth) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return 0;
        }

        int remaining = getRemainingOverRepair(stack);
        if (remaining <= 0) {
            return 0;
        }

        return Math.max(1, Math.round((float) totalBarWidth * remaining / (float) maxDamage));
    }

    private static void setAndMaterialize(ItemStack stack, MaxDamageModifiersComponent component) {
        if (component.isEmpty()) {
            stack.remove(DataComponents.MAX_DAMAGE_MODIFIERS.get());
            writeMaxDamage(stack, component.baseMaxDamage());
            return;
        }

        stack.set(DataComponents.MAX_DAMAGE_MODIFIERS.get(), component);
        writeMaxDamage(stack, component.baseMaxDamage() + component.additiveTotal());
    }

    private static void writeMaxDamage(ItemStack stack, int maxDamage) {
        if (maxDamage <= 0) {
            stack.remove(net.minecraft.core.component.DataComponents.MAX_DAMAGE);
            return;
        }

        stack.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, maxDamage);
    }

    public static boolean canModifyMaxDamage(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getMaxDamage() > 0
                && stack.has(net.minecraft.core.component.DataComponents.DAMAGE)
                && !stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE);
    }

    private static int getStoredDamage(ItemStack stack) {
        // Over-repair can intentionally keep minecraft:damage above the item's base max damage.
        // ItemStack#getDamageValue() clamps to the current max, so use the raw component for this math.
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0);
    }
}
