package com.iamkaf.bonded.compat;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Comparator;
import java.util.List;

/**
 * Optional integration with Liteminer's client-side HUD API.
 */
public final class LiteminerClientCompat {
    private LiteminerClientCompat() {
    }

    public static void init() {
        Bridge.init();
    }

    /**
     * Isolates optional Liteminer client types so they are not resolved when the mod is absent.
     */
    private static final class Bridge {
        private Bridge() {
        }

        private static void init() {
            com.iamkaf.liteminer.api.event.LiteminerClientEvents.MODIFY_HUD.register(context -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level == null || minecraft.player == null
                        || !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
                    return;
                }

                ItemStack tool = minecraft.player.getMainHandItem();
                if (tool.isEmpty()) {
                    return;
                }
                ItemLevelContainer level = tool.get(DataComponents.ITEM_LEVEL_CONTAINER.get());
                if (level == null || isDoneLeveling(level)) {
                    return;
                }

                BlockPos origin = hitResult.getBlockPos();
                BlockState originState = minecraft.level.getBlockState(origin);
                if (!tool.getItem().isCorrectToolForDrops(tool, originState)) {
                    return;
                }

                int originExperience = LiteminerExperience.forBlock(tool, originState);
                List<Integer> secondaryExperience = context.selectedShape()
                        .walk(minecraft.level, minecraft.player, origin)
                        .stream()
                        .sorted(Comparator.comparingInt(pos -> pos.distManhattan(origin)))
                        .filter(pos -> !pos.equals(origin))
                        .map(pos -> minecraft.level.getBlockState(pos))
                        .map(state -> tool.getItem().isCorrectToolForDrops(tool, state)
                                ? LiteminerExperience.forBlock(tool, state)
                                : 0)
                        .toList();
                int totalExperience = LiteminerExperience.total(originExperience, secondaryExperience);

                context.lines().add(Component.translatable(
                        "hud.bonded.liteminer_experience",
                        totalExperience
                ).withStyle(ChatFormatting.YELLOW));
            });
        }

        private static boolean isDoneLeveling(ItemLevelContainer level) {
            return level.getLevel() == Bonded.CONFIG.levelsToUpgrade.get()
                    && level.getExperience() < level.getMaxExperience();
        }
    }
}
