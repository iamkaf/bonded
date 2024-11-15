package com.iamkaf.bonded.fabric.client;

import com.iamkaf.bonded.Bonded;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class BondedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(Bonded.MOD_ID, ConfigurationScreen::new);
    }
}
