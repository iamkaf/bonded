package com.iamkaf.bonded.config;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;

public class BondedCommonConfig {
    public final ConfigValue<Boolean> enableUpgrading;
    public final ConfigValue<Boolean> enableRepairing;
    public final ConfigValue<Boolean> enableTooltips;
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
        builder.header("Features");
        enableUpgrading = builder.bool("upgrading_enabled", true)
                .comment("Allows gear to upgrade at tool benches when it reaches max level.")
                .sync(true)
                .build();
        enableRepairing = builder.bool("repairing_enabled", true)
                .comment("Allows gear to repair at repair benches.")
                .sync(true)
                .build();
        enableTooltips = builder.bool("tooltips_enabled", true)
                .comment("Shows Bonded level, experience, and bonus information in item tooltips.")
                .sync(true)
                .build();
        sendChatMessages = builder.bool("send_chat_messages", true)
                .comment("Sends chat messages when bonded gear levels up or reaches max level.")
                .sync(true)
                .build();
        enableInnateLootBond = builder.bool("innate_loot_bond_enabled", true)
                .comment("Allows eligible gear created by world loot to start with bond.")
                .sync(true)
                .build();

        builder.header("Progression");
        levelsToUpgrade = builder.intRange("levels_to_upgrade", 10, 10, 100)
                .comment("Maximum gear level before a tool bench upgrade is available.")
                .sync(true)
                .build();
        defaultMaxExperienceForUnknownItems = builder.intRange(
                        "default_experience_for_unknown_items",
                        1000,
                        1,
                        Integer.MAX_VALUE
                )
                .comment("Experience required for items without a more specific progression rule.")
                .sync(true)
                .build();
        weaponDamageDealtExperienceGainedMultiplier =
                builder.doubleRange("weapon_damage_dealt_experience_multiplier", 3d, 1.0d, 20d)
                        .comment("Experience multiplier for weapon damage dealt.")
                        .sync(true)
                        .build();
        armorDamageTakenExperienceGainedMultiplier =
                builder.doubleRange("armor_damage_taken_experience_multiplier", 1d, 1.0d, 20d)
                        .comment("Experience multiplier for damage taken while wearing armor.")
                        .sync(true)
                        .build();
        experienceForMiningOres = builder.intRange("experience_for_mining_ores", 10, 1, 10)
                .comment("Experience gained when mining ore blocks.")
                .sync(true)
                .build();

        builder.header("Repair And Loot");
        durabilityGainedOnRepairBench = builder.doubleRange("durability_gained_on_repair_bench", 0.2d, 0.1d, 1d)
                .comment("Durability fraction restored by repair bench materials.")
                .sync(true)
                .build();
        innateLootBondMin = builder.intRange("innate_loot_bond_min", 500, 0, Integer.MAX_VALUE)
                .comment("Minimum bond applied to eligible gear created by world loot.")
                .sync(true)
                .build();
        innateLootBondMax = builder.intRange("innate_loot_bond_max", 999, 0, Integer.MAX_VALUE)
                .comment("Maximum bond applied to eligible gear created by world loot.")
                .sync(true)
                .build();
        chestScrapChance = builder.doubleRange("chest_scrap_chance", 0.08d, 0.0d, 1.0d)
                .comment("Chance for vanilla chest loot tables to include Scrap.")
                .sync(true)
                .build();
        armoredEnemyScrapChance = builder.doubleRange("armored_enemy_scrap_chance", 0.04d, 0.0d, 1.0d)
                .comment("Chance for non-player enemies wearing armor to drop Scrap when killed.")
                .sync(true)
                .build();
        brokenGearScrapCap = builder.intRange("broken_gear_scrap_cap", 32, 0, Integer.MAX_VALUE)
                .comment("Maximum Scrap dropped when bonded gear breaks.")
                .sync(true)
                .build();
    }
}
