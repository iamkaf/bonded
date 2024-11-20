package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin {
    @Inject(method = "brushingCompleted", at = @At("TAIL"))
    private void bonded$brushingCompleted(Player player, CallbackInfo ci) {
        ItemStack stack = bonded$findBrush(player);
        if (stack != null && !player.level().isClientSide) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 25);
        }
    }

    @Unique
    private @Nullable ItemStack bonded$findBrush(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(Items.BRUSH)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(Items.BRUSH)) {
            return offHand;
        }
        return null;
    }
}
