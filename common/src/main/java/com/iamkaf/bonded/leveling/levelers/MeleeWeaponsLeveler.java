package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

public class MeleeWeaponsLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Melee Weapons";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.MELEE_WEAPONS;
    }

    @Override
    public Ingredient getRepairIngredient(ItemStack gear) {
        Ingredient registeredRepairMaterial = TierMap.getRepairMaterial(gear.getItem());

        if (registeredRepairMaterial != null) {
            return registeredRepairMaterial;
        }

        if (gear.getItem() instanceof TieredItem tieredItem) {
            return tieredItem.getTier().getRepairIngredient();
        }
        if (gear.getItem() instanceof TridentItem) {
            return Ingredient.of(Items.PRISMARINE_SHARD);
        }

        return Ingredient.of(Items.NETHER_STAR);
    }
}
