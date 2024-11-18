package com.iamkaf.bonded.registry;

import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.amber.api.util.LiteralSetHolder;
import com.iamkaf.bonded.bonuses.BondBonus;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BondBonusRegistry {
    private final LiteralSetHolder<BondBonus> bonuses = new LiteralSetHolder<>();

    public BondBonus register(BondBonus bonus) {
        bonuses.add(bonus);
        return bonus;
    }

    public void applyBonuses(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        List<BondBonus> bonusToApply = this.bonuses.get()
                .stream()
                .filter(bondBonus -> bondBonus.shouldApply(gear, gearType, container))
                .toList();

        List<ResourceLocation> eee = bonusToApply.stream().map(BondBonus::id).toList();

        gear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(), AppliedBonusesContainer.make(eee));

        var attributeModifierHolders = bonusToApply.stream()
                .map(bondBonus -> bondBonus.getAttributeModifiers(gear, gearType, container))
                .toList();

        for (var bonus : attributeModifierHolders) {
            if (bonus == null) continue;
            ItemHelper.addModifier(gear, bonus.attribute(), bonus.modifier(), bonus.equipmentSlotGroup());
        }
        for (var mod : gear.getItem().getDefaultAttributeModifiers().modifiers()) {
            ItemHelper.addModifier(gear, mod.attribute(), mod.modifier(), mod.slot());
        }
    }
}
