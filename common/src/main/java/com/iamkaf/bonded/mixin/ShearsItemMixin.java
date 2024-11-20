package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public abstract class ShearsItemMixin {
    @Inject(method = "mineBlock", at = @At("TAIL"))
    private void bonded$mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos,
            LivingEntity miningEntity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (miningEntity instanceof Player player && !level.isClientSide) {
                GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 1);
            }
        }
    }
}
