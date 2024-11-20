package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;

public class DigSpeedBonus implements BondBonus {
    private final ResourceLocation id;
    private final int threshold;
    private final float bonus;

    public DigSpeedBonus(ResourceLocation id, int threshold, float bonus) {
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
        return gear.getItem() instanceof DiggerItem && container.getBond() >= threshold;
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
