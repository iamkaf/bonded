package com.iamkaf.bonded.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BondedCommonConfig {
    public final ModConfigSpec.ConfigValue<Boolean> upgradingEnabled;
    public final ModConfigSpec.ConfigValue<Boolean> repairingEnabled;
    public final ModConfigSpec.ConfigValue<Integer> levelsToUpgrade;
    public final ModConfigSpec.ConfigValue<Integer> defaultMaxExperienceForUnknownItems;
    public final ModConfigSpec.ConfigValue<Boolean> enableDurabilityGainOnLevelUp;
    public final ModConfigSpec.ConfigValue<Double> durabilityGainOnLevelUp;
    public final ModConfigSpec.ConfigValue<Double> weaponDamageDealtExperienceGainedMultiplier;
    public final ModConfigSpec.ConfigValue<Double> armorDamageTakenExperienceGainedMultiplier;
    public final ModConfigSpec.ConfigValue<Double> durabilityGainedOnRepairBench;
    public final ModConfigSpec.ConfigValue<Integer> experienceForMiningOres;

//    public final ModConfigSpec.ConfigValue<Boolean> tooltipsEnabled;


    public BondedCommonConfig(ModConfigSpec.Builder builder) {
        upgradingEnabled =
                builder.translation("bonded.config.upgrading_enabled").define("upgrading_enabled", true);
        repairingEnabled =
                builder.translation("bonded.config.repairing_enabled").define("repairing_enabled", true);

        levelsToUpgrade = builder.translation("bonded.config.levels_to_upgrade")
                .defineInRange("levels_to_upgrade", 10, 10, 100);
        defaultMaxExperienceForUnknownItems =
                builder.translation("bonded.config.default_experience_for_unknown_items")
                        .define("default_experience_for_unknown_items", 1000);

//        tooltipsEnabled =
//                builder.translation("bonded.config.tooltips_enabled").define("tooltips_enabled", true);

        enableDurabilityGainOnLevelUp =
                builder.translation("bonded.config.enable_durability_repair_on_level_up")
                        .define("enable_durability_repair_on_level_up", true);

        durabilityGainOnLevelUp = builder.translation("bonded.config.durability_repair_on_level_up")
                .defineInRange("durability_repair_on_level_up", 0.2f, 0.0f, 1f);

        weaponDamageDealtExperienceGainedMultiplier =
                builder.translation("bonded.config.weapon_damage_dealt_experience_multiplier")
                        .defineInRange("weapon_damage_dealt_experience_multiplier", 5f, 1.0f, 20f);
        armorDamageTakenExperienceGainedMultiplier =
                builder.translation("bonded.config.armor_damage_taken_experience_multiplier")
                        .defineInRange("armor_damage_taken_experience_multiplier", 1f, 1.0f, 20f);

        durabilityGainedOnRepairBench = builder.translation("bonded.config.durability_gained_on_repair_bench")
                .defineInRange("durability_gained_on_repair_bench", 0.2f, 0.1f, 1f);

        experienceForMiningOres = builder.translation("bonded.config.experience_for_mining_ores")
                .defineInRange("experience_for_mining_ores", 10, 1, 10);
    }
}
