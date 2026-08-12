package com.iamkaf.bonded.api.augment;

import com.iamkaf.bonded.component.AugmentProgressContainer;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registration and mutation surface for Bonded augments.
 */
public final class AugmentApi {
    private static final Map<Identifier, Augment> AUGMENTS = new LinkedHashMap<>();

    private AugmentApi() {
    }

    public static synchronized Augment register(Augment augment) {
        Objects.requireNonNull(augment, "augment");
        Augment existing = AUGMENTS.putIfAbsent(augment.id(), augment);
        if (existing != null) {
            throw new IllegalArgumentException("Duplicate augment id: " + augment.id());
        }
        return augment;
    }

    public static synchronized Optional<Augment> get(Identifier id) {
        return Optional.ofNullable(AUGMENTS.get(id));
    }

    public static synchronized List<Augment> all() {
        return List.copyOf(AUGMENTS.values());
    }

    public static Map<Identifier, Integer> progress(ItemStack stack) {
        return stack.getOrDefault(
                DataComponents.AUGMENT_PROGRESS.get(),
                AugmentProgressContainer.empty()
        ).progress();
    }

    public static int progress(ItemStack stack, Augment augment) {
        return stack.getOrDefault(
                DataComponents.AUGMENT_PROGRESS.get(),
                AugmentProgressContainer.empty()
        ).get(augment.id());
    }

    public static boolean isActive(ItemStack stack, Augment augment) {
        return progress(stack, augment) >= augment.activationProgress();
    }

    public static AugmentProgressResult addProgress(Player player, ItemStack stack, Augment augment, int amount) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(augment, "augment");
        ensureServer(player);
        ensureRegistered(augment);

        int previous = progress(stack, augment);
        if (stack.isEmpty() || !augment.supports().test(stack)) {
            return result(AugmentProgressResult.Status.INELIGIBLE, augment, previous, previous);
        }
        if (amount <= 0 || previous >= augment.activationProgress()) {
            return result(AugmentProgressResult.Status.UNCHANGED, augment, previous, previous);
        }

        AugmentEvents.ProgressContext context = new AugmentEvents.ProgressContext(player, stack, augment);
        int modifiedAmount = AugmentEvents.MODIFY_PROGRESS_GAIN.invoker().modify(context, amount);
        if (modifiedAmount <= 0) {
            return result(AugmentProgressResult.Status.UNCHANGED, augment, previous, previous);
        }

        int remaining = augment.activationProgress() - previous;
        return setProgress(context, previous + Math.min(modifiedAmount, remaining));
    }

    public static AugmentProgressResult setProgress(Player player, ItemStack stack, Augment augment, int progress) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(augment, "augment");
        ensureServer(player);
        ensureRegistered(augment);

        int previous = progress(stack, augment);
        if (stack.isEmpty() || !augment.supports().test(stack)) {
            return result(AugmentProgressResult.Status.INELIGIBLE, augment, previous, previous);
        }
        return setProgress(new AugmentEvents.ProgressContext(player, stack, augment), progress);
    }

    private static AugmentProgressResult setProgress(AugmentEvents.ProgressContext context, int requestedProgress) {
        Augment augment = context.augment();
        ItemStack stack = context.stack();
        int previous = progress(stack, augment);
        int current = Math.max(0, Math.min(requestedProgress, augment.activationProgress()));
        if (previous == current) {
            return result(AugmentProgressResult.Status.UNCHANGED, augment, previous, current);
        }

        AugmentProgressContainer container = stack.getOrDefault(
                DataComponents.AUGMENT_PROGRESS.get(),
                AugmentProgressContainer.empty()
        ).with(augment.id(), current);
        if (container.isEmpty()) {
            stack.remove(DataComponents.AUGMENT_PROGRESS.get());
        } else {
            stack.set(DataComponents.AUGMENT_PROGRESS.get(), container);
        }

        boolean wasActive = previous >= augment.activationProgress();
        boolean active = current >= augment.activationProgress();
        AugmentProgressResult.Status status = !wasActive && active
                ? AugmentProgressResult.Status.ACTIVATED
                : wasActive && !active
                ? AugmentProgressResult.Status.DEACTIVATED
                : AugmentProgressResult.Status.PROGRESSED;
        AugmentProgressResult result = result(status, augment, previous, current);
        AugmentEvents.PROGRESS_CHANGED.invoker().changed(context, result);
        if (status == AugmentProgressResult.Status.ACTIVATED) {
            AugmentEvents.ACTIVATED.invoker().changed(context, result);
        } else if (status == AugmentProgressResult.Status.DEACTIVATED) {
            AugmentEvents.DEACTIVATED.invoker().changed(context, result);
        }
        return result;
    }

    private static void ensureRegistered(Augment augment) {
        Augment registered;
        synchronized (AugmentApi.class) {
            registered = AUGMENTS.get(augment.id());
        }
        if (registered != augment) {
            throw new IllegalArgumentException("Augment is not registered: " + augment.id());
        }
    }

    private static void ensureServer(Player player) {
        if (player.level().isClientSide()) {
            throw new IllegalStateException("Augment progress can only be changed on the logical server");
        }
    }

    private static AugmentProgressResult result(
            AugmentProgressResult.Status status,
            Augment augment,
            int previous,
            int current
    ) {
        return new AugmentProgressResult(status, augment, previous, current);
    }
}
