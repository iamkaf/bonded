package com.iamkaf.bonded.bonuses;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeModifierHolder(Attribute attribute, AttributeModifier modifier,
                                      EquipmentSlotGroup equipmentSlotGroup) {
}
