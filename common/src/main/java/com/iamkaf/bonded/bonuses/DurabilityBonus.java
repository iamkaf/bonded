package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DurabilityBonus implements BondBonus {
    public final ResourceLocation id;
    private final int threshold;
    private final int bonus;

    public DurabilityBonus(ResourceLocation id, int threshold, int bonus) {
        this.id = id;
        this.threshold = threshold;
        this.bonus = bonus;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        return Bonded.GEAR.isGear(gear) && container.getBond() >= threshold;
    }

    @Override
    public void modifyItem(ItemStack gear, GearTypeLeveler gearTypeLeveler, ItemLevelContainer container) {
        AppliedBonusesContainer bonusContainer = gear.get(DataComponents.APPLIED_BONUSES_CONTAINER.get());
        if (bonusContainer != null && !bonusContainer.bonuses().contains(id)) {
            gear.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, gear.getMaxDamage() + bonus);
        }
    }
}
