package com.iamkaf.bonded.util;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class UpgradeHelper {
    public static boolean isTool(ItemStack item) {
        return item.has(DataComponents.TOOL);
    }

    public static boolean isTool(Item item) {
        return item.getDefaultInstance().has(DataComponents.TOOL);
    }

    public static boolean isWeapon(ItemStack item) {
        return item.has(DataComponents.WEAPON);
    }

    public static boolean isWeapon(Item item) {
        return item.getDefaultInstance().has(DataComponents.WEAPON);
    }

    public static boolean isArmor(ItemStack item) {
        // this assumes all EQUIPPABLE are armor
        return item.has(DataComponents.EQUIPPABLE);
    }

    public static boolean isArmor(Item item) {
        // this assumes all EQUIPPABLE are armor
        return item.getDefaultInstance().has(DataComponents.EQUIPPABLE);
    }

    public static NonNullList<ItemStack> getInventoryItems(Inventory inventory) {
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            items.set(i, inventory.getItem(i));
        }
        return items;
    }

    public static List<ItemStack> getArmorSlots(Player player) {
        return List.of(
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        );
    }
}
