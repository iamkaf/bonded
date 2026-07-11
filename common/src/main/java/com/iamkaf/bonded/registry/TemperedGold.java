package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class TemperedGold {
    public static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            500,
            4.0F,
            3.0F,
            52,
            Tags.TEMPERED_GOLD_TOOL_MATERIALS
    );

    public static final ResourceKey<EquipmentAsset> EQUIPMENT_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Bonded.resource("tempered_gold")
    );

    public static final ArmorMaterial ARMOR_MATERIAL = new ArmorMaterial(
            8,
            ArmorMaterials.GOLD.defense(),
            ArmorMaterials.GOLD.enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_GOLD,
            ArmorMaterials.GOLD.toughness(),
            ArmorMaterials.GOLD.knockbackResistance(),
            Tags.REPAIRS_TEMPERED_GOLD_ARMOR,
            EQUIPMENT_ASSET
    );

    private TemperedGold() {
    }
}
