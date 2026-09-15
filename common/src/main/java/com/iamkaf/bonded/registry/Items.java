package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
//? if <26.3 {
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
//?}
import net.minecraft.world.item.equipment.ArmorType;

public class Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> SCRAP = ITEMS.register(
            "scrap",
            () -> new Item(new Item.Properties().setId(id("scrap")))
    );

    public static final RegistrySupplier<Item> TEMPERED_GOLD_NUGGET = simple("tempered_gold_nugget");
    public static final RegistrySupplier<Item> TEMPERED_GOLD_INGOT = simple("tempered_gold_ingot");
    public static final RegistrySupplier<Item> TEMPERED_GOLD_SWORD = ITEMS.register(
            "tempered_gold_sword",
            () -> new Item(properties("tempered_gold_sword").sword(TemperedGold.TOOL_MATERIAL, 3.0F, -2.4F))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_PICKAXE = ITEMS.register(
            "tempered_gold_pickaxe",
            () -> new Item(properties("tempered_gold_pickaxe").pickaxe(TemperedGold.TOOL_MATERIAL, 1.0F, -2.8F))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_SHOVEL = ITEMS.register(
            "tempered_gold_shovel",
            //? if >=26.3
            /*() -> new Item(properties("tempered_gold_shovel").shovel(TemperedGold.TOOL_MATERIAL, 1.5F, -3.0F))*/
            //? if <26.3
            () -> new ShovelItem(TemperedGold.TOOL_MATERIAL, 1.5F, -3.0F, properties("tempered_gold_shovel"))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_AXE = ITEMS.register(
            "tempered_gold_axe",
            //? if >=26.3
            /*() -> new Item(properties("tempered_gold_axe").axe(TemperedGold.TOOL_MATERIAL, 7.0F, -3.0F))*/
            //? if <26.3
            () -> new AxeItem(TemperedGold.TOOL_MATERIAL, 7.0F, -3.0F, properties("tempered_gold_axe"))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_HOE = ITEMS.register(
            "tempered_gold_hoe",
            //? if >=26.3
            /*() -> new Item(properties("tempered_gold_hoe").hoe(TemperedGold.TOOL_MATERIAL, -1.0F, -1.0F))*/
            //? if <26.3
            () -> new HoeItem(TemperedGold.TOOL_MATERIAL, -1.0F, -1.0F, properties("tempered_gold_hoe"))
    );
    public static final RegistrySupplier<Item> TEMPERED_GOLD_HELMET = armor("tempered_gold_helmet", ArmorType.HELMET);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_CHESTPLATE =
            armor("tempered_gold_chestplate", ArmorType.CHESTPLATE);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_LEGGINGS =
            armor("tempered_gold_leggings", ArmorType.LEGGINGS);
    public static final RegistrySupplier<Item> TEMPERED_GOLD_BOOTS = armor("tempered_gold_boots", ArmorType.BOOTS);

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
        return ITEMS.register(id, () -> new Item(properties(id)));
    }

    private static RegistrySupplier<Item> armor(String id, ArmorType type) {
        return ITEMS.register(id, () -> new Item(properties(id).humanoidArmor(TemperedGold.ARMOR_MATERIAL, type)));
    }

    private static Item.Properties properties(String id) {
        return new Item.Properties().setId(Items.id(id));
    }

    private static ResourceKey<Item> id(String id) {
        return ResourceKey.create(Registries.ITEM, Bonded.resource(id));
    }
}
