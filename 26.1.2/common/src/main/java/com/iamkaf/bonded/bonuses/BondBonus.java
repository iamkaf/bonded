package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface BondBonus {
    Identifier id();

    boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container);

    default AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        return null;
    }

    default void modifyItem(ItemStack gear, GearTypeLeveler gearTypeLeveler, ItemLevelContainer container) {
        // NO-OP
    }
}
