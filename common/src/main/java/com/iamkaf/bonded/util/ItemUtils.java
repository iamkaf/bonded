package com.iamkaf.bonded.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
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
}
