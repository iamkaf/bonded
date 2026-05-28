package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> SCRAP = ITEMS.register(
            "scrap",
            () -> new Item(new Item.Properties().setId(id("scrap")))
    );

    public static void init() {
        CreativeModeTabs.addItem(SCRAP);
        ITEMS.register();
    }

    private static ResourceKey<Item> id(String id) {
        return ResourceKey.create(Registries.ITEM, Bonded.resource(id));
    }
}
