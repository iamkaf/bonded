package com.iamkaf.bonded.command;

import com.iamkaf.bonded.api.augment.Augment;
import com.iamkaf.bonded.api.augment.AugmentApi;
import com.iamkaf.bonded.api.augment.AugmentProgressResult;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

final class AugmentCommands {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_AUGMENT = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.bonded.augment.unknown", id)
    );
    private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEM = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.bonded.augment.no_item", name)
    );
    private static final DynamicCommandExceptionType ERROR_INELIGIBLE_ITEM = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.bonded.augment.ineligible", name)
    );
    private static final SimpleCommandExceptionType ERROR_NO_CHANGE = new SimpleCommandExceptionType(
            Component.translatable("commands.bonded.augment.no_change")
    );

    private AugmentCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("augment")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(createQuery())
                .then(createChange("add", false, 1))
                .then(createChange("set", true, 0));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createQuery() {
        return Commands.literal("query")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(augmentArgument()
                                .executes(AugmentCommands::query)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createChange(
            String name,
            boolean set,
            int minimumAmount
    ) {
        return Commands.literal(name)
                .then(Commands.argument("target", EntityArgument.player())
                        .then(augmentArgument()
                                .then(Commands.argument("amount", IntegerArgumentType.integer(minimumAmount))
                                        .executes(context -> change(context, set)))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> augmentArgument() {
        return Commands.argument("augment", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        AugmentApi.all().stream().map(Augment::id),
                        builder
                ));
    }

    private static int query(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        ItemStack stack = heldItem(player);
        Augment augment = resolve(IdentifierArgument.getId(context, "augment"));
        int progress = AugmentApi.progress(stack, augment);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.bonded.augment.query.success",
                player.getDisplayName(),
                stack.getDisplayName(),
                augment.displayName(),
                progress,
                augment.activationProgress()
        ), false);
        return progress;
    }

    private static int change(CommandContext<CommandSourceStack> context, boolean set) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        ItemStack stack = heldItem(player);
        Augment augment = resolve(IdentifierArgument.getId(context, "augment"));
        int amount = IntegerArgumentType.getInteger(context, "amount");
        AugmentProgressResult result = set
                ? AugmentApi.setProgress(player, stack, augment, amount)
                : AugmentApi.addProgress(player, stack, augment, amount);
        if (result.status() == AugmentProgressResult.Status.INELIGIBLE) {
            throw ERROR_INELIGIBLE_ITEM.create(stack.getDisplayName());
        }
        if (!result.changed()) {
            throw ERROR_NO_CHANGE.create();
        }

        context.getSource().sendSuccess(() -> Component.translatable(
                set ? "commands.bonded.augment.set.success" : "commands.bonded.augment.add.success",
                player.getDisplayName(),
                stack.getDisplayName(),
                augment.displayName(),
                result.currentProgress(),
                augment.activationProgress()
        ), true);
        return result.currentProgress();
    }

    private static ItemStack heldItem(ServerPlayer player) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_HELD_ITEM.create(player.getDisplayName());
        }
        return stack;
    }

    private static Augment resolve(Identifier id) throws CommandSyntaxException {
        return AugmentApi.get(id).orElseThrow(() -> ERROR_UNKNOWN_AUGMENT.create(id));
    }
}
