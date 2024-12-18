package com.iamkaf.bonded.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Tags {
    public static final TagKey<Block> ORES = createBlockTag("c", "ores");
    public static final TagKey<Item> ARMORS = createItemTag("c", "armors");
    public static final TagKey<Item> MELEE_WEAPONS = createItemTag("c", "tools/melee_weapon");
    public static final TagKey<Item> RANGED_WEAPONS = createItemTag("c", "tools/ranged_weapon");
    public static final TagKey<Item> FISHING_RODS = createItemTag("c", "tools/fishing_rod");
    public static final TagKey<Item> BRUSHES = createItemTag("c", "tools/brush");
    public static final TagKey<Item> IGNITERS = createItemTag("c", "tools/igniter");
    public static final TagKey<Item> SHEARS = createItemTag("c", "tools/shear");
    public static final TagKey<Item> DIGGING_EQUIPMENT = createItemTag("bonded", "digging_equipment");
    public static final TagKey<Item> UTILITY_EQUIPMENT = createItemTag("bonded", "utility_tool");
    // TODO: shields, shears, fishing rods, flint and steel

    public static TagKey<Item> createItemTag(String namespace, String tag) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, tag));
    }

    public static TagKey<Block> createBlockTag(String namespace, String tag) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, tag));
    }
}
