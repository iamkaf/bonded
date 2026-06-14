package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.loot.WorldInnateBond;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    private void bonded$applyInnateBondToMonsterEquipment(EquipmentSlot slot, ItemStack itemStack, CallbackInfo ci) {
        WorldInnateBond.applyToMonsterEquipment((LivingEntity) (Object) this, itemStack);
    }
}
