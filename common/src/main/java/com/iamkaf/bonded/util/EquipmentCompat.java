package com.iamkaf.bonded.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.2 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
//?} else {
/*import net.minecraft.world.item.Equipable;
*///?}

/** Reads which slot an item is worn in, whose source changed in 1.21.2. */
public final class EquipmentCompat {
    private EquipmentCompat() {
    }

    /** The slot the stack is worn in, or null when it is not wearable. */
    public static @Nullable EquipmentSlot slot(ItemStack stack) {
        //? if >=1.21.2 {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable == null ? null : equippable.slot();
        //?} else {
        /*Equipable equipable = Equipable.get(stack);
        return equipable == null ? null : equipable.getEquipmentSlot();
        *///?}
    }
}
