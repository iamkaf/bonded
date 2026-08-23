package com.iamkaf.bonded;

import com.iamkaf.konfig.forge.api.v1.KonfigForgeClientScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;

@Mod(Bonded.MOD_ID)
public class BondedForge {

    public BondedForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            KonfigForgeClientScreens.register(Bonded.MOD_ID);
            BondedClient.init();
        }

        // Initialize mod
        Bonded.init();
    }
}
