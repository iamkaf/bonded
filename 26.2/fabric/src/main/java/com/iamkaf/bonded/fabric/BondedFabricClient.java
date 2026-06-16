package com.iamkaf.bonded.fabric;

import com.iamkaf.bonded.BondedClient;
import net.fabricmc.api.ClientModInitializer;

public final class BondedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BondedClient.init();
    }
}
