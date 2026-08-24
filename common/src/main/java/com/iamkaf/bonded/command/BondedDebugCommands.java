package com.iamkaf.bonded.command;

import com.iamkaf.amber.api.event.v1.events.common.CommandEvents;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.loot.WorldInnateBond;
import com.iamkaf.bonded.network.BondedNetworking;
import com.iamkaf.bonded.rules.BondedRules;
import com.iamkaf.bonded.rules.GearRule;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Development-only debug commands for runtime compatibility probes.
 */
public final class BondedDebugCommands {
    private BondedDebugCommands() {
    }

    public static void init() {
        CommandEvents.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(command()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("bondeddebug")
                .then(Commands.literal("innate-bond-thread-safety")
                        .executes(context -> verifyInnateBondThreadSafety(context.getSource())))
                .then(Commands.literal("rules")
                        .then(Commands.literal("query")
                                .executes(context -> queryHeldRule(context.getSource())))
                        .then(Commands.literal("preview-remote-view")
                                .executes(context -> previewRemoteRules(context.getSource())))
                        .then(Commands.literal("user-count")
                                .then(Commands.argument("expected", IntegerArgumentType.integer(0))
                                        .executes(context -> verifyUserRuleCount(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "expected")
                                        ))))
                        .then(Commands.literal("active-user-count")
                                .then(Commands.argument("expected", IntegerArgumentType.integer(0))
                                        .executes(context -> verifyActiveUserRuleCount(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "expected")
                                        )))))
                .then(Commands.literal("swimming")
                        .then(Commands.literal("start")
                                .executes(context -> setSwimming(context.getSource(), true)))
                        .then(Commands.literal("stop")
                                .executes(context -> setSwimming(context.getSource(), false))))
                .then(Commands.literal("attack-one-health-target")
                        .executes(context -> attackOneHealthTarget(context.getSource())));
    }

    private static int queryHeldRule(CommandSourceStack source) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        var rule = BondedRules.rule(stack.getItem());
        boolean anvilRepair = false;
        if (rule != null
                && rule.repairMode() == com.iamkaf.bonded.rules.ResolvedGearRule.RepairMode.ITEM
                && rule.repair() != null) {
            var repairId = net.minecraft.resources.Identifier.tryParse(rule.repair());
            if (repairId != null && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(repairId)) {
                anvilRepair = stack.isValidRepairItem(new ItemStack(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(repairId)
                ));
            }
        }
        String answer = rule == null
                ? "Bonded rule " + id + ": tag fallback, cap=" + BondedRules.experienceCap(stack.getItem())
                        + ", anvilRepair=" + anvilRepair
                : "Bonded rule " + id + ": enabled=" + rule.enabled()
                        + ", type=" + rule.type()
                        + ", cap=" + rule.experienceCap()
                        + ", repair=" + rule.repairMode().name().toLowerCase(java.util.Locale.ROOT)
                        + (rule.repair() == null ? "" : ":" + rule.repair())
                        + ", upgrade=" + (rule.upgradeTo() == null ? "none" : rule.upgradeTo())
                        + ", source=" + rule.source().label()
                        + ", anvilRepair=" + anvilRepair;
        source.sendSuccess(() -> Component.literal(answer), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int previewRemoteRules(CommandSourceStack source) throws CommandSyntaxException {
        BondedNetworking.openDebugGearRulesScreen(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal("Opened the remote gear-rule preview."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int verifyUserRuleCount(CommandSourceStack source, int expected) {
        int actual = Bonded.GEAR_RULES_CONFIG.userRules().size();
        source.sendSuccess(() -> Component.literal("Bonded user gear rules: " + actual), false);
        return actual == expected ? Command.SINGLE_SUCCESS : 0;
    }

    private static int verifyActiveUserRuleCount(CommandSourceStack source, int expected) {
        var items = BondedRules.active().rules().entrySet().stream()
                .filter(entry -> entry.getValue().source().kind() == GearRule.Kind.USER)
                .map(java.util.Map.Entry::getKey)
                .sorted()
                .toList();
        int actual = items.size();
        source.sendSuccess(
                () -> Component.literal("Bonded active user gear rules: " + actual + " " + items),
                false
        );
        return actual == expected ? Command.SINGLE_SUCCESS : 0;
    }

    private static int setSwimming(
            CommandSourceStack source,
            boolean swimming
    ) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        player.setSprinting(swimming);
        player.setSwimming(swimming);
        return Command.SINGLE_SUCCESS;
    }

    private static int attackOneHealthTarget(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        float attackStrength = player.getAttackStrengthScale(0.5F);
        if (attackStrength <= 0.9F) {
            source.sendSuccess(() -> Component.literal("Waiting for attack strength: " + attackStrength), false);
            return 0;
        }

        Zombie target = new Zombie(source.getLevel());
        target.setPos(player.getX(), player.getY(), player.getZ() + 1.5D);
        target.setHealth(1.0F);
        if (!source.getLevel().addFreshEntity(target)) {
            return 0;
        }

        float healthBefore = target.getHealth();
        player.attack(target);
        float healthAfter = target.getHealth();
        target.discard();
        source.sendSuccess(
                () -> Component.literal(
                        "Attacked target: health=" + healthBefore + "->" + healthAfter
                ),
                false
        );
        return healthAfter <= 0.0F ? Command.SINGLE_SUCCESS : 0;
    }

    // This verifies that Bonded applies innate bond in a thread-safe way.
    private static int verifyInnateBondThreadSafety(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Zombie zombie = new Zombie(level);
        ItemStack stack = new ItemStack(Items.IRON_SWORD);

        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(
                () -> {
                    try {
                        WorldInnateBond.applyToMonsterEquipment(zombie, stack);
                    } catch (Throwable throwable) {
                        failure.set(throwable);
                    }
                },
                "Bonded-Innate-Bond-Thread-Safety"
        );
        thread.start();

        try {
            thread.join(5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Bonded innate bond thread-safety probe", exception);
        }

        Throwable throwable = failure.get();
        if (throwable != null) {
            Bonded.LOGGER.error("Bonded innate bond thread-safety probe captured an off-thread innate bond failure.", throwable);
            throw new RuntimeException("Bonded innate bond thread-safety probe captured an off-thread innate bond failure", throwable);
        }

        source.sendSuccess(() -> Component.literal("Bonded innate bond thread-safety probe completed without an exception."), false);
        return Command.SINGLE_SUCCESS;
    }
}
