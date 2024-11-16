package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.leveling.GearManager;
import com.iamkaf.bonded.registry.Tags;
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
    public int getMaxExperience(ItemStack gear) {
        return 1000;
    }

    @Override
    public boolean isUpgradable(ItemStack gear) {
        return false;
    }

    @Override
    public Item getUpgrade(ItemStack gear) {
        return null;
    }

    @Override
    public ItemStack transmuteUpgrade(ItemStack gear) {
        return gear;
    }

    @Override
    public Ingredient getRepairIngredient(ItemStack gear) {
        if (gear.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial().value().repairIngredient().get();
        }

        return null;
    }

    @Override
    public Ingredient getUpgradeIngredient(ItemStack gear) {
        return null;
    }
}
