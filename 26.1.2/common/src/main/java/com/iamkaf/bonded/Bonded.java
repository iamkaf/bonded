package com.iamkaf.bonded;

import com.iamkaf.amber.api.core.v2.AmberInitializer;
import com.iamkaf.bonded.bonuses.Bonuses;
import com.iamkaf.bonded.command.BondedCommands;
import com.iamkaf.bonded.config.BondedCommonConfig;
import com.iamkaf.bonded.leveling.GameplayHooks;
import com.iamkaf.bonded.leveling.GearManager;
import com.iamkaf.bonded.leveling.levelers.Levelers;
import com.iamkaf.bonded.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class Bonded {
    public static final String MOD_ID = "bonded";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final BondedCommonConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    public static GearManager GEAR = new GearManager();

    static {
        Pair<BondedCommonConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(BondedCommonConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
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
