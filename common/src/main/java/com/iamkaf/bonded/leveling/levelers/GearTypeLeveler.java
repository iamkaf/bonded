package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
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

        var upgradedGear = new ItemStack(upgrade.arch$holder(), 1, gear.getComponentsPatch());
        var container = upgradedGear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        assert container != null;
        upgradedGear.set(
                DataComponents.ITEM_LEVEL_CONTAINER.get(),
                ItemLevelContainer.make(TierMap.getExperienceCap(upgrade)).addBond(container.getBond())
        );
        upgradedGear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(), null);
        Bonded.GEAR.initComponent(upgradedGear);
        return upgradedGear;
    }

    Ingredient getRepairIngredient(ItemStack gear);

    default @Nullable Ingredient getUpgradeIngredient(ItemStack gear) {
        return TierMap.getUpgradeMaterial(gear.getItem());
    }
}
