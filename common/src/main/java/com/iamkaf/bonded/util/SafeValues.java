package com.iamkaf.bonded.util;

public class SafeValues {
    static final int MAX_DAMAGE_MIN = 1;

    /**
     * Checks if a damage source is a firework rocket and tries to find the associated crossbow.
     */
    public static int safeMaxDamageValue(int maxDamage) {
        return Math.max(maxDamage, MAX_DAMAGE_MIN);
    }
}