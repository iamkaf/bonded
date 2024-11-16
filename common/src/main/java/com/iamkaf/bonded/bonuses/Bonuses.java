package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.leveling.GearManager;

public class Bonuses {
    public static void init() {
        GearManager.bondBonusRegistry.register(new ArmorHealthBonus5k1Heart());
    }
}
