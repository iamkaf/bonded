package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
//? if >=26.3 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockTransformers;
*///?} else {
import net.minecraft.world.item.AxeItem;
//?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.3
/*@Mixin(Item.class)*/
//? if <26.3
@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void bonded$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        //? if >=26.3 {
        /*var transformer = stack.get(DataComponents.BLOCK_TRANSFORMER);
        if (transformer == null || !transformer.is(BlockTransformers.AXE)) {
            return;
        }
        *///?}

        if (player != null && !player.level().isClientSide() && cir.getReturnValue() == InteractionResult.SUCCESS) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 1);
        }
    }
}
