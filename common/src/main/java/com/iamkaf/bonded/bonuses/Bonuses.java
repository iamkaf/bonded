package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.leveling.GearManager;

public class Bonuses {
    public static void init() {
        GearManager.bondBonusRegistry.register(ArmorFactory.create(1000, 1));
        GearManager.bondBonusRegistry.register(ArmorFactory.create(2000, 1));
        GearManager.bondBonusRegistry.register(ArmorFactory.create(5000, 1));
        GearManager.bondBonusRegistry.register(ArmorFactory.create(10000, 1));
        GearManager.bondBonusRegistry.register(ArmorFactory.create(15000, 1));
        GearManager.bondBonusRegistry.register(ArmorFactory.create(20000, 1));
        GearManager.bondBonusRegistry.register(ArmorHealthFactory.create(5000, 1));
        GearManager.bondBonusRegistry.register(ArmorHealthFactory.create(10000, 1));
        GearManager.bondBonusRegistry.register(ArmorHealthFactory.create(20000, 2));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(500, 50));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(1000, 50));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(2000, 50));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(3000, 50));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(4000, 50));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(5000, 100));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(10000, 500));
        GearManager.bondBonusRegistry.register(DurabilityFactory.create(20000, 1000));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(1000, 1));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(2000, 2));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(3000, 3));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(4000, 4));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(5000, 5));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(6000, 6));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(7000, 7));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(8000, 8));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(9000, 9));
        GearManager.bondBonusRegistry.register(AttackDamageFactory.create(10000, 10));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(1000, 1));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(2000, 2));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(3000, 3));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(4000, 4));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(5000, 5));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(6000, 6));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(7000, 7));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(8000, 8));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(9000, 9));
        GearManager.bondBonusRegistry.register(DigSpeedFactory.create(10000, 10));
    }
}
