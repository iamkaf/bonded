package com.iamkaf.bonded.rules;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.2 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Repairable;
//?} else {
/*import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;
*///?}

/** The items that repair a piece of gear. */
public record RepairMaterial(HolderSet<Item> items) {
    public boolean isValidRepairItem(ItemStack stack) {
        return stack.is(items);
    }

    /** The repair items the game itself defines for this stack, or null when it cannot be repaired. */
    //? if >=1.21.2 {
    public static @Nullable RepairMaterial ofNative(ItemStack stack) {
        Repairable repairable = stack.get(DataComponents.REPAIRABLE);
        return repairable == null ? null : new RepairMaterial(repairable.items());
    }
    //?} else {
    /*// Before the Repairable component, an item answers for itself, so ask it about every item.
    public static @Nullable RepairMaterial ofNative(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return null;
        }
        Item item = stack.getItem();
        List<Holder<Item>> repairItems = BuiltInRegistries.ITEM.stream()
                .filter(candidate -> item.isValidRepairItem(stack, candidate.getDefaultInstance()))
                .<Holder<Item>>map(Item::builtInRegistryHolder)
                .toList();
        return repairItems.isEmpty() ? null : new RepairMaterial(HolderSet.direct(repairItems));
    }
    *///?}
}
