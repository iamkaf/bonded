package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.util.EquipmentCompat;
import com.iamkaf.bonded.registry.Tags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ArmorLeveler implements GearTypeLeveler {
    @Override
    public String id() {
        return "armor";
    }

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
        EquipmentSlot slot = EquipmentCompat.slot(gear);
        if (slot == null) {
            return false;
        }
        return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                || (slot == EquipmentSlot.BODY && gear.isDamageableItem());
    }
}
