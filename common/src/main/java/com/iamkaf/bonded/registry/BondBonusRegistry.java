package com.iamkaf.bonded.registry;

import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.amber.api.util.LiteralSetHolder;
import com.iamkaf.bonded.bonuses.BondBonus;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class BondBonusRegistry {
    private final LiteralSetHolder<BondBonus> bonuses = new LiteralSetHolder<>();

    public BondBonus register(BondBonus bonus) {
        bonuses.add(bonus);
        return bonus;
    }

    @SuppressWarnings("deprecation")
    public void applyBonuses(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        List<BondBonus> bonusToApply = this.bonuses.getSet()
                .stream()
                .filter(bondBonus -> bondBonus.shouldApply(gear, gearType, container))
                .toList();

        var attributeModifierHolders = bonusToApply.stream()
                .map(bondBonus -> bondBonus.getAttributeModifiers(gear, gearType, container))
                .toList();

        List<ItemAttributeModifiers.Entry> defaultModifiers = gear.getItem()
                .getDefaultInstance()
                .getOrDefault(
                        net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                        ItemAttributeModifiers.EMPTY
                )
                .modifiers();

        for (var bonus : attributeModifierHolders) {
            if (bonus == null) continue;
            // this is done so upgraded weapons and tools don't carry over the weaker default stats of the
            // previous tier
            if (defaultModifiers.stream().anyMatch(mod -> mod.modifier().id().equals(bonus.modifier().id())))
                continue;
            ItemHelper.addModifier(gear, bonus.attribute(), bonus.modifier(), bonus.equipmentSlotGroup());
        }

        for (var mod : defaultModifiers) {
            ItemHelper.addModifier(gear, mod.attribute(), mod.modifier(), mod.slot());
        }

        // TODO: i have to come up with something else if i add some other bonus other than the durability
        //  one, that uses .modifyItem()
        gear.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE,
                gear.getItem().getDefaultInstance().getMaxDamage()
        );
        bonusToApply.forEach(bondBonus -> bondBonus.modifyItem(gear, gearType, container));

        List<ResourceLocation> appliedBonuses = bonusToApply.stream().map(BondBonus::id).toList();
        gear.set(DataComponents.APPLIED_BONUSES_CONTAINER.get(),
                AppliedBonusesContainer.make(appliedBonuses)
        );
    }
}
