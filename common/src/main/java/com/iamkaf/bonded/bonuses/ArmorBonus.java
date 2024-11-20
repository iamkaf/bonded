package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

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
        return gear.getItem() instanceof ArmorItem && container.getBond() >= threshold;
    }

    @Override
    public AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        ArmorItem item = (ArmorItem) gear.getItem();
        ResourceLocation idWithEquipmentSlot = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                id.getPath() + "_" + item.getEquipmentSlot().toString().toLowerCase()
        );
        return new AttributeModifierHolder(Attributes.ARMOR,
                new AttributeModifier(idWithEquipmentSlot, bonus, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(item.getEquipmentSlot())
        );
    }
}
