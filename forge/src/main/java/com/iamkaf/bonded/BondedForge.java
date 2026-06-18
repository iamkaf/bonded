package com.iamkaf.bonded;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;

@Mod(Bonded.MOD_ID)
public class BondedForge {

    public BondedForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BondedClient.init();
        }

        // Initialize mod
        Bonded.init();
    }
}
