package com.iamkaf.bonded;

import com.iamkaf.bonded.Bonded;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

/**
 * Fabric entry point.
 */
public class BondedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Bonded.MOD_ID, ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);
        Bonded.init();
    }
}