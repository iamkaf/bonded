package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;

public class ArmorHealthFactory {
    public static ArmorHealthBonus create(int threshold, float bonus) {
        return new ArmorHealthBonus(Bonded.resource("armor_health_" + threshold), threshold, bonus);
    }
}
