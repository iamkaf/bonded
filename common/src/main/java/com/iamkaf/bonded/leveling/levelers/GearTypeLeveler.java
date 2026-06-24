package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.registry.TierMap;
import com.iamkaf.bonded.util.ItemUtils;
import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public interface GearTypeLeveler {
    String name();

    TagKey<Item> tag();

    default boolean supports(ItemStack gear) {
        return true;
    }

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

        MaxDamageModifiers.clearOverRepair(gear);
        var upgradedGear = new ItemStack(upgrade.builtInRegistryHolder(), 1, gear.getComponentsPatch());
        AppliedBonusesContainer previousAppliedBonuses =
                upgradedGear.getOrDefault(DataComponents.APPLIED_BONUSES_CONTAINER.get(), AppliedBonusesContainer.make());
        ItemUtils.reapplyDefaultAttributeModifiers(upgradedGear);
        var container = upgradedGear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        assert container != null;
        upgradedGear.set(
                DataComponents.ITEM_LEVEL_CONTAINER.get(),
                ItemLevelContainer.make(TierMap.getExperienceCap(upgrade)).addBond(container.getBond())
        );
        Bonded.GEAR.bondBonusRegistry.restoreBaseMaxDamage(upgradedGear, previousAppliedBonuses);
        upgradedGear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(), AppliedBonusesContainer.make());
        Bonded.GEAR.initComponent(upgradedGear);
        return upgradedGear;
    }

    default @Nullable TagKey<Item> getUpgradeIngredient(ItemStack gear) {
        return TierMap.getUpgradeMaterial(gear.getItem());
    }
}
