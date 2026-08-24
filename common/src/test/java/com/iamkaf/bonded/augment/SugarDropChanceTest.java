package com.iamkaf.bonded.augment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SugarDropChanceTest {
    @Test
    void usesAnExactFivePercentBoundary() {
        assertTrue(SugarDropChance.matches(0.0F));
        assertTrue(SugarDropChance.matches(Math.nextDown(0.05F)));
        assertFalse(SugarDropChance.matches(0.05F));
        assertFalse(SugarDropChance.matches(1.0F));
        assertFalse(SugarDropChance.matches(-0.01F));
    }
}
