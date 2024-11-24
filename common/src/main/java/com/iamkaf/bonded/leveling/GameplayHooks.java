package com.iamkaf.bonded.leveling;

import com.iamkaf.amber.api.enchantment.EnchantmentUtils;
import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.BondEvent;
import com.iamkaf.bonded.api.event.GameEvents;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.leveling.levelers.MeleeWeaponsLeveler;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.registry.TierMap;
import com.iamkaf.bonded.util.ItemUtils;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameplayHooks {
    public static void init() {
        BlockEvent.BREAK.register(GameplayHooks::onBlockBreak);
        GameEvents.AWARD_ITEM_EXPERIENCE.register(GameplayHooks::onGenericItemExperience);
        PlayerEvent.CRAFT_ITEM.register(GameplayHooks::onItemCrafted);
        GameEvents.MODIFY_SMITHING_RESULT.register(GameplayHooks::onItemSmithed);
        PlayerEvent.PICKUP_ITEM_POST.register(GameplayHooks::onItemPickedUp);
        GameEvents.SHIELD_BLOCK.register(GameplayHooks::onDamageBlockedByShield);
        EntityEvent.LIVING_HURT.register(GameplayHooks::onEntityHurt);
        BondEvent.ITEM_LEVELED_UP.register(GameplayHooks::onItemLeveledUp);
        // TODO: add wood stripping xp, shovel pathing xp and hoe hoeing xp
    }

    private static void onItemLeveledUp(ItemStack stack, Player player, ItemLevelContainer container,
            Integer integer) {
        var itemLevel = stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get()).getLevel();
        var level = player.level();

        Integer maxLevel = Bonded.CONFIG.levelsToUpgrade.get();

        level.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                itemLevel == maxLevel ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS
        );

        if (!Bonded.CONFIG.sendChatMessages.get()) {
            return;
        }

        MutableComponent message = itemLevel == maxLevel ? Component.translatable("bonded.gameplay.max_level",
                stack.getDisplayName().getString(),
                itemLevel
        ) : Component.translatable("bonded.gameplay.level_up", stack.getDisplayName().getString(), itemLevel);

        player.sendSystemMessage(message.append(Component.literal(String.valueOf(itemLevel))
                .withStyle(ChatFormatting.GOLD)));
    }

    private static void emitProgressEvents(ItemStack item, Player player, int experienceAmount) {
        if (item.isEmpty()) {
            return;
        }

        ItemStack gear = Bonded.GEAR.initComponent(item);
        var container = gear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        assert container != null;

        CompoundEventResult<Integer> result = BondEvent.ITEM_EXPERIENCE_GAINED.invoker()
                .experience(gear, player, container, experienceAmount);

        if (result.interruptsFurtherEvaluation()) {
            return;
        }

        var newExperienceAmount = result.object() != null ? result.object() : experienceAmount;

        boolean hasLeveled = Bonded.GEAR.giveItemExperience(gear, newExperienceAmount);
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

    private static void onGenericItemExperience(Player player, ItemStack stack, int experienceAmount) {
        var leveler = Bonded.GEAR.getLeveler(stack);
        boolean isEligibleItemType = leveler != null;

        if (!isEligibleItemType) {
            return;
        }

        emitProgressEvents(stack, player, experienceAmount);
    }

    private static void onItemCrafted(Player player, ItemStack stack, Container container) {
        player.getInventory().items.forEach(item -> Bonded.GEAR.initComponent(item));
    }

    private static void onItemSmithed(ItemStack stack, List<ItemStack> relevantItems) {
        DataComponentType<ItemLevelContainer> component = DataComponents.ITEM_LEVEL_CONTAINER.get();
        var container = stack.get(component);

        boolean isNetheriteUpgrade = relevantItems.stream()
                .anyMatch(stack1 -> stack1.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        // this is kind of risky but will increase mod compat
        boolean isCompat = relevantItems.stream()
                .anyMatch(stack1 -> stack1.getItem()
                        .arch$registryName()
                        .getPath()
                        .contains("upgrade_smithing_template"));

        if (container != null && (isNetheriteUpgrade || isCompat)) {
            stack.set(component,
                    ItemLevelContainer.make(TierMap.getExperienceCap(stack.getItem()))
                            .addBond(container.getBond())
            );
        }
    }

    private static void onItemPickedUp(Player player, ItemEntity itemEntity, ItemStack stack) {
        for (var i = 0; i < player.getInventory().items.size(); i++) {
            if (i == player.getInventory().selected) {
                continue;
            }
            ItemStack item = player.getInventory().getItem(i);
            if (item.isEmpty()) {
                continue;
            }
            Bonded.GEAR.initComponent(item);
        }
    }

    private static void onDamageBlockedByShield(Player pl, Float damage) {
        if (pl instanceof ServerPlayer player) {
            var shield = ItemUtils.findShield(player);
            if (shield == null) return;

            emitProgressEvents(shield,
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

        GearTypeLeveler leveler = Bonded.GEAR.getLeveler(handItem);
        boolean isMeleeWeapon = leveler instanceof MeleeWeaponsLeveler;

        if (isMeleeWeapon) {
            emitProgressEvents(handItem,
                    player,
                    (int) (amount * Bonded.CONFIG.weaponDamageDealtExperienceGainedMultiplier.get())
            );
        }

        boolean isRangedWeapon = handItem.getItem() instanceof ProjectileWeaponItem;
        boolean isProjectile = source.getDirectEntity() instanceof Projectile;
        if (isRangedWeapon && isProjectile) {
            ItemStack foundBow = ItemUtils.tryToFindStack(player, handItem);
            emitProgressEvents(foundBow,
                    player,
                    (int) (amount * Bonded.CONFIG.weaponDamageDealtExperienceGainedMultiplier.get())
            );
        }

        var playerArmorSlots = player.getArmorSlots();

        for (var slot : playerArmorSlots) {
            if (!Bonded.GEAR.isGear(slot)) continue;
            emitProgressEvents(slot,
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
            emitProgressEvents(slot,
                    player,
                    Bonded.CONFIG.armorDamageTakenExperienceGainedMultiplier.get().intValue()
            );
        }
    }
}
