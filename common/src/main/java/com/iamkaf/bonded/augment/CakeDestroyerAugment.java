package com.iamkaf.bonded.augment;

import com.iamkaf.amber.api.event.v1.events.common.BlockEvents;
import com.iamkaf.amber.api.event.v1.events.common.EntityEvent;
import com.iamkaf.amber.api.functions.v1.WorldFunctions;
import com.iamkaf.bonded.api.augment.AugmentApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class CakeDestroyerAugment {
    private static final float MINIMUM_DAMAGE = 2.0F;

    private CakeDestroyerAugment() {
    }

    static void init() {
        BlockEvents.BLOCK_BREAK_AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer && isCake(state)) {
                AugmentApi.addProgress(serverPlayer, serverPlayer.getMainHandItem(), Augments.CAKE_DESTROYER, 1);
            }
        });
        EntityEvent.AFTER_DAMAGE.register(CakeDestroyerAugment::afterDamage);
    }

    private static boolean isCake(BlockState state) {
        return state.is(Blocks.CAKE) || state.is(BlockTags.CANDLE_CAKES);
    }

    private static void afterDamage(
            LivingEntity target,
            DamageSource source,
            float baseDamageTaken,
            float damageTaken,
            boolean blocked
    ) {
        if (blocked || damageTaken <= MINIMUM_DAMAGE
                || !(source.getEntity() instanceof ServerPlayer player)
                || source.getDirectEntity() != player) {
            return;
        }

        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            weapon = player.getMainHandItem();
        }
        if (!AugmentApi.isActive(weapon, Augments.CAKE_DESTROYER)) {
            return;
        }

        if (SugarDropChance.matches(target.level().getRandom().nextFloat())) {
            WorldFunctions.dropItem(target.level(), Items.SUGAR, target.position());
        }
    }
}
