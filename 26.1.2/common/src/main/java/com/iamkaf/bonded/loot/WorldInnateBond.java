package com.iamkaf.bonded.loot;

import com.iamkaf.amber.api.event.v1.events.common.EntityEvent;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class WorldInnateBond {
    private WorldInnateBond() {
    }

    public static void init() {
        EntityEvent.ENTITY_SPAWN.register(WorldInnateBond::onEntitySpawn);
    }

    public static void onEntitySpawn(Entity entity, Level level) {
        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity) || !(entity instanceof Enemy)) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getItemBySlot(slot);
            applyToMonsterEquipment(livingEntity, stack);
        }
    }

    public static void applyToMonsterEquipment(LivingEntity livingEntity, ItemStack stack) {
        if (livingEntity.level().isClientSide() || !(livingEntity instanceof Enemy) || livingEntity instanceof Player) {
            return;
        }

        applyToCreatedGear(stack, livingEntity.level().getRandom());
    }

    public static void applyToLoot(ItemStack stack, ContextKeySet paramSet, LootContext context) {
        if (paramSet == LootContextParamSets.CHEST || isEnemyEntityLoot(paramSet, context)) {
            applyToCreatedGear(stack, context.getRandom());
        }
    }

    private static boolean isEnemyEntityLoot(ContextKeySet paramSet, LootContext context) {
        if (paramSet != LootContextParamSets.ENTITY) {
            return false;
        }

        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        return entity instanceof Enemy && !(entity instanceof Player);
    }

    private static void applyToCreatedGear(ItemStack stack, RandomSource random) {
        if (!Bonded.CONFIG.enableInnateLootBond.get() || stack.isEmpty()) {
            return;
        }

        if (stack.has(DataComponents.ITEM_LEVEL_CONTAINER.get())) {
            return;
        }

        int bond = getRandomInnateBond(random);
        if (bond <= 0) {
            return;
        }

        Bonded.GEAR.initComponentWithBond(stack, bond);
    }

    private static int getRandomInnateBond(RandomSource random) {
        int configuredMin = Bonded.CONFIG.innateLootBondMin.get();
        int configuredMax = Bonded.CONFIG.innateLootBondMax.get();
        int min = Math.min(configuredMin, configuredMax);
        int max = Math.max(configuredMin, configuredMax);
        return min + random.nextInt(max - min + 1);
    }
}
