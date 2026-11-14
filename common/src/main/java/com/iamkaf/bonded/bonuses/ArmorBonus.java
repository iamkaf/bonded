package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.util.EquipmentCompat;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ArmorBonus implements BondBonus {
    private final Identifier id;
    private final int threshold;
    private final float bonus;

    public ArmorBonus(Identifier id, int threshold, float bonus) {
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
        return ItemFunctions.isArmor(gear) && container.getBond() >= threshold;
    }

    @Override
    public AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        var slot = EquipmentCompat.slot(gear);
        if (slot == null) {
            throw new IllegalStateException("Armor item cannot be equipped");
        }

        Identifier idWithEquipmentSlot = Identifier.fromNamespaceAndPath(id.getNamespace(),
                id.getPath() + "_" + slot.toString().toLowerCase()
        );
        return new AttributeModifierHolder(Attributes.ARMOR,
                new AttributeModifier(idWithEquipmentSlot, bonus, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(slot)
        );
    }
}
