package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.world.item.ItemStack;

public interface BondBonus {
    boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container);
    AttributeModifierHolder getBonus(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container);
}
