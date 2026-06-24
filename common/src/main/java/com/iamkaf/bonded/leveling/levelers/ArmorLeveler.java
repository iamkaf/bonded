package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class ArmorLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Armors";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.ARMORS;
    }

    @Override
    public boolean supports(ItemStack gear) {
        Equippable equippable = gear.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return false;
        }

        EquipmentSlot slot = equippable.slot();
        return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                || (slot == EquipmentSlot.BODY && ItemUtils.hasDamageableMaxDamage(gear));
    }
}
