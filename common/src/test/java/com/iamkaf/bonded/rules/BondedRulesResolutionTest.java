package com.iamkaf.bonded.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BondedRulesResolutionTest {
    private static final GearRule.Source BUILTIN =
            new GearRule.Source(GearRule.Kind.BUILTIN, "test:builtin", "Built in", 1);
    private static final GearRule.Source COMPAT =
            new GearRule.Source(GearRule.Kind.COMPAT, "test:compat", "Compatibility", 1);

    @Test
    void laterProfilesOverrideOnlyTheFieldsTheyDeclare() {
        GearRule base = rule(
                "builtin/stick",
                "minecraft:stick",
                "utility",
                120,
                "item",
                "minecraft:oak_planks",
                "minecraft:blaze_rod",
                "minecraft:repairs_blaze_rod",
                true,
                BUILTIN
        );
        GearRule patch = rule(
                "compat/stick",
                "minecraft:stick",
                null,
                450,
                null,
                null,
                null,
                null,
                true,
                COMPAT
        );

        GearRule resolved = BondedRulesLoader.effectiveRules(List.of(
                profile(BUILTIN, base),
                profile(COMPAT, patch)
        ), 1000).getFirst();

        assertEquals("utility", resolved.type());
        assertEquals(450, resolved.experienceCap());
        assertEquals("item", resolved.repairMode());
        assertEquals("minecraft:oak_planks", resolved.repair());
        assertEquals("minecraft:blaze_rod", resolved.upgradeTo());
        assertEquals("minecraft:repairs_blaze_rod", resolved.upgradeIngredient());
        assertEquals(COMPAT, resolved.source());
    }

    @Test
    void disablingALaterProfileKeepsClassificationAndClearsItsUpgrade() {
        GearRule base = rule(
                "builtin/stick",
                "minecraft:stick",
                "utility",
                120,
                "item",
                "minecraft:oak_planks",
                "minecraft:blaze_rod",
                "minecraft:repairs_blaze_rod",
                true,
                BUILTIN
        );
        GearRule disabled = rule(
                "compat/stick",
                "minecraft:stick",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                COMPAT
        );

        GearRule resolved = BondedRulesLoader.effectiveRules(List.of(
                profile(BUILTIN, base),
                profile(COMPAT, disabled)
        ), 1000).getFirst();

        assertEquals("utility", resolved.type());
        assertEquals(120, resolved.experienceCap());
        assertEquals("item", resolved.repairMode());
        assertEquals("minecraft:oak_planks", resolved.repair());
        assertNull(resolved.upgradeTo());
        assertNull(resolved.upgradeIngredient());
        assertFalse(resolved.enabled());
        assertEquals(COMPAT, resolved.source());
    }

    @Test
    void declaringRepairOrUpgradeReplacesTheWholeCorrespondingPair() {
        GearRule base = rule(
                "builtin/stick",
                "minecraft:stick",
                "utility",
                120,
                "item",
                "minecraft:oak_planks",
                "minecraft:blaze_rod",
                "minecraft:repairs_blaze_rod",
                true,
                BUILTIN
        );
        GearRule patch = rule(
                "compat/stick",
                "minecraft:stick",
                null,
                null,
                "none",
                null,
                "minecraft:breeze_rod",
                "minecraft:repairs_breeze_rod",
                true,
                COMPAT
        );

        GearRule resolved = BondedRulesLoader.effectiveRules(List.of(
                profile(BUILTIN, base),
                profile(COMPAT, patch)
        ), 1000).getFirst();

        assertEquals("none", resolved.repairMode());
        assertNull(resolved.repair());
        assertEquals("minecraft:breeze_rod", resolved.upgradeTo());
        assertEquals("minecraft:repairs_breeze_rod", resolved.upgradeIngredient());
    }

    @Test
    void baseDeclarationsReceiveStableDefaults() {
        GearRule declaration = rule(
                "builtin/stick",
                "minecraft:stick",
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                BUILTIN
        );

        GearRule resolved = BondedRulesLoader.effectiveRules(
                List.of(profile(BUILTIN, declaration)),
                640
        ).getFirst();

        assertEquals("inherit", resolved.type());
        assertEquals(640, resolved.experienceCap());
        assertEquals("inherit", resolved.repairMode());
        assertNull(resolved.repair());
        assertNull(resolved.upgradeTo());
        assertNull(resolved.upgradeIngredient());
    }

    @Test
    void effectiveRowsAreAnImmutableStableOrder() {
        GearRule stick = rule(
                "builtin/stick",
                "minecraft:stick",
                "utility",
                100,
                "inherit",
                null,
                null,
                null,
                true,
                BUILTIN
        );
        GearRule rod = rule(
                "builtin/rod",
                "minecraft:blaze_rod",
                "utility",
                200,
                "inherit",
                null,
                null,
                null,
                true,
                BUILTIN
        );

        List<GearRule> resolved = BondedRulesLoader.effectiveRules(
                List.of(profile(BUILTIN, stick, rod)),
                1000
        );

        assertEquals(List.of("minecraft:stick", "minecraft:blaze_rod"),
                resolved.stream().map(GearRule::selector).toList());
        assertThrows(UnsupportedOperationException.class, () -> resolved.add(stick));
    }

    private static BondedRulesLoader.Profile profile(GearRule.Source source, GearRule... rules) {
        return new BondedRulesLoader.Profile(
                source.id(),
                source.label(),
                source.kind(),
                source.version(),
                null,
                List.of(),
                List.of(rules)
        );
    }

    private static GearRule rule(
            String identity,
            String selector,
            String type,
            Integer experienceCap,
            String repairMode,
            String repair,
            String upgradeTo,
            String upgradeIngredient,
            boolean enabled,
            GearRule.Source source
    ) {
        return new GearRule(
                identity,
                selector,
                type,
                experienceCap,
                repairMode,
                repair,
                upgradeTo,
                upgradeIngredient,
                enabled,
                source
        );
    }
}
