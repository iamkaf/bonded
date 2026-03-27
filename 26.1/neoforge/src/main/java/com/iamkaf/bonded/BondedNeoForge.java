package com.iamkaf.bonded;

import com.iamkaf.bonded.Bonded;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Bonded.MOD_ID)
public final class BondedNeoForge {
    public BondedNeoForge(IEventBus eBussy, ModContainer container) {
        // Run our common setup.
        Bonded.init();
        container.registerConfig(ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);
    }
}
