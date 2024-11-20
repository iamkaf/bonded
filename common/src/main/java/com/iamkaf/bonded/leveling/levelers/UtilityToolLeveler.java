package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Objects;

public class UtilityToolLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Utility Tools";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.UTILITY_EQUIPMENT;
    }

    @Override
    public Ingredient getRepairIngredient(ItemStack gear) {
        Ingredient registeredRepairMaterial = TierMap.getRepairMaterial(gear.getItem());

        return Objects.requireNonNullElseGet(registeredRepairMaterial, () -> switch (gear.getItem()) {
            case TieredItem tieredItem -> tieredItem.getTier().getRepairIngredient();
            case ArmorItem armorItem -> armorItem.getMaterial().value().repairIngredient().get();
            default -> Ingredient.of(Items.NETHER_STAR);
        });

    }
}
