package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

public class MiningToolsLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Mining Tools";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.DIGGING_EQUIPMENT;
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

        return Ingredient.of(Items.NETHER_STAR);
    }
}
