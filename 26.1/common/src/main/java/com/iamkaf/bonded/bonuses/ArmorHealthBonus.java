package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class ArmorHealthBonus implements BondBonus {
    public final Identifier id;
    private final int threshold;
    private final float bonus;

    public ArmorHealthBonus(Identifier id, int threshold, float bonus) {
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
        Equippable equippable = gear.get(DataComponents.EQUIPPABLE);

        if (equippable == null) {
            throw new IllegalStateException("Armor item does not have an Equippable component");
        }

        var slot = equippable.slot();
        return new AttributeModifierHolder(Attributes.MAX_HEALTH,
                new AttributeModifier(Bonded.resource(id.getPath() + "_" + slot.toString().toLowerCase()),
                        bonus,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                ItemUtils.slotToSlotGroup(slot)
        );
    }
}
