package com.iamkaf.bonded;

import com.iamkaf.amber.api.core.v2.AmberInitializer;
import com.iamkaf.bonded.bonuses.Bonuses;
import com.iamkaf.bonded.command.BondedCommands;
import com.iamkaf.bonded.config.BondedCommonConfig;
import com.iamkaf.bonded.leveling.GameplayHooks;
import com.iamkaf.bonded.leveling.GearManager;
import com.iamkaf.bonded.leveling.levelers.Levelers;
import com.iamkaf.bonded.loot.ScrapDrops;
import com.iamkaf.bonded.loot.WorldInnateBond;
import com.iamkaf.bonded.registry.*;
import com.mojang.logging.LogUtils;
import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigHandle;
import com.iamkaf.konfig.api.v1.ConfigMigrationContext;
import com.iamkaf.konfig.api.v1.ConfigScope;
import com.iamkaf.konfig.api.v1.Konfig;
import com.iamkaf.konfig.api.v1.SyncMode;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public class Bonded {
    public static final String MOD_ID = "bonded";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final BondedCommonConfig CONFIG;
    public static final ConfigHandle CONFIG_HANDLE;
    public static GearManager GEAR = new GearManager();

    static {
        ConfigBuilder builder = Konfig.builder(MOD_ID, "common")
                .scope(ConfigScope.COMMON)
                .syncMode(SyncMode.LOGIN_AND_RELOAD)
                .fileName("bonded-common.toml")
                .schemaVersion(1)
                .migrate(0, Bonded::migrateFlatConfig)
                .comment("Common Bonded settings synced from the server when multiplayer policy requires it.")
                .info(info -> info
                        .headerKey("bonded.config.info.common.header")
                        .inlineTextKey("bonded.config.info.common.text")
                        .urlKey("bonded.config.info.report_issue", "https://github.com/iamkaf/bonded"));
        CONFIG = new BondedCommonConfig(builder);
        CONFIG_HANDLE = builder.build();
    }

    private static void migrateFlatConfig(ConfigMigrationContext context) {
        context.rename("upgrading_enabled", "features.upgrading_enabled");
        context.rename("repairing_enabled", "features.repairing_enabled");
        context.remove("tooltips_enabled");
        context.rename("send_chat_messages", "features.send_chat_messages");
        context.rename("innate_loot_bond_enabled", "features.innate_loot_bond_enabled");
        context.rename("levels_to_upgrade", "progression.levels_to_upgrade");
        context.rename("default_experience_for_unknown_items", "progression.default_experience_for_unknown_items");
        context.rename(
                "weapon_damage_dealt_experience_multiplier",
                "progression.weapon_damage_dealt_experience_multiplier"
        );
        context.rename(
                "armor_damage_taken_experience_multiplier",
                "progression.armor_damage_taken_experience_multiplier"
        );
        context.rename("experience_for_mining_ores", "progression.experience_for_mining_ores");
        context.rename("durability_gained_on_repair_bench", "repair_and_loot.durability_gained_on_repair_bench");
        context.rename("innate_loot_bond_min", "repair_and_loot.innate_loot_bond_min");
        context.rename("innate_loot_bond_max", "repair_and_loot.innate_loot_bond_max");
        context.rename("chest_scrap_chance", "repair_and_loot.chest_scrap_chance");
        context.rename("armored_enemy_scrap_chance", "repair_and_loot.armored_enemy_scrap_chance");
        context.rename("broken_gear_scrap_cap", "repair_and_loot.broken_gear_scrap_cap");
    }

    public static void init() {
        LOGGER.info("Bonded initializing.");

        AmberInitializer.initialize(MOD_ID);

        // Registries
        Blocks.init();
        Items.init();
        DataComponents.init();
        CreativeModeTabs.init();
        BondedCommands.init();
        GameplayHooks.init();
        WorldInnateBond.init();
        ScrapDrops.init();
        Levelers.init();
        Bonuses.init();
        TierMap.init();
        Sounds.init();
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
