package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ArmorHealthBonus implements BondBonus {
    public final ResourceLocation id;
    private final int threshold;
    private final float bonus;

    public ArmorHealthBonus(ResourceLocation id, int threshold, float bonus) {
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
        return new AttributeModifierHolder(
                Attributes.MAX_HEALTH,
                new AttributeModifier(Bonded.resource(id.getPath() + "_" + item.getEquipmentSlot()
                        .toString()
                        .toLowerCase()), bonus, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(item.getEquipmentSlot())
        );
    }
}
