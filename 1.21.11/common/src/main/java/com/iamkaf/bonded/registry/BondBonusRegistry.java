package com.iamkaf.bonded.registry;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.bonuses.AttributeModifierHolder;
import com.iamkaf.bonded.bonuses.BondBonus;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.CombinedModifier;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BondBonusRegistry {
    private final List<BondBonus> bonuses = new ArrayList<>();

    public BondBonus register(BondBonus bonus) {
        bonuses.add(bonus);
        return bonus;
    }

    @SuppressWarnings("deprecation")
    public void applyBonuses(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        List<BondBonus> bonusToApply = this.bonuses
                .stream()
                .filter(bondBonus -> bondBonus.shouldApply(gear, gearType, container))
                .toList();

        List<AttributeModifierHolder> attributeModifierHolders = bonusToApply.stream()
                .map(bondBonus -> bondBonus.getAttributeModifiers(gear, gearType, container))
                .toList();

        stripManagedBondedAttributeModifiers(gear);
        ItemUtils.reapplyDefaultAttributeModifiers(gear);

        for (var combinedModifier : combineAttributeModifiers(attributeModifierHolders)) {
            ItemFunctions.addModifier(
                    gear,
                    combinedModifier.attribute(),
                    combinedModifier.modifier(),
                    combinedModifier.equipmentSlotGroup()
            );
        }

        // TODO: i have to come up with something else if i add some other bonus other than the durability
        //  one, that uses .modifyItem()
        gear.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE,
                gear.getItem().getDefaultInstance().getMaxDamage()
        );
        bonusToApply.forEach(bondBonus -> bondBonus.modifyItem(gear, gearType, container));

        List<Identifier> appliedBonuses = bonusToApply.stream().map(BondBonus::id).toList();
        gear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(),
                AppliedBonusesContainer.make(appliedBonuses)
        );
    }

    public boolean hasLegacyManagedAttributeModifiers(ItemStack gear) {
        Set<Identifier> defaultModifierIds = getDefaultModifierIds(gear);
        Map<ModifierAggregationKey, Integer> counts = new HashMap<>();
        boolean hasUncombinedBondedModifier = false;

        for (var modifier : gear.getOrDefault(
                net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY
        ).modifiers()) {
            if (!isManagedBondedModifier(modifier.modifier(), defaultModifierIds)) {
                continue;
            }

            counts.merge(
                    new ModifierAggregationKey(modifier.attribute(), modifier.slot(), modifier.modifier().operation()),
                    1,
                    Integer::sum
            );

            if (!modifier.modifier().id().getPath().startsWith("combined/")) {
                hasUncombinedBondedModifier = true;
            }
        }

        return hasUncombinedBondedModifier || counts.values().stream().anyMatch(count -> count > 1);
    }

    private List<CombinedModifier> combineAttributeModifiers(List<AttributeModifierHolder> attributeModifierHolders) {
        Map<ModifierAggregationKey, CombinedModifier> combinedModifiers = new LinkedHashMap<>();

        for (var bonus : attributeModifierHolders) {
            if (bonus == null) {
                continue;
            }

            ModifierAggregationKey key = new ModifierAggregationKey(
                    bonus.attribute(),
                    bonus.equipmentSlotGroup(),
                    bonus.modifier().operation()
            );

            combinedModifiers.compute(key, (ignored, existing) -> {
                if (existing == null) {
                    return new CombinedModifier(bonus);
                }

                existing.combine(bonus.modifier());
                return existing;
            });
        }

        return List.copyOf(combinedModifiers.values());
    }

    private void stripManagedBondedAttributeModifiers(ItemStack gear) {
        Set<Identifier> defaultModifierIds = getDefaultModifierIds(gear);
        ItemAttributeModifiers existingModifiers = gear.getOrDefault(
                net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY
        );
        var builder = ItemAttributeModifiers.builder();

        for (var modifier : existingModifiers.modifiers()) {
            if (!isManagedBondedModifier(modifier.modifier(), defaultModifierIds)) {
                builder.add(modifier.attribute(), modifier.modifier(), modifier.slot());
            }
        }

        gear.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private Set<Identifier> getDefaultModifierIds(ItemStack gear) {
        Set<Identifier> defaultModifierIds = new HashSet<>();
        for (var modifier : gear.getItem()
                .getDefaultInstance()
                .getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                .modifiers()) {
            defaultModifierIds.add(modifier.modifier().id());
        }
        return defaultModifierIds;
    }

    private boolean isManagedBondedModifier(AttributeModifier modifier, Set<Identifier> defaultModifierIds) {
        return modifier.id().getNamespace().equals(Bonded.MOD_ID) && !defaultModifierIds.contains(modifier.id());
    }

    private record ModifierAggregationKey(Holder<Attribute> attribute, EquipmentSlotGroup slotGroup,
                                          AttributeModifier.Operation operation) {
    }
}
