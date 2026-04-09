package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    private void bonded$useOnHead(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
            @Share("bonded$awardPathXp") LocalBooleanRef awardPathXp) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        boolean isGrassPathCandidate = player != null
                && !level.isClientSide()
                && context.getClickedFace() != Direction.DOWN
                && level.getBlockState(clickedPos).is(Blocks.GRASS_BLOCK)
                && level.getBlockState(clickedPos.above()).isAir();

        awardPathXp.set(isGrassPathCandidate);
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void bonded$useOnReturn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
            @Share("bonded$awardPathXp") LocalBooleanRef awardPathXp) {
        if (!awardPathXp.get() || cir.getReturnValue() != InteractionResult.SUCCESS) {
            return;
        }

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player != null && level.getBlockState(clickedPos).is(Blocks.DIRT_PATH)) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 1);
        }
    }
}
