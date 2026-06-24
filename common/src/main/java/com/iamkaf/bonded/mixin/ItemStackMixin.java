package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.util.MaxDamageModifiers;
import com.iamkaf.bonded.loot.ScrapDrops;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private ItemStack bonded$stackBeforeEquipmentBreakDamage;
    @Unique
    private int bonded$equipmentDamageBeforeBreak;
    @Unique
    private boolean bonded$insideDamageApplication;

    /*
     * This is deliberately hooked at the equipped-item entry point. On Minecraft 26.1.2 with
     * NeoForge 26.1.2.22-beta, a broken tool is already gone by the time the lower ServerLevel
     * overload returns, so the Scrap drop code can no longer read the Bond component from it.
     * Taking a copy here keeps the pre-break stack around while still using the normal equipped
     * damage path that Fabric, Forge, and NeoForge all share.
     */
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD")
    )
    private void bonded$captureEquipmentStackBeforeBreakDamage(int amount, LivingEntity owner, EquipmentSlot slot,
            CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        bonded$insideDamageApplication = true;
        bonded$stackBeforeEquipmentBreakDamage = stack.copy();
        bonded$equipmentDamageBeforeBreak = stack.getDamageValue();
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("TAIL")
    )
    private void bonded$dropScrapAfterEquipmentBreakDamage(int amount, LivingEntity owner, EquipmentSlot slot,
            CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (owner instanceof ServerPlayer player
                && bonded$stackBeforeEquipmentBreakDamage != null
                && bonded$didBreakOrReachBreakDamage(stack, bonded$equipmentDamageBeforeBreak)) {
            ScrapDrops.dropFromBrokenGear(bonded$stackBeforeEquipmentBreakDamage, player);
        }
        MaxDamageModifiers.consumeOverRepairIfThresholdReached(stack);
        bonded$insideDamageApplication = false;
        bonded$stackBeforeEquipmentBreakDamage = null;
        bonded$equipmentDamageBeforeBreak = 0;
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("HEAD")
    )
    private void bonded$captureStackDamageApplication(int amount, ServerLevel level, @Nullable ServerPlayer player,
            Consumer<Item> onBreak, CallbackInfo ci) {
        bonded$insideDamageApplication = true;
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private void bonded$consumeOverRepairAfterDamage(int amount, ServerLevel level, @Nullable ServerPlayer player,
            Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        MaxDamageModifiers.consumeOverRepairIfThresholdReached(stack);
        bonded$insideDamageApplication = false;
    }

    @Unique
    private boolean bonded$didBreakOrReachBreakDamage(ItemStack stack, int previousDamage) {
        return stack.isEmpty()
                || (stack.isBroken() && stack.getDamageValue() > previousDamage);
    }

    @Inject(method = "setDamageValue(I)V", at = @At("TAIL"))
    private void bonded$consumeOverRepairAfterDamageValueWrite(int damage, CallbackInfo ci) {
        if (bonded$insideDamageApplication) {
            return;
        }
        MaxDamageModifiers.consumeOverRepairIfThresholdReached((ItemStack) (Object) this);
    }

    @Inject(method = "hurtWithoutBreaking(ILnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void bonded$consumeOverRepairAfterNonBreakingDamage(int amount, Player player, CallbackInfo ci) {
        MaxDamageModifiers.consumeOverRepairIfThresholdReached((ItemStack) (Object) this);
    }
}
