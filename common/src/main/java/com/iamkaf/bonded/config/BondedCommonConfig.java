package com.iamkaf.bonded.config;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;

public class BondedCommonConfig {
    public final ConfigValue<Boolean> enableUpgrading;
    public final ConfigValue<Boolean> enableRepairing;
    public final ConfigValue<Integer> levelsToUpgrade;
    public final ConfigValue<Integer> defaultMaxExperienceForUnknownItems;
    public final ConfigValue<Double> durabilityGainedOnRepairBench;
    public final ConfigValue<Double> weaponDamageDealtExperienceGainedMultiplier;
    public final ConfigValue<Double> armorDamageTakenExperienceGainedMultiplier;
    public final ConfigValue<Integer> experienceForMiningOres;
    public final ConfigValue<Boolean> sendChatMessages;
    public final ConfigValue<Boolean> enableInnateLootBond;
    public final ConfigValue<Integer> innateLootBondMin;
    public final ConfigValue<Integer> innateLootBondMax;
    public final ConfigValue<Double> chestScrapChance;
    public final ConfigValue<Double> armoredEnemyScrapChance;
    public final ConfigValue<Integer> brokenGearScrapCap;


    public BondedCommonConfig(ConfigBuilder builder) {
        builder.push("features")
                .categoryComment("Feature toggles for Bonded gameplay and chat feedback.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.features.header")
                        .inlineTextKey("bonded.config.info.features.text"))
                .header("Features");
        enableUpgrading = builder.bool("upgrading_enabled", true)
                .comment("Allows gear to upgrade at tool benches when it reaches max level.")
                .info(info -> info.inlineTextKey("bonded.config.upgrading_enabled.info"))
                .sync(true)
                .build();
        enableRepairing = builder.bool("repairing_enabled", true)
                .comment("Allows gear to repair at repair benches.")
                .info(info -> info.inlineTextKey("bonded.config.repairing_enabled.info"))
                .sync(true)
                .build();
        sendChatMessages = builder.bool("send_chat_messages", true)
                .comment("Sends chat messages when bonded gear levels up or reaches max level.")
                .info(info -> info.inlineTextKey("bonded.config.send_chat_messages.info"))
                .sync(true)
                .build();
        enableInnateLootBond = builder.bool("innate_loot_bond_enabled", true)
                .comment("Allows eligible gear created by world loot to start with bond.")
                .info(info -> info.inlineTextKey("bonded.config.innate_loot_bond_enabled.info"))
                .sync(true)
                .build();
        builder.pop();

        builder.push("progression")
                .categoryComment("Progression rules for how bonded gear gains experience and reaches upgrades.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.progression.header")
                        .inlineTextKey("bonded.config.info.progression.text"))
                .header("Progression");
        levelsToUpgrade = builder.intRange("levels_to_upgrade", 10, 10, 100)
                .comment("Maximum gear level before a tool bench upgrade is available.")
                .info(info -> info.inlineTextKey("bonded.config.levels_to_upgrade.info"))
                .sync(true)
                .build();
        defaultMaxExperienceForUnknownItems = builder.intRange(
                        "default_experience_for_unknown_items",
                        1000,
                        100,
                        2000
                )
                .comment("Experience required for items without a more specific progression rule.")
                .info(info -> info.inlineTextKey("bonded.config.default_experience_for_unknown_items.info"))
                .sync(true)
                .build();
        weaponDamageDealtExperienceGainedMultiplier =
                builder.doubleRange("weapon_damage_dealt_experience_multiplier", 3d, 1.0d, 20d)
                        .comment("Experience multiplier for weapon damage dealt.")
                        .info(info -> info.inlineTextKey("bonded.config.weapon_damage_dealt_experience_multiplier.info"))
                        .sync(true)
                        .build();
        armorDamageTakenExperienceGainedMultiplier =
                builder.doubleRange("armor_damage_taken_experience_multiplier", 1d, 1.0d, 20d)
                        .comment("Experience multiplier for damage taken while wearing armor.")
                        .info(info -> info.inlineTextKey("bonded.config.armor_damage_taken_experience_multiplier.info"))
                        .sync(true)
                        .build();
        experienceForMiningOres = builder.intRange("experience_for_mining_ores", 10, 1, 10)
                .comment("Experience gained when mining ore blocks.")
                .info(info -> info.inlineTextKey("bonded.config.experience_for_mining_ores.info"))
                .sync(true)
                .build();
        builder.pop();

        builder.push("repair_and_loot")
                .categoryComment("Repair bench values and extra Scrap sources.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.repair_and_loot.header")
                        .inlineTextKey("bonded.config.info.repair_and_loot.text"))
                .header("Repair And Loot");
        durabilityGainedOnRepairBench = builder.doubleRange("durability_gained_on_repair_bench", 0.2d, 0.1d, 1d)
                .comment("Durability fraction restored by repair bench materials.")
                .info(info -> info.inlineTextKey("bonded.config.durability_gained_on_repair_bench.info"))
                .sync(true)
                .build();
        innateLootBondMin = builder.intRange("innate_loot_bond_min", 250, 0, 999)
                .comment("Minimum bond applied to eligible gear created by world loot.")
                .info(info -> info.inlineTextKey("bonded.config.innate_loot_bond_min.info"))
                .sync(true)
                .build();
        innateLootBondMax = builder.intRange("innate_loot_bond_max", 1000, 0, 5000)
                .comment("Maximum bond applied to eligible gear created by world loot.")
                .info(info -> info.inlineTextKey("bonded.config.innate_loot_bond_max.info"))
                .sync(true)
                .build();
        chestScrapChance = builder.doubleRange("chest_scrap_chance", 0.08d, 0.0d, 1.0d)
                .comment("Chance for vanilla chest loot tables to include Scrap.")
                .info(info -> info.inlineTextKey("bonded.config.chest_scrap_chance.info"))
                .sync(true)
                .build();
        armoredEnemyScrapChance = builder.doubleRange("armored_enemy_scrap_chance", 0.04d, 0.0d, 1.0d)
                .comment("Chance for non-player enemies wearing armor to drop Scrap when killed.")
                .info(info -> info.inlineTextKey("bonded.config.armored_enemy_scrap_chance.info"))
                .sync(true)
                .build();
        brokenGearScrapCap = builder.intRange("broken_gear_scrap_cap", 32, 0, 64)
                .comment("Maximum Scrap dropped when bonded gear breaks.")
                .info(info -> info.inlineTextKey("bonded.config.broken_gear_scrap_cap.info"))
                .sync(true)
                .build();
        builder.pop();
    }
}
