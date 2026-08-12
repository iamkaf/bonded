package com.iamkaf.bonded.augment;

final class SugarDropChance {
    private static final float CHANCE = 0.05F;

    private SugarDropChance() {
    }

    static boolean matches(float roll) {
        return roll >= 0.0F && roll < CHANCE;
    }
}
