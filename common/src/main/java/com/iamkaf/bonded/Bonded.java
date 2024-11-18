package com.iamkaf.bonded;

import com.iamkaf.amber.api.core.AmberMod;
import com.iamkaf.bonded.api.event.GameEvents;
import com.iamkaf.bonded.bonuses.Bonuses;
import com.iamkaf.bonded.config.BondedCommonConfig;
import com.iamkaf.bonded.leveling.GameplayHooks;
import com.iamkaf.bonded.leveling.GearManager;
import com.iamkaf.bonded.leveling.levelers.Levelers;
import com.iamkaf.bonded.registry.Blocks;
import com.iamkaf.bonded.registry.CreativeModeTabs;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.registry.Items;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class Bonded extends AmberMod {
    public static final String MOD_ID = "bonded";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final BondedCommonConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    public static GearManager GEAR = new GearManager();
    public static Bonded instance;

    static {
        Pair<BondedCommonConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(BondedCommonConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public Bonded() {
        super(MOD_ID);
        instance = this;
    }

    public static void init() {
        LOGGER.info("Bonded initializing.");

        new Bonded();

        // Registries
        Items.init();
        Blocks.init();
        DataComponents.init();
        CreativeModeTabs.init();
        GameplayHooks.init();
        Levelers.init();
        Bonuses.init();
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void onShieldBlock(Player player, float damage) {
        GameEvents.SHIELD_BLOCK.invoker().block(player, damage);
    }
}
