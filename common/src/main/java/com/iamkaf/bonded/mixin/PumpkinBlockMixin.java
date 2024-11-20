package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin extends Block {
    protected PumpkinBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void bonded$useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!level.isClientSide && stack.getItem() instanceof ShearsItem) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, 10);
        }
    }
}
