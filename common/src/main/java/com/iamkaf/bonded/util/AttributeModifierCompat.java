package com.iamkaf.bonded.util;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Reads and restores an item's own attribute modifiers, which moved into a component in 1.21.2. */
public final class AttributeModifierCompat {
    private AttributeModifierCompat() {
    }

    /** The attribute modifiers the item has before anything is written to the stack. */
    public static ItemAttributeModifiers defaults(ItemStack stack) {
        //? if >=1.21.2 {
        return ItemFunctions.getDefaultAttributeModifiers(stack);
        //?} else {
        /*var item = stack.getItem();
        var component = item.getDefaultInstance()
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        // Armor and other items that override the method keep their modifiers off the component.
        return component.modifiers().isEmpty() ? item.getDefaultAttributeModifiers() : component;
        *///?}
    }

    /** Writes the item's own modifiers to the stack, so adding one more does not hide them. */
    public static void restoreDefaults(ItemStack stack) {
        //? if >=1.21.2 {
        ItemFunctions.restoreDefaultAttributeModifiers(stack);
        //?} else {
        /*var modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var defaultModifier : defaults(stack).modifiers()) {
            var builder = ItemAttributeModifiers.builder();
            for (var modifier : modifiers.modifiers()) {
                if (!modifier.attribute().equals(defaultModifier.attribute())
                        || !modifier.modifier().id().equals(defaultModifier.modifier().id())) {
                    builder.add(modifier.attribute(), modifier.modifier(), modifier.slot());
                }
            }
            builder.add(defaultModifier.attribute(), defaultModifier.modifier(), defaultModifier.slot());
            modifiers = builder.build();
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        *///?}
    }
}
