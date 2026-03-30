package com.iamkaf.bonded.util;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.bonuses.AttributeModifierHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Locale;

public final class CombinedModifier {
    private final Holder<Attribute> attribute;
    private final EquipmentSlotGroup equipmentSlotGroup;
    private final AttributeModifier.Operation operation;
    private final ResourceLocation id;
    private double amount;

    public CombinedModifier(AttributeModifierHolder holder) {
        this.attribute = holder.attribute();
        this.equipmentSlotGroup = holder.equipmentSlotGroup();
        this.operation = holder.modifier().operation();
        this.id = buildId(holder.attribute(), holder.equipmentSlotGroup(), holder.modifier().operation());
        this.amount = holder.modifier().amount();
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    public EquipmentSlotGroup equipmentSlotGroup() {
        return equipmentSlotGroup;
    }

    public void combine(AttributeModifier modifier) {
        if (modifier.operation() != operation) {
            throw new IllegalArgumentException("Cannot combine modifiers with different operations");
        }
        amount += modifier.amount();
    }

    public AttributeModifier modifier() {
        return new AttributeModifier(id, amount, operation);
    }

    private static ResourceLocation buildId(
            Holder<Attribute> attribute,
            EquipmentSlotGroup slotGroup,
            AttributeModifier.Operation operation
    ) {
        ResourceLocation attributeId = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
        return Bonded.resource(
                "combined/"
                        + attributeId.getNamespace()
                        + "/"
                        + attributeId.getPath()
                        + "/"
                        + slotGroupName(slotGroup)
                        + "/"
                        + operation.name().toLowerCase(Locale.ROOT)
        );
    }

    private static String slotGroupName(EquipmentSlotGroup slotGroup) {
        if (slotGroup == EquipmentSlotGroup.MAINHAND) return "mainhand";
        if (slotGroup == EquipmentSlotGroup.OFFHAND) return "offhand";
        if (slotGroup == EquipmentSlotGroup.HAND) return "hand";
        if (slotGroup == EquipmentSlotGroup.FEET) return "feet";
        if (slotGroup == EquipmentSlotGroup.LEGS) return "legs";
        if (slotGroup == EquipmentSlotGroup.CHEST) return "chest";
        if (slotGroup == EquipmentSlotGroup.HEAD) return "head";
        if (slotGroup == EquipmentSlotGroup.ARMOR) return "armor";
        if (slotGroup == EquipmentSlotGroup.BODY) return "body";
        if (slotGroup == EquipmentSlotGroup.ANY) return "any";
        return slotGroup.toString()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9/._-]+", "_");
    }
}
