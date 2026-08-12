package com.iamkaf.bonded.compat;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Optional integration with Liteminer's server-side vein mining events.
 */
public final class LiteminerCompat {
    private LiteminerCompat() {
    }

    public static void init() {
        Bridge.init();
        Bonded.LOGGER.info("Liteminer integration enabled.");
    }

    /**
     * Isolates optional Liteminer types so they are not resolved when the mod is absent.
     */
    private static final class Bridge {
        private static final Map<UUID, Map<BlockPos, Integer>> PENDING_EXPERIENCE = new HashMap<>();

        private Bridge() {
        }

        private static void init() {
            com.iamkaf.liteminer.api.event.LiteminerEvents.BEFORE_VEINMINE.register(context -> {
                if (context.operation() == com.iamkaf.liteminer.api.event.LiteminerEvents.Operation.BREAK) {
                    PENDING_EXPERIENCE.put(context.player().getUUID(), new HashMap<>());
                }
                return InteractionResult.PASS;
            });

            com.iamkaf.liteminer.api.event.LiteminerEvents.ALLOW_BLOCK.register(context -> {
                if (context.operation() != com.iamkaf.liteminer.api.event.LiteminerEvents.Operation.BREAK) {
                    return InteractionResult.PASS;
                }

                var pending = PENDING_EXPERIENCE.get(context.player().getUUID());
                if (pending == null || context.tool().isEmpty()
                        || !context.tool().getItem().isCorrectToolForDrops(context.tool(), context.state())) {
                    return InteractionResult.PASS;
                }

                pending.put(
                        context.pos().immutable(),
                        LiteminerExperience.forBlock(context.tool(), context.state())
                );
                return InteractionResult.PASS;
            });

            com.iamkaf.liteminer.api.event.LiteminerEvents.AFTER_VEINMINE.register(context -> {
                if (context.operation() != com.iamkaf.liteminer.api.event.LiteminerEvents.Operation.BREAK) {
                    return;
                }

                Map<BlockPos, Integer> pending = PENDING_EXPERIENCE.remove(context.player().getUUID());
                if (pending == null || pending.isEmpty()) {
                    return;
                }

                List<Integer> blockExperience = context.processed().stream()
                        .map(pos -> pending.getOrDefault(pos, 0))
                        .toList();
                int roundedExperience = LiteminerExperience.secondary(blockExperience);
                if (roundedExperience > 0) {
                    GameEvents.AWARD_ITEM_EXPERIENCE.invoker()
                            .experience(context.player(), context.tool(), roundedExperience);
                }
            });
        }
    }
}
