package com.iamkaf.bonded.api.event;

import com.iamkaf.amber.api.event.v1.Event;
import com.iamkaf.amber.api.event.v1.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface GameEvents {
    Event<ModifySmithingResult> MODIFY_SMITHING_RESULT = EventFactory.createArrayBacked(
            ModifySmithingResult.class, callbacks -> (stack, relevantItems) -> {
                for (ModifySmithingResult callback : callbacks) {
                    callback.smith(stack, relevantItems);
                }
            }
    );

    Event<ItemExperience> AWARD_ITEM_EXPERIENCE = EventFactory.createArrayBacked(
            ItemExperience.class, callbacks -> (player, stack, experienceAmount) -> {
                for (ItemExperience callback : callbacks) {
                    callback.experience(player, stack, experienceAmount);
                }
            }
    );

    interface ModifySmithingResult {
        void smith(ItemStack stack, List<ItemStack> relevantItems);
    }

    interface ItemExperience {
        void experience(Player player, ItemStack stack, int experienceAmount);
    }
}
