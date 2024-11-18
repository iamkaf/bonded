package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DurabilityBonus1k implements BondBonus {
    public static final ResourceLocation ID = Bonded.resource("durability_per_bond_1k");
    private static final int THRESHOLD = 1000;
    private static final int INCREASE = 100;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        return Bonded.GEAR.isGear(gear) && container.getBond() >= THRESHOLD;
    }

    @Override
    public void modifyItem(ItemStack gear, GearTypeLeveler gearTypeLeveler, ItemLevelContainer container) {
        AppliedBonusesContainer bonusContainer = gear.get(DataComponents.APPLIED_BONUSES_CONTAINER.get());
        if (bonusContainer != null && !bonusContainer.bonuses().contains(ID)) {
            gear.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, gear.getMaxDamage() + INCREASE);
        }
    }
}
