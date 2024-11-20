package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;

public class ArmorFactory {
    public static ArmorBonus create(int threshold, float bonus) {
        return new ArmorBonus(Bonded.resource("armor_" + threshold), threshold, bonus);
    }
}
