package com.iamkaf.bonded;

import com.iamkaf.bonded.Bonded;
import net.minecraftforge.fml.common.Mod;

@Mod(Bonded.MOD_ID)
public class BondedForge {

    public BondedForge() {
        Bonded.init();
    }
}