package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin {
    @Inject(method = "brushingCompleted", at = @At("TAIL"))
    private void bonded$brushingCompleted(ServerLevel level, LivingEntity brusher, ItemStack stack, CallbackInfo ci) {
        if (brusher instanceof Player player) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 25);
        }
    }
}
