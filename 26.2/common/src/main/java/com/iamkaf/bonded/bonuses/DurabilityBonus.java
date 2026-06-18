package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class DurabilityBonus implements BondBonus {
    public final Identifier id;
    private final int threshold;
    private final int bonus;

    public DurabilityBonus(Identifier id, int threshold, int bonus) {
        this.id = id;
        this.threshold = threshold;
        this.bonus = bonus;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        return Bonded.GEAR.isGear(gear)
                && container.getBond() >= threshold
                && ItemUtils.hasDamageableMaxDamage(gear);
    }

    public int bonusAmount() {
        return bonus;
    }

    @Override
    public void modifyItem(ItemStack gear, GearTypeLeveler gearTypeLeveler, ItemLevelContainer container) {
        MaxDamageModifiers.addOrReplaceAdditive(gear, id, bonus);
    }
}
