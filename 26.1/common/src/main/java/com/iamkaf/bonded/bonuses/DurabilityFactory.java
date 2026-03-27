package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;

public class DurabilityFactory {
    public static DurabilityBonus create(int threshold, int bonus) {
        return new DurabilityBonus(Bonded.resource("durability_" + threshold), threshold, bonus);
    }
}
