package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import com.iamkaf.bonded.util.UpgradeHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class ArmorBonus implements BondBonus {
    private final ResourceLocation id;
    private final int threshold;
    private final float bonus;

    public ArmorBonus(ResourceLocation id, int threshold, float bonus) {
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
        return UpgradeHelper.isArmor(gear) && container.getBond() >= threshold;
    }

    @Override
    public AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        Equippable equippable = gear.get(DataComponents.EQUIPPABLE);

        if (equippable == null) {
            throw new IllegalStateException("Armor item does not have an Equippable component");
        }

        var slot = equippable.slot();

        ResourceLocation idWithEquipmentSlot = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                id.getPath() + "_" + slot.toString().toLowerCase()
        );
        return new AttributeModifierHolder(Attributes.ARMOR,
                new AttributeModifier(idWithEquipmentSlot, bonus, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(slot)
        );
    }
}
