package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;

public class AttackDamageFactory {
    public static AttackDamageBonus create(int threshold, float bonus) {
        return new AttackDamageBonus(Bonded.resource("melee_weapon_damage_" + threshold), threshold, bonus);
    }
}
