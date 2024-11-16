package com.iamkaf.bonded.leveling.levelers;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface GearTypeLeveler {
    String name();
    TagKey<Item> tag();
    int getMaxExperience(ItemStack gear);
    boolean isUpgradable(ItemStack gear);
    Item getUpgrade(ItemStack gear);
    ItemStack transmuteUpgrade(ItemStack gear);
    Ingredient getRepairIngredient(ItemStack gear);
    Ingredient getUpgradeIngredient(ItemStack gear);
}
