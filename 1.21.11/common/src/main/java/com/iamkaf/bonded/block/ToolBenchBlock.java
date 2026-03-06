package com.iamkaf.bonded.block;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.amber.api.functions.v1.PlayerFunctions;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!shouldHandle(level, player, hand)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        ItemStack handItem = player.getMainHandItem();

        if (!Bonded.GEAR.hasEnoughLevelsToUpgrade(handItem)) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.tool_bench.item_not_max_level")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.FAIL;
        }

        var leveler = Bonded.GEAR.getLeveler(handItem);
        assert leveler != null;

        TagKey<Item> upgradeItemTag = leveler.getUpgradeIngredient(handItem);

        if (upgradeItemTag == null || !leveler.isUpgradable(handItem)) {
            errorFeedback(level, player, Component.translatable("bonded.tool_bench.no_upgrade_path"));
            return InteractionResult.FAIL;
        }

        if (!ItemFunctions.has(player.getInventory(), upgradeItemTag)) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.tool_bench.missing_ingredient")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.FAIL;
        }

        var upgraded = leveler.transmuteUpgrade(handItem);


        if (upgraded == null || upgraded.isEmpty()) {
            return InteractionResult.FAIL;
        }


        ItemFunctions.consumeIfAvailable(player.getInventory(), upgradeItemTag, 1);
        handItem.shrink(1);
        player.setItemSlot(EquipmentSlot.MAINHAND, upgraded);
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
                    ParticleTypes.CLOUD,
                    pos.getX() + 0.5d,
                    pos.getY() + 1,
                    pos.getZ() + 0.5d,
                    80,
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

        if (!Bonded.CONFIG.enableUpgrading.get()) {
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
