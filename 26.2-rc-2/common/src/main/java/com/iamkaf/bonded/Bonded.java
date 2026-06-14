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
                .comment("Common Bonded settings synced from the server when multiplayer policy requires it.");
        CONFIG = new BondedCommonConfig(builder);
        CONFIG_HANDLE = builder.build();
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
