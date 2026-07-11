package com.iamkaf.bonded.loot;

import com.iamkaf.amber.api.event.v1.events.common.BlockEvents;
import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.amber.api.functions.v1.WorldFunctions;
import com.iamkaf.bonded.registry.Items;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class TemperedGoldDrops {
    private static final float DROP_CHANCE = 0.5F;

    private TemperedGoldDrops() {
    }

    public static void init() {
        BlockEvents.BLOCK_BREAK_AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)
                    || !level.dimension().equals(Level.NETHER)
                    || !state.is(Blocks.NETHER_GOLD_ORE)
                    || !serverPlayer.getMainHandItem().getItem().isCorrectToolForDrops(
                            serverPlayer.getMainHandItem(),
                            state
                    )
                    || ItemFunctions.containsEnchantment(
                            serverPlayer.getMainHandItem(),
                            Identifier.fromNamespaceAndPath("minecraft", "silk_touch")
                    )
                    || level.getRandom().nextFloat() >= DROP_CHANCE) {
                return;
            }

            int count = level.getRandom().nextInt(3) + 1;
            WorldFunctions.dropItem(
                    level,
                    new ItemStack(Items.TEMPERED_GOLD_NUGGET.get(), count),
                    new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
            );
        });
    }
}
