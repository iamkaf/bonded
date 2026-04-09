package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class DigSpeedBonus implements BondBonus {
    private final Identifier id;
    private final int threshold;
    private final float bonus;

    public DigSpeedBonus(Identifier id, int threshold, float bonus) {
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
        return ItemFunctions.isTool(gear) && container.getBond() >= threshold;
    }

    @Override
    public AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        return new AttributeModifierHolder(
                Attributes.MINING_EFFICIENCY,
                new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(EquipmentSlot.MAINHAND)
        );
    }
}
