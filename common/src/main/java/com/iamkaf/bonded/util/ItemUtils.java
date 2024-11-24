package com.iamkaf.bonded.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {
    /**
     * Tries to find a player's equipped shield.
     */
    public static @Nullable ItemStack findShield(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof ShieldItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof ShieldItem) {
            return player.getOffhandItem();
        }
        return null;
    }

    /**
     * Tries to find a stack of a type in the player's inventory.
     */
    public static ItemStack tryToFindStack(Player player, ItemStack stack) {
        if (player.getMainHandItem().is(stack.getItem())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(stack.getItem())) {
            return player.getOffhandItem();
        }
        for (var slot : player.getInventory().items) {
            if (slot.is(stack.getItem())) {
                return slot;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Checks if a damage source is a firework rocket and tries to find the associated crossbow.
     */
    public static ItemStack checkForRocketCrossbow(Player player, DamageSource source) {
        if (!(source.getDirectEntity() instanceof FireworkRocketEntity)) {
            return null;
        }
        return tryToFindStack(player, new ItemStack(Items.CROSSBOW));
    }

    public static EquipmentSlotGroup slotToSlotGroup(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> EquipmentSlotGroup.MAINHAND;
            case OFFHAND -> EquipmentSlotGroup.OFFHAND;
            case FEET -> EquipmentSlotGroup.FEET;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case HEAD -> EquipmentSlotGroup.HEAD;
            case BODY -> EquipmentSlotGroup.BODY;
        };
    }

    @SuppressWarnings("deprecation")
    public static void reapplyDefaultAttributeModifiers(ItemStack item) {
        var defaultModifiers = item.getItem()
                .getDefaultInstance()
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers result = ItemAttributeModifiers.EMPTY;

        ItemAttributeModifiers modifiers =
                item.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var mod : modifiers.modifiers()) {
            if (defaultModifiers.modifiers()
                    .stream()
                    .noneMatch(d -> d.modifier().id().equals(mod.modifier().id()))) {
                result = result.withModifierAdded(mod.attribute(), mod.modifier(), mod.slot());
            }
        }
        for (var d : defaultModifiers.modifiers()) {
            result = result.withModifierAdded(d.attribute(), d.modifier(), d.slot());
        }

        item.set(DataComponents.ATTRIBUTE_MODIFIERS, result);
    }
}
