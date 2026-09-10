package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if >=26.3 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockTransformers;
*///?} else {
import net.minecraft.world.item.ShovelItem;
//?}
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.3
/*@Mixin(Item.class)*/
//? if <26.3
@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    private void bonded$useOnHead(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
            @Share("bonded$awardPathXp") LocalBooleanRef awardPathXp) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        //? if >=26.3 {
        /*var transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
        if (transformer == null || !transformer.is(BlockTransformers.SHOVEL)) {
            return;
        }
        *///?}

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
