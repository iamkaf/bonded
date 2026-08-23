package com.iamkaf.bonded.rules;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearRuleReferenceTest {
    @Test
    void keepsMissingModItemsDormantUntilTheyReturn() {
        String item = "removed_mod:old_hammer";

        assertTrue(GearRuleReference.validIdentifier(item));
        assertTrue(GearRuleReference.validPersistedItem(item));
        assertEquals(GearRuleReference.Availability.DORMANT, GearRuleReference.item(item, Set.of()));
        assertEquals(GearRuleReference.Availability.PRESENT, GearRuleReference.item(item, Set.of(item)));
    }

    @Test
    void rejectsMalformedReferencesInsteadOfPreservingThem() {
        assertFalse(GearRuleReference.validIdentifier("not an identifier"));
        assertEquals(
                GearRuleReference.Availability.INVALID,
                GearRuleReference.item("not an identifier", Set.of())
        );
    }

}
