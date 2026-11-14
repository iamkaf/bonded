package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

public class Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> SCRAP = ITEMS.register("scrap", () -> new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> TEMPERED_GOLD_NUGGET = simple("tempered_gold_nugget");
    public static final RegistrySupplier<Item> TEMPERED_GOLD_INGOT = simple("tempered_gold_ingot");
    public static final RegistrySupplier<Item> TEMPERED_GOLD_SWORD = ITEMS.register(
            "tempered_gold_sword",
            () -> new SwordItem(TemperedGold.TIER, new Item.Properties()
                    .attributes(SwordItem.createAttributes(TemperedGold.TIER, 3, -2.4F)))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_PICKAXE = ITEMS.register(
            "tempered_gold_pickaxe",
            () -> new PickaxeItem(TemperedGold.TIER, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(TemperedGold.TIER, 1.0F, -2.8F)))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_SHOVEL = ITEMS.register(
            "tempered_gold_shovel",
            () -> new ShovelItem(TemperedGold.TIER, new Item.Properties()
                    .attributes(ShovelItem.createAttributes(TemperedGold.TIER, 1.5F, -3.0F)))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_AXE = ITEMS.register(
            "tempered_gold_axe",
            () -> new AxeItem(TemperedGold.TIER, new Item.Properties()
                    .attributes(AxeItem.createAttributes(TemperedGold.TIER, 7.0F, -3.0F)))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_HOE = ITEMS.register(
            "tempered_gold_hoe",
            () -> new HoeItem(TemperedGold.TIER, new Item.Properties()
                    .attributes(HoeItem.createAttributes(TemperedGold.TIER, -1.0F, -1.0F)))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_HELMET = armor("tempered_gold_helmet", ArmorItem.Type.HELMET);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_CHESTPLATE =
            armor("tempered_gold_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_LEGGINGS =
            armor("tempered_gold_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_BOOTS = armor("tempered_gold_boots", ArmorItem.Type.BOOTS);

    public static void init() {
        CreativeModeTabs.addItem(SCRAP);
        CreativeModeTabs.addItem(TEMPERED_GOLD_NUGGET);
        CreativeModeTabs.addItem(TEMPERED_GOLD_INGOT);
        CreativeModeTabs.addItem(TEMPERED_GOLD_SWORD);
        CreativeModeTabs.addItem(TEMPERED_GOLD_PICKAXE);
        CreativeModeTabs.addItem(TEMPERED_GOLD_SHOVEL);
        CreativeModeTabs.addItem(TEMPERED_GOLD_AXE);
        CreativeModeTabs.addItem(TEMPERED_GOLD_HOE);
        CreativeModeTabs.addItem(TEMPERED_GOLD_HELMET);
        CreativeModeTabs.addItem(TEMPERED_GOLD_CHESTPLATE);
        CreativeModeTabs.addItem(TEMPERED_GOLD_LEGGINGS);
        CreativeModeTabs.addItem(TEMPERED_GOLD_BOOTS);
        ITEMS.register();
    }

    private static RegistrySupplier<Item> simple(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static RegistrySupplier<Item> armor(String id, ArmorItem.Type type) {
        return ITEMS.register(id, () -> new ArmorItem(
                TemperedGold.armorMaterial(),
                type,
                new Item.Properties().durability(type.getDurability(TemperedGold.ARMOR_DURABILITY))
        ));
    }
}
