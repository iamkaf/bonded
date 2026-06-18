package com.iamkaf.bonded.block;

import com.iamkaf.amber.api.functions.v1.PlayerFunctions;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.advancement.BondedAdvancements;
import com.iamkaf.bonded.api.event.BondEvent;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.Items;
import com.iamkaf.bonded.util.MaxDamageModifiers;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        if (!level.isClientSide() && hand.equals(InteractionHand.MAIN_HAND) && !Bonded.CONFIG.enableRepairing.get()) {
            PlayerFunctions.sendActionBarMessage(
                    player,
                    Component.translatable("bonded.repair_bench.disabled").withStyle(ChatFormatting.RED)
            );
            return InteractionResult.SUCCESS_SERVER;
        }

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

        if (!MaxDamageModifiers.canRepairWithOverRepair(handItem)) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.repair_bench.fully_repaired")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.SUCCESS_SERVER;
        }

        Optional<ItemStack> repairIngredient = findRepairIngredient(player.getInventory(), repairable);

        if (repairIngredient.isEmpty()) {
            repairIngredient = AdjacentBenchStorage.find(level, pos, item -> isRepairIngredient(item, repairable));
        }

        if (repairIngredient.isEmpty()) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.repair_bench.missing_ingredient")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.SUCCESS_SERVER;
        }

        ItemStack material = repairIngredient.get();
        ItemLevelContainer component = handItem.get(com.iamkaf.bonded.registry.DataComponents.ITEM_LEVEL_CONTAINER.get());
        int repairAmount = Math.round(
                (float) MaxDamageModifiers.getBaseMaxDamage(handItem)
                        * Bonded.CONFIG.durabilityGainedOnRepairBench.get().floatValue()
        );
        repairAmount = BondEvent.MODIFY_REPAIR_AMOUNT.invoker().modify(
                handItem,
                player,
                component,
                material,
                repairAmount
        );
        if (repairAmount <= 0) {
            return InteractionResult.SUCCESS_SERVER;
        }

        Optional<ItemStack> consumedRepairIngredient = consumeRepairIngredient(player.getInventory(), repairable);
        Optional<AdjacentBenchStorage.ConsumedStorageItem> consumedStorageItem = Optional.empty();

        if (consumedRepairIngredient.isEmpty()) {
            consumedStorageItem = AdjacentBenchStorage.consume(level, pos, item -> isRepairIngredient(item, repairable));
            consumedRepairIngredient = consumedStorageItem.map(AdjacentBenchStorage.ConsumedStorageItem::stack);
        }

        if (consumedRepairIngredient.isEmpty()) {
            errorFeedback(
                    level,
                    player,
                    Component.translatable("bonded.repair_bench.missing_ingredient")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.SUCCESS_SERVER;
        }

        material = consumedRepairIngredient.get();
        boolean repaired = MaxDamageModifiers.repairWithOverRepair(handItem, repairAmount);
        if (repaired) {
            BondEvent.ITEM_REPAIRED.invoker().repair(handItem, player, component, material);
            if (player instanceof ServerPlayer serverPlayer) {
                if (MaxDamageModifiers.hasOverRepair(handItem)) {
                    BondedAdvancements.grant(serverPlayer, BondedAdvancements.OVER_REPAIR);
                }
                if (isScrap(material)) {
                    BondedAdvancements.grant(serverPlayer, BondedAdvancements.SCRAP_REPAIR);
                }
            }
        }
        consumedStorageItem.ifPresent(consumed -> AdjacentBenchStorage.emitStorageUseParticles(level, consumed));
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

    private Optional<ItemStack> findRepairIngredient(Inventory inventory, Repairable repairable) {
        Optional<ItemStack> scrap = findFirstMatching(inventory, RepairBenchBlock::isScrap);
        if (scrap.isPresent()) {
            return scrap;
        }

        return findFirstMatching(inventory, repairable::isValidRepairItem);
    }

    private Optional<ItemStack> consumeRepairIngredient(Inventory inventory, Repairable repairable) {
        Optional<ItemStack> scrap = consumeFirstMatching(inventory, RepairBenchBlock::isScrap);
        if (scrap.isPresent()) {
            return scrap;
        }

        return consumeFirstMatching(inventory, repairable::isValidRepairItem);
    }

    private Optional<ItemStack> findFirstMatching(Inventory inventory, java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (!slot.isEmpty() && predicate.test(slot)) {
                return Optional.of(slot.copyWithCount(1));
            }
        }

        return Optional.empty();
    }

    private Optional<ItemStack> consumeFirstMatching(Inventory inventory, java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (predicate.test(slot)) {
                ItemStack consumed = slot.copyWithCount(1);
                slot.shrink(1);
                return Optional.of(consumed);
            }
        }

        return Optional.empty();
    }

    private boolean isRepairIngredient(ItemStack stack, Repairable repairable) {
        return isScrap(stack) || repairable.isValidRepairItem(stack);
    }

    private static boolean isScrap(ItemStack stack) {
        return stack.is(Items.SCRAP.get());
    }

    public static Optional<ItemStack> getInventoryRepairIngredientPreview(Inventory inventory, Repairable repairable) {
        if (hasMatching(inventory, RepairBenchBlock::isScrap)) {
            return Optional.of(Items.SCRAP.get().getDefaultInstance());
        }

        return Optional.of(repairable.items().get(0).value().getDefaultInstance());
    }

    private static boolean hasMatching(Inventory inventory, java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (!slot.isEmpty() && predicate.test(slot)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldHandle(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()|| !hand.equals(InteractionHand.MAIN_HAND)) {
            return false;
        }

        if (!Bonded.CONFIG.enableRepairing.get()) {
            return false;
        }

        var handItem = player.getMainHandItem();

        if (!handItem.isDamageableItem()) {
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
        }

        if (!Bonded.CONFIG.enableRepairing.get()) {
            PlayerFunctions.sendActionBarMessage(
                    player,
                    Component.translatable("bonded.repair_bench.disabled").withStyle(ChatFormatting.RED)
            );
        }

        return InteractionResult.CONSUME;
    }
}
