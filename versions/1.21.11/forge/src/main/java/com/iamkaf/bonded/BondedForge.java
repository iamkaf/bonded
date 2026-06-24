package com.iamkaf.bonded;

import com.iamkaf.bonded.Bonded;
import fuzs.forgeconfigapiport.forge.api.v5.NeoForgeConfigRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Bonded.MOD_ID)
public class BondedForge {

    public BondedForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BondedClient.init();
        }

        // Register config using FCAP API
        NeoForgeConfigRegistry.INSTANCE.register(Bonded.MOD_ID, ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);

        // Initialize mod
        Bonded.init();
    }
}
