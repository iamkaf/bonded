package com.iamkaf.bonded.command;

import com.iamkaf.amber.api.commands.v1.SimpleCommands;
import com.iamkaf.amber.api.event.v1.events.common.CommandEvents;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public final class BondedCommands {
    private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEM = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.bonded.item_experience.no_item", name)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_HELD_ITEM = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.bonded.item_experience.invalid_item", name)
    );
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(
            Component.translatable("commands.bonded.item_experience.set.points.invalid")
    );
    private static final SimpleCommandExceptionType ERROR_SET_LEVELS_INVALID = new SimpleCommandExceptionType(
            Component.translatable("commands.bonded.item_experience.set.levels.invalid")
    );
    private static final SimpleCommandExceptionType ERROR_NO_CHANGE = new SimpleCommandExceptionType(
            Component.translatable("commands.bonded.item_experience.no_change")
    );

    private BondedCommands() {
    }

    public static void init() {
        CommandEvents.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(createBaseCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createBaseCommand() {
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = SimpleCommands.createBaseCommand(Bonded.MOD_ID);
        baseCommand.then(createExperienceCommand("experience"));
        baseCommand.then(createExperienceCommand("xp"));
        return baseCommand;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createExperienceCommand(String name) {
        return Commands.literal(name)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(createAddCommand())
                .then(createSetCommand())
                .then(createQueryCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAddCommand() {
        return Commands.literal("add")
                .then(Commands.argument("target", EntityArgument.players())
                        .then(createAmountCommand("amount", IntegerArgumentType.integer(), Type.POINTS, BondedCommands::addItemExperience))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSetCommand() {
        return Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.players())
                        .then(
                                ((RequiredArgumentBuilder<CommandSourceStack, Integer>) Commands.argument(
                                        "amount",
                                        IntegerArgumentType.integer(0)
                                ).executes(context -> setExperience(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "target"),
                                        IntegerArgumentType.getInteger(context, "amount"),
                                        Type.POINTS
                                )))
                                        .then(Commands.literal("points")
                                                .executes(context -> setExperience(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "target"),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        Type.POINTS
                                                )))
                                        .then(Commands.literal("levels")
                                                .executes(context -> setExperience(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "target"),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        Type.LEVELS
                                                )))
                        ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createQueryCommand() {
        return Commands.literal("query")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("points")
                                .executes(context -> queryExperience(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target"),
                                        Type.POINTS
                                )))
                        .then(Commands.literal("levels")
                                .executes(context -> queryExperience(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target"),
                                        Type.LEVELS
                                ))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> createAmountCommand(
            String name,
            IntegerArgumentType argumentType,
            Type defaultType,
            ExperienceOperation operation
    ) {
        return ((RequiredArgumentBuilder<CommandSourceStack, Integer>) Commands.argument(name, argumentType)
                .executes(context -> operation.run(
                        context.getSource(),
                        EntityArgument.getPlayers(context, "target"),
                        IntegerArgumentType.getInteger(context, name),
                        defaultType
                )))
                .then(Commands.literal("points")
                        .executes(context -> operation.run(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "target"),
                                IntegerArgumentType.getInteger(context, name),
                                Type.POINTS
                        )))
                .then(Commands.literal("levels")
                        .executes(context -> operation.run(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "target"),
                                IntegerArgumentType.getInteger(context, name),
                                Type.LEVELS
                        )));
    }

    private static int queryExperience(CommandSourceStack source, ServerPlayer player, Type type) throws CommandSyntaxException {
        ItemStack stack = getTargetItem(player);
        int value = type.query.applyAsInt(stack);
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.bonded.item_experience.query." + type.name,
                        player.getDisplayName(),
                        stack.getDisplayName(),
                        value
                ),
                false
        );
        return value;
    }

    private static int addItemExperience(
            CommandSourceStack source,
            Collection<? extends ServerPlayer> targets,
            int amount,
            Type type
    ) throws CommandSyntaxException {
        int changed = 0;

        for (ServerPlayer player : targets) {
            ItemStack stack = getTargetItem(player);
            if (type.add(player, stack, amount)) {
                changed++;
            }
        }

        if (changed == 0) {
            throw ERROR_NO_CHANGE.create();
        }

        if (changed == 1) {
            ServerPlayer player = targets.iterator().next();
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.bonded.item_experience.add." + type.name + ".success.single",
                            amount,
                            player.getDisplayName(),
                            player.getMainHandItem().getDisplayName()
                    ),
                    true
            );
        } else {
            int changedCount = changed;
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.bonded.item_experience.add." + type.name + ".success.multiple",
                            amount,
                            changedCount
                    ),
                    true
            );
        }

        return changed;
    }

    private static int setExperience(
            CommandSourceStack source,
            Collection<? extends ServerPlayer> targets,
            int amount,
            Type type
    ) throws CommandSyntaxException {
        int changed = 0;

        for (ServerPlayer player : targets) {
            ItemStack stack = getTargetItem(player);
            if (type.set(stack, amount)) {
                changed++;
            }
        }

        if (changed == 0) {
            throw type == Type.POINTS ? ERROR_SET_POINTS_INVALID.create() : ERROR_SET_LEVELS_INVALID.create();
        }

        if (changed == 1) {
            ServerPlayer player = targets.iterator().next();
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.bonded.item_experience.set." + type.name + ".success.single",
                            amount,
                            player.getDisplayName(),
                            player.getMainHandItem().getDisplayName()
                    ),
                    true
            );
        } else {
            int changedCount = changed;
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.bonded.item_experience.set." + type.name + ".success.multiple",
                            amount,
                            changedCount
                    ),
                    true
            );
        }

        return changed;
    }

    private static ItemStack getTargetItem(ServerPlayer player) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_HELD_ITEM.create(player.getDisplayName());
        }

        if (Bonded.GEAR.getLeveler(stack) == null) {
            throw ERROR_INVALID_HELD_ITEM.create(player.getDisplayName());
        }

        Bonded.GEAR.initComponent(stack);
        return stack;
    }

    private static ItemLevelContainer getContainer(ItemStack stack) {
        return Objects.requireNonNull(stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get()));
    }

    private static boolean addPoints(ServerPlayer player, ItemStack stack, int amount) {
        ItemLevelContainer before = stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
        if (amount >= 0) {
            GameEvents.AWARD_ITEM_EXPERIENCE.invoker().experience(player, stack, amount);
        } else {
            ItemLevelContainer current = getContainer(stack);
            int newAmount = Math.max(0, current.getExperience() + amount);
            stack.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), current.withExperience(newAmount));
        }
        return !Objects.equals(before, stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get()));
    }

    private static boolean setPoints(ItemStack stack, int amount) {
        ItemLevelContainer current = getContainer(stack);
        if (amount >= current.getMaxExperience()) {
            return false;
        }

        if (current.getExperience() == amount) {
            return false;
        }

        stack.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), current.withExperience(amount));
        return true;
    }

    private static boolean addLevels(ServerPlayer player, ItemStack stack, int amount) {
        ItemLevelContainer current = getContainer(stack);
        int newLevel = Math.max(1, Math.min(Bonded.CONFIG.levelsToUpgrade.get(), current.getLevel() + amount));
        if (newLevel == current.getLevel()) {
            return false;
        }

        stack.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), current.withLevel(newLevel));
        return true;
    }

    private static boolean setLevels(ItemStack stack, int amount) {
        int maxLevel = Bonded.CONFIG.levelsToUpgrade.get();
        if (amount < 1 || amount > maxLevel) {
            return false;
        }

        ItemLevelContainer current = getContainer(stack);
        if (current.getLevel() == amount) {
            return false;
        }

        stack.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), current.withLevel(amount));
        return true;
    }

    @FunctionalInterface
    private interface ExperienceOperation {
        int run(CommandSourceStack source, Collection<? extends ServerPlayer> targets, int amount, Type type)
                throws CommandSyntaxException;
    }

    private enum Type {
        POINTS(
                "points",
                BondedCommands::addPoints,
                BondedCommands::setPoints,
                stack -> getContainer(stack).getExperience()
        ),
        LEVELS(
                "levels",
                BondedCommands::addLevels,
                BondedCommands::setLevels,
                stack -> getContainer(stack).getLevel()
        );

        private final String name;
        private final AddHandler addHandler;
        private final BiFunction<ItemStack, Integer, Boolean> setHandler;
        private final ToIntFunction<ItemStack> query;

        Type(
                String name,
                AddHandler addHandler,
                BiFunction<ItemStack, Integer, Boolean> setHandler,
                ToIntFunction<ItemStack> query
        ) {
            this.name = name;
            this.addHandler = addHandler;
            this.setHandler = setHandler;
            this.query = query;
        }

        private boolean add(ServerPlayer player, ItemStack stack, int amount) {
            return addHandler.apply(player, stack, amount);
        }

        private boolean set(ItemStack stack, int amount) {
            return setHandler.apply(stack, amount);
        }
    }

    @FunctionalInterface
    private interface AddHandler {
        boolean apply(ServerPlayer player, ItemStack stack, int amount);
    }
}
