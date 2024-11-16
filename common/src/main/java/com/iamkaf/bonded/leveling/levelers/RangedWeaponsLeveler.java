package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

public class RangedWeaponsLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Ranged Weapons";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.RANGED_WEAPONS;
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
        if (gear.getItem() instanceof TieredItem tieredItem) {
            return tieredItem.getTier().getRepairIngredient();
        }

        return Ingredient.of(Items.NETHER_STAR);
    }

    @Override
    public Ingredient getUpgradeIngredient(ItemStack gear) {
        return null;
    }
}
