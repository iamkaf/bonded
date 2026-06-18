package com.iamkaf.bonded;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Bonded.MOD_ID)
public final class BondedNeoForge {
    public BondedNeoForge(IEventBus eBussy, ModContainer container) {
        // Run our common setup.
        Bonded.init();
    }
}
