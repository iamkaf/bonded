package com.iamkaf.bonded.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BondedCommonConfig {
    public final ModConfigSpec.ConfigValue<Boolean> enableUpgrading;
    public final ModConfigSpec.ConfigValue<Boolean> enableRepairing;
    public final ModConfigSpec.ConfigValue<Boolean> enableTooltips;
    public final ModConfigSpec.ConfigValue<Integer> levelsToUpgrade;
    public final ModConfigSpec.ConfigValue<Integer> defaultMaxExperienceForUnknownItems;
    public final ModConfigSpec.ConfigValue<Double> durabilityGainedOnRepairBench;
    public final ModConfigSpec.ConfigValue<Double> weaponDamageDealtExperienceGainedMultiplier;
    public final ModConfigSpec.ConfigValue<Double> armorDamageTakenExperienceGainedMultiplier;
    public final ModConfigSpec.ConfigValue<Integer> experienceForMiningOres;
    public final ModConfigSpec.ConfigValue<Boolean> sendChatMessages;
    public final ModConfigSpec.ConfigValue<Boolean> enableInnateLootBond;
    public final ModConfigSpec.ConfigValue<Integer> innateLootBondMin;
    public final ModConfigSpec.ConfigValue<Integer> innateLootBondMax;
    public final ModConfigSpec.ConfigValue<Double> chestScrapChance;
    public final ModConfigSpec.ConfigValue<Double> armoredEnemyScrapChance;
    public final ModConfigSpec.ConfigValue<Integer> brokenGearScrapCap;


    public BondedCommonConfig(ModConfigSpec.Builder builder) {
        enableUpgrading =
                builder.translation("bonded.config.upgrading_enabled").define("upgrading_enabled", true);
        enableRepairing =
                builder.translation("bonded.config.repairing_enabled").define("repairing_enabled", true);
        sendChatMessages =
                builder.translation("bonded.config.send_chat_messages").define("send_chat_messages", true);
        enableTooltips =
                builder.translation("bonded.config.tooltips_enabled").define("tooltips_enabled", true);
        enableInnateLootBond =
                builder.translation("bonded.config.innate_loot_bond_enabled").define("innate_loot_bond_enabled", true);

        levelsToUpgrade = builder.translation("bonded.config.levels_to_upgrade")
                .defineInRange("levels_to_upgrade", 10, 10, 100);
        defaultMaxExperienceForUnknownItems =
                builder.translation("bonded.config.default_experience_for_unknown_items")
                        .define("default_experience_for_unknown_items", 1000);

        durabilityGainedOnRepairBench = builder.translation("bonded.config.durability_gained_on_repair_bench")
                .defineInRange("durability_gained_on_repair_bench", 0.2d, 0.1d, 1d);

        weaponDamageDealtExperienceGainedMultiplier =
                builder.translation("bonded.config.weapon_damage_dealt_experience_multiplier")
                        .defineInRange("weapon_damage_dealt_experience_multiplier", 3d, 1.0d, 20d);
        armorDamageTakenExperienceGainedMultiplier =
                builder.translation("bonded.config.armor_damage_taken_experience_multiplier")
                        .defineInRange("armor_damage_taken_experience_multiplier", 1d, 1.0d, 20d);

        experienceForMiningOres = builder.translation("bonded.config.experience_for_mining_ores")
                .defineInRange("experience_for_mining_ores", 10, 1, 10);

        innateLootBondMin = builder.translation("bonded.config.innate_loot_bond_min")
                .defineInRange("innate_loot_bond_min", 500, 0, Integer.MAX_VALUE);
        innateLootBondMax = builder.translation("bonded.config.innate_loot_bond_max")
                .defineInRange("innate_loot_bond_max", 999, 0, Integer.MAX_VALUE);

        chestScrapChance = builder.translation("bonded.config.chest_scrap_chance")
                .defineInRange("chest_scrap_chance", 0.08d, 0.0d, 1.0d);
        armoredEnemyScrapChance = builder.translation("bonded.config.armored_enemy_scrap_chance")
                .defineInRange("armored_enemy_scrap_chance", 0.04d, 0.0d, 1.0d);
        brokenGearScrapCap = builder.translation("bonded.config.broken_gear_scrap_cap")
                .defineInRange("broken_gear_scrap_cap", 32, 0, Integer.MAX_VALUE);
    }
}
