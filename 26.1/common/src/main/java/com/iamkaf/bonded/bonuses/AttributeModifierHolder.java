package com.iamkaf.bonded.bonuses;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeModifierHolder(Holder<Attribute> attribute, AttributeModifier modifier,
                                      EquipmentSlotGroup equipmentSlotGroup) {
}
