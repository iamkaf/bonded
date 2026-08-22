package com.iamkaf.bonded.compat;

import com.iamkaf.amber.api.billboard.v1.Billboard;
import com.iamkaf.amber.api.billboard.v1.BillboardAnimation;
import com.iamkaf.amber.api.billboard.v1.Billboards;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
                    showExperiencePopup(context.player(), context.origin(), roundedExperience);
                }
            });
        }

        private static void showExperiencePopup(
                Player player,
                BlockPos origin,
                int experience
        ) {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Billboard popup = Billboard.text(
                            Vec3.atCenterOf(origin).add(0.0D, 0.45D, 0.0D),
                            Component.literal("+" + experience + " XP"),
                            0.010F,
                            0xFFB4F56A
                    )
                    .forTicks(20)
                    .translateBy(0.0D, 0.22D, 0.0D, BillboardAnimation.Easing.EASE_OUT_CUBIC)
                    .scaleFromTo(0.96D, 1.0D, BillboardAnimation.Easing.EASE_OUT_CUBIC)
                    .fadeOut(BillboardAnimation.Easing.EASE_IN_QUAD);
            Billboards.show(serverPlayer, popup);
        }
    }
}
