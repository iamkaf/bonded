package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.ITEM);

    public static void init() {
        ITEMS.register();
    }
}