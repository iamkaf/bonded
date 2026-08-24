package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.rules.BondedRules;
import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public interface GearTypeLeveler {
    String id();

    String name();

    TagKey<Item> tag();

    default boolean supports(ItemStack gear) {
        return gear.isDamageableItem();
    }

    default int getMaxExperience(ItemStack gear) {
        return BondedRules.experienceCap(gear.getItem());
    }

    default boolean isUpgradable(ItemStack gear) {
        return BondedRules.upgrade(gear.getItem()) != null;
    }

    default @Nullable Item getUpgrade(ItemStack gear) {
        return BondedRules.upgrade(gear.getItem());
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
        ItemFunctions.restoreDefaultAttributeModifiers(upgradedGear);
        var container = upgradedGear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        assert container != null;
        upgradedGear.set(
                DataComponents.ITEM_LEVEL_CONTAINER.get(),
                ItemLevelContainer.make(BondedRules.experienceCap(upgrade)).addBond(container.getBond())
        );
        Bonded.GEAR.bondBonusRegistry.restoreBaseMaxDamage(upgradedGear, previousAppliedBonuses);
        upgradedGear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(), AppliedBonusesContainer.make());
        Bonded.GEAR.initComponent(upgradedGear);
        return upgradedGear;
    }

    default @Nullable TagKey<Item> getUpgradeIngredient(ItemStack gear) {
        return BondedRules.upgradeIngredient(gear.getItem());
    }
}
