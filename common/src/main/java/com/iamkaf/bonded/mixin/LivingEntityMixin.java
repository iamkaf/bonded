package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.loot.WorldInnateBond;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Before 1.21.2 LivingEntity.setItemSlot is abstract, and Mob implements it for monsters.
//? if >=1.21.2
@Mixin(LivingEntity.class)
//? if <1.21.2
/*@Mixin(net.minecraft.world.entity.Mob.class)*/
public class LivingEntityMixin {
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    private void bonded$applyInnateBondToMonsterEquipment(EquipmentSlot slot, ItemStack itemStack, CallbackInfo ci) {
        WorldInnateBond.applyToMonsterEquipment((LivingEntity) (Object) this, itemStack);
    }
}
