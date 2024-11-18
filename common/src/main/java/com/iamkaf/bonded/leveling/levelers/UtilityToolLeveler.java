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
            case ShearsItem shearsItem -> Ingredient.of(Items.IRON_INGOT);
            case FlintAndSteelItem flintAndSteelItem -> Ingredient.of(Items.FLINT);
            case FishingRodItem fishingRodItem -> Ingredient.of(Items.STRING);
            case BrushItem brushItem -> Ingredient.of(Items.COPPER_INGOT);
            default -> Ingredient.of(Items.NETHER_STAR);
        });

    }
}
