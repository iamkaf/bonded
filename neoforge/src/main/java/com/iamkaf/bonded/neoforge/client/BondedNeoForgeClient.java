package com.iamkaf.bonded.neoforge.client;

import com.iamkaf.bonded.Bonded;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Bonded.MOD_ID, dist = Dist.CLIENT)
public class BondedNeoForgeClient {
    public BondedNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
