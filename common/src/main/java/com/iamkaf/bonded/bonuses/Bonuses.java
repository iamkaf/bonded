package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.leveling.GearManager;

public class Bonuses {
    public static void init() {
        GearManager.bondBonusRegistry.register(new ArmorHealthBonus5k1Heart());
        GearManager.bondBonusRegistry.register(new DurabilityBonus1k());
        GearManager.bondBonusRegistry.register(new DurabilityBonus10k());
        GearManager.bondBonusRegistry.register(new DurabilityBonus20k());
    }
}
