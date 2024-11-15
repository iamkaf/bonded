package com.iamkaf.bonded.block;

import com.iamkaf.amber.api.inventory.InventoryHelper;
import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.amber.api.player.FeedbackHelper;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ToolBenchBlock extends Block {
    public ToolBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!shouldHandle(level, player, hand)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        ItemStack handItem = player.getMainHandItem();

        if (!Bonded.GEAR.hasEnoughLevelsToUpgrade(handItem)) {
            errorFeedback(level,
                    player,
                    Component.literal("Item not max level").withStyle(ChatFormatting.RED)
            );
            return ItemInteractionResult.FAIL;
        }

        var leveler = Bonded.GEAR.getLeveler(handItem);
        assert leveler != null;

        var ingredient = leveler.getUpgradeIngredient(handItem);

        if (ingredient == null || ingredient.isEmpty() || !leveler.isUpgradable(handItem)) {
            errorFeedback(level, player, Component.literal("This tier doesn't have an upgrade path."));
            return ItemInteractionResult.FAIL;
        }

        if (!InventoryHelper.has(player.getInventory(), ingredient)) {
            errorFeedback(level,
                    player,
                    Component.literal("Missing 1 " + ItemHelper.getIngredientDisplayName(ingredient))
                            .withStyle(ChatFormatting.RED)
            );
            return ItemInteractionResult.FAIL;
        }

        var upgraded = leveler.transmuteUpgrade(handItem);

        if (upgraded == null || !upgraded.isEmpty()) {
            return ItemInteractionResult.FAIL;
        }

        InventoryHelper.consumeIfAvailable(player.getInventory(), ingredient, 1);
        handItem.shrink(1);
        player.setItemSlot(EquipmentSlot.MAINHAND, upgraded);
        level.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ANVIL_PLACE,
                SoundSource.BLOCKS
        );
        return ItemInteractionResult.SUCCESS;
    }

    private boolean shouldHandle(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND)) {
            return false;
        }

        var handItem = player.getMainHandItem();

        if (!Bonded.GEAR.isGear(handItem)) {
            return false;
        }

        var container = handItem.get(DataComponents.ITEM_LEVEL_CONTAINER.get());

        return container != null;
    }

    private void errorFeedback(Level level, Player player, Component message) {
        level.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.CRAFTER_FAIL,
                SoundSource.PLAYERS
        );
        FeedbackHelper.actionBarMessage(player, message);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }
}
