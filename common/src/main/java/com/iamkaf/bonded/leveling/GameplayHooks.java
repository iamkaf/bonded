package com.iamkaf.bonded.leveling;

import com.iamkaf.amber.api.enchantment.EnchantmentUtils;
import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.BondEvent;
import com.iamkaf.bonded.api.event.GameEvents;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.util.ItemUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GameplayHooks {
    public static void init() {
        BlockEvent.BREAK.register(GameplayHooks::onBlockBreak);
        PlayerEvent.CRAFT_ITEM.register(GameplayHooks::onItemCrafted);
        PlayerEvent.PICKUP_ITEM_POST.register(GameplayHooks::onItemPickedUp);
        GameEvents.SHIELD_BLOCK.register(GameplayHooks::onDamageBlockedByShield);
        EntityEvent.LIVING_HURT.register(GameplayHooks::onEntityHurt);
        // TODO: add wood stripping xp, shovel pathing xp and hoe hoeing xp
    }

    private static void emitProgressEvents(ItemStack item, Player player, int experienceAmount) {
        if (item.isEmpty()) {
            return;
        }

        ItemStack gear = Bonded.GEAR.initComponent(item);
        var container = gear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        assert container != null;

        EventResult result = BondEvent.ITEM_EXPERIENCE_GAINED.invoker()
                .experience(gear, player, container, experienceAmount);

        if (result.interruptsFurtherEvaluation()) {
            return;
        }

        boolean hasLeveled = Bonded.GEAR.giveItemExperience(gear, experienceAmount);
        if (hasLeveled) {
            if (Bonded.CONFIG.enableDurabilityGainOnLevelUp.get()) {
                ItemHelper.repairBy(gear, Bonded.CONFIG.durabilityGainOnLevelUp.get().floatValue());
            }
            BondEvent.ITEM_LEVELED_UP.invoker().level(gear, player, container, container.getLevel());
        }
    }

    private static EventResult onBlockBreak(Level level, BlockPos pos, BlockState state,
            ServerPlayer player, @Nullable IntValue xp) {
        var handItem = player.getMainHandItem();
        if (handItem.isEmpty()) {
            return EventResult.pass();
        }

        var leveler = Bonded.GEAR.getLeveler(handItem);
        boolean isEligibleItemType = leveler != null;

        if (!isEligibleItemType || !(handItem.getItem()).isCorrectToolForDrops(handItem, state)) {
            return EventResult.pass();
        }

        var experienceAmount = getBlockBreakExperienceAmount(level, pos, player);
        emitProgressEvents(handItem, player, experienceAmount);

        return EventResult.pass();
    }

    private static int getBlockBreakExperienceAmount(Level level, BlockPos pos, ServerPlayer player) {
        var hasSilkTouch = EnchantmentUtils.containsEnchantment(player.getMainHandItem(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "silk_touch")
        );
        if (hasSilkTouch) {
            return 1;
        }

        return Bonded.GEAR.getExperienceForBlock(level.getBlockState(pos).getBlock());
    }

    private static void onItemCrafted(Player player, ItemStack stack, Container container) {
        Bonded.GEAR.initComponent(stack);
    }

    private static void onItemPickedUp(Player player, ItemEntity itemEntity, ItemStack stack) {
        Bonded.GEAR.initComponent(stack);
    }

    private static void onDamageBlockedByShield(Player pl, Float damage) {
        if (pl instanceof ServerPlayer player) {
            var shield = ItemUtils.findShield(player);
            if (shield == null) return;

            emitProgressEvents(
                    shield,
                    player,
                    (int) ((damage + 1) * Bonded.CONFIG.armorDamageTakenExperienceGainedMultiplier.get())
            );
        }
    }

    private static EventResult onEntityHurt(LivingEntity entity, DamageSource source, float amount) {
        var level = entity.level();

        if (level.isClientSide) {
            return EventResult.pass();
        }

        if (source.getEntity() instanceof Player player) {
            processPlayerDealtDamage(player, source, amount);
        }
        if (entity instanceof Player player) {
            processPlayerTakenDamage(player, source, amount);
        }

        return EventResult.pass();
    }

    private static void processPlayerDealtDamage(Player player, DamageSource source, float amount) {
        ItemStack handItem = source.getWeaponItem();
        if (handItem == null) {
            handItem = ItemUtils.checkForRocketCrossbow(player, source);
        }
        if (handItem == null || handItem.isEmpty()) {
            return;
        }

        boolean isMeleeWeapon =
                handItem.getItem() instanceof SwordItem || handItem.getItem() instanceof AxeItem || handItem.getItem() instanceof TridentItem;

        if (isMeleeWeapon) {
            emitProgressEvents(
                    handItem,
                    player,
                    (int) (amount * Bonded.CONFIG.weaponDamageDealtExperienceGainedMultiplier.get())
            );
        }

        boolean isRangedWeapon = handItem.getItem() instanceof ProjectileWeaponItem;
        boolean isProjectile = source.getDirectEntity() instanceof Projectile;
        if (isRangedWeapon && isProjectile) {
            ItemStack foundBow = ItemUtils.tryToFindStack(player, handItem);
            emitProgressEvents(
                    foundBow,
                    player,
                    (int) (amount * Bonded.CONFIG.weaponDamageDealtExperienceGainedMultiplier.get())
            );
        }

        var playerArmorSlots = player.getArmorSlots();

        for (var slot : playerArmorSlots) {
            if (!Bonded.GEAR.isGear(slot)) continue;
            emitProgressEvents(
                    slot,
                    player,
                    (int) ((amount * Bonded.CONFIG.weaponDamageDealtExperienceGainedMultiplier.get()) + 1)
            );
        }
    }

    private static void processPlayerTakenDamage(Player player, DamageSource source, float amount) {
        var playerArmorSlots = player.getArmorSlots();

        if (source.getEntity() == null) return;

        for (var slot : playerArmorSlots) {
            if (!Bonded.GEAR.isGear(slot)) continue;
            emitProgressEvents(
                    slot,
                    player,
                    Bonded.CONFIG.armorDamageTakenExperienceGainedMultiplier.get().intValue()
            );
        }
    }
}
