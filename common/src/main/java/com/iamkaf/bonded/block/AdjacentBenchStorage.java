package com.iamkaf.bonded.block;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

final class AdjacentBenchStorage {
    private AdjacentBenchStorage() {
    }

    static boolean has(Level level, BlockPos benchPos, Predicate<ItemStack> predicate) {
        for (BlockPos storagePos : findStorageBlocks(level, benchPos)) {
            Container container = getStorageContainer(level, storagePos);
            if (container == null) {
                continue;
            }

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    static Optional<ItemStack> find(Level level, BlockPos benchPos, Predicate<ItemStack> predicate) {
        for (BlockPos storagePos : findStorageBlocks(level, benchPos)) {
            Container container = getStorageContainer(level, storagePos);
            if (container == null) {
                continue;
            }

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return Optional.of(stack.copyWithCount(1));
                }
            }
        }

        return Optional.empty();
    }

    static Optional<ConsumedStorageItem> consume(Level level, BlockPos benchPos, Predicate<ItemStack> predicate) {
        for (BlockPos storagePos : findStorageBlocks(level, benchPos)) {
            Container container = getStorageContainer(level, storagePos);
            if (container == null) {
                continue;
            }

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty() && predicate.test(stack)) {
                    ItemStack consumed = stack.copyWithCount(1);
                    stack.shrink(1);
                    container.setChanged();
                    return Optional.of(new ConsumedStorageItem(storagePos, consumed));
                }
            }
        }

        return Optional.empty();
    }

    static void emitStorageUseParticles(Level level, ConsumedStorageItem consumed) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = consumed.storagePos();
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, consumed.stack().getItem()),
                    pos.getX() + 0.5d,
                    pos.getY() + 0.8d,
                    pos.getZ() + 0.5d,
                    18,
                    0.25d,
                    0.25d,
                    0.25d,
                    0.05d
            );
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5d,
                    pos.getY() + 1.0d,
                    pos.getZ() + 0.5d,
                    8,
                    0.25d,
                    0.25d,
                    0.25d,
                    0.02d
            );
        }
    }

    private static Set<BlockPos> findStorageBlocks(Level level, BlockPos startPos) {
        Set<BlockPos> visitedBenches = new HashSet<>();
        Set<BlockPos> storageBlocks = new LinkedHashSet<>();
        Queue<BlockPos> benches = new ArrayDeque<>();
        benches.add(startPos.immutable());

        while (!benches.isEmpty()) {
            BlockPos benchPos = benches.remove();
            if (!visitedBenches.add(benchPos)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = benchPos.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);

                if (isBench(adjacentState)) {
                    benches.add(adjacentPos.immutable());
                } else if (isEligibleStorage(adjacentState)) {
                    storageBlocks.add(adjacentPos.immutable());
                }
            }
        }

        return storageBlocks;
    }

    private static boolean isBench(BlockState state) {
        Block block = state.getBlock();
        return block instanceof RepairBenchBlock || block instanceof ToolBenchBlock;
    }

    private static boolean isEligibleStorage(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ChestBlock || block instanceof BarrelBlock;
    }

    private static Container getStorageContainer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, level, pos, true);
        }

        if (block instanceof BarrelBlock && level.getBlockEntity(pos) instanceof Container container) {
            return container;
        }

        return null;
    }

    record ConsumedStorageItem(BlockPos storagePos, ItemStack stack) {
    }
}
