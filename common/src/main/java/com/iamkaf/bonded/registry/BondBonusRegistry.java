package com.iamkaf.bonded.registry;

import com.iamkaf.amber.api.util.LiteralSetHolder;
import com.iamkaf.bonded.bonuses.AttributeModifierHolder;
import com.iamkaf.bonded.bonuses.BondBonus;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BondBonusRegistry {
    private final LiteralSetHolder<BondBonus> bonuses = new LiteralSetHolder<>();

    public BondBonus register(BondBonus bonus) {
        bonuses.add(bonus);
        return bonus;
    }

    public List<AttributeModifierHolder> getModifiersForItem(ItemStack gear, GearTypeLeveler gearType,
            ItemLevelContainer container) {
        return bonuses.get()
                .stream()
                .filter(bondBonus -> bondBonus.shouldApply(gear, gearType, container))
                .map(bondBonus -> bondBonus.getBonus(gear, gearType, container))
                .toList();
    }
}
