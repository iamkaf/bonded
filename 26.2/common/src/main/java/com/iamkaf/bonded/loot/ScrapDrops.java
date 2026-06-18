package com.iamkaf.bonded.loot;

import com.iamkaf.amber.api.event.v1.events.common.LootEvents;
import com.iamkaf.amber.api.event.v1.events.common.EntityEvent;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.advancement.BondedAdvancements;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.registry.Items;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class ScrapDrops {
    private ScrapDrops() {
    }

    public static void init() {
        LootEvents.MODIFY.register((lootTable, addPool) -> {
            if (isMinecraftChestLoot(lootTable)) {
                addPool.accept(scrapChestPool());
            }
        });
        EntityEvent.ENTITY_DEATH.register((entity, source) -> dropFromArmoredEnemy(entity));
    }

    public static void dropFromArmoredEnemy(LivingEntity entity) {
        if (!(entity instanceof Enemy) || entity instanceof Player || !hasArmor(entity)) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        RandomSource random = entity.getRandom();
        if (random.nextFloat() < Bonded.CONFIG.armoredEnemyScrapChance.get()) {
            entity.spawnAtLocation(level, new ItemStack(Items.SCRAP.get()));
        }
    }

    public static void dropFromBrokenGear(ItemStack stack, ServerPlayer player) {
        int count = getBrokenGearScrapCount(stack);
        if (count > 0) {
            player.spawnAtLocation(player.level(), new ItemStack(Items.SCRAP.get(), count));
            BondedAdvancements.grant(player, BondedAdvancements.BROKEN_GEAR_SCRAP);
        }
    }

    public static void dropFromUpgrade(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int count = level.getRandom().nextInt(3);
        if (count > 0) {
            player.spawnAtLocation(serverLevel, new ItemStack(Items.SCRAP.get(), count));
        }
    }

    private static boolean isMinecraftChestLoot(Identifier lootTable) {
        return "minecraft".equals(lootTable.getNamespace()) && lootTable.getPath().startsWith("chests/");
    }

    private static LootPool.Builder scrapChestPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(Bonded.CONFIG.chestScrapChance.get().floatValue()))
                .add(LootItem.lootTableItem(Items.SCRAP.get()))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)));
    }

    private static boolean hasArmor(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !entity.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static int getBrokenGearScrapCount(ItemStack stack) {
        ItemLevelContainer component = stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        if (component == null || component.getBond() <= 0) {
            return 0;
        }

        return Math.min(Bonded.CONFIG.brokenGearScrapCap.get(), Math.max(1, component.getBond() / 100));
    }
}
