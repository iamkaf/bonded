package com.iamkaf.bonded.block;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.amber.api.functions.v1.PlayerFunctions;
import com.iamkaf.bonded.Bonded;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class RepairBenchBlock extends Block {
    public RepairBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!shouldHandle(level, player, hand)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        ItemStack handItem = Bonded.GEAR.initComponent(player.getMainHandItem());

        var leveler = Bonded.GEAR.getLeveler(handItem);
        assert leveler != null;
        Repairable repairable = handItem.get(DataComponents.REPAIRABLE);

        if (repairable == null) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> items = ItemFunctions.getInventoryItems(inventory);

        if (items.stream().noneMatch(repairable::isValidRepairItem)) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.repair_bench.missing_ingredient")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.FAIL;
        }

        ItemFunctions.repairBy(handItem, Bonded.CONFIG.durabilityGainedOnRepairBench.get().floatValue());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var slot = inventory.getItem(i);
            if (repairable.isValidRepairItem(slot)) {
                slot.shrink(1);
                break;
            }
        }
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ANVIL_PLACE,
                SoundSource.BLOCKS
        );
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5d,
                    pos.getY() + 1,
                    pos.getZ() + 0.5d,
                    10,
                    0.01d,
                    0.5d,
                    0.01d,
                    0.05d
            );
        }
        return InteractionResult.SUCCESS;
    }

    private boolean shouldHandle(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()|| !hand.equals(InteractionHand.MAIN_HAND)) {
            return false;
        }

        if (!Bonded.CONFIG.enableRepairing.get()) {
            return false;
        }

        var handItem = player.getMainHandItem();

        if (!handItem.isDamaged()) {
            return false;
        }

        return Bonded.GEAR.isGear(handItem);
    }

    private void errorFeedback(Level level, Player player, Component message) {
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.CRAFTER_FAIL,
                SoundSource.PLAYERS
        );
        PlayerFunctions.sendActionBarMessage(player, message);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }
}
