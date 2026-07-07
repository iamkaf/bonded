package com.iamkaf.bonded.command;

import com.iamkaf.amber.api.event.v1.events.common.CommandEvents;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.loot.WorldInnateBond;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
                .then(Commands.literal("c2me-random")
                        .executes(context -> verifyInnateBondRandomIsThreadSafe(context.getSource())));
    }

    // This verifies Bonded applies innate bond in a thread-safe way.
    private static int verifyInnateBondRandomIsThreadSafe(CommandSourceStack source) {
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
                "Bonded-C2ME-Random-Repro"
        );
        thread.start();

        try {
            thread.join(5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Bonded C2ME random repro", exception);
        }

        Throwable throwable = failure.get();
        if (throwable != null) {
            Bonded.LOGGER.error("Bonded debug C2ME random repro captured the off-thread innate bond failure.", throwable);
            throw new RuntimeException("Bonded debug C2ME random repro captured the off-thread innate bond failure", throwable);
        }

        source.sendSuccess(() -> Component.literal("Bonded C2ME random repro completed without an exception."), false);
        return Command.SINGLE_SUCCESS;
    }
}
