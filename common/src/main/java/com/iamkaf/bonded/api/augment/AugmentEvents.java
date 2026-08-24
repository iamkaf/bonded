package com.iamkaf.bonded.api.augment;

import com.iamkaf.amber.api.event.v1.Event;
import com.iamkaf.amber.api.event.v1.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Server-thread augment lifecycle events. Listeners run permanently in registration order.
 */
public final class AugmentEvents {
    private AugmentEvents() {
    }

    /**
     * Modifies a requested positive progress gain before it is applied.
     */
    public static final Event<ModifyProgressGain> MODIFY_PROGRESS_GAIN = EventFactory.createArrayBacked(
            ModifyProgressGain.class,
            callbacks -> (context, amount) -> {
                int modifiedAmount = amount;
                for (ModifyProgressGain callback : callbacks) {
                    modifiedAmount = Math.max(0, callback.modify(context, modifiedAmount));
                }
                return modifiedAmount;
            }
    );

    public static final Event<ProgressChanged> PROGRESS_CHANGED = EventFactory.createArrayBacked(
            ProgressChanged.class,
            callbacks -> (context, result) -> {
                for (ProgressChanged callback : callbacks) {
                    callback.changed(context, result);
                }
            }
    );

    public static final Event<ActivationChanged> ACTIVATED = EventFactory.createArrayBacked(
            ActivationChanged.class,
            callbacks -> (context, result) -> {
                for (ActivationChanged callback : callbacks) {
                    callback.changed(context, result);
                }
            }
    );

    public static final Event<ActivationChanged> DEACTIVATED = EventFactory.createArrayBacked(
            ActivationChanged.class,
            callbacks -> (context, result) -> {
                for (ActivationChanged callback : callbacks) {
                    callback.changed(context, result);
                }
            }
    );

    public record ProgressContext(Player player, ItemStack stack, Augment augment) {
    }

    @FunctionalInterface
    public interface ModifyProgressGain {
        int modify(ProgressContext context, int amount);
    }

    @FunctionalInterface
    public interface ProgressChanged {
        void changed(ProgressContext context, AugmentProgressResult result);
    }

    @FunctionalInterface
    public interface ActivationChanged {
        void changed(ProgressContext context, AugmentProgressResult result);
    }
}
