package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public interface GearTypeLeveler {
    String name();

    TagKey<Item> tag();

    default int getMaxExperience(ItemStack gear) {
        return TierMap.getExperienceCap(gear.getItem());
    }

    default boolean isUpgradable(ItemStack gear) {
        return TierMap.getUpgrade(gear.getItem()) != null;
    }

    default @Nullable Item getUpgrade(ItemStack gear) {
        return TierMap.getUpgrade(gear.getItem());
    }

    default @Nullable ItemStack transmuteUpgrade(ItemStack gear) {
        var upgrade = getUpgrade(gear);
        if (upgrade == null) {
            return null;
        }

        return new ItemStack(gear.getItem().arch$holder(), 1, gear.getComponentsPatch());
    }

    Ingredient getRepairIngredient(ItemStack gear);

    default @Nullable Ingredient getUpgradeIngredient(ItemStack gear) {
        return TierMap.getUpgradeMaterial(gear.getItem());
    }
}
