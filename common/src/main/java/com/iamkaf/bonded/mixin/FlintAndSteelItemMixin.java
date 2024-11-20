package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void bonded$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().indicateItemUse()) {
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            if (!player.level().isClientSide) {
                GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 1);
            }
        }
    }
}
