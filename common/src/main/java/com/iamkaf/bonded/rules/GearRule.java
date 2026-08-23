package com.iamkaf.bonded.rules;

import org.jetbrains.annotations.Nullable;

/** A single item or item-tag rule before registry selectors are resolved. */
public record GearRule(
        String identity,
        String selector,
        @Nullable String type,
        @Nullable Integer experienceCap,
        @Nullable String repairMode,
        @Nullable String repair,
        @Nullable String upgradeTo,
        @Nullable String upgradeIngredient,
        boolean enabled,
        Source source
) {
    public enum Kind {
        BUILTIN,
        COMPAT,
        USER,
        API
    }

    public record Source(Kind kind, String id, String label, int version) {
    }
}
