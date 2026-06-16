package com.iamkaf.bonded;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point.
 */
public class BondedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Bonded.init();
    }
}
