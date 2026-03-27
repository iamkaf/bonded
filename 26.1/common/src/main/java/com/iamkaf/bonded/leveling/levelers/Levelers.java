package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.leveling.GearManager;

public class Levelers {
    public static void init() {
        GearManager.gearTypeLevelerRegistry.register(new ArmorLeveler());
        GearManager.gearTypeLevelerRegistry.register(new MeleeWeaponsLeveler());
        GearManager.gearTypeLevelerRegistry.register(new RangedWeaponsLeveler());
        GearManager.gearTypeLevelerRegistry.register(new MiningToolsLeveler());
        GearManager.gearTypeLevelerRegistry.register(new UtilityToolLeveler());
    }
}
