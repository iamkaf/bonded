package com.iamkaf.bonded.rules;

import org.jetbrains.annotations.Nullable;

/** A restart-frozen rule for one concrete item identifier. */
public record ResolvedGearRule(
        String item,
        String type,
        int experienceCap,
        RepairMode repairMode,
        @Nullable String repair,
        @Nullable String upgradeTo,
        @Nullable String upgradeIngredient,
        boolean enabled,
        GearRule.Source source
) {
    public enum RepairMode {
        INHERIT,
        NONE,
        ITEM,
        TAG;

        static RepairMode parse(String value) {
            return valueOf(value.toUpperCase(java.util.Locale.ROOT));
        }
    }
}
