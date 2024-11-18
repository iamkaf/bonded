package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ArmorLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Armors";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.ARMORS;
    }

    @Override
    public Ingredient getRepairIngredient(ItemStack gear) {
        Ingredient registeredRepairMaterial = TierMap.getRepairMaterial(gear.getItem());

        if (registeredRepairMaterial != null) {
            return registeredRepairMaterial;
        }

        if (gear.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial().value().repairIngredient().get();
        }

        return null;
    }
}
