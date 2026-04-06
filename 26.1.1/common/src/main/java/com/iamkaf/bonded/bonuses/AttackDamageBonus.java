package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class AttackDamageBonus implements BondBonus {
    public final Identifier id;
    public final int threshold;
    private final float bonus;

    public AttackDamageBonus(Identifier id, int threshold, float bonus) {
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
        // TODO: fix this magic string
        GearTypeLeveler leveler = Bonded.GEAR.getLeveler(gear);
        if (leveler == null) {
            return false;
        }
        return leveler.name().equals("Melee Weapons") && container.getBond() >= threshold;
    }

    @Override
    public AttributeModifierHolder getAttributeModifiers(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        return new AttributeModifierHolder(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(EquipmentSlot.MAINHAND)
        );
    }
}
